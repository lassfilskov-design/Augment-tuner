package com.augment.tuner

import android.bluetooth.*
import android.content.Context
import java.util.*

/**
 * SIMPLE BLE - NO TELEMETRY, NO BLOATWARE
 *
 * Send kommandoer til scooter uden Augment backend tracking
 */

class SimpleBLE(private val context: Context) {

    companion object {
        // BLE Service til at sende commands
        private val SERVICE_WRITE = UUID.fromString("00006683-0000-1000-8000-00805f9b34fb")
    }

    private var gatt: BluetoothGatt? = null
    private var writeChar: BluetoothGattCharacteristic? = null

    /**
     * Connect til controller
     */
    fun connect(macAddress: String, onReady: () -> Unit) {
        val device = (context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager)
            .adapter
            .getRemoteDevice(macAddress)

        gatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                writeChar = gatt.getService(SERVICE_WRITE)?.characteristics?.firstOrNull()
                onReady()
            }
        })
    }

    /**
     * Send speed limit
     */
    fun setSpeed(kmh: Int) {
        val cmd = byteArrayOf(0xA2.toByte(), kmh.toByte(), 0x00)
        send(cmd)
    }

    /**
     * Zero start (kick-less)
     */
    fun setZeroStart(enabled: Boolean, maxKmh: Int = 6) {
        val cmd = byteArrayOf(
            0xA1.toByte(),
            if (enabled) 0x01 else 0x00,
            maxKmh.toByte()
        )
        send(cmd)
    }

    /**
     * Sport+ mode
     */
    fun setSportPlus(enabled: Boolean) {
        val cmd = byteArrayOf(0xA3.toByte(), if (enabled) 0x01 else 0x00)
        send(cmd)
    }

    /**
     * Electronic Turbo
     */
    fun setTurbo(enabled: Boolean) {
        val cmd = byteArrayOf(0xA4.toByte(), if (enabled) 0x01 else 0x00)
        send(cmd)
    }

    /**
     * Send raw command
     */
    private fun send(cmd: ByteArray) {
        writeChar?.let { char ->
            char.value = cmd
            gatt?.writeCharacteristic(char)
        }
    }

    /**
     * Disconnect
     */
    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
    }
}

/**
 * USAGE:
 *
 * val ble = SimpleBLE(context)
 *
 * ble.connect("XX:XX:XX:XX:XX:XX") {
 *     // Controller klar
 *     ble.setSpeed(50)        // 50 km/h
 *     ble.setZeroStart(true)  // Zero start on
 *     ble.setSportPlus(true)  // Sport+ on
 *     ble.setTurbo(true)      // Turbo on
 * }
 */
