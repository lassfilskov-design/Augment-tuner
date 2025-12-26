# Augment E-Scooter Custom Android App - Development Prompt

## Projekt Oversigt

Byg en **Android app** der fungerer som en komplet erstatning for den originale Augment.eco scooter app, men med udvidede funktioner til tuning og kontrol.

### Platform
- **Android Native** (Java/Kotlin)
- Target SDK: 33+
- Min SDK: 24
- BLE (Bluetooth Low Energy) support påkrævet

---

## 1. KRITISK: BLE GATT Services & Characteristics

### Service UUIDs (Fundne i Augment APK)

Scooteren bruger disse BLE services til kommunikation:

```
Primary Services:
- 00006680-0000-1000-8000-00805f9b34fb  (Hovedkontrol)
- 00006681-0000-1000-8000-00805f9b34fb  (Device Info)
- 00006683-0000-1000-8000-00805f9b34fb  (Settings Write)
- 00006684-0000-1000-8000-00805f9b34fb  (Settings Read)
- 00006685-0000-1000-8000-00805f9b34fb
- 00006687-0000-1000-8000-00805f9b34fb
- 00006688-0000-1000-8000-00805f9b34fb  (Status/Telemetry)

Device Information Services:
- 0000d101-0000-1000-8000-00805f9b34fb  (Custom Device Info)
- 0000180A-0000-1000-8000-00805f9b34fb  (Standard Device Info)

OTA Firmware Update Services:
- 0000fed7-0000-1000-8000-00805f9b34fb
- 0000fed8-0000-1000-8000-00805f9b34fb
```

### BLE Operations (Fra APK)

```kotlin
// Eksempel på BLE operations fra appen
discoverAllServicesAndCharacteristicsForDevice(deviceId)
connectToDevice(deviceId)
readCharacteristic(deviceId, serviceUUID, characteristicUUID)
writeCharacteristic(deviceId, serviceUUID, characteristicUUID, data)
monitorCharacteristic(deviceId, serviceUUID, characteristicUUID)
```

---

## 2. DATA STRUKTUR (Ekstremt Vigtig!)

### Speed Limit Format

**CRITICAL: Hastigheder sendes som DECIMAL værdier, IKKE hex-encoded!**

```javascript
// Korrekt format fra APK analyse:
{
  "speedLimitKmhMaster": 45,     // Max hastighed (decimal int)
  "speedLimitKmhCurrent": 39,    // Aktuel grænse (decimal int)
  "speedLimitKmhMin": 20         // Minimum grænse
}
```

**Data bytes når sendt via BLE:**
- 25 km/t = `0x19` (1 byte)
- 39 km/t = `0x27` (1 byte)
- 45 km/t = `0x2D` (1 byte)

### Wheel Size Format

```javascript
{
  "wheelDiameter": 254,          // 10" = 254mm (decimal)
  "wheelCircumference": 798      // Beregnet: diameter * π
}
```

**Standard værdier:**
- 10 inch = 254mm = `0xFE` (1 byte)
- 11 inch = 279mm = `0x117` (2 bytes, little-endian: `0x17 0x01`)

### Lock/Unlock Commands

```javascript
// Fra APK strings:
{
  "lock": true/false,
  "lockHX": "hex_command",      // Hex format kommando
  "unlockHX": "hex_command"
}
```

---

## 3. FUNKTIONER DER SKAL IMPLEMENTERES

### Core Features (Som Original App)

1. **BLE Device Scanning**
   - Scan for Augment scooters (filter by service UUIDs)
   - Vis RSSI signal styrke
   - Auto-connect til sidst brugte scooter

2. **Device Connection**
   - Pair via Bluetooth
   - Auto-reconnect ved disconnect
   - Connection status indikator

3. **Basic Controls**
   - Lock/Unlock scooter
   - Vis batteri niveau
   - Vis hastighedsmåler (real-time fra BLE)
   - Vis distance kørt
   - GPS tracking (optional)

4. **Settings Sync**
   - Læs aktuelle indstillinger fra scooter
   - Vis firmware version
   - Device info (serial number, hardware version)

### EKSTRA FEATURES (Tuning Functions)

