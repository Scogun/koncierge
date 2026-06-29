import java.io.File

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library") version "9.0.1"
}

val hostOs = System.getProperty("os.name")
val isWindows = hostOs.startsWith("Windows")
val isMac = hostOs.startsWith("Mac")

val windowsHelloNativeDir = layout.projectDirectory.dir("src/nativeInterop/windows-hello")
val windowsHelloNativeBuildDir = layout.buildDirectory.dir("windows-hello-native")
val windowsHelloNativeBinDir = windowsHelloNativeBuildDir.map { it.dir("bin") }
val cmakeExecutable = providers.gradleProperty("windowsHello.cmake")
    .orElse(providers.environmentVariable("CMAKE_EXE"))
    .orElse(knownVisualStudioCmakeLocations()
        .firstOrNull { it.isFile }
        ?.absolutePath
        ?: "cmake")

fun knownVisualStudioCmakeLocations(): List<File> {
    if (!isWindows) {
        return emptyList()
    }

    val programFiles = listOfNotNull(
        System.getenv("ProgramFiles"),
        System.getenv("ProgramFiles(x86)"),
    ).distinct()

    val versions = listOf("18", "2026", "2022")
    val editions = listOf("BuildTools", "Community", "Professional", "Enterprise")

    return programFiles.flatMap { root ->
        versions.flatMap { version ->
            editions.map { edition ->
                File(
                    root,
                    "Microsoft Visual Studio/$version/$edition/Common7/IDE/CommonExtensions/Microsoft/CMake/CMake/bin/cmake.exe",
                )
            }
        }
    }
}

val configureWindowsHelloNative = tasks.register<Exec>("configureWindowsHelloNative") {
    group = "build"
    description = "Configures the Windows Hello C++ bridge."
    onlyIf { isWindows }

    inputs.dir(windowsHelloNativeDir)
    outputs.dir(windowsHelloNativeBuildDir)

    doFirst {
        commandLine(
            cmakeExecutable.get(),
            "-S",
            windowsHelloNativeDir.asFile.absolutePath,
            "-B",
            windowsHelloNativeBuildDir.get().asFile.absolutePath,
            "-A",
            "x64",
            "-DCMAKE_BUILD_TYPE=Release",
        )
    }
}

val buildWindowsHelloNative = tasks.register<Exec>("buildWindowsHelloNative") {
    group = "build"
    description = "Builds the Windows Hello DLL used by Kotlin/Native."
    onlyIf { isWindows }
    dependsOn(configureWindowsHelloNative)

    inputs.dir(windowsHelloNativeDir)
    outputs.dir(windowsHelloNativeBinDir)

    doFirst {
        commandLine(
            cmakeExecutable.get(),
            "--build",
            windowsHelloNativeBuildDir.get().asFile.absolutePath,
            "--config",
            "Release",
        )
    }
}

val copyWindowsHelloDll = tasks.register("copyWindowsHelloDll") {
    group = "build"
    description = "Copies windows_hello.dll beside MinGW executables."
    onlyIf { isWindows }
    dependsOn(buildWindowsHelloNative)

    val dllCandidates = windowsHelloNativeBinDir.map { binDir ->
        listOf(
            binDir.file("Release/windows_hello.dll").asFile,
            binDir.file("windows_hello.dll").asFile,
        )
    }

    inputs.files(dllCandidates)
    outputs.dirs(
        layout.buildDirectory.dir("bin/mingwX64/debugExecutable"),
        layout.buildDirectory.dir("bin/mingwX64/releaseExecutable"),
    )

    doLast {
        val dll = dllCandidates.get().firstOrNull(File::isFile)
            ?: throw GradleException("windows_hello.dll was not produced by the native build.")

        copy {
            from(dll)
            into(layout.buildDirectory.dir("bin/mingwX64/debugExecutable"))
        }
        copy {
            from(dll)
            into(layout.buildDirectory.dir("bin/mingwX64/releaseExecutable"))
        }
    }
}

repositories {
    google()
    mavenCentral()
}

kotlin {
    if (isWindows) {
        mingwX64 {
            binaries {
                executable {
                    entryPoint = "com.ucasoft.koncierge.main"
                }
            }
            compilations["main"].cinterops {
                create("wbf") {
                    includeDirs(
                        "C:\\Program Files (x86)\\Windows Kits\\10\\Include\\10.0.26100.0\\shared",
                        "C:\\Program Files (x86)\\Windows Kits\\10\\Include\\10.0.26100.0\\um"
                    )
                    compilerOpts(
                        "-D_AMD64_",
                        "-DWIN32_LEAN_AND_MEAN"
                    )
                }
                create("windowsHello") {
                    definitionFile.set(project.file("src/nativeInterop/cinterop/windowsHello.def"))
                    includeDirs(project.file("src/nativeInterop/windows-hello/include"))
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
    android {
        namespace = "com.ucasoft.koncierge"
        compileSdk = 36
        minSdk = 26
    }

    sourceSets {
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
            }
        }
        getByName("androidMain") {
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

tasks.matching { it.name == "cinteropWindowsHelloMingwX64" }.configureEach {
    dependsOn(buildWindowsHelloNative)
}

tasks.matching { it.name.startsWith("link") && it.name.endsWith("ExecutableMingwX64") }.configureEach {
    dependsOn(buildWindowsHelloNative)
    finalizedBy(copyWindowsHelloDll)
}
