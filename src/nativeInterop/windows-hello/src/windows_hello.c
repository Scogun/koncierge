#include "windows_hello.h"

#include <windows.h>
#include <roapi.h>
#include <winstring.h>
#include <inspectable.h>

#include <stddef.h>

typedef enum AsyncStatus {
    AsyncStatus_Started = 0,
    AsyncStatus_Completed = 1,
    AsyncStatus_Canceled = 2,
    AsyncStatus_Error = 3,
} AsyncStatus;

typedef struct IAsyncInfo IAsyncInfo;
typedef struct IAsyncOperationInt IAsyncOperationInt;
typedef struct IUserConsentVerifierInterop IUserConsentVerifierInterop;
typedef struct IUserConsentVerifierStatics IUserConsentVerifierStatics;

typedef struct IAsyncInfoVtbl {
    HRESULT (STDMETHODCALLTYPE *QueryInterface)(IAsyncInfo* This, REFIID riid, void** ppvObject);
    ULONG (STDMETHODCALLTYPE *AddRef)(IAsyncInfo* This);
    ULONG (STDMETHODCALLTYPE *Release)(IAsyncInfo* This);
    HRESULT (STDMETHODCALLTYPE *GetIids)(IAsyncInfo* This, ULONG* iidCount, IID** iids);
    HRESULT (STDMETHODCALLTYPE *GetRuntimeClassName)(IAsyncInfo* This, HSTRING* className);
    HRESULT (STDMETHODCALLTYPE *GetTrustLevel)(IAsyncInfo* This, TrustLevel* trustLevel);
    HRESULT (STDMETHODCALLTYPE *get_Id)(IAsyncInfo* This, UINT32* id);
    HRESULT (STDMETHODCALLTYPE *get_Status)(IAsyncInfo* This, AsyncStatus* status);
    HRESULT (STDMETHODCALLTYPE *get_ErrorCode)(IAsyncInfo* This, HRESULT* errorCode);
    HRESULT (STDMETHODCALLTYPE *Cancel)(IAsyncInfo* This);
    HRESULT (STDMETHODCALLTYPE *Close)(IAsyncInfo* This);
} IAsyncInfoVtbl;

struct IAsyncInfo {
    const IAsyncInfoVtbl* lpVtbl;
};

typedef struct IAsyncOperationIntVtbl {
    HRESULT (STDMETHODCALLTYPE *QueryInterface)(IAsyncOperationInt* This, REFIID riid, void** ppvObject);
    ULONG (STDMETHODCALLTYPE *AddRef)(IAsyncOperationInt* This);
    ULONG (STDMETHODCALLTYPE *Release)(IAsyncOperationInt* This);
    HRESULT (STDMETHODCALLTYPE *GetIids)(IAsyncOperationInt* This, ULONG* iidCount, IID** iids);
    HRESULT (STDMETHODCALLTYPE *GetRuntimeClassName)(IAsyncOperationInt* This, HSTRING* className);
    HRESULT (STDMETHODCALLTYPE *GetTrustLevel)(IAsyncOperationInt* This, TrustLevel* trustLevel);
    HRESULT (STDMETHODCALLTYPE *put_Completed)(IAsyncOperationInt* This, void* handler);
    HRESULT (STDMETHODCALLTYPE *get_Completed)(IAsyncOperationInt* This, void** handler);
    HRESULT (STDMETHODCALLTYPE *GetResults)(IAsyncOperationInt* This, int32_t* result);
} IAsyncOperationIntVtbl;

struct IAsyncOperationInt {
    const IAsyncOperationIntVtbl* lpVtbl;
};

typedef struct IUserConsentVerifierStaticsVtbl {
    HRESULT (STDMETHODCALLTYPE *QueryInterface)(IUserConsentVerifierStatics* This, REFIID riid, void** ppvObject);
    ULONG (STDMETHODCALLTYPE *AddRef)(IUserConsentVerifierStatics* This);
    ULONG (STDMETHODCALLTYPE *Release)(IUserConsentVerifierStatics* This);
    HRESULT (STDMETHODCALLTYPE *GetIids)(IUserConsentVerifierStatics* This, ULONG* iidCount, IID** iids);
    HRESULT (STDMETHODCALLTYPE *GetRuntimeClassName)(IUserConsentVerifierStatics* This, HSTRING* className);
    HRESULT (STDMETHODCALLTYPE *GetTrustLevel)(IUserConsentVerifierStatics* This, TrustLevel* trustLevel);
    HRESULT (STDMETHODCALLTYPE *CheckAvailabilityAsync)(IUserConsentVerifierStatics* This, IAsyncOperationInt** result);
    HRESULT (STDMETHODCALLTYPE *RequestVerificationAsync)(IUserConsentVerifierStatics* This, HSTRING message, IAsyncOperationInt** result);
} IUserConsentVerifierStaticsVtbl;

