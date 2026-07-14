plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.multiplatform.library)
    alias(libs.plugins.maven.publish)
}

kotlin {
    jvmToolchain(17)

    jvm()

    androidLibrary {
        namespace = "com.ucasoft.koncierge"
        compileSdk = 36
        minSdk = 26
        buildToolsVersion = "36.1.0"
    }

    sourceSets {
        commonMain {
            dependencies {
                api(project(":biometric"))
                implementation(libs.cryptography.core)
                implementation(libs.cryptography.provider.optimal)
                implementation(libs.multiplatform.settings)
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
        configurePom("Koncierge Auth", "PIN storage, hashing, and combined PIN/biometric authentication", this)
    }
}