package com.ucasoft.koncierge

import kotlin.time.Duration

actual class Koncierge {

    private val authenticator: JvmBiometricAuthenticator = currentJvmBiometricAuthenticator()

    actual fun isBiometricAvailable(): Boolean = authenticator.isBiometricAvailable()

    actual suspend fun authenticate(message: String, timeout: Duration): BiometricResults {
        return authenticator.authenticate(message, timeout)
    }
}
