package coffee

interface Heater {
    fun on()
    fun off()
    val isHot: Boolean
}


class ElectricHeater(private val logger: Logger) : Heater {
    override var isHot = false
        private set

    override fun on() {
        isHot = true
        println("~ ~ ~ heating ~ ~ ~")
    }

    override fun off() {
        isHot = false
        println(". . . cooling . . .")
    }
}