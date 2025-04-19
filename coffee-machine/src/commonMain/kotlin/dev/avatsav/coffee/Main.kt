package dev.avatsav.coffee

import dev.avatsav.diwhy.*

fun main() {
    println("---Manual DI---")
    val coffeeMachine1 = createCoffeeMachineManual()
    coffeeMachine1.brew()

    println("\n---Factory Module DI---")
    val coffeeMachine2 = createCoffeeMachineFactoryHolderModule()
    coffeeMachine2.brew()
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

fun createCoffeeMachineFactoryHolderModule(): CoffeeMachine {
    val module = FactoryHolderModule()
    module.bind<Logger, CoffeeMachineLogger>()
    module.bind<Heater, ElectricHeater>()
    module.bind<Pump, Thermosiphon>()
    module.install { CoffeeMachineLogger() }
    module.install { ElectricHeater(get()) }
    module.install { Thermosiphon(get(), get()) }
    module.install { CoffeeMachine(get(), get(), get()) }
    val objectGraph = ObjectGraph(module)
    return objectGraph.get()
}