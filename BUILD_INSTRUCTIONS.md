# 📱 AUGMENT TUNER - BUILD INSTRUCTIONS

## ✅ APP ER NU KLAR!

**Komplet Android app struktur:**

```
Augment-tuner/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml          ✅ INGEN internet permission!
│   │       ├── java/com/augment/tuner/
│   │       │   ├── MainActivity.kt           ✅ Tuning app
│   │       │   ├── CompareControllersActivity.kt  ✅ Test app
│   │       │   └── SimpleBLE.kt             ✅ BLE wrapper (no telemetry)
│   │       └── res/
│   │           └── layout/
│   │               ├── activity_main.xml     ✅ Tuning UI
│   │               └── activity_compare.xml  ✅ Comparison UI
│   ├── build.gradle                          ✅
│   └── proguard-rules.pro                    ✅
├── build.gradle                               ✅
├── settings.gradle                            ✅
└── gradle.properties                          ✅
```

---

## 🛠️ BYGG APPEN

### Method 1: Android Studio (nemmest)

**1. Åbn projektet:**
```bash
cd /home/user/Augment-tuner
```

**2. Start Android Studio:**
```bash
# Linux:
android-studio .

# Windows:
# Åbn Android Studio → File → Open → Vælg Augment-tuner folder

# Mac:
open -a "Android Studio" .
```

**3. Sync Gradle:**
- Android Studio vil automatisk sync'e Gradle
- Vent til "Gradle sync completed"

**4. Connect telefon:**
- Enable USB debugging på telefon
- Connect USB kabel
- Accept debugging på telefon

**5. Build & Run:**
- Tryk på grøn "Run" knap (▶️)
- Eller: `Shift + F10`
- Appen installer automatisk på telefon!

---

### Method 2: Command Line (hurtigst)

**1. Build APK:**
```bash
cd /home/user/Augment-tuner

# Debug build (til test)
./gradlew assembleDebug

# APK placering:
# app/build/outputs/apk/debug/app-debug.apk
```

**2. Install på telefon:**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**3. Kør app:**
```bash
adb shell am start -n com.augment.tuner/.MainActivity
```

---

### Method 3: Release Build (til produktion)

**1. Build signed APK:**
```bash
./gradlew assembleRelease
```

**2. Find APK:**
```bash
ls -lh app/build/outputs/apk/release/app-release-unsigned.apk
```

**3. Sign APK (optional):**
```bash
# Lav signing key (første gang)
keytool -genkey -v -keystore augment-tuner.keystore \
  -alias augment -keyalg RSA -keysize 2048 -validity 10000

# Sign APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore augment-tuner.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk augment

# Align APK
zipalign -v 4 \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  app/build/outputs/apk/release/app-release.apk
```

---

## 📱 BRUG APPEN

### 1. Find Scooter MAC Address

**Method A: nRF Connect app**
```
1. Install "nRF Connect" fra Play Store
2. Åbn appen
3. Scan for devices
4. Find "Augment" eller "MOVE_xxxx"
5. Noter MAC address: XX:XX:XX:XX:XX:XX
```

**Method B: Bluetooth Settings**
```
1. Settings → Bluetooth
2. Find "Augment" i paired devices
3. Tap på settings icon
4. Se MAC address
```

### 2. Åbn Augment Tuner App

**Main screen:**
```
┌────────────────────────────────┐
│     AUGMENT TUNER              │
│ 🔒 Ingen telemetry • Ingen... │
├────────────────────────────────┤
│ Scooter MAC Address            │
│ [XX:XX:XX:XX:XX:XX]           │
│ 💡 Find MAC med nRF Connect... │
│                                │
│ [CONNECT TIL SCOOTER]         │
│ Status: Ikke forbundet         │
├────────────────────────────────┤
│ ⚡ Hastighed                   │
│        38 km/h                 │
│ [========|=========]           │
│ 20 km/h         60 km/h        │
│                                │
│ 🎛️ Features                   │
│ ☐ Zero Start (kick-less)      │
│ ☐ Sport+ Mode                  │
│ ☐ Electronic Turbo             │
│                                │
│ [🚀 SEND TIL SCOOTER]         │
│                                │
│ ⚠️ Test kun i sikkert miljø   │
│    Overvåg motor temperatur    │
└────────────────────────────────┘
```

### 3. Connect til Scooter

```
1. Indsæt MAC address
2. Tap "CONNECT TIL SCOOTER"
3. Vent på "✅ Forbundet"
```

### 4. Sæt Hastighed

