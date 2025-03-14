package com.ucasoft.koncierge

import winbio.enumBiometricUnits
import winbio.openSession
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import winbio.toIdentity
import winbio.verify
import kotlin.coroutines.resume
import kotlin.time.Duration

actual class Koncierge {

    actual fun isBiometricAvailable(): Boolean {
        var result = false
        enumBiometricUnits {
           if (count() > 0) {
               result = true
           }
        }

        return result
    }

    actual suspend fun authenticate(message: String, timeout: Duration): BiometricResults {
        val identity = currentUserIdentity()
        println(identity.toIdentity())
        return withTimeout(timeout) {
            suspendCancellableCoroutine { continuation ->
                openSession {
                    verify(identity)
                    continuation.resume(BiometricResults.Available)
                }
            }
        }
    }
}