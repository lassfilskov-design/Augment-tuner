package com.augment.tuner.data

data class ScooterSettings(
    // Power Modes
    val powerMode: PowerMode = PowerMode.NORMAL,

    // Speed Settings
    val maxSpeed: Int = 25,              // km/h
    val speedLimitEnabled: Boolean = true,
    val cruiseControlEnabled: Boolean = false,

    // Acceleration
    val accelerationMode: AccelerationMode = AccelerationMode.NORMAL,
    val throttleResponse: Int = 50,      // 0-100%

    // Battery
    val batteryLowWarning: Int = 20,     // %
    val batteryCritical: Int = 10,       // %

    // Safety
    val autoLockEnabled: Boolean = true,
    val autoLockDelay: Int = 30,         // seconds

    // Display
    val darkModeEnabled: Boolean = false,
    val speedUnit: SpeedUnit = SpeedUnit.KMH
)

enum class PowerMode {
    ECO,       // Max range, limited power
    NORMAL,    // Balanced
    SPORT,     // Max performance
    CUSTOM     // User-defined
}

enum class AccelerationMode {
    SOFT,      // Smooth, beginner-friendly
    NORMAL,    // Standard
    AGGRESSIVE // Fast response, advanced users
}

enum class SpeedUnit {
    KMH,
    MPH
}

// Tuning presets
object TuningPresets {
    val ECO = ScooterSettings(
        powerMode = PowerMode.ECO,
        maxSpeed = 20,
        accelerationMode = AccelerationMode.SOFT,
        throttleResponse = 30
    )

    val NORMAL = ScooterSettings(
        powerMode = PowerMode.NORMAL,
        maxSpeed = 25,
        accelerationMode = AccelerationMode.NORMAL,
        throttleResponse = 50
    )

    val SPORT = ScooterSettings(
        powerMode = PowerMode.SPORT,
        maxSpeed = 35,
        accelerationMode = AccelerationMode.AGGRESSIVE,
        throttleResponse = 85,
        speedLimitEnabled = false
    )

    val CUSTOM_TURBO = ScooterSettings(
        powerMode = PowerMode.CUSTOM,
        maxSpeed = 45,
        accelerationMode = AccelerationMode.AGGRESSIVE,
        throttleResponse = 100,
        speedLimitEnabled = false
    )
}
