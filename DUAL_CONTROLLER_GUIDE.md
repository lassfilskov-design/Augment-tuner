# DUAL CONTROLLER FIRMWARE ANALYSE

## 🎯 DIN SITUATION

**Du har opdaget:**
- Controller #1: Firmware locked på 38 km/h
- Controller #2: **Anderledes firmware lock** (potentielt højere!)

**Dette er GULD!** 🏆

Hvis Controller #2 har mindre restriktiv firmware:
→ Dump firmware fra Controller #2
→ Flash til Controller #1
→ Begge controllers unlocked!

---

## 🔍 HVORFOR ER DE FORSKELLIGE?

### Mulige årsager:

**1. Forskellige firmware versioner**
```
Controller #1: v2.1.0 (EU edition, 38 km/h locked)
Controller #2: v2.0.5 (pre-EU regulations, 45+ km/h)
```

**2. Forskellige regionale builds**
```
Controller #1: EU build (25 km/h legal → 38 km/h hacked)
Controller #2: US build (20 mph / 32 km/h legal → 45+ km/h capable)
```

**3. Forskellige hardware revisioner**
```
Controller #1: Rev. B (nyere, mere restriktiv)
Controller #2: Rev. A (ældre, mindre restriktiv)
```

**4. Factory vs Field firmware**
```
Controller #1: Opdateret via Augment app (restriktiv firmware)
Controller #2: Aldrig opdateret (original factory firmware)
```

---

## 📊 STEP 1: SAMMENLIGN FIRMWARE INFO

### Check firmware version på begge:

```kotlin
// SERVICE: Device Information
val SERVICE_DEVICE_INFO = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb")

// CHARACTERISTICS:
val CHAR_FIRMWARE_REVISION = UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb")
val CHAR_HARDWARE_REVISION = UUID.fromString("00002A27-0000-1000-8000-00805f9b34fb")
val CHAR_SOFTWARE_REVISION = UUID.fromString("00002A28-0000-1000-8000-00805f9b34fb")

// CONTROLLER #1
gatt.connect("XX:XX:XX:XX:XX:01")
val fw1 = readCharacteristic(CHAR_FIRMWARE_REVISION)
val hw1 = readCharacteristic(CHAR_HARDWARE_REVISION)
val sw1 = readCharacteristic(CHAR_SOFTWARE_REVISION)

println("Controller #1:")
println("  Firmware: $fw1")
println("  Hardware: $hw1")
println("  Software: $sw1")

// CONTROLLER #2
gatt.connect("YY:YY:YY:YY:YY:02")
val fw2 = readCharacteristic(CHAR_FIRMWARE_REVISION)
val hw2 = readCharacteristic(CHAR_HARDWARE_REVISION)
val sw2 = readCharacteristic(CHAR_SOFTWARE_REVISION)

println("Controller #2:")
println("  Firmware: $fw2")
println("  Hardware: $hw2")
println("  Software: $sw2")
```

**Sammenlign:**
- Hvis firmware version er forskellig → Derfor forskellen!
- Hvis hardware revision forskellig → Forskellige chips/limiters
- Hvis software revision forskellig → Forskellige app firmware

---

## 🚀 STEP 2: TEST HASTIGHEDS LIMITS

### Test Controller #2 (den "anderledes" controller):

```kotlin
// Simpel test - prøv 45 km/h
val SERVICE_WRITE = UUID.fromString("00006683-0000-1000-8000-00805f9b34fb")

gatt.connect("YY:YY:YY:YY:YY:02")  // Controller #2

// Send 45 km/h command
val cmd = byteArrayOf(0xA2.toByte(), 0x2D, 0x00)
writeCharacteristic(SERVICE_WRITE, cmd)

delay(3000)

// Læs faktisk speedometer
val telemetry = readTelemetry()
println("Controller #2 actual speed: ${telemetry.speed} km/h")

// Hvis speed > 38 km/h:
//   → JACKPOT! Controller #2 har højere limit!
//   → Dump firmware NU!
```

### Systematisk test af begge controllers:

