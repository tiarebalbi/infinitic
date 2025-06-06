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
@Suppress("ConstPropertyName")
object Libs {

  const val org = "io.infinitic"

  object Kotlin {
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect"
  }

  object Coroutines {
    private const val version = "1.10.1"
    const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$version"
    const val jdk8 = "org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$version"
  }

  object Caffeine {
    const val caffeine = "com.github.ben-manes.caffeine:caffeine:3.1.8"
  }

  object Serialization {
    private const val version = "1.8.0"
    const val json = "org.jetbrains.kotlinx:kotlinx-serialization-json:$version"
    const val core = "org.jetbrains.kotlinx:kotlinx-serialization-core:$version"
  }

  object CloudEvents {
    private const val version = "4.0.1"
    const val core = "io.cloudevents:cloudevents-core:$version"
    const val api = "io.cloudevents:cloudevents-api:$version"
    const val json = "io.cloudevents:cloudevents-json-jackson:$version"
  }

  object JsonPath {
    const val jayway = "com.jayway.jsonpath:json-path:2.9.0"
  }

  object Jackson {
    private const val version = "2.18.2"
    const val core = "com.fasterxml.jackson.core:jackson-core:$version"
    const val databind = "com.fasterxml.jackson.core:jackson-databind:$version"
    const val kotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:$version"
    const val jsr310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$version"
  }

  object Uuid {
    const val generator = "com.fasterxml.uuid:java-uuid-generator:5.1.0"
  }

  object Kotest {
    private const val version = "5.9.1"
    const val property = "io.kotest:kotest-property-jvm:$version"
    const val junit5 = "io.kotest:kotest-runner-junit5-jvm:$version"
    const val datatest = "io.kotest:kotest-framework-datatest:$version"
  }

  object TestContainers {
    private const val version = "1.20.4"
    const val testcontainers = "org.testcontainers:testcontainers:$version"
    const val mysql = "org.testcontainers:mysql:$version"
    const val postgresql = "org.testcontainers:postgresql:$version"
  }

  object Mockk {
    const val mockk = "io.mockk:mockk:1.13.14"
  }

  object Avro4k {
    const val core = "com.github.avro-kotlin.avro4k:avro4k-core:1.10.1"
  }

  object Hoplite {
    private const val version = "2.9.0"
    const val core = "com.sksamuel.hoplite:hoplite-core:$version"
    const val yaml = "com.sksamuel.hoplite:hoplite-yaml:$version"
  }

  object Pulsar {
    const val version = "4.0.5"
    const val client = "org.apache.pulsar:pulsar-client:$version"
    const val clientAdmin = "org.apache.pulsar:pulsar-client-admin:$version"
    const val clientAdminApi = "org.apache.pulsar:pulsar-client-admin-api:$version"
    const val functions = "org.apache.pulsar:pulsar-functions-api:$version"
    const val authAthenz = "org.apache.pulsar:pulsar-client-auth-athenz:$version"
    const val authSasl = "org.apache.pulsar:pulsar-client-auth-sasl:$version"
  }

  object Kweb {
    const val core = "io.kweb:kweb-core:1.4.8"
  }

  object EasyRandom {
    const val core = "org.jeasy:easy-random-core:5.0.0"
  }

  object Slf4j {
    private const val version = "2.0.16"
    const val simple = "org.slf4j:slf4j-simple:$version"
    const val api = "org.slf4j:slf4j-api:$version"
  }

  object Logging {
    const val jvm = "io.github.oshai:kotlin-logging-jvm:7.0.3"
  }

  object Compress {
    const val commons = "org.apache.commons:commons-compress:1.27.1"
  }
}
