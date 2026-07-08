package com.ucasoft.koncierge

import android.content.Context
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

actual class Koncierge {

    private val authenticator: BiometricAuthenticator?

    constructor(context: Context) {
        authenticator = BiometricAuthenticator(context)
    }

    actual fun isBiometricAvailable(): Boolean = authenticator?.isAvailable() == true

    actual suspend fun authenticate(message: String, timeout: Duration): BiometricResults {
        val authenticator = authenticator ?: return BiometricResults.FeatureUnavailable
        return withTimeout(timeout) {
            authenticator.authenticate(message)
        }
    }
}
