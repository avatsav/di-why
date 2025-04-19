package dev.avatsav.diwhy

import kotlin.reflect.KClass

class FactoryHolderModule : Module {
    private val factories = mutableMapOf<KClass<*>, Factory<*>>()

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(requestedType: KClass<T>): Factory<T>? {
        return factories[requestedType] as Factory<T>?
    }

    fun <T : Any> install(requestedType: KClass<T>, factory: Factory<T>) {
        factories[requestedType] = factory as Factory<*>
    }
}

inline fun <reified T : Any> FactoryHolderModule.install(
    noinline factory: ObjectGraph.() -> T
) = install(T::class, factory)

inline fun <reified REQUESTED : Any, reified PROVIDED : REQUESTED> FactoryHolderModule.bind() {
    install(REQUESTED::class) { objectGraph ->
        objectGraph[PROVIDED::class]
    }
}