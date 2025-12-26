# TELEMETRY-FREE AUGMENT APP

## 🎯 FORMÅL

**Simpel app UDEN:**
- ❌ Augment backend tracking
- ❌ GraphQL telemetry
- ❌ Brugerdata indsamling
- ❌ GPS tracking
- ❌ Analytics
- ❌ Bloatware

**KUN:**
- ✅ Direkte BLE connection
- ✅ Send kommandoer til scooter
- ✅ Ingen internet forbindelse nødvendig

---

## 📱 APP STRUKTUR

```
AugmentTuner/
├── app/
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/augment/tuner/
│           │   ├── MainActivity.kt
│           │   └── SimpleBLE.kt  ← Allerede lavet!
│           └── res/
│               └── layout/
│                   └── activity_main.xml
└── build.gradle
```

---

## 🔧 MANIFEST (Ingen internet permissions!)

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.augment.tuner">

    <!-- BLE permissions ONLY -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    <!-- NO INTERNET! -->
    <!-- <uses-permission android:name="android.permission.INTERNET" /> -->

    <application
        android:label="Augment Tuner"
        android:theme="@style/Theme.AppCompat">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## 🎨 LAYOUT (Simpel UI)

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="16dp">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="AUGMENT TUNER"
        android:textSize="24sp"
        android:textStyle="bold"
        android:gravity="center"
        android:padding="16dp"/>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="🔒 Ingen telemetry, ingen tracking"
        android:textSize="12sp"
        android:gravity="center"
        android:paddingBottom="24dp"/>

    <!-- Connect button -->
    <Button
        android:id="@+id/btnConnect"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Connect til Scooter"/>

    <TextView
        android:id="@+id/tvStatus"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Status: Ikke forbundet"
        android:padding="8dp"/>

    <!-- Speed control -->
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Hastighed"
        android:textStyle="bold"
        android:paddingTop="16dp"/>

    <SeekBar
        android:id="@+id/seekSpeed"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:max="60"
        android:progress="38"/>

    <TextView
        android:id="@+id/tvSpeed"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="38 km/h"
        android:gravity="center"/>

    <!-- Features -->
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Features"
        android:textStyle="bold"
        android:paddingTop="16dp"/>

    <CheckBox
        android:id="@+id/cbZeroStart"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Zero Start (kick-less)"/>

    <CheckBox
        android:id="@+id/cbSportPlus"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Sport+ Mode"/>

    <CheckBox
        android:id="@+id/cbTurbo"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Electronic Turbo"/>

    <!-- Apply button -->
    <Button
        android:id="@+id/btnApply"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="Send til Scooter"
        android:layout_marginTop="24dp"
        android:enabled="false"/>

</LinearLayout>
```

---

## 📱 MAIN ACTIVITY

```kotlin
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
        tvStatus.text = "Status: Forbinder..."

        // TODO: Scan for scooter eller hardcode MAC address
        val scooterMac = "XX:XX:XX:XX:XX:XX"  // ← Indsæt din scooter MAC

        ble.connect(scooterMac) {
            runOnUiThread {
                isConnected = true
                tvStatus.text = "Status: ✅ Forbundet"
                btnConnect.text = "Disconnect"
                btnApply.isEnabled = true
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

        Toast.makeText(this, "Indstillinger sendt!", Toast.LENGTH_SHORT).show()
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
```

---

## 🚀 BYGG APP

### build.gradle (app)

```gradle
plugins {
    id 'com.android.application'
    id 'kotlin-android'
}

android {
    compileSdk 34

    defaultConfig {
        applicationId "com.augment.tuner"
        minSdk 23
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled false
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = '1.8'
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
}
```

### build.gradle (project)

```gradle
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.2.0'
        classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
```

---

## ✅ FEATURES

**Denne app:**
- ✅ **Ingen internet** - virker 100% offline
- ✅ **Ingen telemetry** - sender INTET til Augment backend
- ✅ **Ingen tracking** - ingen GPS, ingen analytics
- ✅ **Simpel** - kun BLE commands
- ✅ **Controller #1 og #2** - virker med begge controllers
- ✅ **Test firmware limits** - prøv forskellige hastigheder

---

## 🧪 TEST CONTROLLER #2

```kotlin
// I MainActivity.connectToScooter()

// Test Controller #1
val controller1_mac = "XX:XX:XX:XX:XX:01"

// Test Controller #2
val controller2_mac = "YY:YY:YY:YY:YY:02"

// Prøv begge:
// 1. Connect til Controller #1
// 2. Sæt speed til 50 km/h
// 3. Se om den når over 38 km/h
// 4. Gentag med Controller #2
```

---

## 🔒 PRIVACY

**Denne app indsamler:**
- ❌ INTET

**Ingen data forlader din telefon!**

**Ingen permissions ud over BLE:**
- ✅ Bluetooth (kun til scooter)
- ✅ Location (Android krav for BLE scan)
- ❌ INGEN internet
- ❌ INGEN storage
- ❌ INGEN camera
- ❌ INGEN contacts

---

## 🚨 BRUG

1. **Byg app** med Android Studio
2. **Installer** på telefon via USB
3. **Slå WiFi og mobil data FRA** (test offline!)
4. **Åbn app**
5. **Connect** til scooter
6. **Test** forskellige speeds
7. **Find** max speed på begge controllers

**INGEN Augment backend telemetry! 🎉**
