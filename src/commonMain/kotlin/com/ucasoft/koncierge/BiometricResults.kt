package com.ucasoft.koncierge

sealed interface BiometricResults {
    data object HardwareUnavailable: BiometricResults
    data object FeatureUnavailable: BiometricResults
    data object NotEnrolled: BiometricResults
    data object Locked: BiometricResults
    data object Available: BiometricResults
    data class AuthenticationError(val cause: String): BiometricResults
    data object AuthenticationFailed: BiometricResults
    data object AuthenticationCancelled: BiometricResults
    data object AuthenticationSuccessful: BiometricResults
}