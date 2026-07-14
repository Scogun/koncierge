plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(17)

    jvm()

    androidLibrary {
        namespace = "com.ucasoft.koncierge.auth.compose"
        compileSdk = 36
        minSdk = 26
        buildToolsVersion = "36.1.0"
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":auth"))
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.material.icons)
                implementation(libs.window.core)

                implementation(libs.compose.ui.tooling)
                implementation(libs.compose.ui.tooling.preview)
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.kotest.assertions.core)
            }
        }
    }

    targets.configureEach {
        compilations.configureEach {
            compileTaskProvider.get().compilerOptions {
                freeCompilerArgs.add("-Xexpect-actual-classes")
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    pom {
        configurePom("Koncierge Auth Compose", "Compose Multiplatform authentication screen", this)
    }
}