package com.ucasoft.koncierge

import com.ucasoft.koncierge.hash.PinCodeHashProvider
import com.ucasoft.koncierge.store.PinCodeStoreProvider
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AuthenticatorTest {
    @Test
    fun storePinCodeStoresHashedPinCode() = runTest {
        val store = InMemoryPinCodeStoreProvider()
        val authenticator = Authenticator(
            koncierge = Koncierge(),
            pinCodeHashProvider = PrefixPinCodeHashProvider(),
            pinCodeStoreProvider = store,
        )

        authenticator.storePinCode("1234")

        store.get()
            .shouldBe("hashed:1234")
            .shouldNotBe("1234")
    }

    @Test
    fun verifyPinCodeReturnsTrueForStoredPinCode() = runTest {
        val authenticator = Authenticator(
            koncierge = Koncierge(),
            pinCodeHashProvider = PrefixPinCodeHashProvider(),
            pinCodeStoreProvider = InMemoryPinCodeStoreProvider(),
        )

        authenticator.storePinCode("1234")

        authenticator.verifyPinCode("1234").shouldBeTrue()
    }

    @Test
    fun verifyPinCodeReturnsFalseForDifferentPinCode() = runTest {
        val authenticator = Authenticator(
            koncierge = Koncierge(),
            pinCodeHashProvider = PrefixPinCodeHashProvider(),
            pinCodeStoreProvider = InMemoryPinCodeStoreProvider(),
        )

        authenticator.storePinCode("1234")

        authenticator.verifyPinCode("1111").shouldBeFalse()
    }

    @Test
    fun verifyPinCodeReturnsFalseWhenPinCodeIsNotStored() = runTest {
        val authenticator = Authenticator(
            koncierge = Koncierge(),
            pinCodeHashProvider = PrefixPinCodeHashProvider(),
            pinCodeStoreProvider = InMemoryPinCodeStoreProvider(),
        )

        authenticator.verifyPinCode("1234").shouldBeFalse()
    }

    private class PrefixPinCodeHashProvider : PinCodeHashProvider {
        override suspend fun hash(pinCode: String): String = "hashed:$pinCode"
    }

    private class InMemoryPinCodeStoreProvider : PinCodeStoreProvider {
        override val pinCodeKey: String = "pin-code"

        private var value: String? = null

        override fun store(pinCode: String) {
            value = pinCode
        }

        override fun remove() {
            value = null
        }

        override fun get(): String? = value
    }
}