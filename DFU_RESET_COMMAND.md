# NORDIC DFU RESET COMMAND

## 🎯 DU SPURGTE: "Reset COmmand for at få firmwaren"

**Svar: JA - Nordic DFU protokollen!**

---

## 🔧 DFU (Device Firmware Update) SERVICES

### Service UUIDs:

```
DFU Service:         0000FED7-0000-1000-8000-00805f9b34fb
DFU Control Point:   0000FED8-0000-1000-8000-00805f9b34fb
                     8EC90001-F315-4F60-9FB8-838830DAEA50 (alternative)

DFU Packet:          8EC90002-F315-4F60-9FB8-838830DAEA50
DFU Version:         8EC90003-F315-4F60-9FB8-838830DAEA50
```

---

## 📡 RESET COMMAND

### Command: Enter DFU Bootloader Mode

**Hex:** `0x01`

**Formål:** Genstart controller i bootloader mode for firmware update

```kotlin
// Service
val DFU_CONTROL_POINT = UUID.fromString("8EC90001-F315-4F60-9FB8-838830DAEA50")

// Command
val ENTER_BOOTLOADER = byteArrayOf(0x01)

// Send command
gatt.getService(DFU_SERVICE)
    ?.getCharacteristic(DFU_CONTROL_POINT)
    ?.let { char ->
        char.value = ENTER_BOOTLOADER
        char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        gatt.writeCharacteristic(char)
    }

// Controller genstarter nu i DFU mode
// MAC address ændres: XX:XX:XX:XX:XX:XX → XX:XX:XX:XX:XX:XY (sidste byte +1)
```

---

## 🚀 KOMPLET DFU COMMAND LISTE

### Control Point Commands:

```kotlin
object DfuCommand {
    const val START_DFU: Byte = 0x01           // Enter bootloader
    const val INITIALIZE: Byte = 0x02          // Send init packet
    const val RECEIVE_FIRMWARE: Byte = 0x03    // Ready to receive
    const val VALIDATE: Byte = 0x04            // Validate firmware
    const val ACTIVATE_RESET: Byte = 0x05      // Activate and reset
    const val RESET: Byte = 0x06               // Reset without activate
    const val REPORT_SIZE: Byte = 0x07         // Packet receipt notification
    const val PACKET_RECEIPT: Byte = 0x08      // Packet receipt notification interval
    const val RESPONSE_CODE: Byte = 0x10       // Response from bootloader
    const val PACKET_RECEIPT_NOTIF: Byte = 0x11 // Notification
}
```

### Response Codes:

```kotlin
object DfuResponse {
    const val SUCCESS: Byte = 0x01
    const val INVALID_STATE: Byte = 0x02
    const val NOT_SUPPORTED: Byte = 0x03
    const val DATA_SIZE_EXCEEDS_LIMIT: Byte = 0x04
    const val CRC_ERROR: Byte = 0x05
    const val OPERATION_FAILED: Byte = 0x06
}
```

---

## 📖 KOMPLET WORKFLOW: DUMP FIRMWARE

### Step 1: Connect til controller

```kotlin
val device = bluetoothAdapter.getRemoteDevice("XX:XX:XX:XX:XX:XX")
val gatt = device.connectGatt(context, false, gattCallback)
```

### Step 2: Discover services

```kotlin
override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
    if (newState == BluetoothProfile.STATE_CONNECTED) {
        gatt.discoverServices()
    }
}
```

### Step 3: Find DFU service

```kotlin
override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
    val dfuService = gatt.getService(UUID.fromString("0000FED7-0000-1000-8000-00805f9b34fb"))
    val controlPoint = dfuService?.getCharacteristic(
        UUID.fromString("8EC90001-F315-4F60-9FB8-838830DAEA50")
    )

    if (controlPoint != null) {
        println("✓ DFU Service found!")
    } else {
        println("✗ DFU Service not found - check UUID")
    }
}
```

### Step 4: Enable notifications

```kotlin
// Enable notifications for responses
val descriptor = controlPoint.getDescriptor(
    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
)
descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
gatt.writeDescriptor(descriptor)
gatt.setCharacteristicNotification(controlPoint, true)
```

### Step 5: Send RESET command

```kotlin
val enterBootloaderCmd = byteArrayOf(0x01)
controlPoint.value = enterBootloaderCmd
controlPoint.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
gatt.writeCharacteristic(controlPoint)
```

