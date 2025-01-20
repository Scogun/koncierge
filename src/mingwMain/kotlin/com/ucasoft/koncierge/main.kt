package com.ucasoft.koncierge

import kotlinx.cinterop.*
import platform.posix.memcpy
import platform.windows.*
import winbio.*
import winbio.BYTEVar
import winbio.DWORDVar

@OptIn(ExperimentalForeignApi::class)
fun main() {
    enumBiometricUnits {
        forEach { unit ->
            println(
                """
                  Unit ID: ${unit.unitId}
                  Device instance ID: ${unit.deviceInstanceId}
                  Pool type: ${unit.poolType}
                  Biometric factor: ${unit.biometricFactor}
                  Sensor subtype: ${unit.sensorSubType}
                  Sensor capabilities: ${unit.capabilities}
                  Description: ${unit.description}
                  Manufacturer: ${unit.manufacturer}
                  Model: ${unit.model}
                  Serial no: ${unit.serialNumber}
                  Firmware version: ${unit.firmwareVersion.majorVersion}.${unit.firmwareVersion.minorVersion}
              """.trimIndent()
            )
        }
    }
    val window = GetConsoleWindow()
    SetForegroundWindow(window)
    //val identity = currentUserIdentity()
    openSession {
        identify {
            println("Identify result: $it")
            verify(it)
        }
    }

}

@OptIn(ExperimentalForeignApi::class)
fun currentUserIdentity(): WINBIO_IDENTITY {
    return memScoped {
        val tokenHandler = alloc<HANDLEVar>()
        if (OpenProcessToken(
                GetCurrentProcess(),
                TOKEN_READ.toUInt(),
                tokenHandler.ptr
            ) == 0
        ) {
            println("1: ${GetLastError()}")
            throw RuntimeException("Failed to open process token")
        }

        val tokenLength = alloc<DWORDVar>()
        tokenLength.value = SECURITY_MAX_SID_SIZE.toUInt()

        val token = allocArray<BYTEVar>(SECURITY_MAX_SID_SIZE.toInt())
        if (GetTokenInformation(
                tokenHandler.value,
                1u,
                token,
                tokenLength.value,
                tokenLength.ptr
            ) == 0
        ) {
            println("2: ${GetLastError()}")
            throw RuntimeException("Failed to get token information")
        }

        val sid = token.reinterpret<TOKEN_USER>().pointed.User.Sid

        CloseHandle(tokenHandler.value)

        val result = alloc<WINBIO_IDENTITY>()
        result.Type = WINBIO_ID_TYPE_SID
        val sidSize = GetLengthSid(sid)
        memcpy(result.Value.AccountSid.Data, sid, sidSize.convert())
        result.Value.AccountSid.Size = sidSize

        result
    }
}