@file:OptIn(ExperimentalForeignApi::class)
package winbio

import kotlinx.cinterop.*
import platform.windows.*
import winbio.structures.Identity
import winbio.structures.RejectDetail
import winbio.structures.UnitSchema

fun enumBiometricUnits(block: List<UnitSchema>.() -> Unit) {
    memScoped {
        val schemas = alloc<CPointerVar<WINBIO_UNIT_SCHEMA>>()
        val count = alloc<SIZE_TVar>()

        val result = WinBioEnumBiometricUnits(
            WINBIO_TYPE_FINGERPRINT,
            schemas.ptr,
            count.ptr
        )

        if (result == S_OK) {
            val units = mutableListOf<UnitSchema>()
            for (i in 0 until count.value.toInt()) {
                units.add(schemas.value!![i].toUnitSchema())
            }
            block(units)
        }

        if (schemas.value != null) {
            WinBioFree(schemas.value)
        }
    }
}

fun openSession(units: List<WINBIO_UNIT_ID> = emptyList(), block: WINBIO_SESSION_HANDLE.() -> Unit) {
    memScoped {
        val session = alloc<WINBIO_SESSION_HANDLEVar>()
        val unitArray = allocArray<WINBIO_UNIT_IDVar>(units.size) {
            units[it]
        }

        val result = WinBioOpenSession(
            WINBIO_TYPE_FINGERPRINT,
            WINBIO_POOL_SYSTEM,
            WINBIO_FLAG_DEFAULT,
            null,
            0u,
            null,
            session.ptr
        )

        if (result == S_OK) {
            println("Session is open: ${session.value}")
            block(session.value)
        } else {
            println(result == E_ACCESSDENIED)
            println("WinBioOpenSession failed: $result")
        }

        val closeResult = WinBioCloseSession(session.value)
        if (closeResult != S_OK) {
            println("WinBioCloseSession failed: $closeResult")
        } else {
            println("Session is closed.")
        }
    }
}

fun WINBIO_SESSION_HANDLE.identify(block: (Identity<Any>) -> Unit) {
    memScoped {

        println("Session is: ${this@identify}")
        val unit = alloc<WINBIO_UNIT_IDVar>()
        val identity = alloc<WINBIO_IDENTITY>()
        val subFactor = alloc<WINBIO_BIOMETRIC_SUBTYPEVar>()
        val rejectDetail = alloc<WINBIO_REJECT_DETAILVar>()

        println("Place your finger on the sensor.")
        val result = WinBioIdentify(
            this@identify,
            unit.ptr,
            identity.ptr,
            subFactor.ptr,
            rejectDetail.ptr
        )
        println("Processed...")

        when (result) {
            S_OK -> {
                println("Identification successful.")
                println("Unit ID: ${unit.value}")
                println("SubFactor: ${subFactor.value}")
                block(identity.toIdentity())
            }
            WINBIO_E_UNKNOWN_ID -> println("Unknown identity.")
            WINBIO_E_BAD_CAPTURE -> println("Bad capture. Reason: ${RejectDetail.valueOf(rejectDetail.value)}")
            else -> println("WinBioIdentify failed: $result")
        }
    }
}

fun WINBIO_SESSION_HANDLE.asyncIdentify() {

    fun identifyCallback(
        identifyCallbackContext: CPointer<out CPointed>?,
        operationStatus: HRESULT,
        unitId: WINBIO_UNIT_ID,
        identity: CPointer<WINBIO_IDENTITY>?,
        subFactor: WINBIO_BIOMETRIC_SUBTYPE,
        rejectDetail: WINBIO_REJECT_DETAIL
    ) {
        println("Callback received.")
        println("Operation status: $operationStatus")
        println("Unit ID: $unitId")
        println("User Identity: ${identity?.pointed?.Value}")
        println("SubFactor: $subFactor")
    }

    memScoped {

        println("Place your finger on the sensor.")
        val result = WinBioIdentifyWithCallback(
            this@asyncIdentify,
            staticCFunction(::identifyCallback),
            null
        )
        println("Processed...")

        when (result) {
            S_OK -> println("Identification successful.")
            WINBIO_E_UNKNOWN_ID -> println("Unknown identity.")
            WINBIO_E_BAD_CAPTURE -> println("Bad capture.")
            else -> println("WinBioIdentify failed: $result")
        }

        WinBioWait(this@asyncIdentify)
    }
}

fun WINBIO_SESSION_HANDLE.verify(identity: Identity<Any>) {
    verify(identity.toNative())
}

fun WINBIO_SESSION_HANDLE.verify(identity: WINBIO_IDENTITY) {
    memScoped {
        val unit = alloc<WINBIO_UNIT_IDVar>()
        val match = alloc<BOOLEANVar>()
        val rejectDetail = alloc<WINBIO_REJECT_DETAILVar>()

        println("Place your finger on the sensor.")
        val result = WinBioVerify(
            this@verify,
            identity.ptr,
            245u,
            unit.ptr,
            match.ptr,
            rejectDetail.ptr
        )
        println("Processed...")
        println(rejectDetail.value)

        when (result) {
            S_OK -> println("Verification successful.")
            E_HANDLE -> println("Invalid session handle.")
            E_INVALIDARG -> println("Invalid argument.")
            E_POINTER -> println("Null pointer.")
            WINBIO_E_BAD_CAPTURE -> println("Bad capture. Reason: ${RejectDetail.valueOf(rejectDetail.value)}")
            WINBIO_E_ENROLLMENT_IN_PROGRESS -> println("Enrollment in progress.")
            WINBIO_E_NO_MATCH -> println("No match.")
            else -> println("WinBioVerify failed: $result")
        }
    }
}

fun WINBIO_SESSION_HANDLE.asyncVerify(identity: Identity<*>) {
    asyncVerify(identity.toNative())
}

fun WINBIO_SESSION_HANDLE.asyncVerify(identity: WINBIO_IDENTITY) {

    fun verifyCallback(
        verifyCallbackContext: CPointer<out CPointed>?,
        operationStatus: HRESULT,
        unitId: WINBIO_UNIT_ID,
        math: boolean,
        rejectDetail: WINBIO_REJECT_DETAIL
    ) {
        println("Callback received.")
        println("Operation status: $operationStatus")
        println("Unit ID: $unitId")
        println(math.toInt() == 1)
    }

    memScoped {
        println("Place your finger on the sensor.")
        val result = WinBioVerifyWithCallback(
            this@asyncVerify,
            identity.ptr,
            245u,
            staticCFunction(::verifyCallback),
            null
        )
        println("Processed...")

        WinBioWait(this@asyncVerify)
    }
}