package dev.avatsav.coffee

import dev.avatsav.diwhy.Binds
import dev.avatsav.diwhy.Graph

@Graph(modules = [CoffeeModule::class])
interface AppDependencyGraph {
    val coffeeMachine: CoffeeMachine
}

@Binds<Heater, ElectricHeater>
@Binds<Pump, Thermosiphon>
@Binds<Logger, CoffeeMachineLogger>
interface CoffeeModule