package com.augment.tuner

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.UUID

class MainActivity : AppCompatActivity() {

    // Augment Scooter Service UUID (verified from APK analysis)
    private val AUGMENT_SERVICE_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb")

    private val PERMISSION_REQUEST_CODE = 1001

    // UI elements
    private lateinit var btnConnect: Button
    private lateinit var btnApply: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvMacAddress: TextView
    private lateinit var tvServiceUuid: TextView
    private lateinit var tvManufacturerData: TextView
    private lateinit var tvBattery: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvVoltage: TextView

    // Bluetooth
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var isScanning = false
    private var connectedDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupBluetooth()
        checkPermissions()
        setupListeners()
    }

    private fun initializeViews() {
        btnConnect = findViewById(R.id.btnConnect)
        btnApply = findViewById(R.id.btnApply)
        tvStatus = findViewById(R.id.tvStatus)
        tvMacAddress = findViewById(R.id.tvMacAddress)
        tvServiceUuid = findViewById(R.id.tvServiceUuid)
        tvManufacturerData = findViewById(R.id.tvManufacturerData)
        tvBattery = findViewById(R.id.tvBattery)
        tvSpeed = findViewById(R.id.tvSpeed)
        tvVoltage = findViewById(R.id.tvVoltage)
    }

    private fun setupBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth ikke understøttet", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun checkPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    private fun setupListeners() {
        btnConnect.setOnClickListener {
            if (isScanning) {
                stopScan()
            } else {
                startScan()
            }
        }

        btnApply.setOnClickListener {
            applySettings()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startScan() {
        if (!hasRequiredPermissions()) {
            Toast.makeText(this, "Mangler Bluetooth tilladelser", Toast.LENGTH_SHORT).show()
            checkPermissions()
            return
        }

        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(AUGMENT_SERVICE_UUID))
            .build()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bluetoothLeScanner?.startScan(listOf(scanFilter), scanSettings, scanCallback)
        isScanning = true
        btnConnect.text = "STOP SCANNING"
        updateStatus("Scanning for Augment scooters...", "#FF9800")
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        bluetoothLeScanner?.stopScan(scanCallback)
        isScanning = false
        btnConnect.text = "SCAN FOR SCOOTER"
        if (connectedDevice == null) {
            updateStatus("Disconnected", "#FF0000")
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val scanRecord = result.scanRecord

            runOnUiThread {
                tvMacAddress.text = "MAC: ${device.address}"

                // Parse manufacturer data
                val manufacturerData = scanRecord?.getManufacturerSpecificData(0x5240) // Company ID from screenshot
                if (manufacturerData != null) {
                    val hexData = manufacturerData.joinToString("") { "%02X".format(it) }
                    tvManufacturerData.text = "Mfr Data: 0x$hexData"

                    // Parse telemetry from manufacturer data (theoretical)
                    if (manufacturerData.size >= 4) {
                        // This is speculative - actual format unknown
                        val battery = (manufacturerData[2].toInt() and 0xFF)
                        tvBattery.text = "Battery: $battery%"
                    }
                }

                // Connect to device
                stopScan()
                connectToDevice(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "Scan fejlede: $errorCode", Toast.LENGTH_SHORT).show()
                stopScan()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice(device: BluetoothDevice) {
        updateStatus("Connecting to ${device.address}...", "#FF9800")
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevice = gatt.device
                    runOnUiThread {
                        updateStatus("Connected: ${gatt.device.address}", "#00AA00")
                        btnConnect.text = "DISCONNECT"
                        btnApply.isEnabled = true
                    }
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectedDevice = null
                    runOnUiThread {
                        updateStatus("Disconnected", "#FF0000")
                        btnConnect.text = "SCAN FOR SCOOTER"
                        btnApply.isEnabled = false
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(AUGMENT_SERVICE_UUID)
                runOnUiThread {
                    if (service != null) {
                        tvServiceUuid.text = "Service UUID: ${service.uuid}"
                        Toast.makeText(this@MainActivity,
                            "✓ Augment service found!",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun applySettings() {
        if (connectedDevice == null) {
            Toast.makeText(this, "Ingen scooter forbundet", Toast.LENGTH_SHORT).show()
            return
        }

        // TODO: Implement actual scooter configuration
        Toast.makeText(this, "Settings applied (local only)", Toast.LENGTH_SHORT).show()
    }

    private fun updateStatus(message: String, color: String) {
        tvStatus.text = "Status: $message"
        tvStatus.setTextColor(android.graphics.Color.parseColor(color))
    }

    private fun hasRequiredPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()
        if (isScanning) {
            bluetoothLeScanner?.stopScan(scanCallback)
        }
        bluetoothGatt?.close()
    }
}
