package com.augment.tuner.testing

import android.bluetooth.*
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.util.*

/**
 * AUGMENT DUAL CONTROLLER TEST
 *
 * Tester to controllers samtidig for at finde firmware limits
 * Controller #1: 38 km/h locked (confirmed)
 * Controller #2: Unknown limit - måske højere?
 */

data class TestResult(
    val controllerName: String,
    val targetSpeed: Int,
    val actualSpeed: Int,
    val motorTemp: Int,
    val batteryTemp: Int,
    val success: Boolean,
    val notes: String
)

data class ControllerInfo(
    val name: String,
    val macAddress: String,
    val device: BluetoothDevice,
    var gatt: BluetoothGatt? = null,
    var settingsWriteChar: BluetoothGattCharacteristic? = null,
    var telemetryChar: BluetoothGattCharacteristic? = null,
    val results: MutableList<TestResult> = mutableListOf()
)

class DualControllerTester(private val context: Context) {

    companion object {
        private const val TAG = "DualControllerTest"

        // BLE Services
        private val SERVICE_SETTINGS_WRITE = UUID.fromString("00006683-0000-1000-8000-00805f9b34fb")
        private val SERVICE_TELEMETRY = UUID.fromString("00006688-0000-1000-8000-00805f9b34fb")

        // Test speeds (km/h)
        private val TEST_SPEEDS = listOf(40, 45, 50, 55, 60)

        // Temperature limits
        private const val MOTOR_TEMP_WARNING = 70
        private const val MOTOR_TEMP_CRITICAL = 80
        private const val BATTERY_TEMP_WARNING = 50
        private const val BATTERY_TEMP_CRITICAL = 60
    }

    private val controllers = mutableListOf<ControllerInfo>()
    private var testJob: Job? = null

    /**
     * Tilføj controller til test
     */
    fun addController(name: String, macAddress: String, device: BluetoothDevice) {
        controllers.add(ControllerInfo(name, macAddress, device))
        Log.d(TAG, "Added controller: $name ($macAddress)")
    }

    /**
     * Start dual controller test
     */
    suspend fun startDualTest() = withContext(Dispatchers.IO) {
        Log.d(TAG, "═══════════════════════════════════════════════════════")
        Log.d(TAG, "  AUGMENT DUAL CONTROLLER TEST START")
        Log.d(TAG, "═══════════════════════════════════════════════════════")

        if (controllers.size != 2) {
            Log.e(TAG, "ERROR: Need exactly 2 controllers, got ${controllers.size}")
            return@withContext
        }

        // Connect to both controllers
        Log.d(TAG, "Connecting to controllers...")
        controllers.forEach { controller ->
            connectController(controller)
            delay(1000) // Wait between connections
        }

        // Wait for both to be ready
        delay(2000)

        // Verify both connected
        val allConnected = controllers.all { it.gatt != null && it.settingsWriteChar != null }
        if (!allConnected) {
            Log.e(TAG, "ERROR: Not all controllers connected successfully")
            return@withContext
        }

        Log.d(TAG, "✓ Both controllers connected!")
        Log.d(TAG, "")

        // Run test sequence
        for (speed in TEST_SPEEDS) {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "Testing speed: $speed km/h")
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

            // Test both controllers in parallel
            coroutineScope {
                controllers.forEach { controller ->
                    launch {
                        testSpeedOnController(controller, speed)
                    }
                }
            }

            // Check temperatures after test
            delay(2000)
            controllers.forEach { controller ->
                val temps = readTemperature(controller)
                Log.d(TAG, "${controller.name}: Motor=${temps.first}°C, Battery=${temps.second}°C")

                if (temps.first >= MOTOR_TEMP_CRITICAL) {
                    Log.e(TAG, "🔥 CRITICAL: ${controller.name} motor temp ${temps.first}°C!")
                    Log.e(TAG, "Aborting test sequence for safety!")
                    return@withContext
                } else if (temps.first >= MOTOR_TEMP_WARNING) {
                    Log.w(TAG, "⚠️  WARNING: ${controller.name} motor temp ${temps.first}°C - cooling down...")
                    delay(30000) // 30 sec cool down
                }
            }

            Log.d(TAG, "")
            delay(5000) // Wait between speed tests
        }