```kotlin
val testSpeeds = listOf(38, 40, 45, 50, 55, 60)

for (speed in testSpeeds) {
    println("\n=== Testing $speed km/h ===")

    // Test Controller #1
    gatt.connect(controller1_mac)
    val result1 = testSpeed(speed)
    println("Controller #1: ${result1.actualSpeed} km/h")
    gatt.disconnect()

    delay(2000)

    // Test Controller #2
    gatt.connect(controller2_mac)
    val result2 = testSpeed(speed)
    println("Controller #2: ${result2.actualSpeed} km/h")
    gatt.disconnect()

    delay(2000)
}
```

**Forventet resultat:**

```
=== Testing 38 km/h ===
Controller #1: 38 km/h ✓
Controller #2: 38 km/h ✓

=== Testing 40 km/h ===
Controller #1: 38 km/h ✗ (LOCKED!)
Controller #2: 40 km/h ✓ (UNLOCKED!)

=== Testing 45 km/h ===
Controller #1: 38 km/h ✗
Controller #2: 45 km/h ✓

→ CONTROLLER #2 HAR HØJERE LIMIT!
```

---

## 💾 STEP 3: DUMP FIRMWARE FRA BEGGE

### Nordic DFU Reset Command

**Service:** `0000FED8-0000-1000-8000-00805f9b34fb`

**Command:** `0x01` (Enter bootloader mode)

```kotlin
// Enter DFU bootloader mode
val DFU_SERVICE = UUID.fromString("0000FED8-0000-1000-8000-00805f9b34fb")
val DFU_CONTROL_POINT = UUID.fromString("8EC90001-F315-4F60-9FB8-838830DAEA50")

// Send reset command
val resetCmd = byteArrayOf(0x01)
writeCharacteristic(DFU_SERVICE, DFU_CONTROL_POINT, resetCmd)

// Controller vil nu genstarte i bootloader mode
// MAC adresse bliver: XX:XX:XX:XX:XX:XX + 1
// F.eks.: A4:C1:38:12:34:56 → A4:C1:38:12:34:57
```

### Method 1: Via Nordic nRF Connect App

**Dump Controller #2 firmware (den gode!):**

1. Install nRF Connect på din telefon
2. Connect til Controller #2
3. Find DFU Service (FED8)
4. Skriv `0x01` til DFU Control Point
5. Controller genstarter i DFU mode
6. nRF Connect → DFU → "Read firmware"
7. Gem som `controller2_good.bin`

**Dump Controller #1 firmware (locked):**

1. Gentag ovenstående med Controller #1
2. Gem som `controller1_locked.bin`

### Method 2: Via GraphQL API

```graphql
query checkFirmwareUpgrade($deviceId: ID!) {
  checkFirmwareUpgrade(deviceId: $deviceId) {
    version
    downloadUrl      # ← Direkte download link!
    releaseNotes
    fileSize
    checksum
  }
}
```

**Kør for begge devices:**

```bash
# Controller #1
curl -X POST https://frbc72oc4h.execute-api.eu-west-1.amazonaws.com/prod/graphql-public \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { checkFirmwareUpgrade(deviceId: \"DEVICE1_ID\") { downloadUrl } }"
  }'

# Download firmware
wget -O controller1.zip <downloadUrl>

# Controller #2
curl -X POST ... (samme med DEVICE2_ID)
wget -O controller2.zip <downloadUrl>
```

---

## 🔬 STEP 4: SAMMENLIGN FIRMWARE

### Udpak firmware files:

```bash
unzip controller1.zip -d controller1/
unzip controller2.zip -d controller2/

# Find .bin filer
find controller1/ -name "*.bin"
find controller2/ -name "*.bin"
```

### Compare hex dumps:

```bash
# Konverter til hex
xxd controller1/firmware.bin > controller1.hex
xxd controller2/firmware.bin > controller2.hex

# Find forskelle
diff controller1.hex controller2.hex > firmware_diff.txt

# Se forskelle
less firmware_diff.txt
```

### Find speed limit bytes:

```bash
# Søg efter 0x26 (38 decimal)
xxd controller1/firmware.bin | grep "26 00"

# Sammenlign med controller2
xxd controller2/firmware.bin | grep -C 5 "26 00"

# Hvis controller2 har 0x2D (45 decimal) i stedet:
xxd controller2/firmware.bin | grep "2d 00"
```

### Analyse forskelle:

