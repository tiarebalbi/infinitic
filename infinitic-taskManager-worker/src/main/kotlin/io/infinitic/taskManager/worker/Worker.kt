package io.infinitic.taskManager.worker
import io.infinitic.common.data.SerializedData
import io.infinitic.taskManager.common.Constants
import io.infinitic.taskManager.common.avro.AvroConverter
import io.infinitic.taskManager.common.data.TaskAttemptContext
import io.infinitic.taskManager.common.data.TaskAttemptError
import io.infinitic.taskManager.common.data.TaskOutput
import io.infinitic.taskManager.common.exceptions.ClassNotFoundDuringTaskInstantiation
import io.infinitic.taskManager.common.exceptions.RetryDelayHasWrongReturnType
import io.infinitic.taskManager.common.exceptions.ErrorDuringTaskInstantiation
import io.infinitic.taskManager.common.exceptions.InvalidUseOfDividerInTaskName
import io.infinitic.taskManager.common.exceptions.TaskAttemptContextRetrievedOutsideOfProcessingThread
import io.infinitic.taskManager.common.exceptions.TaskAttemptContextSetFromExistingProcessingThread
import io.infinitic.taskManager.common.exceptions.MultipleUseOfDividerInTaskName
import io.infinitic.taskManager.common.exceptions.NoMethodFoundWithParameterCount
import io.infinitic.taskManager.common.exceptions.NoMethodFoundWithParameterTypes
import io.infinitic.taskManager.common.exceptions.ProcessingTimeout
import io.infinitic.taskManager.common.exceptions.TooManyMethodsFoundWithParameterCount
import io.infinitic.taskManager.common.messages.ForWorkerMessage
import io.infinitic.taskManager.common.messages.TaskAttemptCompleted
import io.infinitic.taskManager.common.messages.TaskAttemptFailed
import io.infinitic.taskManager.common.messages.TaskAttemptStarted
import io.infinitic.taskManager.common.messages.RunTask
import io.infinitic.taskManager.messages.envelopes.AvroEnvelopeForWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

open class Worker {
    lateinit var dispatcher: Dispatcher

    companion object {

        // map taskName <> taskInstance
        private val registeredTasks = ConcurrentHashMap<String, Any>()

        // cf. https://www.baeldung.com/java-threadlocal
        private val contexts = ConcurrentHashMap<Long, TaskAttemptContext>()

        private fun getContextKey() = Thread.currentThread().id

        /**
         * Use this to retrieve TaskAttemptContext associated to current task
         */
        // TODO: currently running task in coroutines (instead of Thread) is not supported (as Context is mapped to threadId)
        val context: TaskAttemptContext
            get() {
                val key = getContextKey()
                if (! contexts.containsKey(key)) throw TaskAttemptContextRetrievedOutsideOfProcessingThread()

                return contexts[key]!!
            }

        /**
         * Use this method to register the task instance to use for a given name
         */
        fun register(taskName: String, taskInstance: Any) {
            if (taskName.contains(Constants.METHOD_DIVIDER)) throw InvalidUseOfDividerInTaskName(taskName)

            registeredTasks[taskName] = taskInstance
        }

        /**
         * Use this method to unregister a given name (mostly used in tests)
         */
        fun unregister(taskName: String) {
            registeredTasks.remove(taskName)
        }

        /**
         * Use this method to register the task instance to use for a given class
         */
        inline fun <reified T> register(taskInstance: Any) = register(T::class.java.name, taskInstance)

        /**
         * Use this method to unregister a given class (mostly used in tests)
         */
        inline fun <reified T> unregister() = unregister(T::class.java.name)
    }

    fun setAvroDispatcher(avroDispatcher: AvroDispatcher) {
        dispatcher = Dispatcher(avroDispatcher)
    }

    fun handle(msg: AvroEnvelopeForWorker) {
        runBlocking {
            handle(AvroConverter.fromWorkers(msg))
        }
    }

