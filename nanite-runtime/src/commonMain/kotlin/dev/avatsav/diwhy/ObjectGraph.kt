package dev.avatsav.diwhy

import kotlin.reflect.KClass

class ObjectGraph(private val modules: List<Module>) {

    constructor(vararg modules: Module) : this(modules.toList())

    // cache of factories
    private val factoryHolder = FactoryHolderModule()

    operator fun <T : Any> get(requestedType: KClass<T>): T {
        val knownFactoryOrNull = factoryHolder[requestedType]
        val factory = knownFactoryOrNull ?: getAndCacheFactory(requestedType)
        return factory.get(this)
    }

    private fun <T : Any> getAndCacheFactory(requestedType: KClass<T>): Factory<T> = modules.firstNotNullOf {
        it[requestedType]
    }.also { factory ->
        factoryHolder.install(requestedType, factory)
    }
}

inline fun <reified T : Any> ObjectGraph.get() = get(T::class)

