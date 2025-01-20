@file:OptIn(ExperimentalForeignApi::class)
package winbio.structures

import kotlinx.cinterop.*
import winbio.*

enum class IdentityType(val value: UInt) {
    NULL(WINBIO_ID_TYPE_NULL),
    WILDCARD(WINBIO_ID_TYPE_WILDCARD),
    GUID(WINBIO_ID_TYPE_GUID),
    SID(WINBIO_ID_TYPE_SID);

    companion object {
        fun valueOf(value: UInt): IdentityType {
            return entries.find { it.value == value } ?: throw IllegalArgumentException("Unknown value: $value")
        }
    }
}

data class Identity<T>(
    val type: IdentityType,
    val value: T
)

enum class RejectDetail(val value: UInt) {
    FP_TOO_HIGH(WINBIO_FP_TOO_HIGH),
    FP_TOO_LOW(WINBIO_FP_TOO_LOW),
    FP_TOO_LEFT(WINBIO_FP_TOO_LEFT),
    FP_TOO_RIGHT(WINBIO_FP_TOO_RIGHT),
    FP_TOO_FAST(WINBIO_FP_TOO_FAST),
    FP_TOO_SLOW(WINBIO_FP_TOO_SLOW),
    FP_POOR_QUALITY(WINBIO_FP_POOR_QUALITY),
    FP_TOO_SKEWED(WINBIO_FP_TOO_SKEWED),
    FP_TOO_SHORT(WINBIO_FP_TOO_SHORT),
    FP_MERGE_FAILURE(WINBIO_FP_MERGE_FAILURE);

    companion object {
        fun valueOf(value: UInt): RejectDetail {
            return entries.find { it.value == value } ?: throw IllegalArgumentException("Unknown value: $value")
        }
    }
}