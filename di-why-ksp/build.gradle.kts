plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get().toInt()))
    }
}

dependencies {
    implementation(projects.diWhyRuntime)
    implementation(libs.ksp)
    implementation(libs.kotlinPoet)
    implementation(libs.kotlinPoet.ksp)
}