### Step 6: Wait for response

```kotlin
override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
    val response = characteristic.value

    if (response[0] == 0x10.toByte()) { // Response code
        val opcode = response[1]
        val status = response[2]

        if (status == 0x01.toByte()) { // SUCCESS
            println("✓ Controller entering DFU mode...")
            // Controller will disconnect and reconnect with +1 MAC
        }
    }
}
```

### Step 7: Reconnect til DFU bootloader

```kotlin
// Original MAC: A4:C1:38:12:34:56
// DFU MAC:      A4:C1:38:12:34:57 (sidste byte +1)

val originalMac = "A4:C1:38:12:34:56"
val dfuMac = incrementMacAddress(originalMac)

delay(2000) // Wait for controller to restart

val dfuDevice = bluetoothAdapter.getRemoteDevice(dfuMac)
val dfuGatt = dfuDevice.connectGatt(context, false, dfuCallback)
```

### Step 8: Download firmware

**OBS:** Nordic DFU kan ikke "læse" eksisterende firmware direkte!

**Du skal downloade firmware fra Augment backend:**

```graphql
query checkFirmwareUpgrade($deviceId: ID!) {
  checkFirmwareUpgrade(deviceId: $deviceId) {
    version
    downloadUrl      # ← Firmware .zip download link
    releaseNotes
    checksum
  }
}
```

**Eller via direkte URL (hvis kendt):**
```
https://s3.amazonaws.com/augment-firmware/controller_v2.1.0.zip
```

---

## 💾 ALTERNATIVE: DUMP VIA nRF CONNECT APP

**Nemmeste metode:**

1. **Install nRF Connect** på Android
2. **Scan** for din controller
3. **Connect** til controller
4. **Find DFU Service** (FED7 eller FED8)
5. **Skriv `01`** til DFU Control Point
6. **Controller genstarter** i DFU mode
7. **nRF Connect viser** "DFU Bootloader"
8. **DFU → Read firmware** (hvis understøttet)
9. **Eller DFU → Upload** (flash ny firmware)

**OBS:** De fleste Nordic chips tillader IKKE firmware readback!
→ Du skal downloade firmware fra backend eller dump via JTAG/SWD

---

## 🔬 FIRMWARE DOWNLOAD FRA AUGMENT BACKEND

### Method 1: GraphQL Query

```javascript
const query = `
  query checkFirmwareUpgrade($deviceId: ID!) {
    checkFirmwareUpgrade(deviceId: $deviceId) {
      version
      downloadUrl
      releaseNotes
      checksum
    }
  }
`;

const response = await fetch(
  'https://frbc72oc4h.execute-api.eu-west-1.amazonaws.com/prod/graphql-public',
  {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${authToken}`
    },
    body: JSON.stringify({
      query,
      variables: {
        deviceId: "YOUR_DEVICE_ID"  // Fra Augment app
      }
    })
  }
);

const data = await response.json();
const firmwareUrl = data.data.checkFirmwareUpgrade.downloadUrl;

// Download firmware
const firmware = await fetch(firmwareUrl);
const firmwareBlob = await firmware.blob();
// Save as controller_firmware.zip
```

### Method 2: mitmproxy Intercept

```bash
# 1. Start mitmproxy
mitmproxy -p 8080

# 2. Sæt telefon proxy til mitmproxy
# WiFi → Proxy → Manual → localhost:8080

# 3. Åbn Augment app
# 4. Gå til Settings → Firmware Update
# 5. Check for updates

# 6. mitmproxy vil vise:
GET https://s3.amazonaws.com/.../firmware_v2.1.0.zip
→ Save URL!

# 7. Download direkte:
wget <firmware_url>
```

### Method 3: Decompile APK

```bash
# Find embedded firmware URL i APK
strings index.android.bundle | grep -i "firmware\|s3.amazonaws"

