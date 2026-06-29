#include "windows_hello.h"

#include <windows.h>
#include <roapi.h>
#include <unknwn.h>
#include <winrt/Windows.Foundation.h>
#include <winrt/Windows.Security.Credentials.UI.h>
#include <userconsentverifierinterop.h>

using winrt::Windows::Foundation::IAsyncOperation;
using winrt::Windows::Security::Credentials::UI::UserConsentVerificationResult;
using winrt::Windows::Security::Credentials::UI::UserConsentVerifier;
using winrt::Windows::Security::Credentials::UI::UserConsentVerifierAvailability;

namespace {

constexpr int32_t S_OK_CODE = 0;
constexpr int32_t E_INVALIDARG_CODE = static_cast<int32_t>(0x80070057);
constexpr int32_t E_FAIL_CODE = static_cast<int32_t>(0x80004005);

void write_result(WindowsHelloResult* out_result, int32_t code, int32_t hresult = S_OK_CODE) noexcept {
    if (out_result == nullptr) {
        return;
    }

    out_result->code = code;
    out_result->hresult = hresult;
}

HWND resolve_owner_window(uint64_t owner_window_handle) noexcept {
    auto hwnd = reinterpret_cast<HWND>(owner_window_handle);
    if (hwnd != nullptr) {
        return hwnd;
    }

    hwnd = GetActiveWindow();
    if (hwnd != nullptr) {
        return hwnd;
    }

    return GetForegroundWindow();
}

int32_t availability_code(UserConsentVerifierAvailability availability) noexcept {
    switch (availability) {
        case UserConsentVerifierAvailability::Available:
            return WH_AVAILABILITY_AVAILABLE;
        case UserConsentVerifierAvailability::DeviceNotPresent:
            return WH_AVAILABILITY_DEVICE_NOT_PRESENT;
        case UserConsentVerifierAvailability::NotConfiguredForUser:
            return WH_AVAILABILITY_NOT_CONFIGURED;
        case UserConsentVerifierAvailability::DisabledByPolicy:
            return WH_AVAILABILITY_DISABLED_BY_POLICY;
        case UserConsentVerifierAvailability::DeviceBusy:
            return WH_AVAILABILITY_DEVICE_BUSY;
        default:
            return WH_AVAILABILITY_UNKNOWN;
    }
}

int32_t verification_code(UserConsentVerificationResult result) noexcept {
    switch (result) {
        case UserConsentVerificationResult::Verified:
            return WH_RESULT_VERIFIED;
        case UserConsentVerificationResult::DeviceNotPresent:
            return WH_RESULT_DEVICE_NOT_PRESENT;
        case UserConsentVerificationResult::NotConfiguredForUser:
            return WH_RESULT_NOT_CONFIGURED;
        case UserConsentVerificationResult::DisabledByPolicy:
            return WH_RESULT_DISABLED_BY_POLICY;
        case UserConsentVerificationResult::DeviceBusy:
            return WH_RESULT_DEVICE_BUSY;
        case UserConsentVerificationResult::RetriesExhausted:
            return WH_RESULT_RETRIES_EXHAUSTED;
        case UserConsentVerificationResult::Canceled:
            return WH_RESULT_CANCELED;
        default:
            return WH_RESULT_UNKNOWN;
    }
}

void ensure_apartment() {
    static thread_local bool initialized = false;
    if (!initialized) {
        const auto hr = RoInitialize(RO_INIT_MULTITHREADED);
        if (FAILED(hr) && hr != RPC_E_CHANGED_MODE) {
            winrt::check_hresult(hr);
        }
        initialized = true;
    }
}

} // namespace

extern "C" WINDOWS_HELLO_API int32_t windows_hello_check_availability(WindowsHelloResult* out_result) {
    if (out_result == nullptr) {
        return E_INVALIDARG_CODE;
    }

    try {
        ensure_apartment();
        const auto availability = UserConsentVerifier::CheckAvailabilityAsync().get();
        write_result(out_result, availability_code(availability));
        return S_OK_CODE;
    } catch (const winrt::hresult_error& error) {
        write_result(out_result, WH_AVAILABILITY_UNKNOWN, static_cast<int32_t>(error.code().value));
        return static_cast<int32_t>(error.code().value);
    } catch (...) {
        write_result(out_result, WH_AVAILABILITY_UNKNOWN, E_FAIL_CODE);
        return E_FAIL_CODE;
    }
}

extern "C" WINDOWS_HELLO_API int32_t windows_hello_request_verification(
    uint64_t owner_window_handle,
    const wchar_t* prompt,
    WindowsHelloResult* out_result
) {
    if (out_result == nullptr) {
        return E_INVALIDARG_CODE;
    }

    try {
        ensure_apartment();

        const auto hwnd = resolve_owner_window(owner_window_handle);
        const winrt::hstring message(prompt == nullptr ? L"Verify your identity" : prompt);

        auto interop = winrt::get_activation_factory<UserConsentVerifier, IUserConsentVerifierInterop>();
        IAsyncOperation<UserConsentVerificationResult> operation{ nullptr };

        winrt::check_hresult(
            interop->RequestVerificationForWindowAsync(
                hwnd,
                static_cast<HSTRING>(winrt::get_abi(message)),
                winrt::guid_of<IAsyncOperation<UserConsentVerificationResult>>(),
                winrt::put_abi(operation)
            )
        );

        write_result(out_result, verification_code(operation.get()));
        return S_OK_CODE;
    } catch (const winrt::hresult_error& error) {
        write_result(out_result, WH_RESULT_UNKNOWN, static_cast<int32_t>(error.code().value));
        return static_cast<int32_t>(error.code().value);
    } catch (...) {
        write_result(out_result, WH_RESULT_UNKNOWN, E_FAIL_CODE);
        return E_FAIL_CODE;
    }
}