```
1. Træk slider til ønsket hastighed (f.eks. 45 km/h)
2. Vælg features (Zero Start, Sport+, Turbo)
3. Tap "🚀 SEND TIL SCOOTER"
4. Done! Scooter er nu tunet!
```

---

## 🧪 TEST BEGGE CONTROLLERS

**Hvis du har to controllers med forskellig firmware:**

**1. Åbn CompareControllersActivity:**
```kotlin
// Rediger CompareControllersActivity.kt linje 31-32:
private val CONTROLLER_1_MAC = "XX:XX:XX:XX:XX:01"  // ← Controller #1
private val CONTROLLER_2_MAC = "YY:YY:YY:YY:02"    // ← Controller #2
```

**2. Rebuild app**

**3. Start comparison:**
- Tap "START COMPARISON TEST"
- App tester automatisk begge controllers
- Viser side-by-side resultater

**4. Resultat:**
```
Controller #1:              Controller #2:
Firmware: v2.1.0           Firmware: v2.0.5
✓ 38 km/h → 38 km/h       ✓ 38 km/h → 38 km/h
✗ 40 km/h → 38 km/h       ✓ 40 km/h → 40 km/h
✗ 45 km/h → 38 km/h       ✓ 45 km/h → 45 km/h

Max: 38 km/h               Max: 45 km/h

→ CONTROLLER #2 HAR HØJERE LIMIT! 🎉
```

---

## 🔒 PRIVACY & TELEMETRY

**Denne app sender INGEN data til:**
- ❌ Augment backend
- ❌ Google Analytics
- ❌ Telemetry servers
- ❌ Tracking services

**Bevis:**
```bash
# Check AndroidManifest.xml
grep -i "internet" app/src/main/AndroidManifest.xml
# Output: <!-- <uses-permission android:name="android.permission.INTERNET" /> -->
#         ↑ COMMENTED OUT = NO INTERNET ACCESS!

# Check for backend calls
grep -r "graphql\|amazonaws\|augment.*api" app/src/main/java/
# Output: (nothing)
#         ↑ NO BACKEND CODE!
```

**Test offline:**
```bash
# 1. Installer app
# 2. Slå WiFi + Mobile Data FRA
# 3. Åbn app og connect til scooter
# 4. Det virker! 🎉
```

---

## 🐛 TROUBLESHOOTING

### Problem: "Ugyldig MAC adresse"
**Løsning:**
- MAC format SKAL være: `XX:XX:XX:XX:XX:XX`
- Brug store bogstaver: `A4:C1:38:12:34:56` ✅
- Ikke små: `a4:c1:38:12:34:56` ✗

### Problem: "Kan ikke forbinde"
**Løsning:**
- Scooter skal være tændt
- Bluetooth skal være enabled
- Scooter må ikke være forbundet til original Augment app
- Prøv at genstarte scooter

### Problem: "Build failed"
**Løsning:**
```bash
# Clean build
./gradlew clean

# Rebuild
./gradlew assembleDebug
```

### Problem: "Permission denied"
**Løsning:**
- Accept Bluetooth permissions i app
- Accept Location permission (Android krav for BLE)

---

## 📊 APP FEATURES

| Feature | MainActivity | CompareActivity |
|---------|-------------|-----------------|
| Set hastighed | ✅ | ❌ |
| Zero Start | ✅ | ❌ |
| Sport+ Mode | ✅ | ❌ |
| Electronic Turbo | ✅ | ❌ |
| Test begge controllers | ❌ | ✅ |
| Firmware info | ❌ | ✅ |
| Speed limit test | ❌ | ✅ |

---

## 🚀 NEXT STEPS

**1. Build appen**
```bash
./gradlew assembleDebug
```

**2. Install på telefon**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**3. Find scooter MAC**
```
nRF Connect → Scan → Noter MAC
```

**4. Tune scooter!**
```
Åbn app → Connect → Sæt 50 km/h → Send
```

**5. Test limit**
```
Kør scooter → Se om den når 50 km/h
```

**6. Hvis den ikke når over 38 km/h:**
```
→ Firmware er locked
→ Se FIRMWARE_MODDING_GUIDE.md
→ Eller test Controller #2 (hvis du har den)
```

---

## ✅ DONE!

**Du har nu:**
- ✅ Komplet Android app
- ✅ Ingen telemetry
- ✅ Direkte BLE control
- ✅ Test tool til begge controllers
- ✅ Build instructions
- ✅ Klar til at tune! 🚀

**Bare bygg og kør!** 📱
