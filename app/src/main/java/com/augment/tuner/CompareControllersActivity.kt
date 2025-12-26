package com.augment.tuner

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*
import java.util.*

/**
 * AUGMENT CONTROLLER COMPARISON APP
 *
 * Simpel app til at sammenligne to controllers:
 * - Controller #1: 38 km/h locked (known)
 * - Controller #2: Unknown limit (potentielt højere)
 *
 * INGEN TELEMETRY - KUN BLE!
 */

class CompareControllersActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ControllerCompare"

        // BLE Services
        private val SERVICE_DEVICE_INFO = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb")
        private val SERVICE_SETTINGS_WRITE = UUID.fromString("00006683-0000-1000-8000-00805f9b34fb")
        private val SERVICE_TELEMETRY = UUID.fromString("00006688-0000-1000-8000-00805f9b34fb")

        // Device Info Characteristics
        private val CHAR_FIRMWARE_REVISION = UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb")
        private val CHAR_HARDWARE_REVISION = UUID.fromString("00002A27-0000-1000-8000-00805f9b34fb")
        private val CHAR_SOFTWARE_REVISION = UUID.fromString("00002A28-0000-1000-8000-00805f9b34fb")

        // Test speeds
        private val TEST_SPEEDS = listOf(38, 40, 45, 50, 55)
    }

    // UI Elements
    private lateinit var tvController1Info: TextView
    private lateinit var tvController2Info: TextView
    private lateinit var tvController1Results: TextView
    private lateinit var tvController2Results: TextView
    private lateinit var btnTestBoth: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView

    // Controller MAC addresses - TILPAS DISSE!
    private val CONTROLLER_1_MAC = "XX:XX:XX:XX:XX:01"  // ← Din Controller #1
    private val CONTROLLER_2_MAC = "YY:YY:YY:YY:YY:02"  // ← Din Controller #2

    // BLE
    private var bluetoothAdapter: BluetoothAdapter? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    data class ControllerInfo(
        val name: String,
        val firmwareVersion: String,
        val hardwareVersion: String,
        val softwareVersion: String
    )

    data class SpeedTestResult(
        val targetSpeed: Int,
        val actualSpeed: Int,
        val success: Boolean
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)

        // Initialize BLE
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Find UI elements
        tvController1Info = findViewById(R.id.tvController1Info)
        tvController2Info = findViewById(R.id.tvController2Info)
        tvController1Results = findViewById(R.id.tvController1Results)
        tvController2Results = findViewById(R.id.tvController2Results)
        btnTestBoth = findViewById(R.id.btnTestBoth)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)

        // Test button
        btnTestBoth.setOnClickListener {
            scope.launch {
                runComparisonTest()
            }
        }

        // Request permissions
        requestPermissions()
    }

    private suspend fun runComparisonTest() = withContext(Dispatchers.IO) {
        updateStatus("Starting comparison test...")
        setTestingState(true)

        try {
            // Step 1: Get controller info
            updateStatus("Reading Controller #1 info...")
            val info1 = getControllerInfo(CONTROLLER_1_MAC)

            withContext(Dispatchers.Main) {
                tvController1Info.text = """
                    Controller #1
                    Firmware: ${info1.firmwareVersion}
                    Hardware: ${info1.hardwareVersion}
                    Software: ${info1.softwareVersion}
                """.trimIndent()
            }

            delay(1000)

            updateStatus("Reading Controller #2 info...")
            val info2 = getControllerInfo(CONTROLLER_2_MAC)

            withContext(Dispatchers.Main) {
                tvController2Info.text = """
                    Controller #2
                    Firmware: ${info2.firmwareVersion}
                    Hardware: ${info2.hardwareVersion}
                    Software: ${info2.softwareVersion}
                """.trimIndent()
            }

            delay(1000)

            // Step 2: Test speeds on both controllers
            val results1 = mutableListOf<SpeedTestResult>()
            val results2 = mutableListOf<SpeedTestResult>()

            for (speed in TEST_SPEEDS) {
                updateStatus("Testing $speed km/h on Controller #1...")
                val result1 = testSpeed(CONTROLLER_1_MAC, speed)
                results1.add(result1)

                delay(2000)

                updateStatus("Testing $speed km/h on Controller #2...")
                val result2 = testSpeed(CONTROLLER_2_MAC, speed)
                results2.add(result2)

                delay(2000)
            }

            // Step 3: Display results
            withContext(Dispatchers.Main) {
                displayResults(results1, results2)
            }

            updateStatus("✓ Test complete!")

        } catch (e: Exception) {
            Log.e(TAG, "Test failed", e)
            updateStatus("✗ Test failed: ${e.message}")
        } finally {
            setTestingState(false)
        }
    }

    private suspend fun getControllerInfo(macAddress: String): ControllerInfo = suspendCancellableCoroutine { continuation ->
        val device = bluetoothAdapter?.getRemoteDevice(macAddress)
        if (device == null) {
            continuation.resume(ControllerInfo("Unknown", "N/A", "N/A", "N/A")) {}
            return@suspendCancellableCoroutine
        }

        var firmwareRev = "N/A"
        var hardwareRev = "N/A"
        var softwareRev = "N/A"

        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d(TAG, "Connected to $macAddress, discovering services...")
                    gatt.discoverServices()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val deviceInfoService = gatt.getService(SERVICE_DEVICE_INFO)

                    // Read firmware version
                    deviceInfoService?.getCharacteristic(CHAR_FIRMWARE_REVISION)?.let { char ->
                        gatt.readCharacteristic(char)
                    }
                }
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                val value = String(characteristic.value ?: byteArrayOf())

                when (characteristic.uuid) {
                    CHAR_FIRMWARE_REVISION -> {
                        firmwareRev = value
                        // Read hardware next
                        gatt.getService(SERVICE_DEVICE_INFO)?.getCharacteristic(CHAR_HARDWARE_REVISION)?.let { char ->
                            gatt.readCharacteristic(char)
                        }
                    }
                    CHAR_HARDWARE_REVISION -> {
                        hardwareRev = value
                        // Read software next
                        gatt.getService(SERVICE_DEVICE_INFO)?.getCharacteristic(CHAR_SOFTWARE_REVISION)?.let { char ->
                            gatt.readCharacteristic(char)
                        }
                    }
                    CHAR_SOFTWARE_REVISION -> {
                        softwareRev = value
                        // Done reading - disconnect and return result
                        gatt.disconnect()
                        gatt.close()
                        continuation.resume(ControllerInfo(macAddress, firmwareRev, hardwareRev, softwareRev)) {}
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            continuation.resume(ControllerInfo("No Permission", "N/A", "N/A", "N/A")) {}
            return@suspendCancellableCoroutine
        }

        device.connectGatt(this, false, gattCallback)
    }

    private suspend fun testSpeed(macAddress: String, targetSpeed: Int): SpeedTestResult = suspendCancellableCoroutine { continuation ->
        val device = bluetoothAdapter?.getRemoteDevice(macAddress)
        if (device == null) {
            continuation.resume(SpeedTestResult(targetSpeed, 0, false)) {}
            return@suspendCancellableCoroutine
        }

        var actualSpeed = 0
        var settingsChar: BluetoothGattCharacteristic? = null
        var telemetryChar: BluetoothGattCharacteristic? = null

        val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt.discoverServices()
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    settingsChar = gatt.getService(SERVICE_SETTINGS_WRITE)?.characteristics?.firstOrNull()
                    telemetryChar = gatt.getService(SERVICE_TELEMETRY)?.characteristics?.firstOrNull()

                    // Send speed command
                    settingsChar?.let { char ->
                        val command = byteArrayOf(0xA2.toByte(), targetSpeed.toByte(), 0x00)
                        char.value = command
                        char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                        gatt.writeCharacteristic(char)
                    }
                }
            }

            override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // Wait a bit then read telemetry
                    scope.launch {
                        delay(3000)

                        // Read telemetry
                        telemetryChar?.let { char ->
                            gatt.readCharacteristic(char)
                        }
                    }
                }
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (characteristic.uuid == telemetryChar?.uuid) {
                    val value = characteristic.value
                    if (value != null && value.size >= 4) {
                        actualSpeed = value[2].toInt() and 0xFF
                    }

                    gatt.disconnect()
                    gatt.close()

                    val success = actualSpeed >= targetSpeed - 2  // 2 km/h margin
                    continuation.resume(SpeedTestResult(targetSpeed, actualSpeed, success)) {}
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            continuation.resume(SpeedTestResult(targetSpeed, 0, false)) {}
            return@suspendCancellableCoroutine
        }

        device.connectGatt(this, false, gattCallback)
    }

    private fun displayResults(results1: List<SpeedTestResult>, results2: List<SpeedTestResult>) {
        val sb1 = StringBuilder("Controller #1 Results:\n\n")
        val sb2 = StringBuilder("Controller #2 Results:\n\n")

        for (i in TEST_SPEEDS.indices) {
            val r1 = results1[i]
            val r2 = results2[i]

            val icon1 = if (r1.success) "✓" else "✗"
            val icon2 = if (r2.success) "✓" else "✗"

            sb1.append("$icon1 ${r1.targetSpeed} km/h → ${r1.actualSpeed} km/h\n")
            sb2.append("$icon2 ${r2.targetSpeed} km/h → ${r2.actualSpeed} km/h\n")
        }

        // Analysis
        val max1 = results1.filter { it.success }.maxOfOrNull { it.actualSpeed } ?: 0
        val max2 = results2.filter { it.success }.maxOfOrNull { it.actualSpeed } ?: 0

        sb1.append("\nMax achieved: $max1 km/h")
        sb2.append("\nMax achieved: $max2 km/h")

        tvController1Results.text = sb1.toString()
        tvController2Results.text = sb2.toString()

        // Show recommendation
        val recommendation = when {
            max2 > max1 -> {
                """
                🎉 JACKPOT!

                Controller #2 har højere limit!
                Max: $max2 km/h vs $max1 km/h

                → Dump firmware fra Controller #2
                → Flash til Controller #1
                → Begge unlocked!
                """.trimIndent()
            }
            max1 > max2 -> {
                """
                Controller #1 har højere limit
                Max: $max1 km/h vs $max2 km/h

                → Brug Controller #1 firmware
                """.trimIndent()
            }
            else -> {
                """
                Begge controllers samme limit: $max1 km/h

                → Firmware modding nødvendig
                → Se FIRMWARE_MODDING_GUIDE.md
                """.trimIndent()
            }
        }

        Toast.makeText(this, recommendation, Toast.LENGTH_LONG).show()
        Log.d(TAG, recommendation)
    }

    private fun updateStatus(status: String) {
        scope.launch(Dispatchers.Main) {
            tvStatus.text = status
            Log.d(TAG, status)
        }
    }

    private fun setTestingState(testing: Boolean) {
        scope.launch(Dispatchers.Main) {
            btnTestBoth.isEnabled = !testing
            progressBar.visibility = if (testing) android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        ActivityCompat.requestPermissions(this, permissions, 1)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
