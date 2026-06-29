@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.ucasoft.koncierge

import winhello.WindowsHello
import winhello.WindowsHelloAvailability
import winhello.WindowsHelloVerificationResult
import kotlin.time.Duration

actual class Koncierge {

    actual fun isBiometricAvailable() = checkAvailability() == BiometricResults.Available

    actual suspend fun authenticate(message: String, timeout: Duration): BiometricResults {
        val availability = checkAvailability()
        if (availability != BiometricResults.Available) {
            return availability
        }

        val verification = WindowsHello.requestVerification(prompt = message)
        return verification.result.toBiometricResult(verification.hresult)
    }

    private fun checkAvailability(): BiometricResults = when (WindowsHello.checkAvailability()) {
        WindowsHelloAvailability.Available -> BiometricResults.Available
        WindowsHelloAvailability.DeviceNotPresent -> BiometricResults.HardwareUnavailable
        WindowsHelloAvailability.NotConfigured -> BiometricResults.NotEnrolled
        WindowsHelloAvailability.DisabledByPolicy -> BiometricResults.FeatureUnavailable
        WindowsHelloAvailability.DeviceBusy -> BiometricResults.Locked
        WindowsHelloAvailability.Unknown -> BiometricResults.AuthenticationError("Windows Hello availability is unknown")
    }

    private fun WindowsHelloVerificationResult.toBiometricResult(hresult: Int): BiometricResults = when (this) {
        WindowsHelloVerificationResult.Verified -> BiometricResults.AuthenticationSuccessful
        WindowsHelloVerificationResult.DeviceNotPresent -> BiometricResults.HardwareUnavailable
        WindowsHelloVerificationResult.NotConfigured -> BiometricResults.NotEnrolled
        WindowsHelloVerificationResult.DisabledByPolicy -> BiometricResults.FeatureUnavailable
        WindowsHelloVerificationResult.DeviceBusy -> BiometricResults.Locked
        WindowsHelloVerificationResult.RetriesExhausted -> BiometricResults.Locked
        WindowsHelloVerificationResult.Canceled -> BiometricResults.AuthenticationCancelled
        WindowsHelloVerificationResult.Unknown -> BiometricResults.AuthenticationError(
            "Windows Hello verification failed with HRESULT $hresult"
        )
    }
}
