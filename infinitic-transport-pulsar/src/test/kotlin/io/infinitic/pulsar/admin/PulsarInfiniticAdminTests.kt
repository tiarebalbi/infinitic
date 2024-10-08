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

package io.infinitic.pulsar.admin

import io.infinitic.common.fixtures.DockerOnly
import io.infinitic.pulsar.config.policies.PoliciesConfig
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.annotation.EnabledIf
import io.kotest.core.spec.style.StringSpec
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.apache.pulsar.client.admin.PulsarAdmin
import org.apache.pulsar.client.admin.PulsarAdminException
import kotlin.time.Duration.Companion.minutes

@EnabledIf(DockerOnly::class)
class PulsarInfiniticAdminTests : StringSpec(
    {
      val pulsarServer = DockerOnly().pulsarServer!!.also { it.start() }
      val pulsarAdmin = PulsarAdmin.builder().serviceHttpUrl(pulsarServer.httpServiceUrl).build()
      val admin = InfiniticPulsarAdmin(pulsarAdmin)

      afterSpec {
        pulsarAdmin.close()
        pulsarServer.stop()
      }

      "can create the same tenant twice simultaneously".config(timeout = 2.minutes) {
        // As topics are created on the fly, it can happen that
        // the same topic is created twice simultaneously, e.g. when dispatching a task
        // with multiple new tags (there is one topic per tag).
        // This test ensures that this case is handled correctly.
        shouldNotThrow<PulsarAdminException> {
          coroutineScope {
            launch { admin.syncInitTenantOnce("test-tenant", null, null).getOrThrow() }
            launch { admin.syncInitTenantOnce("test-tenant", null, null).getOrThrow() }
          }
        }
      }

      "can create the same namespace twice simultaneously" {
        // As topics are created on the fly, it can happen that
        // the same topic is created twice simultaneously, e.g. when dispatching a task
        // with multiple new tags (there is one topic per tag).
        // This test ensures that this case is handled correctly.
        shouldNotThrow<PulsarAdminException> {
          admin.syncInitTenantOnce("test-tenant", null, null).getOrThrow()
          coroutineScope {
            launch {
              admin.syncInitNamespaceOnce("test-tenant/test-namespace", PoliciesConfig())
                  .getOrThrow()
            }
            launch {
              admin.syncInitNamespaceOnce("test-tenant/test-namespace", PoliciesConfig())
                  .getOrThrow()
            }
          }
        }
      }

      "can create the same topic twice simultaneously" {
        // As topics are created on the fly, it can happen that
        // the same topic is created twice simultaneously, e.g. when dispatching a task
        // with multiple new tags (there is one topic per tag).
        // This test ensures that this case is handled correctly.
        shouldNotThrow<PulsarAdminException> {
          coroutineScope {
            launch { admin.syncInitTopicOnce("test-topic", true, 60).getOrThrow() }
            launch { admin.syncInitTopicOnce("test-topic", true, 60).getOrThrow() }
          }
        }
      }
    },
)
