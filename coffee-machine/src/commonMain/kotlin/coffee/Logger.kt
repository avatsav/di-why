package coffee

interface Logger {
    fun log(message: String)
}

class CoffeeMachineLogger : Logger {
    override fun log(message: String) {
        println(message)
    }
}