struct IUserConsentVerifierStatics {
    const IUserConsentVerifierStaticsVtbl* lpVtbl;
};

typedef struct IUserConsentVerifierInteropVtbl {
    HRESULT (STDMETHODCALLTYPE *QueryInterface)(IUserConsentVerifierInterop* This, REFIID riid, void** ppvObject);
    ULONG (STDMETHODCALLTYPE *AddRef)(IUserConsentVerifierInterop* This);
    ULONG (STDMETHODCALLTYPE *Release)(IUserConsentVerifierInterop* This);
    HRESULT (STDMETHODCALLTYPE *GetIids)(IUserConsentVerifierInterop* This, ULONG* iidCount, IID** iids);
    HRESULT (STDMETHODCALLTYPE *GetRuntimeClassName)(IUserConsentVerifierInterop* This, HSTRING* className);
    HRESULT (STDMETHODCALLTYPE *GetTrustLevel)(IUserConsentVerifierInterop* This, TrustLevel* trustLevel);
    HRESULT (STDMETHODCALLTYPE *RequestVerificationForWindowAsync)(
        IUserConsentVerifierInterop* This,
        HWND appWindow,
        HSTRING message,
        REFIID riid,
        void** asyncOperation
    );
} IUserConsentVerifierInteropVtbl;

struct IUserConsentVerifierInterop {
    const IUserConsentVerifierInteropVtbl* lpVtbl;
};

static const IID IID_IAsyncInfo = {
    0x00000036, 0x0000, 0x0000, { 0xC0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x46 }
};

static const IID IID_IAsyncOperationUserConsentVerificationResult = {
    0xfd596ffd, 0x2318, 0x558f, { 0x9d, 0xbe, 0xd2, 0x1d, 0xf4, 0x37, 0x64, 0xa5 }
};

static const IID IID_IUserConsentVerifierInterop = {
    0x39e050c3, 0x4e74, 0x441a, { 0x8d, 0xc0, 0xb8, 0x11, 0x04, 0xdf, 0x94, 0x9c }
};

static const IID IID_IUserConsentVerifierStatics = {
    0xaf4f3f91, 0x564c, 0x4ddc, { 0xb8, 0xb5, 0x97, 0x34, 0x47, 0x62, 0x7c, 0x65 }
};

static const int32_t S_OK_CODE = 0;
static const int32_t E_INVALIDARG_CODE = (int32_t)0x80070057;
static const int32_t E_FAIL_CODE = (int32_t)0x80004005;
static const int32_t E_CANCELED_CODE = (int32_t)0x800704C7;

#if defined(_MSC_VER)
__declspec(thread) static int apartment_initialized = 0;
#else
static __thread int apartment_initialized = 0;
#endif

static void write_result(WindowsHelloResult* out_result, int32_t code, int32_t hresult) {
    if (out_result == NULL) {
        return;
    }

    out_result->code = code;
    out_result->hresult = hresult;
}

static HRESULT ensure_apartment(void) {
    HRESULT hr;

    if (apartment_initialized) {
        return S_OK;
    }

    hr = RoInitialize(RO_INIT_MULTITHREADED);
    if (SUCCEEDED(hr) || hr == RPC_E_CHANGED_MODE) {
        apartment_initialized = 1;
        return S_OK;
    }

    return hr;
}

static HWND resolve_owner_window(uint64_t owner_window_handle) {
    HWND hwnd = (HWND)(UINT_PTR)owner_window_handle;
    if (hwnd != NULL) {
        return hwnd;
    }

    hwnd = GetActiveWindow();
    if (hwnd != NULL) {
        return hwnd;
    }

    return GetForegroundWindow();
}

static HRESULT create_hstring(const wchar_t* value, HSTRING* out_string) {
    if (value == NULL) {
        value = L"";
    }

    return WindowsCreateString(value, (UINT32)wcslen(value), out_string);
}

static HRESULT get_user_consent_verifier_factory(REFIID iid, void** factory) {
    HRESULT hr;
    HSTRING class_name = NULL;

    hr = create_hstring(L"Windows.Security.Credentials.UI.UserConsentVerifier", &class_name);
    if (FAILED(hr)) {
        return hr;
    }

    hr = RoGetActivationFactory(class_name, iid, factory);
    WindowsDeleteString(class_name);
    return hr;
}

