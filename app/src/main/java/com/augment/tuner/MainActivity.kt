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
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import java.util.UUID

class MainActivity : AppCompatActivity() {

    // Augment Scooter Service UUID (verified from APK analysis)
    private val AUGMENT_SERVICE_UUID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb")

    private val PERMISSION_REQUEST_CODE = 1001

    // UI elements - Connection
    private lateinit var tvConnectionStatus: TextView
    private lateinit var btnConnect: MaterialButton
    private lateinit var tvScooterInfo: TextView

    // UI elements - Quick Stats
    private lateinit var tvQuickBattery: TextView
    private lateinit var tvQuickSpeed: TextView
    private lateinit var tvQuickDistance: TextView

    // UI elements - Navigation
    private lateinit var btnDiagnostics: MaterialButton
    private lateinit var btnRideHistory: MaterialButton
    private lateinit var btnAdvancedTuning: MaterialButton
    private lateinit var btnSettings: MaterialButton

    // Bluetooth
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var isScanning = false
    private var connectedDevice: BluetoothDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_enhanced)

        initializeViews()
        setupBluetooth()
        checkPermissions()
        setupListeners()
    }

    private fun initializeViews() {
        // Connection
        tvConnectionStatus = findViewById(R.id.tvConnectionStatus)
        btnConnect = findViewById(R.id.btnConnect)
        tvScooterInfo = findViewById(R.id.tvScooterInfo)

        // Quick Stats
        tvQuickBattery = findViewById(R.id.tvQuickBattery)
        tvQuickSpeed = findViewById(R.id.tvQuickSpeed)
        tvQuickDistance = findViewById(R.id.tvQuickDistance)

        // Navigation
        btnDiagnostics = findViewById(R.id.btnDiagnostics)
        btnRideHistory = findViewById(R.id.btnRideHistory)
        btnAdvancedTuning = findViewById(R.id.btnAdvancedTuning)
        btnSettings = findViewById(R.id.btnSettings)
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
        // Connection
        btnConnect.setOnClickListener {
            if (isScanning) {
                stopScan()
            } else {
                startScan()
            }
        }

        // Navigation
        btnDiagnostics.setOnClickListener {
            startActivity(Intent(this, DiagnosticsActivity::class.java).apply {
                putExtra("DEVICE_ADDRESS", connectedDevice?.address)
            })
        }

        btnRideHistory.setOnClickListener {
            startActivity(Intent(this, RideHistoryActivity::class.java))
        }

        btnAdvancedTuning.setOnClickListener {
            startActivity(Intent(this, AdvancedTuningActivity::class.java))
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
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
        updateConnectionStatus("🔍 Scanning...", "#FF9800")
    }

    @SuppressLint("MissingPermission")
    private fun stopScan() {
        bluetoothLeScanner?.stopScan(scanCallback)
        isScanning = false
        btnConnect.text = "SCAN FOR SCOOTER"
        if (connectedDevice == null) {
            updateConnectionStatus("⚫ Disconnected", "#FF0000")
        }
    }

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val scanRecord = result.scanRecord

            runOnUiThread {
                tvScooterInfo.text = "MAC: ${device.address}"

                // Parse manufacturer data
                val manufacturerData = scanRecord?.getManufacturerSpecificData(0x5240)
                if (manufacturerData != null && manufacturerData.size >= 4) {
                    val battery = (manufacturerData[2].toInt() and 0xFF)
                    tvQuickBattery.text = "$battery%"
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
        updateConnectionStatus("🔄 Connecting...", "#FF9800")
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevice = gatt.device
                    runOnUiThread {
                        updateConnectionStatus("🟢 Connected", "#00AA00")
                        btnConnect.text = "DISCONNECT"
                        tvScooterInfo.text = "MAC: ${gatt.device.address}"
                    }
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectedDevice = null
                    runOnUiThread {
                        updateConnectionStatus("⚫ Disconnected", "#FF0000")
                        btnConnect.text = "SCAN FOR SCOOTER"
                        tvScooterInfo.text = "MAC: Not connected"
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

    private fun updateConnectionStatus(message: String, color: String) {
        tvConnectionStatus.text = message
        tvConnectionStatus.setTextColor(android.graphics.Color.parseColor(color))
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
