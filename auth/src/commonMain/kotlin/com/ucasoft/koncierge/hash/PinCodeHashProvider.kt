package com.ucasoft.koncierge.hash

interface PinCodeHashProvider {
    suspend fun hash(pinCode: String): String
}