    suspend fun handle(msg: ForWorkerMessage) {
        when (msg) {
            is RunTask -> {
                // let engine know that we are processing the message
                sendTaskStarted(msg)

                // trying to instantiate the task
                val (task, method, parameters, options) = try {
                    parse(msg)
                } catch (e: Exception) {
                    // returning the exception (no retry)
                    sendTaskFailed(msg, e, null)
                    // we stop here
                    return
                }

                val context = TaskAttemptContext(
                    taskId = msg.taskId,
                    taskAttemptId = msg.taskAttemptId,
                    taskAttemptIndex = msg.taskAttemptIndex,
                    taskAttemptRetry = msg.taskAttemptRetry,
                    taskMeta = msg.taskMeta,
                    taskOptions = msg.taskOptions
                )

                // the method invocation is done through a coroutine
                // - to manage processing timeout
                // - to manage cancellation after timeout
                val parentJob = Job()
                CoroutineScope(Dispatchers.Default + parentJob).launch {
                    var contextKey = 0L

                    launch {
                        try {
                            // ensure that contextKey is linked to same thread than method.invoke below
                            contextKey = getContextKey()

                            // add context to static list
                            setContext(contextKey, context)

                            // running timeout delay if any
                            if (options.runningTimeout != null && options.runningTimeout!! > 0F) {
                                launch {
                                    delay((1000 * options.runningTimeout!!).toLong())
                                    // update context with the cause (to be potentially used in getRetryDelay method)
                                    context.exception = ProcessingTimeout(task.javaClass.name, options.runningTimeout!!)
                                    // returning a timeout
                                    getRetryDelayAndFailTask(task, msg, parentJob, contextKey, context)
                                }
                            }

                            val output = method.invoke(task, *parameters)
                            // isActive below checks that the coroutine has not been canceled by timeout
                            if (isActive) {
                                completeTask(msg, output, parentJob, contextKey)
                            }
                        } catch (e: InvocationTargetException) {
                            if (isActive) {
                                // update context with the cause (to be potentially used in getRetryDelay method)
                                context.exception = e.cause
                                // retrieve delay before retry
                                getRetryDelayAndFailTask(task, msg, parentJob, contextKey, context)
                            }
                        } catch (e: Exception) {
                            if (isActive) {
                                // returning the exception (no retry)
                                failTask(msg, e, null, parentJob, contextKey)
                            }
                        }
                    }
                }.join()
            }
        }
    }

    private fun setContext(key: Long, context: TaskAttemptContext) {
        if (contexts.containsKey(key)) throw TaskAttemptContextSetFromExistingProcessingThread()
        contexts[key] = context
    }

    private fun delContext(key: Long) {
        if (! contexts.containsKey(key)) throw TaskAttemptContextSetFromExistingProcessingThread()
        contexts.remove(key)
    }

    private fun getRetryDelayAndFailTask(task: Any, msg: RunTask, parentJob: Job, contextKey: Long, context: TaskAttemptContext) {
        when (val delay = getDelayBeforeRetry(task, context)) {
            is RetryDelayRetrieved -> {
                println("delay=$delay")
                // returning the original cause
                failTask(msg, context.exception, delay.value, parentJob, contextKey)
            }
            is RetryDelayFailed -> {
                println("delay=$delay")
                // returning the error in getRetryDelay, without retry
                failTask(msg, delay.e, null, parentJob, contextKey)
            }
        }
    }

    private fun completeTask(msg: RunTask, output: Any?, parentJob: Job, contextKey: Long) {
        // returning output
        sendTaskCompleted(msg, output)

        // removing context from static list
        delContext(contextKey)

        // make sure to close both coroutines
        parentJob.cancel()
    }

    private fun failTask(msg: RunTask, e: Throwable?, delay: Float?, parentJob: Job, contextKey: Long) {
        // returning throwable
        sendTaskFailed(msg, e, delay)

        // removing context from static list
        delContext(contextKey)

        // make sure to close both coroutines
        parentJob.cancel()
    }

    private fun parse(msg: RunTask): TaskCommand {
        println("1" + msg.taskInput.map { println(it)})
        println("2" + msg.taskInput.size)
        val (taskName, methodName) = getClassAndMethodNames(msg)
        val task = getTaskInstance(taskName)
        val parameterTypes = getMetaParameterTypes(msg)
        val method = getMethod(task, methodName, msg.taskInput.size, parameterTypes)
        val parameters = msg.taskInput.data

        return TaskCommand(task, method, parameters, msg.taskOptions)
    }

    private fun getClassAndMethodNames(msg: RunTask): List<String> {
        val parts = msg.taskName.name.split(Constants.METHOD_DIVIDER)
        return when (parts.size) {
            1 -> parts + Constants.METHOD_DEFAULT
            2 -> parts
            else -> throw MultipleUseOfDividerInTaskName(msg.taskName.name)
        }
    }

