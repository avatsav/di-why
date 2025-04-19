package coffee

class CoffeeMachine(
    private val logger: Logger,
    private val heater: Heater,
    private val pump: Pump,
) {

    fun brew() {
        heater.on()
        pump.pump()
        logger.log("coffee is brewed")
        heater.off()
    }

}