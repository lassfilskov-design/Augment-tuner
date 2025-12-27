package com.augment.tuner

import android.content.Context
import android.os.Bundle
import android.view.View
import android:widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.augment.tuner.data.AccelerationMode
import com.augment.tuner.data.PowerMode
import com.augment.tuner.data.ScooterSettings
import com.augment.tuner.data.TuningPresets
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "scooter_settings")

class AdvancedTuningActivity : AppCompatActivity() {

    // Views
    private lateinit var chipGroupPresets: ChipGroup
    private lateinit var sliderMaxSpeed: Slider
    private lateinit var tvMaxSpeedValue: TextView
    private lateinit var switchSpeedLimit: SwitchMaterial
    private lateinit var switchCruiseControl: SwitchMaterial

    private lateinit var chipGroupPowerMode: ChipGroup
    private lateinit var sliderThrottle: Slider
    private lateinit var tvThrottleValue: TextView

    private lateinit var chipGroupAcceleration: ChipGroup

    private lateinit var sliderLowBattery: Slider
    private lateinit var tvLowBatteryValue: TextView
    private lateinit var switchAutoLock: SwitchMaterial
    private lateinit var layoutAutoLockDelay: LinearLayout
    private lateinit var sliderAutoLock: Slider
    private lateinit var tvAutoLockValue: TextView

    private lateinit var btnResetDefaults: MaterialButton
    private lateinit var btnApplySettings: MaterialButton

