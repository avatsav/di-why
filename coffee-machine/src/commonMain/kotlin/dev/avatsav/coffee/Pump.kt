package dev.avatsav.coffee

 import dev.avatsav.diwhy.Inject

interface Pump {
    fun pump()
}

class Thermosiphon @Inject constructor(
    private val logger: Logger,
    private val heater: Heater
) : Pump {
    override fun pump() {
        if (heater.isHot) {
            logger.log("=> => pumping => =>")
        }
    }
}