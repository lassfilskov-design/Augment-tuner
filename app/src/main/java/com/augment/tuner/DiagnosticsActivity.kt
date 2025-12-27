package com.augment.tuner

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.augment.tuner.bluetooth.BLECommand
import com.augment.tuner.bluetooth.BLEResponse
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class DiagnosticsActivity : AppCompatActivity() {

    private lateinit var tvDiagBattery: TextView
    private lateinit var tvDiagVoltage: TextView
    private lateinit var tvDiagCurrent: TextView
    private lateinit var tvDiagPower: TextView
    private lateinit var tvDiagTemp: TextView
    private lateinit var tvDiagSpeed: TextView

    private lateinit var tvBatteryHealth: TextView
    private lateinit var tvChargeCycles: TextView
    private lateinit var tvCellVoltage: TextView
    private lateinit var tvBatteryCapacity: TextView

    private lateinit var tvFirmwareVersion: TextView
    private lateinit var tvModel: TextView
    private lateinit var tvErrorCode: TextView
    private lateinit var tvOdometer: TextView

    private lateinit var chartVoltage: LineChart
    private lateinit var btnRefreshDiag: MaterialButton

    private var bluetoothGatt: BluetoothGatt? = null
    private var deviceAddress: String? = null

    private val voltageHistory = ArrayList<Entry>()
    private var voltageCounter = 0f

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diagnostics)

        deviceAddress = intent.getStringExtra("DEVICE_ADDRESS")

        initViews()
        setupChart()
        setupListeners()

        // Start auto-refresh
        startAutoRefresh()
    }

    private fun initViews() {
        // Live telemetry
        tvDiagBattery = findViewById(R.id.tvDiagBattery)
        tvDiagVoltage = findViewById(R.id.tvDiagVoltage)
        tvDiagCurrent = findViewById(R.id.tvDiagCurrent)
        tvDiagPower = findViewById(R.id.tvDiagPower)
        tvDiagTemp = findViewById(R.id.tvDiagTemp)
        tvDiagSpeed = findViewById(R.id.tvDiagSpeed)

        // Battery health
        tvBatteryHealth = findViewById(R.id.tvBatteryHealth)
        tvChargeCycles = findViewById(R.id.tvChargeCycles)
        tvCellVoltage = findViewById(R.id.tvCellVoltage)
        tvBatteryCapacity = findViewById(R.id.tvBatteryCapacity)

        // System info
        tvFirmwareVersion = findViewById(R.id.tvFirmwareVersion)
        tvModel = findViewById(R.id.tvModel)
        tvErrorCode = findViewById(R.id.tvErrorCode)
        tvOdometer = findViewById(R.id.tvOdometer)

        // Chart and button
        chartVoltage = findViewById(R.id.chartVoltage)
        btnRefreshDiag = findViewById(R.id.btnRefreshDiag)
    }

    private fun setupChart() {
        chartVoltage.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)

            xAxis.apply {
                textColor = Color.WHITE
                setDrawGridLines(false)
            }

            axisLeft.apply {
                textColor = Color.WHITE
                setDrawGridLines(true)
                axisMinimum = 30f
                axisMaximum = 45f
            }

            axisRight.isEnabled = false
            legend.textColor = Color.WHITE
        }
    }

    private fun setupListeners() {
        btnRefreshDiag.setOnClickListener {
            refreshDiagnostics()
        }
    }

    private fun startAutoRefresh() {
        scope.launch {
            while (isActive) {
                refreshDiagnostics()
                delay(2000) // Refresh every 2 seconds
            }
        }
    }

    private fun refreshDiagnostics() {
        scope.launch {
            try {
                // Read diagnostics data from scooter
                readDiagnosticData()
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@DiagnosticsActivity,
                        "Failed to read data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private suspend fun readDiagnosticData() {
        withContext(Dispatchers.IO) {
            // This is theoretical - actual BLE communication would happen here
            // For now, we'll simulate with mock data

            // In real implementation, you would:
            // 1. Send BLECommand.ReadDiagnostics() to scooter
            // 2. Parse BLEResponse.Diagnostics
            // 3. Update UI

            // Mock data for demonstration
            withContext(Dispatchers.Main) {
                updateLiveTelemetry(
                    battery = 87,
                    voltage = 41.2f,
                    current = 2.5f,
                    power = 103,
                    temperature = 32,
                    speed = 18
                )

                updateBatteryHealth(
                    health = 95,
                    cycles = 47,
                    cellVoltage = 3.7f,
                    capacity = 14.5f
                )

                updateSystemInfo(
                    firmware = "v2.4.1",
                    model = "Augment Eco",
                    errorCode = 0,
                    odometer = 1247
                )

                // Add voltage to chart
                addVoltageDataPoint(41.2f)
            }
        }
    }

    private fun updateLiveTelemetry(
        battery: Int,
        voltage: Float,
        current: Float,
        power: Int,
        temperature: Int,
        speed: Int
    ) {
        tvDiagBattery.text = "$battery%"
        tvDiagVoltage.text = String.format("%.1f V", voltage)
        tvDiagCurrent.text = String.format("%.1f A", current)
        tvDiagPower.text = "$power W"
        tvDiagTemp.text = "$temperature °C"
        tvDiagSpeed.text = "$speed km/h"

        // Color code battery
        tvDiagBattery.setTextColor(
            when {
                battery >= 50 -> Color.parseColor("#00AA00")
                battery >= 20 -> Color.parseColor("#FFA500")
                else -> Color.parseColor("#FF0000")
            }
        )

        // Color code temperature
        tvDiagTemp.setTextColor(
            when {
                temperature <= 40 -> Color.parseColor("#00AA00")
                temperature <= 50 -> Color.parseColor("#FFA500")
                else -> Color.parseColor("#FF0000")
            }
        )
    }

    private fun updateBatteryHealth(
        health: Int,
        cycles: Int,
        cellVoltage: Float,
        capacity: Float
    ) {
        tvBatteryHealth.text = "$health%"
        tvChargeCycles.text = "$cycles"
        tvCellVoltage.text = String.format("%.1fV avg", cellVoltage)
        tvBatteryCapacity.text = String.format("%.1f Ah", capacity)

        // Color code health
        tvBatteryHealth.setTextColor(
            when {
                health >= 80 -> Color.parseColor("#00AA00")
                health >= 60 -> Color.parseColor("#FFA500")
                else -> Color.parseColor("#FF0000")
            }
        )
    }

    private fun updateSystemInfo(
        firmware: String,
        model: String,
        errorCode: Int,
        odometer: Int
    ) {
        tvFirmwareVersion.text = firmware
        tvModel.text = model

        if (errorCode == 0) {
            tvErrorCode.text = "None"
            tvErrorCode.setTextColor(Color.parseColor("#00AA00"))
        } else {
            tvErrorCode.text = "Error $errorCode"
            tvErrorCode.setTextColor(Color.parseColor("#FF0000"))
        }

        tvOdometer.text = String.format("%,d km", odometer)
    }

    private fun addVoltageDataPoint(voltage: Float) {
        voltageHistory.add(Entry(voltageCounter++, voltage))

        // Keep only last 30 data points
        if (voltageHistory.size > 30) {
            voltageHistory.removeAt(0)
            // Recalculate X values
            voltageHistory.forEachIndexed { index, entry ->
                entry.x = index.toFloat()
            }
            voltageCounter = voltageHistory.size.toFloat()
        }

        updateChart()
    }

    private fun updateChart() {
        val dataSet = LineDataSet(voltageHistory, "Battery Voltage").apply {
            color = Color.parseColor("#00AA00")
            setCircleColor(Color.parseColor("#00AA00"))
            lineWidth = 2f
            circleRadius = 3f
            setDrawCircleHole(false)
            valueTextSize = 0f
            setDrawFilled(true)
            fillColor = Color.parseColor("#00AA00")
            fillAlpha = 50
        }

        chartVoltage.data = LineData(dataSet)
        chartVoltage.notifyDataSetChanged()
        chartVoltage.invalidate()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            // Handle connection state changes
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            characteristic?.value?.let { data ->
                val response = BLECommand.parseResponse(data)

                when (response) {
                    is BLEResponse.Diagnostics -> {
                        scope.launch {
                            updateLiveTelemetry(
                                battery = response.battery,
                                voltage = response.cellVoltage * 10,
                                current = response.current,
                                power = response.power,
                                temperature = response.temperature,
                                speed = response.speed
                            )
                        }
                    }
                    else -> {
                        // Handle other response types
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        bluetoothGatt?.close()
    }
}
