package com.ucasoft.koncierge.store

interface PinCodeStoreProvider {

    val pinCodeKey: String

    fun store(pinCode: String)
    fun remove()
    fun get(): String?
}