#### 5. Zero Start Mode
```kotlin
// Tillad scooter at starte uden skub
data class ZeroStartSettings(
    val enabled: Boolean,
    val maxSpeed: Int = 6  // Max hastighed for zero start (km/t)
)
```

**BLE Command:**
- Service: `00006683` (Settings Write)
- Data format: `[0xA1, enabled ? 0x01 : 0x00, maxSpeed]`

#### 6. No Limit Mode
```kotlin
// Fjern hastighedsbegrænsning helt
data class NoLimitSettings(
    val enabled: Boolean,
    val customLimit: Int? = null  // Hvis null = ingen grænse
)

// Send til scooter:
fun setSpeedLimit(limit: Int?) {
    val data = when {
        limit == null -> byteArrayOf(0xFF, 0xFF)  // No limit
        limit > 0 -> byteArrayOf(limit.toByte(), 0x00)
        else -> byteArrayOf(0x27, 0x00)  // Default 39 km/t
    }
    writeCharacteristic(SETTINGS_SERVICE, SPEED_CHAR, data)
}
```

#### 7. Remove Theft Lock
```kotlin
// Deaktiver theft lock permanent
data class TheftLockSettings(
    val autoLockEnabled: Boolean = false,
    val lockAfterTrips: Int = 0  // 0 = aldrig
)

// Original: Lock efter hver 10. tur
// Custom: Deaktiver helt
fun disableAutoLock() {
    val data = byteArrayOf(
        0xB1,           // Command: Lock settings
        0x00,           // Disable auto-lock
        0x00, 0x00      // Trips before lock = 0
    )
    writeCharacteristic(SETTINGS_SERVICE, LOCK_CHAR, data)
}
```

#### 8. Wheel Size Configuration
```kotlin
// UI til manuel hjulstørrelse indstilling
data class WheelSettings(
    val diameterMm: Int,  // Brugerdefineret i mm
    val presetInches: Double? = null  // eller vælg preset (8.5", 10", 11")
)

fun setWheelSize(diameterMm: Int) {
    // Send som 2-byte little-endian værdi
    val data = byteArrayOf(
        0xC1,                        // Command: Wheel config
        (diameterMm and 0xFF).toByte(),
        ((diameterMm shr 8) and 0xFF).toByte()
    )
    writeCharacteristic(SETTINGS_SERVICE, WHEEL_CHAR, data)
}
```

#### 9. Cruise Control
```kotlin
// Aktivér cruise control (hold konstant hastighed)
data class CruiseControlSettings(
    val enabled: Boolean,
    val activationSpeed: Int = 15  // Aktiver ved X km/t
)
```

---

## 4. APP ARKITEKTUR

### Technology Stack

```
- Language: Kotlin
- Architecture: MVVM (Model-View-ViewModel)
- BLE Library: Nordic BLE Library (com.nordicsemi.android:ble:2.6.1)
- Dependency Injection: Hilt
- UI: Jetpack Compose
- Navigation: Compose Navigation
- State Management: StateFlow/SharedFlow
- Local Storage: DataStore (for app settings)
```

### Key Components

```kotlin
// BLE Manager
class ScooterBleManager(context: Context) : BleManager(context) {

    // Characteristics
    private var speedCharacteristic: BluetoothGattCharacteristic? = null
    private var lockCharacteristic: BluetoothGattCharacteristic? = null
    private var wheelCharacteristic: BluetoothGattCharacteristic? = null
    private var statusCharacteristic: BluetoothGattCharacteristic? = null

    // Read device status
    suspend fun readDeviceStatus(): DeviceStatus

    // Write speed limit
    suspend fun setSpeedLimit(limit: Int)

    // Lock/Unlock
    suspend fun lockScooter()
    suspend fun unlockScooter()

    // Custom settings
    suspend fun enableZeroStart(enabled: Boolean, maxSpeed: Int)
    suspend fun removeSpeedLimit()
    suspend fun setWheelSize(diameterMm: Int)
    suspend fun disableTheftLock()
}

// View Model
class ScooterViewModel : ViewModel() {
    private val _deviceState = MutableStateFlow<DeviceState>(DeviceState.Disconnected)
    val deviceState: StateFlow<DeviceState> = _deviceState.asStateFlow()

    private val _scooterSettings = MutableStateFlow(ScooterSettings())
    val scooterSettings: StateFlow<ScooterSettings> = _scooterSettings.asStateFlow()

    fun connectToScooter(deviceId: String)
    fun applyCustomSettings(settings: CustomSettings)
}

// Data Classes
data class ScooterSettings(
    val speedLimitKmh: Int = 39,
    val wheelDiameterMm: Int = 254,
    val zeroStartEnabled: Boolean = false,
    val autoLockEnabled: Boolean = true,
    val cruiseControlEnabled: Boolean = false
)

data class CustomSettings(
    val noLimitMode: Boolean = false,
    val customSpeedLimit: Int? = null,
    val zeroStart: ZeroStartSettings = ZeroStartSettings(false, 6),
    val theftLock: TheftLockSettings = TheftLockSettings(),
    val wheelSize: WheelSettings = WheelSettings(254)
)

data class DeviceStatus(
    val batteryPercent: Int,
    val speedKmh: Double,
    val distanceKm: Double,
    val isLocked: Boolean,
    val firmwareVersion: String
)
```

