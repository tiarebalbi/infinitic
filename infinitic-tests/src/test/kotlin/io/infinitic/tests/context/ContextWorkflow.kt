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
package io.infinitic.tests.context

import io.infinitic.common.tasks.data.TaskMeta
import io.infinitic.common.workflows.data.workflows.WorkflowMeta
import io.infinitic.utils.TestService
import io.infinitic.utils.UtilService
import io.infinitic.workflows.Workflow

interface ContextWorkflow {
  fun context1(): String

  fun context2(): Set<String>

  fun context3(): WorkflowMeta

  fun context4(): String?

  fun context5(): String?

  fun context6(): Set<String>

  fun context7(): TaskMeta

  fun context8(): Double?

  fun context9(): Double?
}

@Suppress("unused")
class ContextWorkflowImpl : Workflow(), ContextWorkflow {

  private val _workflowId = workflowId
  private val _tags = tags
  private val _meta = meta

  private val utilService = newService(
      UtilService::class.java,
      tags = setOf("foo", "bar"),
      meta = mutableMapOf("foo" to "bar".toByteArray()),
  )

  private val testService = newService(TestService::class.java)

  override fun context1(): String = _workflowId

  override fun context2(): Set<String> = _tags

  override fun context3() = WorkflowMeta(_meta)

  override fun context4() = utilService.workflowId()

  override fun context5() = utilService.workflowName()

  override fun context6() = utilService.tags()

  override fun context7() = utilService.meta()

  override fun context8(): Double? = utilService.getRetry()

  override fun context9(): Double? = testService.getTimeout()
}
