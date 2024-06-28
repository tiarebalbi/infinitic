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
package io.infinitic.cache

data class Caffeine(
  val maximumSize: Long? = 10000, // 10k units
  val expireAfterAccess: Long? = 3600, // 1 hour
  val expireAfterWrite: Long? = 3600 // 1 hour
) {
  init {
    maximumSize?.let { require(it > 0) { "maximumSize MUST be > 0" } }
    expireAfterAccess?.let { require(it >= 0) { "expireAfterAccess MUST be >= 0" } }
    expireAfterWrite?.let { require(it >= 0) { "expireAfterWrite MUST be >= 0" } }
  }

  companion object {
    @JvmStatic
    fun builder() = CaffeineBuilder()
  }

  /**
   * Caffeine builder (Useful for Java user)
   */
  class CaffeineBuilder {
    private val default = Caffeine()

    private var maximumSize = default.maximumSize
    private var expireAfterAccess = default.expireAfterAccess
    private var expireAfterWrite = default.expireAfterWrite

    fun setMaximumSize(maximumSize: Long) = apply { this.maximumSize = maximumSize }
    fun setExpireAfterAccess(expAftAccess: Long) = apply { this.expireAfterAccess = expAftAccess }
    fun setExpireAfterWrite(expAftWrite: Long) = apply { this.expireAfterWrite = expAftWrite }

    fun build() = Caffeine(
        maximumSize = maximumSize,
        expireAfterAccess = expireAfterAccess,
        expireAfterWrite = expireAfterWrite,
    )
  }
}