        // Print comparison results
        printComparisonResults()

        // Disconnect
        controllers.forEach { controller ->
            controller.gatt?.disconnect()
            controller.gatt?.close()
        }

        Log.d(TAG, "═══════════════════════════════════════════════════════")
        Log.d(TAG, "  TEST COMPLETE")
        Log.d(TAG, "═══════════════════════════════════════════════════════")
    }

    /**
     * Connect til controller
     */
    private suspend fun connectController(controller: ControllerInfo) = suspendCancellableCoroutine<Unit> { continuation ->
        controller.gatt = controller.device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "${controller.name}: Connected, discovering services...")
                    gatt.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "${controller.name}: Disconnected")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // Find characteristics
                    val settingsService = gatt.getService(SERVICE_SETTINGS_WRITE)
                    val telemetryService = gatt.getService(SERVICE_TELEMETRY)

                    controller.settingsWriteChar = settingsService?.characteristics?.firstOrNull()
                    controller.telemetryChar = telemetryService?.characteristics?.firstOrNull()

                    if (controller.settingsWriteChar != null && controller.telemetryChar != null) {
                        Log.d(TAG, "${controller.name}: Services discovered!")

                        // Enable notifications for telemetry
                        controller.telemetryChar?.let { char ->
                            gatt.setCharacteristicNotification(char, true)
                            char.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))?.let { desc ->
                                desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                gatt.writeDescriptor(desc)
                            }
                        }

                        continuation.resume(Unit) {}
                    } else {
                        Log.e(TAG, "${controller.name}: Failed to find required characteristics")
                        continuation.resume(Unit) {}
                    }
                }
            }
        })
    }

    /**
     * Test specifik hastighed på én controller
     */
    private suspend fun testSpeedOnController(controller: ControllerInfo, targetSpeed: Int) = withContext(Dispatchers.IO) {
        val command = byteArrayOf(0xA2.toByte(), targetSpeed.toByte(), 0x00)

        Log.d(TAG, "${controller.name}: Sending ${targetSpeed} km/h command...")

        controller.settingsWriteChar?.let { char ->
            char.value = command
            char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            val success = controller.gatt?.writeCharacteristic(char) ?: false

            if (success) {
                delay(3000) // Wait for scooter to respond

                // Read actual speed from telemetry
                val actualSpeed = readCurrentSpeed(controller)
                val (motorTemp, batteryTemp) = readTemperature(controller)

                val result = TestResult(
                    controllerName = controller.name,
                    targetSpeed = targetSpeed,
                    actualSpeed = actualSpeed,
                    motorTemp = motorTemp,
                    batteryTemp = batteryTemp,
                    success = actualSpeed >= targetSpeed - 2, // Allow 2 km/h margin
                    notes = if (actualSpeed >= targetSpeed - 2) {
                        "✓ Speed reached!"
                    } else {
                        "✗ Firmware locked (stuck at $actualSpeed km/h)"
                    }
                )

                controller.results.add(result)

                Log.d(TAG, "${controller.name}: Target=$targetSpeed km/h, Actual=$actualSpeed km/h - ${result.notes}")
            } else {
                Log.e(TAG, "${controller.name}: Failed to write command")
            }
        }
    }

    /**
     * Læs nuværende hastighed fra telemetry
     */
    private suspend fun readCurrentSpeed(controller: ControllerInfo): Int = withContext(Dispatchers.IO) {
        // Read telemetry characteristic
        controller.telemetryChar?.let { char ->
            controller.gatt?.readCharacteristic(char)
            delay(500) // Wait for read

            // Speed is typically in byte 2-3 of telemetry
            val value = char.value
            if (value != null && value.size >= 4) {
                return@withContext value[2].toInt() and 0xFF
            }
        }
        return@withContext 0
    }

    /**
     * Læs temperatur fra controller
     */
    private suspend fun readTemperature(controller: ControllerInfo): Pair<Int, Int> = withContext(Dispatchers.IO) {
        controller.telemetryChar?.let { char ->
            controller.gatt?.readCharacteristic(char)
            delay(500)

            val value = char.value
            if (value != null && value.size >= 2) {
                val motorTemp = value[0].toInt() and 0xFF
                val batteryTemp = value[1].toInt() and 0xFF
                return@withContext Pair(motorTemp, batteryTemp)
            }
        }
        return@withContext Pair(0, 0)
    }

    /**
     * Print sammenligning af resultater
     */
    private fun printComparisonResults() {
        Log.d(TAG, "")
        Log.d(TAG, "═══════════════════════════════════════════════════════")
        Log.d(TAG, "  COMPARISON RESULTS")
        Log.d(TAG, "═══════════════════════════════════════════════════════")
        Log.d(TAG, "")

        val controller1 = controllers[0]
        val controller2 = controllers[1]

        Log.d(TAG, "Speed Test | ${controller1.name} | ${controller2.name}")
        Log.d(TAG, "─────────────────────────────────────────────────────")

        for (speed in TEST_SPEEDS) {
            val result1 = controller1.results.find { it.targetSpeed == speed }
            val result2 = controller2.results.find { it.targetSpeed == speed }

            val speed1 = result1?.actualSpeed ?: 0
            val speed2 = result2?.actualSpeed ?: 0

            val icon1 = if (result1?.success == true) "✓" else "✗"
            val icon2 = if (result2?.success == true) "✓" else "✗"

            Log.d(TAG, "${speed} km/h   | $icon1 ${speed1} km/h | $icon2 ${speed2} km/h")
        }

        Log.d(TAG, "")
        Log.d(TAG, "═══════════════════════════════════════════════════════")
        Log.d(TAG, "  ANALYSIS")
        Log.d(TAG, "═══════════════════════════════════════════════════════")

        // Find highest successful speed for each controller
        val maxSpeed1 = controller1.results.filter { it.success }.maxOfOrNull { it.actualSpeed } ?: 0
        val maxSpeed2 = controller2.results.filter { it.success }.maxOfOrNull { it.actualSpeed } ?: 0

        Log.d(TAG, "")
        Log.d(TAG, "${controller1.name}:")
        Log.d(TAG, "  Max speed achieved: $maxSpeed1 km/h")
        Log.d(TAG, "  Firmware limit: ${if (maxSpeed1 < 45) "LOCKED" else "UNLOCKED"}")

        Log.d(TAG, "")
        Log.d(TAG, "${controller2.name}:")
        Log.d(TAG, "  Max speed achieved: $maxSpeed2 km/h")
        Log.d(TAG, "  Firmware limit: ${if (maxSpeed2 < 45) "LOCKED" else "UNLOCKED"}")

        Log.d(TAG, "")
        Log.d(TAG, "RECOMMENDATION:")
        if (maxSpeed1 > maxSpeed2) {
            Log.d(TAG, "  → Use ${controller1.name} for higher speeds")
            Log.d(TAG, "  → ${controller1.name} has less restrictive firmware")
        } else if (maxSpeed2 > maxSpeed1) {
            Log.d(TAG, "  → Use ${controller2.name} for higher speeds")
            Log.d(TAG, "  → ${controller2.name} has less restrictive firmware")
        } else {
            Log.d(TAG, "  → Both controllers have same firmware limits")
            Log.d(TAG, "  → Firmware modification may be necessary")
        }

        Log.d(TAG, "")
    }
}

/**
 * USAGE EXAMPLE:
 *
 * val tester = DualControllerTester(context)
 *
 * // Add Controller #1 (38 km/h locked)
 * tester.addController(
 *     name = "Controller #1",
 *     macAddress = "XX:XX:XX:XX:XX:XX",
 *     device = bluetoothDevice1
 * )
 *
 * // Add Controller #2 (unknown limit)
 * tester.addController(
 *     name = "Controller #2",
 *     macAddress = "YY:YY:YY:YY:YY:YY",
 *     device = bluetoothDevice2
 * )
 *
 * // Run test
 * lifecycleScope.launch {
 *     tester.startDualTest()
 * }
 */
