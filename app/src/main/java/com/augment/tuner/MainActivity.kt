package com.augment.tuner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var ble: SimpleBLE
    private var isConnected = false

    // UI elements
    private lateinit var btnConnect: Button
    private lateinit var btnApply: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var seekSpeed: SeekBar
    private lateinit var cbZeroStart: CheckBox
    private lateinit var cbSportPlus: CheckBox
    private lateinit var cbTurbo: CheckBox
    private lateinit var etMacAddress: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize BLE
        ble = SimpleBLE(this)

        // Find UI elements
        btnConnect = findViewById(R.id.btnConnect)
        btnApply = findViewById(R.id.btnApply)
        tvStatus = findViewById(R.id.tvStatus)
        tvSpeed = findViewById(R.id.tvSpeed)
        seekSpeed = findViewById(R.id.seekSpeed)
        cbZeroStart = findViewById(R.id.cbZeroStart)
        cbSportPlus = findViewById(R.id.cbSportPlus)
        cbTurbo = findViewById(R.id.cbTurbo)
        etMacAddress = findViewById(R.id.etMacAddress)

        // Speed slider
        seekSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvSpeed.text = "$progress km/h"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Connect button
        btnConnect.setOnClickListener {
            if (!isConnected) {
                connectToScooter()
            } else {
                disconnect()
            }
        }

        // Apply button
        btnApply.setOnClickListener {
            applySettings()
        }

        // Request permissions
        requestPermissions()
    }

    private fun connectToScooter() {
        val scooterMac = etMacAddress.text.toString().trim()

        if (scooterMac.isEmpty() || !scooterMac.matches(Regex("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}"))) {
            Toast.makeText(this, "Ugyldig MAC adresse! Format: XX:XX:XX:XX:XX:XX", Toast.LENGTH_LONG).show()
            return
        }

        tvStatus.text = "Status: Forbinder..."
        btnConnect.isEnabled = false

        ble.connect(scooterMac) {
            runOnUiThread {
                isConnected = true
                tvStatus.text = "Status: ✅ Forbundet"
                btnConnect.text = "Disconnect"
                btnConnect.isEnabled = true
                btnApply.isEnabled = true
                Toast.makeText(this, "Forbundet til scooter!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun disconnect() {
        ble.disconnect()
        isConnected = false
        tvStatus.text = "Status: Ikke forbundet"
        btnConnect.text = "Connect til Scooter"
        btnApply.isEnabled = false
    }

    private fun applySettings() {
        val speed = seekSpeed.progress
        val zeroStart = cbZeroStart.isChecked
        val sportPlus = cbSportPlus.isChecked
        val turbo = cbTurbo.isChecked

        // Send commands
        ble.setSpeed(speed)
        ble.setZeroStart(zeroStart)
        ble.setSportPlus(sportPlus)
        ble.setTurbo(turbo)

        Toast.makeText(this, "Indstillinger sendt! 🚀", Toast.LENGTH_SHORT).show()
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
        if (isConnected) {
            ble.disconnect()
        }
    }
}
