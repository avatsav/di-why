package dev.avatsav.coffee

interface Pump {
    fun pump()
}

class Thermosiphon(
    private val logger: Logger,
    private val heater: Heater
) : Pump {
    override fun pump() {
        if (heater.isHot) {
            logger.log("=> => pumping => =>")
        }
    }
}