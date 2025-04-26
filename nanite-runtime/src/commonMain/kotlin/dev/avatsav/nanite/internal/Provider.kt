package dev.avatsav.nanite.internal

fun interface Provider<T> {
    operator fun invoke(): T
}