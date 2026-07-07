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
                api(project(":biometry"))
                implementation("dev.whyoleg.cryptography:cryptography-core:0.6.0")
                implementation("dev.whyoleg.cryptography:cryptography-provider-optimal:0.6.0")
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
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
