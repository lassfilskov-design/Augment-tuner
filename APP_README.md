# Augment Tuner - Telemetry-Free Android App

Privacy-focused Bluetooth app for Augment e-scooters with **ZERO telemetry**.

## 🔐 Privacy Features

✅ **NO INTERNET PERMISSION** - Cannot send data to external servers
✅ **Bluetooth ONLY** - Direct local connection to scooter
✅ **No tracking** - All data stays on your device
✅ **Open source** - Verify the code yourself

## 📱 Features

- Scan for Augment scooters (Service UUID: 0000ff01-...)
- Connect via Bluetooth Low Energy
- Read manufacturer data (telemetry)
- Display battery, speed, voltage
- Local-only configuration

## 🚀 Build Instructions

### Requirements
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34
- Kotlin 1.9.22

### Steps

1. Open project in Android Studio
```bash
cd /home/user/Augment-tuner
# Open in Android Studio
```

2. Sync Gradle
```
File → Sync Project with Gradle Files
```

3. Build APK
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

4. Install on device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 📋 Permissions Required

- `BLUETOOTH` / `BLUETOOTH_SCAN` - Scan for devices
- `BLUETOOTH_ADMIN` / `BLUETOOTH_CONNECT` - Connect to scooter
- `ACCESS_FINE_LOCATION` - Required by Android for BLE scanning

⚠️ **NO INTERNET PERMISSION** = **NO TELEMETRY**

## 🔧 How It Works

### 1. Scanning
The app scans for devices advertising the Augment service UUID:
```
0000ff01-0000-1000-8000-00805f9b34fb
```

### 2. Connection
Connects to scooter via Bluetooth Low Energy (BLE)

### 3. Manufacturer Data
Reads manufacturer-specific data from advertising packet:
```
Company ID: 0x5240
Data format: 0x5266B6A8 (example)
```

### 4. Telemetry Parsing
Extracts telemetry from manufacturer data:
- Battery percentage
- Speed
- Voltage
- Status flags

**All processing happens locally** - no data leaves your device.

## 📊 Manufacturer Data Format

Based on observations from Bluetooth sniffing:

```
MAC Address: A8:B6:66:52:52:40
             └──┘ Denmark indicator (theory)

Manufacturer Data: 0x5266B6A8
                   │ │  │  └─ Status/flags?
                   │ │  └──── Battery %?
                   │ └─────── Speed/telemetry?
                   └──────── Device type?
```

⚠️ **Note**: Exact format is theoretical and may vary.

## 🔍 Technical Details

### Service UUID
```
0000ff01-0000-1000-8000-00805f9b34fb
```
This is the Bluetooth GATT service that Augment scooters advertise.

### Characteristics
The app discovers and reads characteristics under the ff01 service to:
- Get scooter status
- Read telemetry
- Send commands (future feature)

### No Network Calls
```kotlin
// AndroidManifest.xml
<!-- NO INTERNET PERMISSION = NO TELEMETRY! -->
<!-- <uses-permission android:name="android.permission.INTERNET" /> -->
```

The app **cannot** connect to external servers even if malicious code was injected.

## 🛠️ Development

### Project Structure
```
Augment-tuner/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/augment/tuner/
│   │       │   └── MainActivity.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   └── activity_main.xml
│   │       │   └── values/
│   │       │       ├── strings.xml
│   │       │       └── themes.xml
│   │       └── AndroidManifest.xml
│   ├── build.gradle
│   └── proguard-rules.pro
├── build.gradle
├── settings.gradle
└── gradle.properties
```

### Key Files
- `MainActivity.kt` - Main app logic, BLE scanning and connection
- `activity_main.xml` - UI layout
- `AndroidManifest.xml` - **NO INTERNET PERMISSION**

## 🧪 Testing

1. Enable Bluetooth on Android device
2. Grant location permission (required for BLE scanning)
3. Turn on Augment scooter
4. Tap "SCAN FOR SCOOTER"
5. App should find and connect to scooter
6. View telemetry data

## 📝 Future Features

- [ ] Lock/unlock scooter
- [ ] Firmware updates (local file)
- [ ] Custom speed limits
- [ ] Ride statistics (local only)
- [ ] Export data to CSV

## ⚠️ Legal Disclaimer

This app is for **educational purposes** and **personal use** only.

- Reverse engineered from Augment APK
- No affiliation with Augment
- Use at your own risk
- Modifying scooter settings may void warranty

## 📜 License

This project is reverse-engineered for educational purposes.

## 🙏 Credits

- APK analysis and UUID discovery
- Bluetooth protocol reverse engineering
- Privacy-focused design
