package com.ucasoft.koncierge

import com.ucasoft.koncierge.hash.CryptographyPinCodeHashProvider
import com.ucasoft.koncierge.hash.PinCodeHashProvider
import com.ucasoft.koncierge.store.PinCodeStoreProvider
import com.ucasoft.koncierge.store.SettingsPinCodeStoreProvider

open class Authenticator(
    private val koncierge: Koncierge,
    private val pinCodeHashProvider: PinCodeHashProvider = CryptographyPinCodeHashProvider(),
    private val pinCodeStoreProvider: PinCodeStoreProvider = SettingsPinCodeStoreProvider()
) {

    val isPinCodeSet: Boolean
        get() = !pinCodeStoreProvider.get().isNullOrBlank()
    
    val isBiometryAvailable: Boolean
        get() = koncierge.isBiometricAvailable()

    suspend fun storePinCode(pinCode: String) {
        pinCodeStoreProvider.store(pinCodeHashProvider.hash(pinCode))
    }

    fun removePinCode() {
        pinCodeStoreProvider.remove()
    }

    suspend fun verifyPinCode(pinCode: String): Boolean {
        val storedPinCodeHash = pinCodeStoreProvider.get() ?: return false
        return constantTimeEquals(storedPinCodeHash, pinCodeHashProvider.hash(pinCode))
    }

    suspend fun verifyBiometry(message: String) =
        isBiometryAvailable && koncierge.authenticate(message) == BiometricResults.AuthenticationSuccessful

    private fun constantTimeEquals(left: String, right: String): Boolean {
        val maxLength = maxOf(left.length, right.length)
        var diff = left.length xor right.length

        for (index in 0 until maxLength) {
            val leftCode = left.getOrNull(index)?.code ?: 0
            val rightCode = right.getOrNull(index)?.code ?: 0
            diff = diff or (leftCode xor rightCode)
        }

        return diff == 0
    }
}