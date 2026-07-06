import java.io.File

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library") version "9.0.1"
    id("com.vanniktech.maven.publish") version "0.37.0"
}

val hostOs = System.getProperty("os.name")
val isWindows = hostOs.startsWith("Windows")
val isMac = hostOs.startsWith("Mac")

val windowsHelloNativeDir = layout.projectDirectory.dir("src/nativeInterop/windows-hello")
val windowsHelloNativeObjectFile = layout.buildDirectory.file("windows-hello-native/obj/windows_hello.o")
val windowsHelloNativeArchiveFile = layout.buildDirectory.file("windows-hello-native/bin/Release/libwindows_hello.a")
val windowsHelloJvmObjectFile = layout.buildDirectory.file("windows-hello-jvm/obj/windows_hello.o")
val windowsHelloJvmLibraryFile = layout.buildDirectory.file("windows-hello-jvm/bin/windows_hello.dll")
val windowsHelloJvmResourceFile =
    layout.projectDirectory.file("src/jvmMain/resources/native/windows/x86-64/windows_hello.dll")
val windowsHelloMingwRoot = providers.gradleProperty("windowsHello.mingwRoot")
    .map(::File)
    .orElse(providers.provider {
        File(System.getProperty("user.home"), ".konan/dependencies/msys2-mingw-w64-x86_64-2")
    })

fun windowsHelloMingwTool(name: String): File = File(windowsHelloMingwRoot.get(), "bin/$name.exe")

fun registerWindowsHelloCompileTask(
    taskName: String,
    desc: String,
    inputDirectory: Directory,
    outputFile: Provider<RegularFile>,
    linkingFlag: String
) = tasks.register<Exec>(taskName) {
    group = "build"
    description = desc
    onlyIf { isWindows }

    inputs.dir(inputDirectory)
    outputs.file(outputFile)

    doFirst {
        val gcc = windowsHelloMingwTool("gcc")
        if (!gcc.isFile) {
            throw GradleException(
                "MinGW gcc was not found at ${gcc.absolutePath}. " +
                        "Run a mingwX64 Kotlin/Native task first, or set -PwindowsHello.mingwRoot=<path>."
            )
        }

        outputFile.get().asFile.parentFile.mkdirs()
        environment("PATH", "${gcc.parentFile.absolutePath}${File.pathSeparator}${System.getenv("PATH")}")
        commandLine(
            gcc.absolutePath,
            "-std=c11",
            "-DWIN32_LEAN_AND_MEAN",
            "-DNOMINMAX",
            linkingFlag,
            "-I${inputDirectory.dir("include").asFile.absolutePath}",
            "-c",
            inputDirectory.file("src/windows_hello.c").asFile.absolutePath,
            "-o",
            outputFile.get().asFile.absolutePath,
        )
    }
}

fun registerWindowsHelloBuildTask(
    taskName: String,
    desc: String,
    inputDirectory: Directory,
    inputFile: Provider<RegularFile>,
    outputFile: Provider<RegularFile>,
    toolName: String,
    toolArguments: List<String>,
    vararg resultFileArguments: String,
) = tasks.register<Exec>(taskName) {
    group = "build"
    description = desc
    onlyIf { isWindows }

    inputs.dir(inputDirectory)
    inputs.file(inputFile)
    outputs.file(outputFile)

    doFirst {
        val tool = windowsHelloMingwTool(toolName)
        if (!tool.isFile) {
            throw GradleException(
                "MinGW $toolName was not found at ${tool.absolutePath}. " +
                        "Run a mingwX64 Kotlin/Native task first, or set -PwindowsHello.mingwRoot=<path>."
            )
        }

        val toolResultFile = outputFile.get().asFile
        toolResultFile.parentFile.mkdirs()
        if (toolResultFile.isFile && !toolResultFile.delete()) {
            throw GradleException("Could not replace ${toolResultFile.absolutePath}.")
        }

        environment("PATH", "${tool.parentFile.absolutePath}${File.pathSeparator}${System.getenv("PATH")}")
        commandLine(
            tool.absolutePath,
            *toolArguments.toTypedArray(),
            toolResultFile.absolutePath,
            *resultFileArguments
        )
    }
}

