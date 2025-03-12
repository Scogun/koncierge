pluginManagement {
    resolutionStrategy {
        plugins {
            val kotlinVersion = "2.1.10"
            kotlin("multiplatform") version kotlinVersion apply false
        }
    }
    repositories {
        google()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "koncierge"