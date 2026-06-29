package com.ucasoft.koncierge

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.isActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BiometricAuthenticator(private val context: Context) {

    private val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    } else {
        BIOMETRIC_STRONG
    }

    fun checkAvailability(): BiometricResults {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(authenticators).toBiometricResult()
    }

    fun isAvailable(): Boolean = checkAvailability() == BiometricResults.Available

    suspend fun authenticate(prompt: String): BiometricResults {
        val availability = checkAvailability()
        if (availability != BiometricResults.Available) {
            return availability
        }

        val promptInfo = PromptInfo.Builder()
            .setTitle("Auth")
            .setSubtitle(prompt)
            .setDescription("Authenticate your biometric credential")
            .setAllowedAuthenticators(authenticators)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            promptInfo.setNegativeButtonText("Cancel")
        }

        return suspendCancellableCoroutine { continuation ->

            val biometricPrompt = BiometricPrompt(
                context as FragmentActivity,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (continuation.isActive) {
                            continuation.resume(BiometricResults.AuthenticationSuccessful)
                        }
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (continuation.isActive) {
                            continuation.resume(errorCode.toBiometricResult(errString.toString()))
                        }
                    }
                })

            continuation.invokeOnCancellation {
                biometricPrompt.cancelAuthentication()
            }

            biometricPrompt.authenticate(promptInfo.build())
        }
    }

    private fun Int.toBiometricResult(): BiometricResults {
        return when (this) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricResults.Available
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricResults.HardwareUnavailable
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricResults.NotEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED,
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricResults.FeatureUnavailable
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricResults.AuthenticationError(
                "Biometric availability is unknown"
            )
            else -> BiometricResults.AuthenticationError("Unknown biometric availability status: $this")
        }
    }

    private fun Int.toBiometricResult(message: String): BiometricResults {
        return when (this) {
            BiometricPrompt.ERROR_HW_UNAVAILABLE,
            BiometricPrompt.ERROR_HW_NOT_PRESENT -> BiometricResults.HardwareUnavailable
            BiometricPrompt.ERROR_NO_BIOMETRICS,
            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> BiometricResults.NotEnrolled
            BiometricPrompt.ERROR_LOCKOUT,
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> BiometricResults.Locked
            BiometricPrompt.ERROR_CANCELED,
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> BiometricResults.AuthenticationCancelled
            BiometricPrompt.ERROR_TIMEOUT,
            BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> BiometricResults.AuthenticationFailed
            BiometricPrompt.ERROR_NO_SPACE,
            BiometricPrompt.ERROR_VENDOR -> BiometricResults.AuthenticationError(message)
            else -> BiometricResults.AuthenticationError(message.ifBlank { "Unknown authentication error: $this" })
        }
    }
}