static HRESULT wait_for_operation(IAsyncOperationInt* operation) {
    HRESULT hr;
    IAsyncInfo* async_info = NULL;

    hr = operation->lpVtbl->QueryInterface(operation, &IID_IAsyncInfo, (void**)&async_info);
    if (FAILED(hr)) {
        return hr;
    }

    for (;;) {
        AsyncStatus status = AsyncStatus_Started;
        hr = async_info->lpVtbl->get_Status(async_info, &status);
        if (FAILED(hr)) {
            break;
        }

        if (status == AsyncStatus_Completed) {
            hr = S_OK;
            break;
        }

        if (status == AsyncStatus_Error) {
            HRESULT error_code = E_FAIL;
            hr = async_info->lpVtbl->get_ErrorCode(async_info, &error_code);
            if (SUCCEEDED(hr)) {
                hr = error_code;
            }
            break;
        }

        if (status == AsyncStatus_Canceled) {
            hr = E_CANCELED_CODE;
            break;
        }

        Sleep(10);
    }

    async_info->lpVtbl->Release(async_info);
    return hr;
}

static HRESULT get_async_int_result(IAsyncOperationInt* operation, int32_t* out_code) {
    HRESULT hr = wait_for_operation(operation);
    if (FAILED(hr)) {
        return hr;
    }

    return operation->lpVtbl->GetResults(operation, out_code);
}

int32_t windows_hello_check_availability(WindowsHelloResult* out_result) {
    HRESULT hr;
    IUserConsentVerifierStatics* statics = NULL;
    IAsyncOperationInt* operation = NULL;
    int32_t availability = WH_AVAILABILITY_UNKNOWN;

    if (out_result == NULL) {
        return E_INVALIDARG_CODE;
    }

    hr = ensure_apartment();
    if (FAILED(hr)) {
        write_result(out_result, WH_AVAILABILITY_UNKNOWN, (int32_t)hr);
        return (int32_t)hr;
    }

    hr = get_user_consent_verifier_factory(&IID_IUserConsentVerifierStatics, (void**)&statics);
    if (SUCCEEDED(hr)) {
        hr = statics->lpVtbl->CheckAvailabilityAsync(statics, &operation);
    }
    if (SUCCEEDED(hr)) {
        hr = get_async_int_result(operation, &availability);
    }

    if (operation != NULL) {
        operation->lpVtbl->Release(operation);
    }
    if (statics != NULL) {
        statics->lpVtbl->Release(statics);
    }

    if (FAILED(hr)) {
        write_result(out_result, WH_AVAILABILITY_UNKNOWN, (int32_t)hr);
        return (int32_t)hr;
    }

    write_result(out_result, availability, S_OK_CODE);
    return S_OK_CODE;
}

int32_t windows_hello_request_verification(
    uint64_t owner_window_handle,
    const wchar_t* prompt,
    WindowsHelloResult* out_result
) {
    HRESULT hr;
    HSTRING message = NULL;
    IUserConsentVerifierInterop* interop = NULL;
    IAsyncOperationInt* operation = NULL;
    int32_t verification = WH_RESULT_UNKNOWN;

    if (out_result == NULL) {
        return E_INVALIDARG_CODE;
    }

    hr = ensure_apartment();
    if (FAILED(hr)) {
        write_result(out_result, WH_RESULT_UNKNOWN, (int32_t)hr);
        return (int32_t)hr;
    }

    hr = create_hstring(prompt == NULL ? L"Verify your identity" : prompt, &message);
    if (SUCCEEDED(hr)) {
        hr = get_user_consent_verifier_factory(&IID_IUserConsentVerifierInterop, (void**)&interop);
    }
    if (SUCCEEDED(hr)) {
        hr = interop->lpVtbl->RequestVerificationForWindowAsync(
            interop,
            resolve_owner_window(owner_window_handle),
            message,
            &IID_IAsyncOperationUserConsentVerificationResult,
            (void**)&operation
        );
    }
    if (SUCCEEDED(hr)) {
        hr = get_async_int_result(operation, &verification);
    }

    if (operation != NULL) {
        operation->lpVtbl->Release(operation);
    }
    if (interop != NULL) {
        interop->lpVtbl->Release(interop);
    }
    if (message != NULL) {
        WindowsDeleteString(message);
    }

    if (FAILED(hr)) {
        write_result(out_result, WH_RESULT_UNKNOWN, (int32_t)hr);
        return (int32_t)hr;
    }

    write_result(out_result, verification, S_OK_CODE);
    return S_OK_CODE;
}
