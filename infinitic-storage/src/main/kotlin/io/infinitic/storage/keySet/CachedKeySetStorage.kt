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
package io.infinitic.storage.keySet

import io.github.oshai.kotlinlogging.KotlinLogging
import io.infinitic.cache.keySet.CachedKeySet
import org.jetbrains.annotations.TestOnly

class CachedKeySetStorage(
  private val cache: CachedKeySet<ByteArray>,
  private val storage: KeySetStorage
) : KeySetStorage {

  private val logger = KotlinLogging.logger {}

  override suspend fun get(key: String): Set<ByteArray> =
      cache.get(key) ?: run {
        logger.debug { "key $key - getSet - absent from cache, get from storage" }
        storage.get(key).also { cache.set(key, it) }
      }

  override suspend fun add(key: String, value: ByteArray) {
    storage.add(key, value)
    cache.add(key, value)
  }

  override suspend fun remove(key: String, value: ByteArray) {
    cache.remove(key, value)
    storage.remove(key, value)
  }

  override suspend fun get(keys: Set<String>): Map<String, Set<ByteArray>> {
    val cached = keys.associateWith { cache.get(it) }.toMutableMap()
    val missing = cached.filterValues { it == null }.keys
    storage.get(missing).forEach { (k, v) ->
      cache.set(k, v)
      cached[k] = v
    }
    return cached.mapValues { it.value!! }
  }

  override suspend fun update(
    add: Map<String, Set<ByteArray>>,
    remove: Map<String, Set<ByteArray>>
  ) {
    storage.update(add, remove)
    add.forEach { (key, values) -> values.forEach { cache.add(key, it) } }
    remove.forEach { (key, values) -> values.forEach { cache.remove(key, it) } }
  }

  override fun close() {
    storage.close()
  }

  @TestOnly
  override fun flush() {
    storage.flush()
    cache.flush()
  }
}