# Output:
https://s3-eu-west-1.amazonaws.com/augment-production/firmware/
```

---

## 📦 FIRMWARE ZIP STRUKTUR

**Typisk Nordic DFU firmware .zip:**

```
firmware.zip
├── manifest.json          # Firmware metadata
├── nrf52_application.bin  # Application firmware
├── nrf52_bootloader.bin   # Bootloader (optional)
└── init_packet.dat        # Init packet (verification)
```

**manifest.json:**
```json
{
  "manifest": {
    "application": {
      "bin_file": "nrf52_application.bin",
      "dat_file": "init_packet.dat"
    }
  }
}
```

---

## 🛠️ FLASH FIRMWARE VIA KOTLIN

### Komplet DFU workflow:

```kotlin
suspend fun flashFirmware(gatt: BluetoothGatt, firmwareData: ByteArray, initPacket: ByteArray) {
    val dfuService = gatt.getService(UUID.fromString("0000FED7-0000-1000-8000-00805f9b34fb"))
    val controlPoint = dfuService.getCharacteristic(UUID.fromString("8EC90001-F315-4F60-9FB8-838830DAEA50"))
    val packetChar = dfuService.getCharacteristic(UUID.fromString("8EC90002-F315-4F60-9FB8-838830DAEA50"))

    // 1. Start DFU
    controlPoint.value = byteArrayOf(0x01)  // START_DFU
    gatt.writeCharacteristic(controlPoint)
    delay(500)

    // 2. Send init packet size
    val initSize = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(initPacket.size).array()
    controlPoint.value = byteArrayOf(0x02) + initSize  // INITIALIZE
    gatt.writeCharacteristic(controlPoint)
    delay(200)

    // 3. Send init packet data
    packetChar.value = initPacket
    packetChar.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
    gatt.writeCharacteristic(packetChar)
    delay(500)

    // 4. Send firmware size
    val fwSize = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(firmwareData.size).array()
    controlPoint.value = byteArrayOf(0x03) + fwSize  // RECEIVE_FIRMWARE
    gatt.writeCharacteristic(controlPoint)
    delay(200)

    // 5. Send firmware in chunks (20 bytes per packet)
    val chunkSize = 20
    for (i in firmwareData.indices step chunkSize) {
        val chunk = firmwareData.copyOfRange(i, min(i + chunkSize, firmwareData.size))
        packetChar.value = chunk
        gatt.writeCharacteristic(packetChar)
        delay(20)  // Small delay between packets
    }

    delay(1000)

    // 6. Validate firmware
    controlPoint.value = byteArrayOf(0x04)  // VALIDATE
    gatt.writeCharacteristic(controlPoint)
    delay(2000)

    // 7. Activate and reset
    controlPoint.value = byteArrayOf(0x05)  // ACTIVATE_RESET
    gatt.writeCharacteristic(controlPoint)

    // Controller will now reboot with new firmware!
}
```

---

## ⚡ QUICK GUIDE - DUMP FIRMWARE NU!

### Hurtigste metode (via Augment backend):

```bash
# 1. Hent auth token fra Augment app (se FIRMWARE_MODDING_GUIDE.md)

# 2. Query firmware URL
curl -X POST https://frbc72oc4h.execute-api.eu-west-1.amazonaws.com/prod/graphql-public \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { checkFirmwareUpgrade(deviceId: \"YOUR_DEVICE_ID\") { downloadUrl version } }"
  }'

# 3. Download firmware
wget -O controller_firmware.zip <downloadUrl>

# 4. Unzip
unzip controller_firmware.zip

# 5. Hex edit speed limit
xxd nrf52_application.bin | grep "26 00"  # Find 38 km/h (0x26)
# Patch til 50 km/h (0x32) med hex editor

# 6. Flash tilbage via nRF Connect app
```

---

## 🎯 TL;DR

**RESET Command:**
```
Service: 0000FED8-0000-1000-8000-00805f9b34fb
Command: 0x01 (Enter DFU bootloader mode)
```

**Men du kan IKKE dumpe firmware direkte!**

**Du skal:**
1. Download firmware fra Augment backend (GraphQL)
2. Eller intercept firmware download via mitmproxy
3. Derefter kan du modificere og flash tilbage

**Hurtigst:** Brug nRF Connect app til firmware flash

---

## 📚 NEXT STEPS

1. ✅ Download firmware fra begge controllers (via backend)
2. ✅ Sammenlign firmware binaries
3. ✅ Find speed limit bytes
4. ✅ Patch eller flash nyeste firmware
5. ✅ Test på begge controllers

**SE OGSÅ:**
- `DUAL_CONTROLLER_GUIDE.md` - Sammenligning af to controllers
- `FIRMWARE_MODDING_GUIDE.md` - Firmware modification workflow
- `FIRMWARE_ONLINE_VS_LOCAL.md` - Online vs local firmware
