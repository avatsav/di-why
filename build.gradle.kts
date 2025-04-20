import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompilerOptions
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.vanniktech.mavenPublish) apply false
}

subprojects {
    plugins.withType<KotlinBasePlugin> {
        project.tasks.withType<KotlinCompilationTask<*>>().configureEach {
            compilerOptions {
                progressiveMode.set(true)
                if (this is KotlinJvmCompilerOptions) {
                    jvmTarget.set(libs.versions.jvmTarget.map(JvmTarget::fromTarget))
                }
            }
        }
    }
}