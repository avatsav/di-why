package dev.avatsav.coffee

import dev.avatsav.diwhy.Inject
import dev.avatsav.diwhy.Singleton

interface Heater {
    fun on()
    fun off()
    val isHot: Boolean
}


@Singleton
class ElectricHeater @Inject constructor(private val logger: Logger) : Heater {
    override var isHot = false
        private set

    override fun on() {
        isHot = true
        println("~ ~ ~ heating ~ ~ ~")
    }

    override fun off() {
        isHot = false
        println(". . . cooling . . .")
    }
}