package dev.avatsav.coffee

import dev.avatsav.diwhy.Inject
import dev.avatsav.diwhy.Singleton

interface Logger {
    fun log(message: String)
}

@Singleton
class CoffeeMachineLogger @Inject constructor() : Logger {
    override fun log(message: String) {
        println(message)
    }
}