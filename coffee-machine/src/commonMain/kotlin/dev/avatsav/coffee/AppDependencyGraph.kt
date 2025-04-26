package dev.avatsav.coffee

import dev.avatsav.nanite.Graph
import dev.avatsav.nanite.internal.Provider

@Graph
interface AppDependencyGraph {
    val coffeeMachine: CoffeeMachine
}


/**
 * This is what we should generate.
 */
public class GeneratedAppDependencyGraph : AppDependencyGraph {

    override val coffeeMachine: CoffeeMachine = provideCoffeeMachine()

    private val provideHeater: Provider<ElectricHeater>
        get() = Provider { ElectricHeater(provideLogger()) }
    private val provideCoffeeMachine: Provider<CoffeeMachine>
        get() = Provider { CoffeeMachine(logger = provideLogger(), heater = provideHeater(), pump = providePump()) }

    private val providePump: Provider<Thermosiphon>
        get() = Provider { Thermosiphon(provideLogger(), provideHeater()) }

    private val provideLogger: Provider<CoffeeMachineLogger>
        get() = Provider { CoffeeMachineLogger() }

}