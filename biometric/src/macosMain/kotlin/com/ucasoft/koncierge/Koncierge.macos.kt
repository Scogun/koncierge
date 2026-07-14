package com.ucasoft.koncierge

import kotlinx.cinterop.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import platform.Foundation.NSError
import platform.LocalAuthentication.*
import kotlin.coroutines.resume
import kotlin.time.Duration

@OptIn(ExperimentalForeignApi::class)
actual class Koncierge {

    private val context = LAContext()

    actual fun isBiometricAvailable(): Boolean {
        return isAvailable() == BiometricResults.Available
    }

    actual suspend fun authenticate(message: String, timeout: Duration): BiometricResults {
        val checkAvailability = isAvailable()
        if (checkAvailability == BiometricResults.Available) {
            return withTimeout(timeout) {
                suspendCancellableCoroutine { continuation ->
                    context.evaluatePolicy(
                        LAPolicyDeviceOwnerAuthenticationWithBiometrics,
                        localizedReason = message
                    ) { success, error ->
                        if (success) {
                            continuation.resume(BiometricResults.AuthenticationSuccessful)
                        } else {
                            continuation.resume(error.toBiometricResult())
                        }
                    }

                    continuation.invokeOnCancellation {
                        context.invalidate()
                    }
                }
            }
        }

        return checkAvailability
    }

    @OptIn(BetaInteropApi::class)
    internal fun isAvailable(): BiometricResults = memScoped {
        val errorRef = alloc<ObjCObjectVar<NSError?>>()
        val result = context.canEvaluatePolicy(
            LAPolicyDeviceOwnerAuthenticationWithBiometrics,
            errorRef.ptr,
        )
        if (result) {
            return@memScoped BiometricResults.Available
        }

        val error = errorRef.value
        return@memScoped error.toBiometricResult()
    }

    private fun NSError?.toBiometricResult(): BiometricResults {
        return when (this?.code) {
            LAErrorAuthenticationFailed -> BiometricResults.AuthenticationFailed
            LAErrorUserCancel,
            LAErrorSystemCancel,
            LAErrorAppCancel,
            LAErrorUserFallback -> BiometricResults.AuthenticationCancelled
            LAErrorBiometryNotAvailable -> BiometricResults.HardwareUnavailable
            LAErrorBiometryNotEnrolled,
            LAErrorPasscodeNotSet -> BiometricResults.NotEnrolled
            LAErrorBiometryLockout -> BiometricResults.Locked
            LAErrorNotInteractive -> BiometricResults.FeatureUnavailable
            LAErrorInvalidContext -> BiometricResults.AuthenticationError("Invalid authentication context")
            else -> BiometricResults.AuthenticationError(this?.localizedDescription ?: "Unknown authentication error")
        }
    }
}