---

## 5. UI/UX DESIGN

### Main Screen
```
┌─────────────────────────────┐
│  Augment Custom             │
│                             │
│  [Scooter Icon]             │
│  ● Connected                │
│                             │
│  Battery: 85%  🔋           │
│  Speed: 23 km/h             │
│  Distance: 15.3 km          │
│                             │
│  ┌──────────┐  ┌──────────┐│
│  │  Lock    │  │  Unlock  ││
│  └──────────┘  └──────────┘│
│                             │
│  [Settings Button]          │
└─────────────────────────────┘
```

### Settings Screen (Tuning)
```
┌─────────────────────────────┐
│  ← Tuning Settings          │
│                             │
│  Speed Settings             │
│  ├─ No Limit Mode    [✓]   │
│  ├─ Custom Limit     45 km/h│
│  └─ Original         39 km/h│
│                             │
│  Zero Start                 │
│  ├─ Enabled          [✓]   │
│  └─ Max Speed        6 km/h │
│                             │
│  Security                   │
│  └─ Disable Auto-Lock [✓]  │
│                             │
│  Wheel Configuration        │
│  ├─ Diameter         254mm  │
│  └─ Preset           10"    │
│                             │
│  Cruise Control             │
│  ├─ Enabled          [ ]   │
│  └─ Activation       15 km/h│
│                             │
│  [Apply Settings]           │
└─────────────────────────────┘
```

---

## 6. BLE COMMAND REFERENCE

### Command Format (Generelt)

```
Byte 0: Command Type
Byte 1-N: Data

Command Types:
0xA1 = Zero Start Settings
0xA2 = Speed Limit Settings
0xB1 = Lock Settings
0xC1 = Wheel Configuration
0xD1 = Cruise Control
```

### Eksempel Commands

```kotlin
// Set speed limit to 45 km/h
val cmd = byteArrayOf(
    0xA2.toByte(),  // Speed limit command
    0x2D,           // 45 decimal = 0x2D hex
    0x00            // Padding/checksum (kan variere)
)

// Enable zero start, max 6 km/h
val cmd = byteArrayOf(
    0xA1.toByte(),  // Zero start command
    0x01,           // Enable (0x00 = disable)
    0x06            // Max 6 km/h
)

// Disable auto theft lock
val cmd = byteArrayOf(
    0xB1.toByte(),  // Lock settings command
    0x00,           // Disable auto-lock
    0x00, 0x00      // Trips count = 0
)

// Set wheel size to 254mm (10 inches)
val cmd = byteArrayOf(
    0xC1.toByte(),  // Wheel config command
    0xFE,           // 254 low byte
    0x00            // 254 high byte (little-endian)
)
```

---

## 7. HVOR FINDER DU COMMAND STRUKTUREN I APK?

### APK Bundle Analyse

1. **Service UUIDs:**
   - Søg i `index.android.bundle` efter: `00006680`, `00006683`, `00006684`
   - Find alle UUID patterns: `[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}`

2. **Settings Functions:**
   - Søg efter: `speedLimitKmhMaster`, `speedLimitKmhCurrent`
   - Søg efter: `setConnectedDeviceSpeed`
   - Søg efter: `deviceSettingsToBuf` (buffer encoding function)

