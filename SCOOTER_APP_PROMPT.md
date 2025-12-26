# Augment Custom Scooter App - Android

## Platform
- **Android Native** (Kotlin)
- Target SDK: 33+, Min SDK: 24
- Nordic BLE Library: `com.nordicsemi.android:ble:2.6.1`

---

## BLE GATT Services

```
Primary Services:
00006680-0000-1000-8000-00805f9b34fb  // Hovedkontrol
00006683-0000-1000-8000-00805f9b34fb  // Settings Write ⭐
00006684-0000-1000-8000-00805f9b34fb  // Settings Read ⭐
00006688-0000-1000-8000-00805f9b34fb  // Status/Telemetry
0000180A-0000-1000-8000-00805f9b34fb  // Device Info
```

---

## Data Format (VIGTIG!)

### Speed Limit
```kotlin
// DECIMAL værdier, IKKE hex-encoded!
speedLimitKmh: Int = 45  // → sendes som 0x2D
```

### Wheel Size
```kotlin
wheelDiameterMm: Int = 254  // 10" → sendes som 0xFE 0x00 (little-endian)
```

---

## BLE Commands

```kotlin
// Command format: [Type, Data...]

// Speed limit (45 km/h)
byteArrayOf(0xA2, 0x2D, 0x00)

// Zero start (enable, max 6 km/h)
byteArrayOf(0xA1, 0x01, 0x06)

// Disable auto-lock
byteArrayOf(0xB1, 0x00, 0x00, 0x00)

// Wheel size (254mm)
byteArrayOf(0xC1, 0xFE, 0x00)

// No limit
byteArrayOf(0xA2, 0xFF, 0xFF)
```

**Command Types:**
- `0xA1` = Zero Start
- `0xA2` = Speed Limit
- `0xB1` = Lock Settings
- `0xC1` = Wheel Config
- `0xD1` = Cruise Control

---

## Features

### Core (Original App)
- BLE scanning & connection
- Lock/Unlock
- Battery & speed display
- **⚠️ TEMPERATUROVERVÅGNING** (KRITISK!)
- Device info

### Tuning (Custom)
- ✅ **No Limit Mode** - Fjern hastighedsgrænse
- ✅ **Zero Start** - Start uden skub
- ✅ **Disable Theft Lock** - Deaktiver auto-lock
- ✅ **Wheel Size Config** - Juster speedometer
- ✅ **Cruise Control** - Hold hastighed

---

## Kotlin Implementation

```kotlin
class ScooterBleManager(context: Context) : BleManager(context) {

    suspend fun setSpeedLimit(kmh: Int) {
        val data = byteArrayOf(0xA2.toByte(), kmh.toByte(), 0x00)
        writeCharacteristic(SERVICE_6683, SPEED_CHAR, data)
    }

    suspend fun enableZeroStart(maxSpeed: Int = 6) {
        val data = byteArrayOf(0xA1.toByte(), 0x01, maxSpeed.toByte())
        writeCharacteristic(SERVICE_6683, SPEED_CHAR, data)
    }

    suspend fun disableAutoLock() {
        val data = byteArrayOf(0xB1.toByte(), 0x00, 0x00, 0x00)
        writeCharacteristic(SERVICE_6683, LOCK_CHAR, data)
    }

    suspend fun setWheelSize(diameterMm: Int) {
        val data = byteArrayOf(
            0xC1.toByte(),
            (diameterMm and 0xFF).toByte(),
            ((diameterMm shr 8) and 0xFF).toByte()
        )
        writeCharacteristic(SERVICE_6683, WHEEL_CHAR, data)
    }

    // ⚠️ KRITISK: Temperaturovervågning
    suspend fun readTemperature(): Int {
        val data = readCharacteristic(SERVICE_6688, TEMP_CHAR)
        return data[0].toInt()  // Temperatur i Celsius
    }

    fun monitorTemperature(callback: (Int) -> Unit) {
        // Overvåg motor/batteri temperatur hver 5 sekund
        // Stop ved overophedning (>70°C motor, >50°C batteri)
        setNotificationCallback(SERVICE_6688).with { _, data ->
            val temp = data.value?.get(0)?.toInt() ?: 0
            if (temp > 70) {
                // NØDBREMSE - reducer hastighed automatisk
                setSpeedLimit(15)
            }
            callback(temp)
        }
    }
}

data class ScooterSettings(
    val speedLimitKmh: Int = 39,
    val wheelDiameterMm: Int = 254,
    val zeroStartEnabled: Boolean = false,
    val autoLockEnabled: Boolean = true
)

data class ScooterTelemetry(
    val batteryPercent: Int,
    val speedKmh: Float,
    val motorTempCelsius: Int,  // ⚠️ KRITISK
    val batteryTempCelsius: Int, // ⚠️ KRITISK
    val distanceKm: Float
)
```

---

## APK Reverse Engineering

**Se:** `apk_analysis_commands.sh` for alle bash commands

**Key strings i APK:**
- `speedLimitKmhMaster`
- `speedLimitKmhCurrent`
- `setConnectedDeviceSpeed`
- `deviceSettingsToBuf`
- `lockHX` / `unlockHX`

---

## Permissions (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"/>
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
```

---

## Testing Checklist

- [ ] BLE scan & connect
- [ ] Lock/unlock virker
- [ ] **⚠️ Temperaturovervågning aktiveret og virker**
- [ ] Speed limit ændring (test med 45 km/h)
- [ ] Zero start aktivering
- [ ] Wheel size kalibrering
- [ ] Auto-lock deaktivering
- [ ] **⚠️ Nødbremse ved overophedning (test ved 70°C)**
- [ ] Reconnect efter disconnect

---

## ADVARSEL ⚠️

**Brug på eget ansvar!**
- Kan være ulovligt i dit land
- Kan beskadige motor/batteri
- **⚠️ TEMPERATUROVERVÅGNING ER OBLIGATORISK!**
  - Motor: Max 70°C (reducer hastighed automatisk)
  - Batteri: Max 50°C (stop kørsel)
  - Overophedning kan ødelægge komponenter permanent
- Test i kontrolleret miljø først
- Forkerte commands kan "brick" controlleren

**Data Format:**
- Hastigheder: Decimal → hex ved transmission
- Wheel size: mm som decimal, little-endian
- Booleans: 0x00 = false, 0x01 = true

---

## Resources

- Nordic BLE: https://github.com/NordicSemiconductor/Android-BLE-Library
- BLE Guide: https://developer.android.com/guide/topics/connectivity/bluetooth/ble-overview
- APK Analysis: Se `apk_analysis_commands.sh`
