package dev.avatsav.diwhy

fun interface Factory<T : Any> {
    fun get(objectGraph: ObjectGraph): T
}