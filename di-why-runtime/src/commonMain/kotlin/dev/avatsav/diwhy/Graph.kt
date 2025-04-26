package dev.avatsav.diwhy

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class Graph(
    val modules: Array<KClass<*>> = []
)

private val UNINITIALIZED = Any()

fun <T> graphSingleton(factory: () -> T): () -> T {
    var instance: Any? = UNINITIALIZED
    return {
        if (instance === UNINITIALIZED) {
            instance = factory()
        }
        instance as T
    }
}