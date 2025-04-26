package dev.avatsav.coffee

import dev.avatsav.nanite.Bind
import dev.avatsav.nanite.Inject

interface Logger {
    fun log(message: String)
}

@Bind<Logger>
class CoffeeMachineLogger @Inject constructor() : Logger {
    override fun log(message: String) {
        println(message)
    }
}