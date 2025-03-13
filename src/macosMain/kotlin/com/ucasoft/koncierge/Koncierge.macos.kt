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
                            continuation.resume(
                                when (error?.code) {
                                    LAErrorAuthenticationFailed -> BiometricResults.AuthenticationFailed
                                    LAErrorUserCancel -> BiometricResults.AuthenticationCancelled
                                    else -> BiometricResults.AuthenticationError(
                                        error?.localizedDescription ?: "Unknown error"
                                    )
                                }
                            )
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
    private fun isAvailable(): BiometricResults {
        val errorRef = memScoped { alloc<ObjCObjectVar<NSError?>>().ptr }
        val result = context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, errorRef)
        if (result) {
            return BiometricResults.Available
        }

        val error = errorRef.pointed.value
        return when (error?.code) {
            LAErrorBiometryNotAvailable -> BiometricResults.HardwareUnavailable
            LAErrorBiometryNotEnrolled -> BiometricResults.NotEnrolled
            LAErrorBiometryLockout -> BiometricResults.Locked
            else -> throw UnsupportedOperationException(error?.localizedDescription)
        }
    }
}