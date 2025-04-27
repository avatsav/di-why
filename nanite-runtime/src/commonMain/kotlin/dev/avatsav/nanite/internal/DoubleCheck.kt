package dev.avatsav.nanite.internal

import kotlinx.atomicfu.atomic

/**
 * A [Provider] implementation that lazily initializes a singleton instance using the double-check locking pattern.
 *
 * @param T The type of object to be provided
 * @param delegate The factory function that creates the instance
 */
internal class DoubleCheck<T>(private val delegate: Provider<T>) : Provider<T> {

    private val instance = atomic<T?>(null)

    override fun invoke(): T {
        val result = instance.value
        if (result != null) {
            return result
        }

        val created = delegate()
        return if (instance.compareAndSet(null, created)) {
            created
        } else {
            instance.value!!
        }
    }
}

/**
 * Creates a new [DoubleCheck] provider for the given delegate provider.
 *
 * @param delegate The provider that will be used to create the singleton instance
 * @return A [DoubleCheck] provider that will lazily initialize and cache the instance
 */
fun <T> singletonProvider(delegate: Provider<T>): Provider<T> = DoubleCheck(delegate)



