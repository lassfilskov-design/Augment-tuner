package com.augment.tuner.bluetooth

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * BLE Commands for Augment scooter control
 * NOTE: These are THEORETICAL - actual commands need to be reverse engineered
 */
sealed class BLECommand(val data: ByteArray) {

    // Lock/Unlock
    class Lock : BLECommand(byteArrayOf(0x01, 0x01))
    class Unlock : BLECommand(byteArrayOf(0x01, 0x00))

    // Speed Control
    class SetMaxSpeed(speedKmh: Int) : BLECommand(
        byteArrayOf(0x02, speedKmh.toByte())
    )

    // Power Mode
    class SetPowerMode(mode: Int) : BLECommand(
        byteArrayOf(0x03, mode.toByte())
    )
    // 0x00 = ECO, 0x01 = NORMAL, 0x02 = SPORT

    // Acceleration
    class SetAcceleration(level: Int) : BLECommand(
        byteArrayOf(0x04, level.toByte())
    )

    // Cruise Control
    class SetCruiseControl(enabled: Boolean) : BLECommand(
        byteArrayOf(0x05, if (enabled) 0x01 else 0x00)
    )

    // Read Commands
    class ReadBattery : BLECommand(byteArrayOf(0x10))
    class ReadSpeed : BLECommand(byteArrayOf(0x11))
    class ReadVoltage : BLECommand(byteArrayOf(0x12))
    class ReadDiagnostics : BLECommand(byteArrayOf(0x13))

    // Firmware
    class ReadFirmwareVersion : BLECommand(byteArrayOf(0x20))

    companion object {
        // Parse response from scooter
        fun parseResponse(data: ByteArray): BLEResponse {
            if (data.isEmpty()) return BLEResponse.Error("Empty response")

            return when (data[0].toInt()) {
                0x10 -> {
                    val battery = data.getOrNull(1)?.toInt() ?: 0
                    BLEResponse.BatteryLevel(battery and 0xFF)
                }
                0x11 -> {
                    val speed = data.getOrNull(1)?.toInt() ?: 0
                    BLEResponse.Speed((speed and 0xFF))
                }
                0x12 -> {
                    if (data.size >= 3) {
                        val voltage = ByteBuffer.wrap(data, 1, 2)
                            .order(ByteOrder.LITTLE_ENDIAN)
                            .short.toInt()
                        BLEResponse.Voltage(voltage / 10f)
                    } else {
                        BLEResponse.Error("Invalid voltage data")
                    }
                }
                0x13 -> {
                    // Diagnostics data
                    if (data.size >= 8) {
                        BLEResponse.Diagnostics(
                            battery = data[1].toInt() and 0xFF,
                            speed = data[2].toInt() and 0xFF,
                            temperature = data[3].toInt() and 0xFF,
                            errorCode = data[4].toInt() and 0xFF,
                            cellVoltage = (data[5].toInt() and 0xFF) / 10f,
                            current = (data[6].toInt() and 0xFF) / 10f,
                            power = (data[7].toInt() and 0xFF) * 10
                        )
                    } else {
                        BLEResponse.Error("Invalid diagnostics data")
                    }
                }
                0x20 -> {
                    val version = data.drop(1).map { it.toInt() and 0xFF }.joinToString(".")
                    BLEResponse.FirmwareVersion(version)
                }
                else -> BLEResponse.Unknown(data)
            }
        }
    }
}

sealed class BLEResponse {
    data class BatteryLevel(val percentage: Int) : BLEResponse()
    data class Speed(val kmh: Int) : BLEResponse()
    data class Voltage(val volts: Float) : BLEResponse()
    data class FirmwareVersion(val version: String) : BLEResponse()
    data class Diagnostics(
        val battery: Int,
        val speed: Int,
        val temperature: Int,
        val errorCode: Int,
        val cellVoltage: Float,
        val current: Float,
        val power: Int
    ) : BLEResponse()
    data class Error(val message: String) : BLEResponse()
    data class Unknown(val data: ByteArray) : BLEResponse()
}
