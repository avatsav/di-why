package dev.avatsav.nanite.internal

fun interface Factory<T> {
    operator fun invoke(): T
}