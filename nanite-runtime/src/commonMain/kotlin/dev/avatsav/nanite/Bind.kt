package dev.avatsav.nanite

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Bind<Requested : Any>