3. **Lock Commands:**
   - Søg efter: `lockHX`, `unlockHX`, `lock`, `unlock`
   - Find hex patterns ved: `0xB1`, `0xB2` (lock-relaterede)

4. **Hex Values:**
   - Speed limits er **decimal** tal, konverteret til hex ved sending
   - Wheel size er **decimal** mm-værdi, sendt som little-endian 2-byte

### Tools til APK Reverse Engineering

```bash
# Extract APK
apktool d augment.apk

# Search bundle for BLE UUIDs
strings assets/index.android.bundle | grep "6680\|6683\|6684"

# Find speed limit references
strings assets/index.android.bundle | grep -i "speedlimit\|wheelsize"

# Extract all hex patterns
strings assets/index.android.bundle | grep -E "0x[0-9A-F]{2}"
```

---

## 8. SIKKERHED & ADVARSLER

### App Permissions (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"/>
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
```

### Safety Warning Dialog

```kotlin
// Vis advarsel når "No Limit" aktiveres
AlertDialog.Builder(context)
    .setTitle("Sikkerhedsadvarsel")
    .setMessage("""
        Du er ved at fjerne hastighedsbegrænsningen.
        Dette kan være ulovligt i dit land og kan ødelægge scooteren.
        Fortsæt på eget ansvar.
    """)
    .setPositiveButton("Forstået") { _, _ -> applyNoLimit() }
    .setNegativeButton("Annuller", null)
    .show()
```

---

## 9. IMPLEMENTATION PLAN

### Phase 1: Core BLE Communication
1. Setup Nordic BLE library
2. Implement device scanning
3. Connect to scooter
4. Read/write basic characteristics
5. Monitor battery & speed

### Phase 2: Original App Features
1. Lock/unlock functionality
2. Device info display
3. Settings read/write
4. Status monitoring

### Phase 3: Custom Tuning Features
1. No limit mode
2. Zero start
3. Disable theft lock
4. Wheel size config
5. Cruise control

### Phase 4: UI Polish
1. Modern Material Design 3
2. Dark mode support
3. Smooth animations
4. Error handling & user feedback

---

## 10. TESTING CHECKLIST

- [ ] BLE scan finder Augment scooters
- [ ] Connection etableres korrekt
- [ ] Read device status virker
- [ ] Lock/unlock kommandoer virker
- [ ] Speed limit ændring reflekteres i scooter
- [ ] Zero start aktivering fungerer
- [ ] Wheel size ændring påvirker speedometer korrekt
- [ ] Auto-lock kan deaktiveres
- [ ] App reconnect efter disconnect
- [ ] Fejlhåndtering for tabte connections
- [ ] Sikkerhedsadvarsler vises korrekt

---

## 11. KILDER & REFERENCE

### Fra Augment APK Analyse
- Service UUIDs: `/home/user/Augment-tuner/api-config.json`
- API Schema: `/home/user/Augment-tuner/augment-api-schema.json`
- Bundle: `/home/user/Augment-tuner/apk-extracted/assets/index.android.bundle`

### Documentation
- Nordic BLE Library: https://github.com/NordicSemiconductor/Android-BLE-Library
- Bluetooth GATT: https://developer.android.com/guide/topics/connectivity/bluetooth/ble-overview
- Jetpack Compose: https://developer.android.com/jetpack/compose

---

## NOTES

**VIGTIG:** Denne app ændrer hardware-indstillinger på scooteren. Brug på eget ansvar!

- Hastighedsgrænser kan være lovpligtige i dit land
- At fjerne hastighedsbegrænsning kan beskadige motor/batteri
- Zero start kan øge strømforbrug betydeligt
- Test altid i kontrolleret miljø først

**Data Format:**
- Alle hastigheder: Decimal integers (konverteret til hex ved transmission)
- Wheel size: Millimeter som decimal (254 = 10 inch)
- Boolean flags: 0x00 = false, 0x01 = true
- Multi-byte values: Little-endian byte order

**Command Discovery:**
- Brug UART connection til at overvåge BLE trafik mellem original app og scooter
- Log alle write operations for at reverse-engineer command format
- Test commands forsigtig - forkerte values kan "brick" controlleren
