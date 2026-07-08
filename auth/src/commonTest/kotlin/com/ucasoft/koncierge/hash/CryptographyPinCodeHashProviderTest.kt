package com.ucasoft.koncierge.hash

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CryptographyPinCodeHashProviderTest {
    @Test
    fun hashReturnsBase64EncodedSha256Digest() = runTest {
        val provider = CryptographyPinCodeHashProvider()

        val hash = provider.hash("1234")

        hash
            .shouldBe("A6xnQhbz4Vx2HuGl4lXwZ5U2I8iziLRFnhP5eNfIRvQ=")
            .shouldNotBe("1234")
    }
}
