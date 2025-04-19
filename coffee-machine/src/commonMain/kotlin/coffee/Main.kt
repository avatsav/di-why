package coffee

fun main() {
    val coffeeMachine = createCoffeeMachine()
    coffeeMachine.brew()
}

fun createCoffeeMachine(): CoffeeMachine {
    val logger = CoffeeMachineLogger()
    val heater = ElectricHeater(logger)
    val pump = Thermosiphon(logger, heater)
    return CoffeeMachine(
        logger = logger,
        heater = heater,
        pump = pump,
    )
}