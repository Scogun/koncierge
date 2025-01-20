@file:OptIn(ExperimentalForeignApi::class)
package winbio.structures

import kotlinx.cinterop.ExperimentalForeignApi
import winbio.*

enum class PoolType(val value: UInt) {
    UNKNOWN(WINBIO_POOL_UNKNOWN),
    SYSTEM(WINBIO_POOL_SYSTEM),
    PRIVATE(WINBIO_POOL_PRIVATE);

    companion object {
        fun valueOf(value: UInt): PoolType {
            return entries.find { it.value == value } ?: throw IllegalArgumentException("Unknown value: $value")
        }
    }
}

enum class SensorSubType(val value: UInt) {
    UNKNOWN(WINBIO_SENSOR_SUBTYPE_UNKNOWN),
    SWIPE(WINBIO_FP_SENSOR_SUBTYPE_SWIPE),
    TOUCH(WINBIO_FP_SENSOR_SUBTYPE_TOUCH);

    companion object {
        fun valueOf(value: UInt): SensorSubType {
            return entries.find { it.value == value } ?: throw IllegalArgumentException("Unknown value: $value")
        }
    }
}

enum class Capability(val value: UInt) {
    SENSOR(WINBIO_CAPABILITY_SENSOR),
    MATCHING(WINBIO_CAPABILITY_MATCHING),
    DATABASE(WINBIO_CAPABILITY_DATABASE),
    PROCESSING(WINBIO_CAPABILITY_PROCESSING),
    ENCRYPTION(WINBIO_CAPABILITY_ENCRYPTION),
    NAVIGATION(WINBIO_CAPABILITY_NAVIGATION),
    INDICATOR(WINBIO_CAPABILITY_INDICATOR),
    VIRTUAL_SENSOR(WINBIO_CAPABILITY_VIRTUAL_SENSOR);

    companion object {
        fun valuesOf(value: UInt): Set<Capability> {
            return entries.filter { it.value and value == it.value }.toSet()
        }
    }
}

data class Version(
    val majorVersion: UInt,
    val minorVersion: UInt
)

data class UnitSchema (
    val unitId: UInt,
    val poolType: PoolType,
    val biometricFactor: UInt,
    val sensorSubType: SensorSubType,
    val capabilities: Set<Capability>,
    val deviceInstanceId: String,
    val description: String,
    val manufacturer: String,
    val model: String,
    val serialNumber: String,
    val firmwareVersion: Version
)