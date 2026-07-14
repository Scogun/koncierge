package com.ucasoft.koncierge.hash

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.SHA256
import kotlin.io.encoding.Base64

class CryptographyPinCodeHashProvider: PinCodeHashProvider {

    private val sha = CryptographyProvider.Default.get(SHA256).hasher()

    override suspend fun hash(pinCode: String) = Base64.encode(sha.hash(pinCode.encodeToByteArray()))
}