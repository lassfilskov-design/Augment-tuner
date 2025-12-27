package com.augment.tuner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.augment.tuner.data.AppDatabase
import com.augment.tuner.data.SpeedUnit
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter

val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class SettingsActivity : AppCompatActivity() {

    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var chipGroupSpeedUnit: ChipGroup
    private lateinit var btnClearRideHistory: MaterialButton
    private lateinit var btnExportData: MaterialButton
    private lateinit var btnResetAllSettings: MaterialButton
    private lateinit var tvAppVersion: TextView

    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        database = AppDatabase.getDatabase(this)

        initViews()
        setupListeners()
        loadSettings()
    }

    private fun initViews() {
        switchDarkMode = findViewById(R.id.switchDarkMode)
        chipGroupSpeedUnit = findViewById(R.id.chipGroupSpeedUnit)
        btnClearRideHistory = findViewById(R.id.btnClearRideHistory)
        btnExportData = findViewById(R.id.btnExportData)
        btnResetAllSettings = findViewById(R.id.btnResetAllSettings)
        tvAppVersion = findViewById(R.id.tvAppVersion)

        // Set app version
        tvAppVersion.text = BuildConfig.VERSION_NAME
    }

    private fun setupListeners() {
        // Dark mode toggle
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            setDarkMode(isChecked)
            saveDarkModeSetting(isChecked)
        }

        // Speed unit selection
        chipGroupSpeedUnit.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            val speedUnit = when (checkedIds[0]) {
                R.id.chipMph -> SpeedUnit.MPH
                else -> SpeedUnit.KMH
            }

            saveSpeedUnit(speedUnit)
        }

        // Clear ride history
        btnClearRideHistory.setOnClickListener {
            showClearHistoryDialog()
        }

        // Export data
        btnExportData.setOnClickListener {
            exportRideData()
        }

        // Reset all settings
        btnResetAllSettings.setOnClickListener {
            showResetAllDialog()
        }
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            val preferences = appSettingsDataStore.data.first()

            // Dark mode
            val isDarkMode = preferences[SettingsKeys.DARK_MODE] ?: false
            switchDarkMode.isChecked = isDarkMode

            // Speed unit
            val speedUnit = preferences[SettingsKeys.SPEED_UNIT] ?: SpeedUnit.KMH.name
            when (speedUnit) {
                SpeedUnit.MPH.name -> findViewById<Chip>(R.id.chipMph).isChecked = true
                else -> findViewById<Chip>(R.id.chipKmh).isChecked = true
            }
        }
    }

    private fun setDarkMode(enabled: Boolean) {
        if (enabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun saveDarkModeSetting(enabled: Boolean) {
        lifecycleScope.launch {
            appSettingsDataStore.edit { preferences ->
                preferences[SettingsKeys.DARK_MODE] = enabled
            }
        }
    }

    private fun saveSpeedUnit(unit: SpeedUnit) {
        lifecycleScope.launch {
            appSettingsDataStore.edit { preferences ->
                preferences[SettingsKeys.SPEED_UNIT] = unit.name
            }
            Toast.makeText(
                this@SettingsActivity,
                "Speed unit changed to ${unit.name}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showClearHistoryDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Ride History")
            .setMessage("This will permanently delete all ride data. This cannot be undone!")
            .setPositiveButton("Delete All") { _, _ ->
                clearRideHistory()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearRideHistory() {
        lifecycleScope.launch {
            database.rideDao().deleteAllRides()
            Toast.makeText(
                this@SettingsActivity,
                "✅ All ride data deleted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun exportRideData() {
        lifecycleScope.launch {
            try {
                val rides = database.rideDao().getAllRides().first()

                if (rides.isEmpty()) {
                    Toast.makeText(
                        this@SettingsActivity,
                        "No ride data to export",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // Convert to JSON
                val gson = GsonBuilder().setPrettyPrinting().create()
                val json = gson.toJson(rides)

                // Save to external storage
                val exportDir = File(getExternalFilesDir(null), "exports")
                exportDir.mkdirs()

                val timestamp = System.currentTimeMillis()
                val exportFile = File(exportDir, "augment_rides_$timestamp.json")

                FileWriter(exportFile).use { writer ->
                    writer.write(json)
                }

                // Show success with share option
                AlertDialog.Builder(this@SettingsActivity)
                    .setTitle("Export Successful")
                    .setMessage("Ride data exported to:\n${exportFile.absolutePath}")
                    .setPositiveButton("Share") { _, _ ->
                        shareFile(exportFile)
                    }
                    .setNegativeButton("Close", null)
                    .show()

            } catch (e: Exception) {
                Toast.makeText(
                    this@SettingsActivity,
                    "Export failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun shareFile(file: File) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                this@SettingsActivity,
                "${applicationContext.packageName}.fileprovider",
                file
            ))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Share ride data"))
    }

    private fun showResetAllDialog() {
        AlertDialog.Builder(this)
            .setTitle("⚠️ Reset Everything")
            .setMessage("This will:\n• Delete all ride data\n• Reset all settings to defaults\n• Clear all preferences\n\nThis CANNOT be undone!")
            .setPositiveButton("RESET EVERYTHING") { _, _ ->
                resetEverything()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun resetEverything() {
        lifecycleScope.launch {
            // Clear ride database
            database.rideDao().deleteAllRides()

            // Clear app settings
            appSettingsDataStore.edit { it.clear() }

            // Clear scooter settings
            settingsDataStore.edit { it.clear() }

            Toast.makeText(
                this@SettingsActivity,
                "✅ Everything has been reset",
                Toast.LENGTH_SHORT
            ).show()

            // Reload settings
            loadSettings()
        }
    }

    private object SettingsKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val SPEED_UNIT = stringPreferencesKey("speed_unit")
    }
}
