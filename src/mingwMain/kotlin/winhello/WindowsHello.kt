package winhello

import windowshello.WH_AVAILABILITY_AVAILABLE
import windowshello.WH_AVAILABILITY_DEVICE_BUSY
import windowshello.WH_AVAILABILITY_DEVICE_NOT_PRESENT
import windowshello.WH_AVAILABILITY_DISABLED_BY_POLICY
import windowshello.WH_AVAILABILITY_NOT_CONFIGURED
import windowshello.WH_RESULT_CANCELED
import windowshello.WH_RESULT_DEVICE_BUSY
import windowshello.WH_RESULT_DEVICE_NOT_PRESENT
import windowshello.WH_RESULT_DISABLED_BY_POLICY
import windowshello.WH_RESULT_NOT_CONFIGURED
import windowshello.WH_RESULT_RETRIES_EXHAUSTED
import windowshello.WH_RESULT_VERIFIED
import windowshello.WindowsHelloResult
import windowshello.windows_hello_check_availability
import windowshello.windows_hello_request_verification
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.wcstr

@OptIn(ExperimentalForeignApi::class)
object WindowsHello {
    fun checkAvailability(): WindowsHelloAvailability = memScoped {
        val out = alloc<WindowsHelloResult>()
        windows_hello_check_availability(out.ptr)
        availabilityFromCode(out.code)
    }

    fun requestVerification(
        prompt: String,
        ownerWindowHandle: Long = 0,
    ): WindowsHelloVerification = memScoped {
        val out = alloc<WindowsHelloResult>()
        windows_hello_request_verification(ownerWindowHandle.toULong(), prompt.wcstr.ptr, out.ptr)

        WindowsHelloVerification(
            result = verificationFromCode(out.code),
            hresult = out.hresult,
        )
    }

    private fun availabilityFromCode(code: Int): WindowsHelloAvailability = when (code.toUInt()) {
        WH_AVAILABILITY_AVAILABLE -> WindowsHelloAvailability.Available
        WH_AVAILABILITY_DEVICE_NOT_PRESENT -> WindowsHelloAvailability.DeviceNotPresent
        WH_AVAILABILITY_NOT_CONFIGURED -> WindowsHelloAvailability.NotConfigured
        WH_AVAILABILITY_DISABLED_BY_POLICY -> WindowsHelloAvailability.DisabledByPolicy
        WH_AVAILABILITY_DEVICE_BUSY -> WindowsHelloAvailability.DeviceBusy
        else -> WindowsHelloAvailability.Unknown
    }

    private fun verificationFromCode(code: Int): WindowsHelloVerificationResult = when (code.toUInt()) {
        WH_RESULT_VERIFIED -> WindowsHelloVerificationResult.Verified
        WH_RESULT_DEVICE_NOT_PRESENT -> WindowsHelloVerificationResult.DeviceNotPresent
        WH_RESULT_NOT_CONFIGURED -> WindowsHelloVerificationResult.NotConfigured
        WH_RESULT_DISABLED_BY_POLICY -> WindowsHelloVerificationResult.DisabledByPolicy
        WH_RESULT_DEVICE_BUSY -> WindowsHelloVerificationResult.DeviceBusy
        WH_RESULT_RETRIES_EXHAUSTED -> WindowsHelloVerificationResult.RetriesExhausted
        WH_RESULT_CANCELED -> WindowsHelloVerificationResult.Canceled
        else -> WindowsHelloVerificationResult.Unknown
    }
}

enum class WindowsHelloAvailability {
    Available,
    DeviceNotPresent,
    NotConfigured,
    DisabledByPolicy,
    DeviceBusy,
    Unknown,
}

enum class WindowsHelloVerificationResult {
    Verified,
    DeviceNotPresent,
    NotConfigured,
    DisabledByPolicy,
    DeviceBusy,
    RetriesExhausted,
    Canceled,
    Unknown,
}

data class WindowsHelloVerification(
    val result: WindowsHelloVerificationResult,
    val hresult: Int,
) {
    val isVerified: Boolean = result == WindowsHelloVerificationResult.Verified
}