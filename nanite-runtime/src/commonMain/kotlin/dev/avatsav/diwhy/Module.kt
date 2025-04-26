package dev.avatsav.diwhy

import kotlin.reflect.KClass

interface Module {
    operator fun <T : Any> get(requestedType: KClass<T>): Factory<T>?
}