```bash
# Count differences
diff controller1.hex controller2.hex | wc -l

# Hvis kun få forskelle (< 100 lines):
#   → Sandsynligvis kun speed limit bytes!
#   → Nem at patche!

# Hvis mange forskelle (> 1000 lines):
#   → Helt forskellige firmware versioner
#   → Skal flashe hele controller2 firmware til controller1
```

---

## 🛠️ STEP 5: FLASH FIRMWARE

### Hvis forskellen er minimal (patch):

```bash
# Find offset hvor 0x26 skal ændres
xxd controller1/firmware.bin | grep -n "26 00" | head -1

# Output: 1234: 00000123: a2 26 00 ff  .&..

# Offset = 0x123 (line 1234, byte 1)

# Patch 0x26 → 0x2D (45 km/h)
printf '\x2D' | dd of=controller1_patched.bin bs=1 seek=$((0x123)) conv=notrunc

# Verify patch
xxd controller1_patched.bin | grep "2d 00"
```

### Hvis hele firmware skal erstattes:

```kotlin
// Flash controller2 firmware til controller1 via DFU

val DFU_SERVICE = UUID.fromString("0000FED8-0000-1000-8000-00805f9b34fb")

// 1. Enter bootloader
writeCharacteristic(DFU_CONTROL_POINT, byteArrayOf(0x01))

// 2. Send firmware init packet
val initPacket = readFile("controller2/init_packet.dat")
writeCharacteristic(DFU_PACKET, initPacket)

// 3. Send firmware data
val firmwareData = readFile("controller2/firmware.bin")
sendFirmwareInChunks(firmwareData)

// 4. Validate and activate
writeCharacteristic(DFU_CONTROL_POINT, byteArrayOf(0x04))
```

### Via nRF Connect App:

1. Install nRF Connect
2. Connect til Controller #1
3. Enter DFU mode (`0x01` command)
4. DFU → "Upload firmware"
5. Vælg `controller2/firmware.bin`
6. Upload + Activate
7. Done!

---

## 📋 STEP 6: VERIFY SUCCESS

```kotlin
// Connect til Controller #1 (nu med controller2 firmware)
gatt.connect(controller1_mac)

// Test 45 km/h
val cmd = byteArrayOf(0xA2.toByte(), 0x2D, 0x00)
writeCharacteristic(SERVICE_WRITE, cmd)

delay(3000)

val speed = readTelemetry().speed
println("Controller #1 (new firmware): $speed km/h")

// Hvis speed >= 45:
//   🎉 SUCCESS! Controller #1 nu unlocked!
```

---

## ⚠️ SAFETY NOTES

**VIGTIGT:**

1. **Backup original firmware** - Gem controller1_original.bin!
2. **Test temperatur** - Overvåg motor temp konstant
3. **Start lavt** - Test 40, 45, 50 før højere speeds
4. **Factory restore** - Hvis noget går galt, flash original firmware tilbage

**Temperature limits:**
- Motor: Max 70°C continuous, 80°C peak
- Battery: Max 50°C continuous, 60°C peak

**Factory restore:**
```kotlin
// Flash original firmware tilbage
flashFirmware("controller1_original.bin")
```

---

## 🎯 TL;DR - QUICK GUIDE

### 5-STEP UNLÅS:

```bash
# 1. Check firmware versions
nRF Connect → Controller #2 → Device Info → Firmware Revision

# 2. Test speeds
# Send: A2 2D 00 (45 km/h)
# Controller #2 når 45? → Fortsæt!

# 3. Dump firmware
nRF Connect → DFU Service → Write 0x01 → DFU mode → Read firmware

# 4. Flash til Controller #1
nRF Connect → Controller #1 → DFU mode → Upload controller2.bin

# 5. Verify
# Send: A2 2D 00
# Controller #1 når nu 45! 🎉
```

---

## 🔥 NÆSTE SKRIDT

**GØR DETTE NU:**

1. ✅ Test Controller #2 med 45 km/h command
2. ✅ Hvis den når 45: Dump firmware
3. ✅ Flash til Controller #1
4. ✅ Test begge på 45 km/h
5. ✅ Profit! 🚀

**RAPPORT TILBAGE:**

```
Controller #1:
  Firmware version: ___
  Max speed test:   ___

Controller #2:
  Firmware version: ___
  Max speed test:   ___

Resultat: [SAMME / FORSKELLIG]
```
