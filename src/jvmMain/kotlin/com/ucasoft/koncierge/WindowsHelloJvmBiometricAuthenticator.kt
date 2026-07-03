package com.ucasoft.koncierge

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Structure
import com.sun.jna.WString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.outputStream
import kotlin.time.Duration

internal class WindowsHelloJvmBiometricAuthenticator : JvmBiometricAuthenticator {

    private val libraryResult: Result<WindowsHelloLibrary> by lazy { loadLibrary() }

    override fun isBiometricAvailable(): Boolean = checkAvailability() == BiometricResults.Available

    override suspend fun authenticate(message: String, timeout: Duration): BiometricResults {
        val availability = checkAvailability()
        if (availability != BiometricResults.Available) {
            return availability
        }

        return withTimeout(timeout) {
            withContext(Dispatchers.IO) {
                val result = WindowsHelloResult()
                val library = libraryResult.getOrElse { error ->
                    return@withContext error.toLoadErrorResult()
                }
                val hresult = library.windows_hello_request_verification(0L, WString(message), result)

                result.read()
                if (hresult < 0) {
                    BiometricResults.AuthenticationError("Windows Hello verification failed with HRESULT $hresult")
                } else {
                    result.code.toVerificationResult(result.hresult)
                }
            }
        }
    }

    private fun checkAvailability(): BiometricResults {
        val result = WindowsHelloResult()
        val library = libraryResult.getOrElse { error ->
            return error.toLoadErrorResult()
        }
        val hresult = library.windows_hello_check_availability(result)

        result.read()
        return if (hresult < 0) {
            BiometricResults.AuthenticationError("Windows Hello availability check failed with HRESULT $hresult")
        } else {
            result.code.toAvailabilityResult(result.hresult)
        }
    }

    private fun loadLibrary(): Result<WindowsHelloLibrary> {
        return runCatching {
            val resourcePath = "/native/windows/x86-64/windows_hello.dll"
            val resource = WindowsHelloJvmBiometricAuthenticator::class.java.getResourceAsStream(resourcePath)
            if (resource == null) {
                Native.load("windows_hello", WindowsHelloLibrary::class.java)
            } else {
                val libraryFile = Files.createTempFile("koncierge-windows-hello", ".dll")
                resource.use { input ->
                    libraryFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                libraryFile.toFile().deleteOnExit()
                Native.load(libraryFile.absolutePathString(), WindowsHelloLibrary::class.java)
            }
        }
    }

    private fun Throwable.toLoadErrorResult(): BiometricResults {
        return BiometricResults.AuthenticationError("Could not load Windows Hello native library: ${message.orEmpty()}")
    }

    private fun Int.toAvailabilityResult(hresult: Int): BiometricResults {
        return when (this) {
            WH_AVAILABILITY_AVAILABLE -> BiometricResults.Available
            WH_AVAILABILITY_DEVICE_NOT_PRESENT -> BiometricResults.HardwareUnavailable
            WH_AVAILABILITY_NOT_CONFIGURED -> BiometricResults.NotEnrolled
            WH_AVAILABILITY_DISABLED_BY_POLICY -> BiometricResults.FeatureUnavailable
            WH_AVAILABILITY_DEVICE_BUSY -> BiometricResults.Locked
            else -> BiometricResults.AuthenticationError("Windows Hello availability failed with HRESULT $hresult")
        }
    }

    private fun Int.toVerificationResult(hresult: Int): BiometricResults {
        return when (this) {
            WH_RESULT_VERIFIED -> BiometricResults.AuthenticationSuccessful
            WH_RESULT_DEVICE_NOT_PRESENT -> BiometricResults.HardwareUnavailable
            WH_RESULT_NOT_CONFIGURED -> BiometricResults.NotEnrolled
            WH_RESULT_DISABLED_BY_POLICY -> BiometricResults.FeatureUnavailable
            WH_RESULT_DEVICE_BUSY,
            WH_RESULT_RETRIES_EXHAUSTED -> BiometricResults.Locked
            WH_RESULT_CANCELED -> BiometricResults.AuthenticationCancelled
            else -> BiometricResults.AuthenticationError("Windows Hello verification failed with HRESULT $hresult")
        }
    }

    private interface WindowsHelloLibrary : Library {
        fun windows_hello_check_availability(outResult: WindowsHelloResult): Int
        fun windows_hello_request_verification(ownerWindowHandle: Long, prompt: WString, outResult: WindowsHelloResult): Int
    }

    @Suppress("unused")
    open class WindowsHelloResult : Structure() {
        @JvmField
        var code: Int = 0

        @JvmField
        var hresult: Int = 0

        override fun getFieldOrder(): List<String> = listOf("code", "hresult")
    }

    private companion object {
        private const val WH_AVAILABILITY_AVAILABLE = 0
        private const val WH_AVAILABILITY_DEVICE_NOT_PRESENT = 1
        private const val WH_AVAILABILITY_NOT_CONFIGURED = 2
        private const val WH_AVAILABILITY_DISABLED_BY_POLICY = 3
        private const val WH_AVAILABILITY_DEVICE_BUSY = 4

        private const val WH_RESULT_VERIFIED = 0
        private const val WH_RESULT_DEVICE_NOT_PRESENT = 1
        private const val WH_RESULT_NOT_CONFIGURED = 2
        private const val WH_RESULT_DISABLED_BY_POLICY = 3
        private const val WH_RESULT_DEVICE_BUSY = 4
        private const val WH_RESULT_RETRIES_EXHAUSTED = 5
        private const val WH_RESULT_CANCELED = 6
    }
}
