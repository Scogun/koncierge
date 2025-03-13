package com.ucasoft.koncierge

import kotlin.time.Duration

expect class Koncierge {
    fun isBiometricAvailable(): Boolean
    suspend fun authenticate(message: String, timeout: Duration = Duration.INFINITE) : BiometricResults
}