package dev.avatsav.coffee

fun main() {
    println("---Manual DI---")
    val coffeeMachine1 = createCoffeeMachineManual()
    coffeeMachine1.brew()
}

fun createCoffeeMachineManual(): CoffeeMachine {
    val logger = CoffeeMachineLogger()
    val heater = ElectricHeater(logger)
    val pump = Thermosiphon(logger, heater)
    return CoffeeMachine(
        logger = logger,
        heater = heater,
        pump = pump,
    )
}