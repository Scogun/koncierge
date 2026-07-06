pluginManagement {
    resolutionStrategy {
        plugins {
            val kotlinVersion = "2.4.0"
            kotlin("multiplatform") version kotlinVersion apply false
        }
    }
    repositories {
        google()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "koncierge"