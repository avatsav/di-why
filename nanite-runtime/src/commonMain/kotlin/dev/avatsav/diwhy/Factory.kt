package dev.avatsav.diwhy

fun interface Factory<T : Any> {
    fun get(objectGraph: ObjectGraph): T
}

@Suppress("UNCHECKED_CAST")
// TODO: Use Double Check à la Dagger
fun <T : Any> singleton(factory: Factory<T>): Factory<T> {
    var instance: Any? = UNINITIALIZED
    return Factory { graph ->
        if (instance === UNINITIALIZED) {
            instance = factory.get(graph)
        }
        instance as T
    }
}

private val UNINITIALIZED = Any()
