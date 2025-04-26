package dev.avatsav.coffee

import dev.avatsav.nanite.Bind
import dev.avatsav.nanite.Inject

interface Pump {
    fun pump()
}

@Bind<Pump>
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