package io.infinitic.taskManager.tests.inMemory

import io.infinitic.taskManager.client.AvroTaskDispatcher as AvroTaskClientDispatcher
import io.infinitic.taskManager.engine.avroInterfaces.AvroDispatcher as AvroTaskEngineDispatcher
import io.infinitic.taskManager.messages.envelopes.AvroEnvelopeForTaskEngine
import io.infinitic.taskManager.messages.envelopes.AvroEnvelopeForMonitoringGlobal
import io.infinitic.taskManager.messages.envelopes.AvroEnvelopeForMonitoringPerName
import io.infinitic.taskManager.messages.envelopes.AvroEnvelopeForWorker
import io.infinitic.workflowManager.engine.avroInterfaces.AvroDispatcher as AvroWorkflowEngineDispatcher
import io.infinitic.taskManager.worker.AvroDispatcher as AvroWorkerDispatcher
import io.infinitic.workflowManager.messages.envelopes.AvroEnvelopeForWorkflowEngine
import io.infinitic.workflowManager.client.AvroWorkflowDispatcher as AvroWorkflowClientDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class InMemoryDispatcher :
    AvroWorkerDispatcher,
    AvroTaskEngineDispatcher,
    AvroTaskClientDispatcher,
    AvroWorkflowEngineDispatcher,
    AvroWorkflowClientDispatcher {
    // Here we favor lambda to avoid a direct dependency with engines instances
    lateinit var workflowEngineHandle: suspend (msg: AvroEnvelopeForWorkflowEngine) -> Unit
    lateinit var taskEngineHandle: suspend (msg: AvroEnvelopeForTaskEngine) -> Unit
    lateinit var monitoringPerNameHandle: suspend (msg: AvroEnvelopeForMonitoringPerName) -> Unit
    lateinit var monitoringGlobalHandle: suspend (msg: AvroEnvelopeForMonitoringGlobal) -> Unit
    lateinit var workerHandle: suspend (msg: AvroEnvelopeForWorker) -> Unit

    lateinit var scope: CoroutineScope

    override suspend fun toWorkflowEngine(msg: AvroEnvelopeForWorkflowEngine, after: Float) {
        scope.launch {
            if (after > 0F) {
                delay((1000 * after).toLong())
            }
            workflowEngineHandle(msg)
        }
    }

    override suspend fun toDeciders(msg: AvroEnvelopeForTaskEngine) {
        scope.launch {
            taskEngineHandle(msg)
        }
    }

    override suspend fun toWorkers(msg: AvroEnvelopeForTaskEngine) {
        scope.launch {
            taskEngineHandle(msg)
        }
    }

    override suspend fun toWorkflowEngine(msg: AvroEnvelopeForWorkflowEngine) {
        scope.launch {
            workflowEngineHandle(msg)
        }
    }

    override suspend fun toTaskEngine(msg: AvroEnvelopeForTaskEngine, after: Float) {
        scope.launch {
            if (after > 0F) {
                delay((1000 * after).toLong())
            }
            taskEngineHandle(msg)
        }
    }

    override suspend fun toTaskEngine(msg: AvroEnvelopeForTaskEngine) = toTaskEngine(msg, 0F)

    override suspend fun toMonitoringPerName(msg: AvroEnvelopeForMonitoringPerName) {
        scope.launch {
            monitoringPerNameHandle(msg)
        }
    }

    override suspend fun toMonitoringGlobal(msg: AvroEnvelopeForMonitoringGlobal) {
        scope.launch {
            monitoringGlobalHandle(msg)
        }
    }

    override suspend fun toWorkers(msg: AvroEnvelopeForWorker) {
        scope.launch {
            workerHandle(msg)
        }
    }
}
