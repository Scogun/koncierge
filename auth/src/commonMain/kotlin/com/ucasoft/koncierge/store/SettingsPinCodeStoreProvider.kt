package com.ucasoft.koncierge.store

import com.russhwolf.settings.Settings

class SettingsPinCodeStoreProvider(
    override val pinCodeKey: String = PIN_CODE_KEY
): PinCodeStoreProvider {

    private val settings = Settings()

    override fun store(pinCode: String) {
        settings.putString(pinCodeKey, pinCode)
    }

    override fun remove() {
        settings.remove(pinCodeKey)
    }

    override fun get() = settings.getStringOrNull(pinCodeKey)

    companion object {
        const val PIN_CODE_KEY = "USER_PIN_CODE"
    }
}