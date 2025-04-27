package dev.avatsav.nanite.internal

fun interface Provider<T> {
    operator fun invoke(): T
}


/**
 * Creates a [Provider] instance that generates values by invoking the provided lambda function.
 *
 * @param T The type of value provided by the `Provider`.
 * @param provider A lambda function that produces the values when the `Provider` is invoked.
 * @return A `Provider` instance that delegates to the specified lambda function.
 */
inline fun <T> provider(crossinline provider: () -> T): Provider<T> =
    Provider { provider() }