val compileWindowsHelloNative = registerWindowsHelloCompileTask(
    "compileWindowsHelloNative",
    "Compiles the Windows Hello C bridge with MinGW.",
    windowsHelloNativeDir,
    windowsHelloNativeObjectFile,
    "-DWINDOWS_HELLO_STATIC"
)

val buildWindowsHelloNative = registerWindowsHelloBuildTask(
    "buildWindowsHelloNative",
    "Builds the Windows Hello static archive used by Kotlin/Native.",
    windowsHelloNativeDir,
    windowsHelloNativeObjectFile,
    windowsHelloNativeArchiveFile,
    "ar",
    listOf("rcs"),
    windowsHelloNativeObjectFile.get().asFile.absolutePath
)

val compileWindowsHelloJvmNative = registerWindowsHelloCompileTask(
    "compileWindowsHelloJvmNative",
    "Compiles the Windows Hello C bridge used by the JVM target.",
    windowsHelloNativeDir,
    windowsHelloJvmObjectFile,
    "-DWINDOWS_HELLO_EXPORTS"
)

val buildWindowsHelloJvmNative = registerWindowsHelloBuildTask(
    "buildWindowsHelloJvmNative",
    "Builds the Windows Hello static archive used by the JVM target.",
    windowsHelloNativeDir,
    windowsHelloJvmObjectFile,
    windowsHelloJvmLibraryFile,
    "gcc",
    listOf("-shared", "-static", "-static-libgcc", "-o"),
    windowsHelloJvmLibraryFile.get().asFile.absolutePath,
    "-lruntimeobject",
    "-luser32",
)

val syncWindowsHelloJvmNativeResource = tasks.register<Copy>("syncWindowsHelloJvmNativeResource") {
    group = "build"
    description = "Refreshes the packaged Windows Hello JVM DLL resource."
    onlyIf { isWindows }
    dependsOn(buildWindowsHelloJvmNative)

    from(windowsHelloJvmLibraryFile)
    into(windowsHelloJvmResourceFile.asFile.parentFile)
}

val verifyWindowsHelloJvmNativeResource = tasks.register("verifyWindowsHelloJvmNativeResource") {
    group = "verification"
    description = "Verifies that the Windows Hello JVM DLL is packaged as a source resource."

    inputs.file(windowsHelloJvmResourceFile)

    doLast {
        if (!windowsHelloJvmResourceFile.asFile.isFile) {
            throw GradleException(
                "Missing ${windowsHelloJvmResourceFile.asFile}. " +
                    "Run syncWindowsHelloJvmNativeResource on Windows before publishing."
            )
        }
    }
}

group = "com.ucasoft.koncierge"

version = "0.1.0"

repositories {
    google()
    mavenCentral()
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

    if (isWindows) {
        mingwX64 {
            binaries {
                executable {
                    entryPoint = "com.ucasoft.koncierge.main"
                }
            }
            compilations["main"].cinterops {
                // TODO Disable WinBio for now
                /*create("wbf") {
                    includeDirs(
                        "C:\\Program Files (x86)\\Windows Kits\\10\\Include\\10.0.26100.0\\shared",
                        "C:\\Program Files (x86)\\Windows Kits\\10\\Include\\10.0.26100.0\\um"
                    )
                    compilerOpts(
                        "-D_AMD64_",
                        "-DWIN32_LEAN_AND_MEAN"
                    )
                }*/
                create("windowsHello") {
                    definitionFile.set(project.file("src/nativeInterop/cinterop/windowsHello.def"))
                    includeDirs(project.file("src/nativeInterop/windows-hello/include"))
                }
            }
        }
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
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
                    definitionFile.set(project.file("src/nativeInterop/cinterop/touchId.def"))
                }
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
            }
        }
        jvmMain {
            dependencies {
                implementation("net.java.dev.jna:jna:5.19.1")
            }
        }
        androidMain {
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
}

tasks.named("jvmProcessResources") {
    dependsOn(verifyWindowsHelloJvmNativeResource)
}
