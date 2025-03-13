package com.ucasoft.koncierge

import kotlin.time.Duration

actual class Koncierge {
    actual fun isBiometricAvailable(): Boolean {
        TODO("Not yet implemented")
    }

    actual suspend fun authenticate(message: String, timeout: Duration): BiometricResults {
        TODO("Not yet implemented")
    }
}