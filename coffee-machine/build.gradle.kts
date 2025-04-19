import android.databinding.tool.ext.capitalizeUS

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.ksp)
}

group = "dev.avatsav"
version = "1.0.0"

kotlin {
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.diWhyRuntime)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}
val kspTargets = kotlin.targets.names.map { it.capitalizeUS() }

dependencies {
    for (target in kspTargets) {
        val targetConfigSuffix = if (target == "Metadata") "CommonMainMetadata" else target
        add("ksp${targetConfigSuffix}", projects.diWhyKsp)
    }
}