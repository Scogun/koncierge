plugins {
    kotlin("multiplatform")
}

repositories {
    mavenCentral()
}

kotlin {
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
    macosArm64()
}