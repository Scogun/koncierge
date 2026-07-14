package com.ucasoft.koncierge

import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import kotlin.io.path.absolutePathString
import kotlin.io.path.outputStream
import kotlin.time.Duration

internal class MacOsJvmBiometricAuthenticator : JvmBiometricAuthenticator {

    private val libraryResult: Result<MacosBiometricLibrary> by lazy { loadLibrary() }

    override fun isBiometricAvailable(): Boolean = checkAvailability() == BiometricResults.Available

    override suspend fun authenticate(message: String, timeout: Duration): BiometricResults {
        val availability = checkAvailability()
        if (availability != BiometricResults.Available) {
            return availability
        }
        if (!timeout.isInfinite() && timeout <= Duration.ZERO) {
            return BiometricResults.AuthenticationCancelled
        }

        return withContext(Dispatchers.IO) {
            val library = libraryResult.getOrElse { error ->
                return@withContext error.toLoadErrorResult()
            }
            val timeoutMillis = if (timeout.isInfinite()) {
                -1L
            } else {
                maxOf(1L, timeout.inWholeMilliseconds)
            }
            val messageBytes = message.encodeToByteArray()
            val localizedReason = Memory(messageBytes.size.toLong() + 1L).apply {
                write(0, messageBytes, 0, messageBytes.size)
                setByte(messageBytes.size.toLong(), 0)
            }
            library.koncierge_macos_biometric_authenticate(localizedReason, timeoutMillis)
                .toAuthenticationResult()
        }
    }

    internal fun checkAvailability(): BiometricResults {
        val library = libraryResult.getOrElse { error ->
            return error.toLoadErrorResult()
        }
        return library.koncierge_macos_biometric_check_availability().toAvailabilityResult()
    }

    private fun loadLibrary(): Result<MacosBiometricLibrary> = runCatching {
        val resourcePath = "/native/macos/arm64/libkoncierge_macos_biometric.dylib"
        val resource = MacOsJvmBiometricAuthenticator::class.java.getResourceAsStream(resourcePath)
        if (resource == null) {
            Native.load("koncierge_macos_biometric", MacosBiometricLibrary::class.java)
        } else {
            val libraryFile = Files.createTempFile("koncierge-macos-biometric", ".dylib")
            resource.use { input ->
                libraryFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            libraryFile.toFile().deleteOnExit()
            Native.load(libraryFile.absolutePathString(), MacosBiometricLibrary::class.java)
        }
    }

    private fun Throwable.toLoadErrorResult(): BiometricResults {
        return BiometricResults.AuthenticationError(
            "Could not load macOS biometric native library: ${message.orEmpty()}"
        )
    }

    private fun Int.toAvailabilityResult(): BiometricResults = when (this) {
        MB_AVAILABILITY_AVAILABLE -> BiometricResults.Available
        MB_AVAILABILITY_HARDWARE_UNAVAILABLE -> BiometricResults.HardwareUnavailable
        MB_AVAILABILITY_NOT_ENROLLED -> BiometricResults.NotEnrolled
        MB_AVAILABILITY_LOCKED -> BiometricResults.Locked
        MB_AVAILABILITY_FEATURE_UNAVAILABLE -> BiometricResults.FeatureUnavailable
        else -> BiometricResults.AuthenticationError("macOS biometric availability failed")
    }

    private fun Int.toAuthenticationResult(): BiometricResults = when (this) {
        MB_RESULT_VERIFIED -> BiometricResults.AuthenticationSuccessful
        MB_RESULT_AUTHENTICATION_FAILED -> BiometricResults.AuthenticationFailed
        MB_RESULT_CANCELED,
        MB_RESULT_TIMEOUT -> BiometricResults.AuthenticationCancelled
        MB_RESULT_HARDWARE_UNAVAILABLE -> BiometricResults.HardwareUnavailable
        MB_RESULT_NOT_ENROLLED -> BiometricResults.NotEnrolled
        MB_RESULT_LOCKED -> BiometricResults.Locked
        MB_RESULT_FEATURE_UNAVAILABLE -> BiometricResults.FeatureUnavailable
        else -> BiometricResults.AuthenticationError("macOS biometric authentication failed")
    }

    private interface MacosBiometricLibrary : Library {
        fun koncierge_macos_biometric_check_availability(): Int

        fun koncierge_macos_biometric_authenticate(
            localizedReason: Memory,
            timeoutMillis: Long,
        ): Int
    }

    private companion object {
        private const val MB_AVAILABILITY_AVAILABLE = 0
        private const val MB_AVAILABILITY_HARDWARE_UNAVAILABLE = 1
        private const val MB_AVAILABILITY_NOT_ENROLLED = 2
        private const val MB_AVAILABILITY_LOCKED = 3
        private const val MB_AVAILABILITY_FEATURE_UNAVAILABLE = 4

        private const val MB_RESULT_VERIFIED = 0
        private const val MB_RESULT_AUTHENTICATION_FAILED = 1
        private const val MB_RESULT_CANCELED = 2
        private const val MB_RESULT_HARDWARE_UNAVAILABLE = 3
        private const val MB_RESULT_NOT_ENROLLED = 4
        private const val MB_RESULT_LOCKED = 5
        private const val MB_RESULT_FEATURE_UNAVAILABLE = 6
        private const val MB_RESULT_TIMEOUT = 7
    }
}
