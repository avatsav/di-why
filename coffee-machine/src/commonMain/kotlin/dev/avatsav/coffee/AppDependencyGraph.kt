package dev.avatsav.coffee

import dev.avatsav.nanite.Graph
import dev.avatsav.nanite.internal.Provider
import dev.avatsav.nanite.internal.provider
import dev.avatsav.nanite.internal.singletonProvider

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
        get() = singletonProvider { ElectricHeater(provideLogger()) }
    private val provideCoffeeMachine: Provider<CoffeeMachine>
        get() = provider { CoffeeMachine(logger = provideLogger(), heater = provideHeater(), pump = providePump()) }

    private val providePump: Provider<Thermosiphon>
        get() = provider { Thermosiphon(provideLogger(), provideHeater()) }

    private val provideLogger: Provider<CoffeeMachineLogger>
        get() = singletonProvider { CoffeeMachineLogger() }

}