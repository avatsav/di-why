package dev.avatsav.diwhy

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Binds<Requested : Any, Provided : Requested>()