package dev.avatsav.coffee

import dev.avatsav.nanite.Bind
import dev.avatsav.nanite.Inject

interface Heater {
    fun on()
    fun off()
    val isHot: Boolean
}


@Bind<Heater>
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