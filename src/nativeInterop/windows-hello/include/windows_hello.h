#pragma once

#include <stdint.h>
#include <wchar.h>

#ifdef _WIN32
    #ifdef WINDOWS_HELLO_EXPORTS
        #define WINDOWS_HELLO_API __declspec(dllexport)
    #else
        #define WINDOWS_HELLO_API __declspec(dllimport)
    #endif
#else
    #define WINDOWS_HELLO_API
#endif

#ifdef __cplusplus
extern "C" {
#endif

enum WindowsHelloAvailabilityCode {
    WH_AVAILABILITY_AVAILABLE = 0,
    WH_AVAILABILITY_DEVICE_NOT_PRESENT = 1,
    WH_AVAILABILITY_NOT_CONFIGURED = 2,
    WH_AVAILABILITY_DISABLED_BY_POLICY = 3,
    WH_AVAILABILITY_DEVICE_BUSY = 4,
    WH_AVAILABILITY_UNKNOWN = 100,
};

enum WindowsHelloVerificationCode {
    WH_RESULT_VERIFIED = 0,
    WH_RESULT_DEVICE_NOT_PRESENT = 1,
    WH_RESULT_NOT_CONFIGURED = 2,
    WH_RESULT_DISABLED_BY_POLICY = 3,
    WH_RESULT_DEVICE_BUSY = 4,
    WH_RESULT_RETRIES_EXHAUSTED = 5,
    WH_RESULT_CANCELED = 6,
    WH_RESULT_UNKNOWN = 100,
};

typedef struct WindowsHelloResult {
    int32_t code;
    int32_t hresult;
} WindowsHelloResult;

WINDOWS_HELLO_API int32_t windows_hello_check_availability(WindowsHelloResult* out_result);

WINDOWS_HELLO_API int32_t windows_hello_request_verification(
    uint64_t owner_window_handle,
    const wchar_t* prompt,
    WindowsHelloResult* out_result
);

#ifdef __cplusplus
}
#endif
