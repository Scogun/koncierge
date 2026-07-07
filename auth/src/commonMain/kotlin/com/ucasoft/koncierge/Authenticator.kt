package com.ucasoft.koncierge

import com.ucasoft.koncierge.hash.CryptographyPinCodeHashProvider
import com.ucasoft.koncierge.hash.PinCodeHashProvider
import com.ucasoft.koncierge.store.PinCodeStoreProvider
import com.ucasoft.koncierge.store.SettingsPinCodeStoreProvider

open class Authenticator(
    private val koncierge: Koncierge,
    val pinCodeHashProvider: PinCodeHashProvider = CryptographyPinCodeHashProvider(),
    val pinCodeStoreProvider: PinCodeStoreProvider = SettingsPinCodeStoreProvider()
) {

    suspend fun storePinCode(pinCode: String) {
        pinCodeStoreProvider.store(pinCodeHashProvider.hash(pinCode))
    }

    suspend fun verifyPinCode(pinCode: String): Boolean {
        val storedPinCodeHash = pinCodeStoreProvider.get() ?: return false
        val pinCodeHash = pinCodeHashProvider.hash(pinCode)

        return constantTimeEquals(storedPinCodeHash, pinCodeHash)
    }

    suspend fun verifyBiometry(): Boolean {
        if (koncierge.isBiometricAvailable()) {
            return koncierge.authenticate("") == BiometricResults.AuthenticationSuccessful
        }

        return false
    }

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