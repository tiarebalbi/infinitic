/**
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as defined below, subject to
 * the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights under the License will not
 * include, and the License does not grant to you, the right to Sell the Software.
 *
 * For purposes of the foregoing, “Sell” means practicing any or all of the rights granted to you
 * under the License to provide to third parties, for a fee or other consideration (including
 * without limitation fees for hosting or consulting/ support services related to the Software), a
 * product or service whose value derives, entirely or substantially, from the functionality of the
 * Software. Any license notice or attribution required by the License must also include this
 * Commons Clause License Condition notice.
 *
 * Software: Infinitic
 *
 * License: MIT License (https://opensource.org/licenses/MIT)
 *
 * Licensor: infinitic.io
 */
package io.infinitic.tests.timeouts

import io.infinitic.annotations.Timeout
import io.infinitic.exceptions.TaskTimedOutException
import io.infinitic.exceptions.WorkflowTimedOutException
import io.infinitic.tasks.WithTimeout
import io.infinitic.utils.After1Second
import io.infinitic.utils.UtilService
import io.infinitic.workflows.Workflow


internal interface TimeoutsWorkflow {

  // the workflow method 'withMethodTimeout' has a 100ms timeout
  @Timeout(After1Second::class)
  fun withTimeoutOnMethod(duration: Long): Long

  fun withTimeoutOnTask(wait: Long): Long

  fun withTimeoutOnTaskExecution(wait: Long): Long

  fun withManagedTimeoutOnTaskExecution(): Long

  fun withCaughtTimeoutOnTask(wait: Long): Long

  fun withManualRetry(): Int


  fun withTimeoutOnChild(wait: Long): Long

  fun withCaughtTimeoutOnChild(wait: Long): Long
}


@Suppress("unused")
internal class TimeoutsWorkflowImpl : Workflow(), TimeoutsWorkflow {

  private val child = newWorkflow(TimeoutsWorkflow::class.java)

  private val utilService = newService(
      UtilService::class.java,
      tags = setOf("foo", "bar"),
      meta = mutableMapOf("foo" to "bar".toByteArray()),
  )
  private val timeoutsWorkflow =
      newWorkflow(TimeoutsWorkflow::class.java, tags = setOf("foo", "bar"))

  override fun withTimeoutOnMethod(duration: Long) = utilService.await(duration)

  // the task 'withTimeout' has a 100ms timeout
  override fun withTimeoutOnTask(wait: Long): Long = utilService.withServiceTimeout(wait)

  // the task 'withExecutionTimeout' has a 100ms execution timeout
  override fun withTimeoutOnTaskExecution(wait: Long): Long = utilService.withExecutionTimeout(wait)

  override fun withManagedTimeoutOnTaskExecution(): Long = utilService.withManagedExecutionTimeout()

  // the task 'withTimeout' has a 100ms timeout
  override fun withCaughtTimeoutOnTask(wait: Long): Long = try {
    utilService.withServiceTimeout(wait)
  } catch (e: TaskTimedOutException) {
    -1
  }

  // the task 'tryAgain' has a 100ms timeout and wait for 10s for the first sequence
  override fun withManualRetry(): Int = utilService.tryAgain()

  override fun withTimeoutOnChild(wait: Long): Long = child.withTimeoutOnMethod(wait)

  override fun withCaughtTimeoutOnChild(wait: Long): Long = try {
    child.withTimeoutOnMethod(wait)
  } catch (e: WorkflowTimedOutException) {
    -1
  }
}

interface ITimeoutWorkflow : WithTimeout {

  // the workflow method 'withMethodTimeout' has a 1s timeout
  fun withTimeoutOnMethod(duration: Long): Long

  override fun getTimeoutSeconds(): Double? = 1.0
}

class ITimeoutsWorkflowImpl : Workflow(), ITimeoutWorkflow {

  private val utilService = newService(UtilService::class.java)

  override fun withTimeoutOnMethod(duration: Long) = utilService.await(duration)
}
