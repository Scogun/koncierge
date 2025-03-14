plugins {
    kotlin("multiplatform")
    id("com.android.library") version "8.7.3"
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isWindows = hostOs.startsWith("Windows")
    val isMac = hostOs.startsWith("Mac")
    if (isWindows) {
        mingwX64 {
            binaries {
                executable {
                    entryPoint = "com.ucasoft.koncierge.main"
                }
            }
            compilations["main"].cinterops {
                val wbf by creating {
                    includeDirs(
                        "C:\\Program Files (x86)\\Windows Kits\\10\\Include\\10.0.26100.0\\shared",
                        "C:\\Program Files (x86)\\Windows Kits\\10\\Include\\10.0.26100.0\\um"
                    )
                    compilerOpts(
                        "-D_AMD64_",
                        "-DWIN32_LEAN_AND_MEAN"
                    )
                }
            }
        }
    }
    if (isMac) {
        macosArm64 {
            binaries {
                executable {
                    entryPoint = "com.ucasoft.koncierge.main"
                }
            }
            compilations["main"].cinterops {
                create("LocalAuthentication") {
                    definitionFile = file("src/nativeInterop/cinterop/touchId.def")
                }
            }
        }
    }
    androidTarget {
        publishLibraryVariants("debug", "release")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("androidx.biometric:biometric:1.1.0")
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

android {
    namespace = "com.ucasoft.koncierge"
    compileSdk = 35
    defaultConfig {
        minSdk = 26
        testOptions {
            targetSdk = 35
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}