    private var currentSettings = ScooterSettings()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_tuning)

        initViews()
        setupListeners()
        loadSettings()
    }

    private fun initViews() {
        chipGroupPresets = findViewById(R.id.chipGroupPresets)
        sliderMaxSpeed = findViewById(R.id.sliderMaxSpeed)
        tvMaxSpeedValue = findViewById(R.id.tvMaxSpeedValue)
        switchSpeedLimit = findViewById(R.id.switchSpeedLimit)
        switchCruiseControl = findViewById(R.id.switchCruiseControl)

        chipGroupPowerMode = findViewById(R.id.chipGroupPowerMode)
        sliderThrottle = findViewById(R.id.sliderThrottle)
        tvThrottleValue = findViewById(R.id.tvThrottleValue)

        chipGroupAcceleration = findViewById(R.id.chipGroupAcceleration)

        sliderLowBattery = findViewById(R.id.sliderLowBattery)
        tvLowBatteryValue = findViewById(R.id.tvLowBatteryValue)
        switchAutoLock = findViewById(R.id.switchAutoLock)
        layoutAutoLockDelay = findViewById(R.id.layoutAutoLockDelay)
        sliderAutoLock = findViewById(R.id.sliderAutoLock)
        tvAutoLockValue = findViewById(R.id.tvAutoLockValue)

        btnResetDefaults = findViewById(R.id.btnResetDefaults)
        btnApplySettings = findViewById(R.id.btnApplySettings)
    }

    private fun setupListeners() {
        // Preset selection
        chipGroupPresets.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val preset = when (checkedIds[0]) {
                R.id.chipEco -> TuningPresets.ECO
                R.id.chipSport -> TuningPresets.SPORT
                R.id.chipTurbo -> TuningPresets.CUSTOM_TURBO
                else -> TuningPresets.NORMAL
            }

            applyPreset(preset)
        }

        // Speed settings
        sliderMaxSpeed.addOnChangeListener { _, value, _ ->
            tvMaxSpeedValue.text = "${value.toInt()} km/h"
        }

        // Throttle
        sliderThrottle.addOnChangeListener { _, value, _ ->
            tvThrottleValue.text = "${value.toInt()}%"
        }

        // Low battery warning
        sliderLowBattery.addOnChangeListener { _, value, _ ->
            tvLowBatteryValue.text = "${value.toInt()}%"
        }

        // Auto lock
        switchAutoLock.setOnCheckedChangeListener { _, isChecked ->
            layoutAutoLockDelay.visibility = if (isChecked) View.VISIBLE else View.GONE
            sliderAutoLock.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        sliderAutoLock.addOnChangeListener { _, value, _ ->
            tvAutoLockValue.text = "${value.toInt()}s"
        }

        // Buttons
        btnResetDefaults.setOnClickListener {
            showResetDialog()
        }

        btnApplySettings.setOnClickListener {
            applySettingsToScooter()
        }
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            val preferences = settingsDataStore.data.first()

            currentSettings = ScooterSettings(
                powerMode = PowerMode.valueOf(
                    preferences[PreferenceKeys.POWER_MODE] ?: PowerMode.NORMAL.name
                ),
                maxSpeed = preferences[PreferenceKeys.MAX_SPEED] ?: 25,
                speedLimitEnabled = preferences[PreferenceKeys.SPEED_LIMIT_ENABLED] ?: true,
                cruiseControlEnabled = preferences[PreferenceKeys.CRUISE_CONTROL] ?: false,
                accelerationMode = AccelerationMode.valueOf(
                    preferences[PreferenceKeys.ACCELERATION_MODE] ?: AccelerationMode.NORMAL.name
                ),
                throttleResponse = preferences[PreferenceKeys.THROTTLE_RESPONSE] ?: 50,
                batteryLowWarning = preferences[PreferenceKeys.BATTERY_LOW_WARNING] ?: 20,
                autoLockEnabled = preferences[PreferenceKeys.AUTO_LOCK_ENABLED] ?: true,
                autoLockDelay = preferences[PreferenceKeys.AUTO_LOCK_DELAY] ?: 30
            )

            updateUIFromSettings(currentSettings)
        }
    }

    private fun updateUIFromSettings(settings: ScooterSettings) {
        // Speed settings
        sliderMaxSpeed.value = settings.maxSpeed.toFloat()
        tvMaxSpeedValue.text = "${settings.maxSpeed} km/h"
        switchSpeedLimit.isChecked = settings.speedLimitEnabled
        switchCruiseControl.isChecked = settings.cruiseControlEnabled

        // Power settings
        when (settings.powerMode) {
            PowerMode.ECO -> findViewById<Chip>(R.id.chipPowerEco).isChecked = true
            PowerMode.NORMAL -> findViewById<Chip>(R.id.chipPowerNormal).isChecked = true
            PowerMode.SPORT -> findViewById<Chip>(R.id.chipPowerSport).isChecked = true
            PowerMode.CUSTOM -> findViewById<Chip>(R.id.chipPowerCustom).isChecked = true
        }

        sliderThrottle.value = settings.throttleResponse.toFloat()
        tvThrottleValue.text = "${settings.throttleResponse}%"

        // Acceleration
        when (settings.accelerationMode) {
            AccelerationMode.SOFT -> findViewById<Chip>(R.id.chipAccelSoft).isChecked = true
            AccelerationMode.NORMAL -> findViewById<Chip>(R.id.chipAccelNormal).isChecked = true
            AccelerationMode.AGGRESSIVE -> findViewById<Chip>(R.id.chipAccelAggressive).isChecked = true
        }

        // Battery & Safety
        sliderLowBattery.value = settings.batteryLowWarning.toFloat()
        tvLowBatteryValue.text = "${settings.batteryLowWarning}%"
        switchAutoLock.isChecked = settings.autoLockEnabled
        sliderAutoLock.value = settings.autoLockDelay.toFloat()
        tvAutoLockValue.text = "${settings.autoLockDelay}s"

        layoutAutoLockDelay.visibility = if (settings.autoLockEnabled) View.VISIBLE else View.GONE
        sliderAutoLock.visibility = if (settings.autoLockEnabled) View.VISIBLE else View.GONE
    }

    private fun applyPreset(preset: ScooterSettings) {
        currentSettings = preset
        updateUIFromSettings(preset)
        Toast.makeText(this, "Preset applied", Toast.LENGTH_SHORT).show()
    }

    private fun showResetDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset to Defaults")
            .setMessage("This will reset all settings to factory defaults. Continue?")
            .setPositiveButton("Reset") { _, _ ->
                applyPreset(TuningPresets.NORMAL)
                saveSettings()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun applySettingsToScooter() {
        // Collect current values from UI
        currentSettings = ScooterSettings(
            powerMode = when (chipGroupPowerMode.checkedChipId) {
                R.id.chipPowerEco -> PowerMode.ECO
                R.id.chipPowerSport -> PowerMode.SPORT
                R.id.chipPowerCustom -> PowerMode.CUSTOM
                else -> PowerMode.NORMAL
            },
            maxSpeed = sliderMaxSpeed.value.toInt(),
            speedLimitEnabled = switchSpeedLimit.isChecked,
            cruiseControlEnabled = switchCruiseControl.isChecked,
            accelerationMode = when (chipGroupAcceleration.checkedChipId) {
                R.id.chipAccelSoft -> AccelerationMode.SOFT
                R.id.chipAccelAggressive -> AccelerationMode.AGGRESSIVE
                else -> AccelerationMode.NORMAL
            },
            throttleResponse = sliderThrottle.value.toInt(),
            batteryLowWarning = sliderLowBattery.value.toInt(),
            autoLockEnabled = switchAutoLock.isChecked,
            autoLockDelay = sliderAutoLock.value.toInt()
        )

        saveSettings()

        // In real implementation, send BLE commands to scooter here
        // For now, just show confirmation
        Toast.makeText(
            this,
            "✅ Settings saved! (BLE commands would be sent in production)",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun saveSettings() {
        lifecycleScope.launch {
            settingsDataStore.edit { preferences ->
                preferences[PreferenceKeys.POWER_MODE] = currentSettings.powerMode.name
                preferences[PreferenceKeys.MAX_SPEED] = currentSettings.maxSpeed
                preferences[PreferenceKeys.SPEED_LIMIT_ENABLED] = currentSettings.speedLimitEnabled
                preferences[PreferenceKeys.CRUISE_CONTROL] = currentSettings.cruiseControlEnabled
                preferences[PreferenceKeys.ACCELERATION_MODE] = currentSettings.accelerationMode.name
                preferences[PreferenceKeys.THROTTLE_RESPONSE] = currentSettings.throttleResponse
                preferences[PreferenceKeys.BATTERY_LOW_WARNING] = currentSettings.batteryLowWarning
                preferences[PreferenceKeys.AUTO_LOCK_ENABLED] = currentSettings.autoLockEnabled
                preferences[PreferenceKeys.AUTO_LOCK_DELAY] = currentSettings.autoLockDelay
            }
        }
    }

    private object PreferenceKeys {
        val POWER_MODE = stringPreferencesKey("power_mode")
        val MAX_SPEED = intPreferencesKey("max_speed")
        val SPEED_LIMIT_ENABLED = booleanPreferencesKey("speed_limit_enabled")
        val CRUISE_CONTROL = booleanPreferencesKey("cruise_control")
        val ACCELERATION_MODE = stringPreferencesKey("acceleration_mode")
        val THROTTLE_RESPONSE = intPreferencesKey("throttle_response")
        val BATTERY_LOW_WARNING = intPreferencesKey("battery_low_warning")
        val AUTO_LOCK_ENABLED = booleanPreferencesKey("auto_lock_enabled")
        val AUTO_LOCK_DELAY = intPreferencesKey("auto_lock_delay")
    }
}
