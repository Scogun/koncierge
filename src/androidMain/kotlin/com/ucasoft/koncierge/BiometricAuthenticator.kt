package com.ucasoft.koncierge

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class BiometricAuthenticator(private val context: Context) {

    private val authenticators = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    } else {
        BIOMETRIC_STRONG
    }

    fun isAvailable(): Boolean {
        val manager = BiometricManager.from(context)
        return manager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    suspend fun authenticate(prompt: String) {

        val promptInfo = PromptInfo.Builder()
            .setTitle("Auth")
            .setSubtitle(prompt)
            .setDescription("Authenticate your biometric credential")
            .setAllowedAuthenticators(authenticators)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            promptInfo.setNegativeButtonText("Cancel")
        }

        return suspendCoroutine { continuation ->

            val biometricPrompt = BiometricPrompt(
                context as FragmentActivity,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        continuation.resume(Unit)
                    }
                })

            biometricPrompt.authenticate(promptInfo.build())
        }
    }
}