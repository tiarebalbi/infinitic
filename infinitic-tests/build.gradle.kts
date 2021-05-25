/**
 * "Commons Clause" License Condition v1.0
 *
 * The Software is provided to you by the Licensor under the License, as defined
 * below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights under the
 * License will not include, and the License does not grant to you, the right to
 * Sell the Software.
 *
 * For purposes of the foregoing, “Sell” means practicing any or all of the rights
 * granted to you under the License to provide to third parties, for a fee or
 * other consideration (including without limitation fees for hosting or
 * consulting/ support services related to the Software), a product or service
 * whose value derives, entirely or substantially, from the functionality of the
 * Software. Any license notice or attribution required by the License must also
 * include this Commons Clause License Condition notice.
 *
 * Software: Infinitic
 *
 * License: MIT License (https://opensource.org/licenses/MIT)
 *
 * Licensor: infinitic.io
 */

dependencies {
    implementation(Libs.Coroutines.core)
    implementation(Libs.Coroutines.jdk8)
    implementation(Libs.Jackson.databind) // <= check why this dependency is necessary

    implementation(Libs.Slf4j.simple)
    implementation(Libs.Kotest.junit5)
    implementation(Libs.Kotest.property)
    implementation(Libs.Mockk.mockk)
    implementation(testFixtures(project(":infinitic-common")))

    implementation(project(":infinitic-pulsar"))
    implementation(project(":infinitic-task-executor"))
    implementation(project(":infinitic-storage"))
    implementation(project(":infinitic-tag-engine"))

    testImplementation(project(":infinitic-inMemory"))
    // should be removed with pulsar 2.8
    testImplementation("org.apache.avro:avro") { version { strictly("1.9.+") } }
}

apply("../publish.gradle.kts")