@file:OptIn(ExperimentalForeignApi::class)
package winbio

import kotlinx.cinterop.*
import platform.posix.memcpy
import winbio.structures.*
import winbio.structures.PoolType as KPoolType
import winbio.structures.SensorSubType as KSensorSubType

fun WINBIO_VERSION.toVersion() = Version(MajorVersion, MinorVersion)

fun WINBIO_UNIT_SCHEMA.toUnitSchema() = UnitSchema(
    UnitId,
    KPoolType.valueOf(PoolType),
    BiometricFactor,
    KSensorSubType.valueOf(SensorSubType),
    Capability.valuesOf(Capabilities),
    DeviceInstanceId.toKString(),
    Description.toKString(),
    Manufacturer.toKString(),
    Model.toKString(),
    SerialNumber.toKString(),
    FirmwareVersion.toVersion()
)

fun WINBIO_IDENTITY.toIdentity() = Identity(
    IdentityType.valueOf(Type),
    when (Type) {
        WINBIO_ID_TYPE_NULL -> this.Value.Null
        WINBIO_ID_TYPE_WILDCARD -> this.Value.Wildcard
        WINBIO_ID_TYPE_GUID -> this.Value.TemplateGuid
        WINBIO_ID_TYPE_SID -> sidToKString(this.Value.AccountSid)
        else -> throw IllegalArgumentException("Unknown identity type: $Type")
    }
)

fun Identity<*>.toNative() : WINBIO_IDENTITY {
    return memScoped {
        val result = alloc<WINBIO_IDENTITY>()

        result.Type = when (type) {
            IdentityType.SID -> WINBIO_ID_TYPE_SID
            IdentityType.NULL -> WINBIO_ID_TYPE_NULL
            IdentityType.WILDCARD -> WINBIO_ID_TYPE_WILDCARD
            IdentityType.GUID -> WINBIO_ID_TYPE_GUID
        }

        when (type) {
            IdentityType.SID -> {
                val sidArray = stringToSid(value as String)
                val sid = result.Value.AccountSid
                sid.Size = sidArray.size.convert()
                memcpy(sid.Data, sidArray.refTo(0), sid.Size.convert())
            }
            else -> {}
        }

        result
    }
}

private fun sidToKString(sid: anonymousStruct2): String {
    val data = sid.Data
    val buffer = UByteArray(sid.Size.toInt()) {
        data[it].toUByte()
    }

    if (buffer[0].toInt() != 1) {
        throw IllegalArgumentException("Invalid SID revision: ${buffer[0]}")
    }

    val stringSidBuilder = StringBuilder("S-1-")

    val subAuthorityCount = buffer[1]
    var identifierAuthority: Long = 0
    var offset = 2
    var size = 6

    for (i in 0 until size) {
        identifierAuthority = identifierAuthority or ((buffer[offset + i]).toLong() shl (8 * (5 - i)))
    }

    stringSidBuilder.append(identifierAuthority)
    offset = 8
    size = 4
    repeat(subAuthorityCount.toInt()) {
        var subAuthority: Long = 0
        for (j in 0 until size) {
            subAuthority = subAuthority or ((buffer[offset + j]).toLong() shl (8 * j))
        }
        stringSidBuilder.append('-').append(subAuthority)
        offset += size
    }

    return stringSidBuilder.toString()
}

private fun stringToSid(sid: String) : ByteArray {
    if (!sid.startsWith("S-")) {
        throw Exception("Invalid SID format!")
    }

    val parts = sid.split("-")
    if (parts.size < 3) {
        throw Exception("Invalid SID format!")
    }

    val revision = parts[1].toInt()
    val authority = parts[2].toULong()
    val subAuthority = parts.drop(3).map { it.toUInt() }

    if (subAuthority.size > 255) {
        throw Exception("Too many sub-authorities!")
    }

    val sidSize = 8 + subAuthority.size * 4
    val result = ByteArray(sidSize)

    result[0] = revision.toByte()
    result[1] = subAuthority.size.toByte()

    var size = 6
    var offset = 2

    for (i in 0 until size) {
        result[i + offset] = (authority shr (8 * (5 - i)) and 0xFFu).toByte()
    }

    offset = 4
    size = 4
    subAuthority.forEach {
        offset += size
        for (j in 0 until size) {
            result[offset + j] = ((it shr (8 * j)) and 0xFFu).toByte()
        }
    }

    return result
}