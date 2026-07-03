package com.ucasoft.koncierge

import kotlin.time.Duration

internal interface JvmBiometricAuthenticator {
    fun isBiometricAvailable(): Boolean
    suspend fun authenticate(message: String, timeout: Duration): BiometricResults
}

internal fun currentJvmBiometricAuthenticator(): JvmBiometricAuthenticator {
    return when (JvmOperatingSystem.current()) {
        JvmOperatingSystem.Windows -> WindowsHelloJvmBiometricAuthenticator()
        JvmOperatingSystem.Linux -> LinuxFprintdBiometricAuthenticator()
        JvmOperatingSystem.Mac,
        JvmOperatingSystem.Unsupported -> UnsupportedJvmBiometricAuthenticator
    }
}

internal enum class JvmOperatingSystem {
    Windows,
    Mac,
    Linux,
    Unsupported;

    companion object {
        fun current(): JvmOperatingSystem {
            val osName = System.getProperty("os.name").orEmpty().lowercase()
            return when {
                osName.startsWith("windows") -> Windows
                osName.startsWith("mac") || osName.startsWith("darwin") -> Mac
                osName.startsWith("linux") -> Linux
                else -> Unsupported
            }
        }
    }
}

internal object UnsupportedJvmBiometricAuthenticator : JvmBiometricAuthenticator {
    override fun isBiometricAvailable(): Boolean = false

    override suspend fun authenticate(message: String, timeout: Duration): BiometricResults {
        return BiometricResults.FeatureUnavailable
    }
}
