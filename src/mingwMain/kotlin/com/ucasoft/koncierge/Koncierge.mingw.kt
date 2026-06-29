@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.ucasoft.koncierge

import kotlinx.cinterop.memScoped
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import winbio.openSession
import winbio.toIdentity
import winbio.verify
import winhello.WindowsHello
import winhello.WindowsHelloAvailability
import winhello.WindowsHelloVerificationResult
import kotlin.coroutines.resume
import kotlin.time.Duration

actual class Koncierge {

    actual fun isBiometricAvailable() = WindowsHello.checkAvailability() == WindowsHelloAvailability.Available

    actual suspend fun authenticate(message: String, timeout: Duration): BiometricResults {
        val verification = WindowsHello.requestVerification(
            prompt = "Confirm your identity to continue"
        )

        if (verification.result == WindowsHelloVerificationResult.Verified) {
            return BiometricResults.AuthenticationSuccessful
        }

        return withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                memScoped {
                    val identity = currentUserIdentity()
                    println(identity.toIdentity())
                    openSession {
                        verify(identity)
                        continuation.resume(BiometricResults.Available)
                    }
                }
            }
        }
    }
}
