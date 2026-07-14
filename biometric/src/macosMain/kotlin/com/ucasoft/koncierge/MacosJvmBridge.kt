@file:OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)

package com.ucasoft.koncierge

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.CName
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@CName(
    externName = "koncierge_macos_biometric_check_availability",
    shortName = "koncierge_macos_biometric_check_availability",
)
fun checkMacosJvmBiometricAvailability(): Int {
    return runCatching {
        Koncierge().isAvailable().toBridgeAvailabilityCode()
    }.getOrDefault(BRIDGE_AVAILABILITY_UNKNOWN)
}

@CName(
    externName = "koncierge_macos_biometric_authenticate",
    shortName = "koncierge_macos_biometric_authenticate",
)
fun authenticateMacosJvmBiometric(
    localizedReason: CPointer<ByteVar>?,
    timeoutMillis: Long,
): Int {
    val timeout = if (timeoutMillis < 0) Duration.INFINITE else timeoutMillis.milliseconds
    return try {
        runBlocking {
            Koncierge().authenticate(localizedReason?.toKString().orEmpty(), timeout)
        }.toBridgeAuthenticationCode()
    } catch (_: TimeoutCancellationException) {
        BRIDGE_RESULT_TIMEOUT
    } catch (_: Throwable) {
        BRIDGE_RESULT_UNKNOWN
    }
}

private fun BiometricResults.toBridgeAvailabilityCode(): Int = when (this) {
    BiometricResults.Available -> BRIDGE_AVAILABILITY_AVAILABLE
    BiometricResults.HardwareUnavailable -> BRIDGE_AVAILABILITY_HARDWARE_UNAVAILABLE
    BiometricResults.NotEnrolled -> BRIDGE_AVAILABILITY_NOT_ENROLLED
    BiometricResults.Locked -> BRIDGE_AVAILABILITY_LOCKED
    BiometricResults.FeatureUnavailable -> BRIDGE_AVAILABILITY_FEATURE_UNAVAILABLE
    else -> BRIDGE_AVAILABILITY_UNKNOWN
}

private fun BiometricResults.toBridgeAuthenticationCode(): Int = when (this) {
    BiometricResults.AuthenticationSuccessful -> BRIDGE_RESULT_VERIFIED
    BiometricResults.AuthenticationFailed -> BRIDGE_RESULT_AUTHENTICATION_FAILED
    BiometricResults.AuthenticationCancelled -> BRIDGE_RESULT_CANCELED
    BiometricResults.HardwareUnavailable -> BRIDGE_RESULT_HARDWARE_UNAVAILABLE
    BiometricResults.NotEnrolled -> BRIDGE_RESULT_NOT_ENROLLED
    BiometricResults.Locked -> BRIDGE_RESULT_LOCKED
    BiometricResults.FeatureUnavailable -> BRIDGE_RESULT_FEATURE_UNAVAILABLE
    else -> BRIDGE_RESULT_UNKNOWN
}

private const val BRIDGE_AVAILABILITY_AVAILABLE = 0
private const val BRIDGE_AVAILABILITY_HARDWARE_UNAVAILABLE = 1
private const val BRIDGE_AVAILABILITY_NOT_ENROLLED = 2
private const val BRIDGE_AVAILABILITY_LOCKED = 3
private const val BRIDGE_AVAILABILITY_FEATURE_UNAVAILABLE = 4
private const val BRIDGE_AVAILABILITY_UNKNOWN = 100

private const val BRIDGE_RESULT_VERIFIED = 0
private const val BRIDGE_RESULT_AUTHENTICATION_FAILED = 1
private const val BRIDGE_RESULT_CANCELED = 2
private const val BRIDGE_RESULT_HARDWARE_UNAVAILABLE = 3
private const val BRIDGE_RESULT_NOT_ENROLLED = 4
private const val BRIDGE_RESULT_LOCKED = 5
private const val BRIDGE_RESULT_FEATURE_UNAVAILABLE = 6
private const val BRIDGE_RESULT_TIMEOUT = 7
private const val BRIDGE_RESULT_UNKNOWN = 100
