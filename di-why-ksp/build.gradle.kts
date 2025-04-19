plugins {
    kotlin("jvm")
}

dependencies {
    implementation(projects.diWhyRuntime)
    implementation(libs.ksp)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinPoet.ksp)
}