    private fun getTaskInstance(name: String): Any {
        // return registered instance if any
        if (registeredTasks.containsKey(name)) return registeredTasks[name]!!

        // if no instance is registered, try to instantiate this task
        val klass = getClass(name)

        return try {
            klass.newInstance()
        } catch (e: Exception) {
            throw ErrorDuringTaskInstantiation(name)
        }
    }

    private fun getMetaParameterTypes(msg: RunTask) = msg.taskMeta.parameterTypes
        ?.map { getClass(it) }
        ?.toTypedArray()

    private fun getClass(name: String) = when (name) {
        "bytes" -> Byte::class.java
        "short" -> Short::class.java
        "int" -> Int::class.java
        "long" -> Long::class.java
        "float" -> Float::class.java
        "double" -> Double::class.java
        "boolean" -> Boolean::class.java
        "char" -> Character::class.java
        else ->
            try {
                Class.forName(name)
            } catch (e: ClassNotFoundException) {
                throw ClassNotFoundDuringTaskInstantiation(name)
            }
    }

    // TODO: currently method using "suspend" keyword are not supported
    private fun getMethod(task: Any, methodName: String, parameterCount: Int, parameterTypes: Array<Class<*>>?): Method {
        // Case where parameter types have been provided
        if (parameterTypes != null) return try {
            task::class.java.getMethod(methodName, *parameterTypes)
        } catch (e: NoSuchMethodException) {
            throw NoMethodFoundWithParameterTypes(task::class.java.name, methodName, parameterTypes.map { it.name })
        }

        // if not, hopefully there is only one method with this name
        val methods = task::class.javaObjectType.methods.filter { it.name == methodName && it.parameterCount == parameterCount }
        if (methods.isEmpty()) throw NoMethodFoundWithParameterCount(task::class.java.name, methodName, parameterCount)
        if (methods.size > 1) throw TooManyMethodsFoundWithParameterCount(task::class.java.name, methodName, parameterCount)

        return methods[0]
    }

    // TODO: currently it's not possible to use class extension to implement a working getRetryDelay() method
    private fun getDelayBeforeRetry(task: Any, context: TaskAttemptContext): RetryDelay {
        val method = try {
            task::class.java.getMethod(Constants.DELAY_BEFORE_RETRY_METHOD, TaskAttemptContext::class.java)
        } catch (e: NoSuchMethodException) {
            return RetryDelayRetrieved(null)
        }

        val actualType = method.genericReturnType.typeName
        val expectedType = Float::class.javaObjectType.name
        if (actualType != expectedType) return RetryDelayFailed(
            RetryDelayHasWrongReturnType(task::class.java.name, actualType, expectedType)
        )

        return try {
            RetryDelayRetrieved(method.invoke(task, context) as Float?)
        } catch (e: InvocationTargetException) {
            RetryDelayFailed(e.cause)
        }
    }

    private fun sendTaskStarted(msg: RunTask) {
        val taskAttemptStarted = TaskAttemptStarted(
            taskId = msg.taskId,
            taskAttemptId = msg.taskAttemptId,
            taskAttemptRetry = msg.taskAttemptRetry,
            taskAttemptIndex = msg.taskAttemptIndex
        )

        dispatcher.toTaskEngine(taskAttemptStarted)
    }

    private fun sendTaskFailed(msg: RunTask, error: Throwable?, delay: Float? = null) {
        println("sendTaskFailed: $error")
        val taskAttemptFailed = TaskAttemptFailed(
            taskId = msg.taskId,
            taskAttemptId = msg.taskAttemptId,
            taskAttemptRetry = msg.taskAttemptRetry,
            taskAttemptIndex = msg.taskAttemptIndex,
            taskAttemptDelayBeforeRetry = delay,
            taskAttemptError = TaskAttemptError(error)
        )

        dispatcher.toTaskEngine(taskAttemptFailed)
    }

    private fun sendTaskCompleted(msg: RunTask, output: Any?) {
        val taskAttemptCompleted = TaskAttemptCompleted(
            taskId = msg.taskId,
            taskAttemptId = msg.taskAttemptId,
            taskAttemptRetry = msg.taskAttemptRetry,
            taskAttemptIndex = msg.taskAttemptIndex,
            taskOutput = TaskOutput(output)
        )

        dispatcher.toTaskEngine(taskAttemptCompleted)
    }
}
