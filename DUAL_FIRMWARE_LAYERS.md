# AUGMENT - DUAL FIRMWARE ARCHITECTURE

## 🔬 DU OPDAGEDE: 2 LAG FIRMWARE

**Dette forklarer ALT!**

```
┌─────────────────────────────────┐
│   LAYER 1: BOOTLOADER           │  ← Låst? Unlocked?
│   - Nordic DFU                  │
│   - Speed limits?               │
│   - Service: FED7, FED8         │
└─────────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│   LAYER 2: APPLICATION          │  ← Låst på 38 km/h?
│   - BLE commands (0xA2, etc)    │
│   - Services: 6683, 6684, 6688  │
│   - GraphQL connection          │
└─────────────────────────────────┘
```

---

## 📊 MULIGE SCENARIER

### Scenario 1: Bootloader locked, App unlocked
```
Bootloader firmware: Kan ikke ændres
Application firmware: Kan ændres via BLE commands

→ BLE speed commands virker (0xA2)
→ Men bootloader begrænser max speed
→ DU ER HER SANDSYNLIGVIS ← 38 km/h limit!
```

### Scenario 2: Bootloader unlocked, App locked
```
Bootloader firmware: Kan moddes
Application firmware: Låst på 38 km/h

→ Skal flashe modified bootloader
→ Bootloader override app limits
```

### Scenario 3: Begge locked
```
Begge lag er låst

→ Skal flashe modified firmware på begge lag
```

---

## 🎯 HVORFOR CONTROLLER #2 ER ANDERLEDES

**Din Controller #2 har sandsynligvis:**

- Ældre bootloader version (mere permissiv)
- Nyere bootloader version (unlocked)
- Andet hardware revision
- **Test dette:**

```kotlin
// Check firmware version
service 0000180A  // Device Information
characteristic: Firmware Revision String

// Check bootloader version
service 0000FED7  // DFU Service
// Bootloader info i DFU packet
```

---

## 🔧 SIMPEL TEST (UDEN BLOATWARE)

```kotlin
// CONTROLLER #1
gatt.connect("XX:XX:XX:XX:XX:01")
send(0xA2, 0x32, 0x00)  // 50 km/h
wait(3 sek)
read_speed()
// → Forventet: stuck at 38 km/h

// CONTROLLER #2
gatt.connect("YY:YY:YY:YY:YY:02")
send(0xA2, 0x32, 0x00)  // 50 km/h
wait(3 sek)
read_speed()
// → Forventet: ???
```

**Hvis Controller #2 når 50 km/h:**
→ Den har unlocked bootloader ELLER unlocked app firmware!

---

## 📖 FIRMWARE LAYERS I DETALJE

### Layer 1: Bootloader (Nordic nRF52)

**Location:** Flash 0x00000000 - 0x0000FFFF (første 64KB)

**Ansvar:**
- DFU (Device Firmware Update)
- Firmware validation
- Speed limit enforcement? ← MÅSKE!
- Hardware protection

**Services:**
```
0000FED7    DFU Service
0000FED8    DFU Control Point
```

**Commands:**
```
0x01        Start DFU (enter bootloader mode)
0x02        Receive firmware init packet
0x03        Receive firmware data
0x05        Validate and activate
```

**Kan indeholde:**
- Master speed limit (override app commands)
- Hardware protection limits
- Regional restrictions (EU/US/etc)

---

### Layer 2: Application Firmware

**Location:** Flash 0x00010000 - 0x000XXXXX (resten)

**Ansvar:**
- BLE GATT services
- Speed commands (0xA2)
- Motor control
- Telemetry
- GraphQL kommunikation

**Services:**
```
00006680-88    Custom services
0000180A       Device Information
0000d101       Extra features
```

**Commands:**
```
0xA1    Zero Start
0xA2    Speed Limit  ← Dette niveau!
0xA3    Sport+
0xA4    Turbo
```

**App firmware sender:**
→ Speed command til motor controller
→ Men bootloader kan override!

---

## 🚀 NÆSTE SKRIDT

### 1. CHECK FIRMWARE VERSIONS

**Controller #1:**
```kotlin
val firmwareRev = readCharacteristic(
    service = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb"),
    characteristic = "Firmware Revision String"
)
println("Controller #1 firmware: $firmwareRev")
```

**Controller #2:**
```kotlin
val firmwareRev = readCharacteristic(
    service = UUID.fromString("0000180A-0000-1000-8000-00805f9b34fb"),
    characteristic = "Firmware Revision String"
)
println("Controller #2 firmware: $firmwareRev")
```

**Sammenlign:**
- Hvis forskellig version → forklarer limit forskelle
- Hvis samme version → hardware revision forskelle

---

### 2. TEST BOOTLOADER LIMITS

**Send DFU command til begge:**

```kotlin
// Enter bootloader mode
service = UUID.fromString("0000FED8-0000-1000-8000-00805f9b34fb")
command = byteArrayOf(0x01) // Jump to bootloader

// Hvis den accepterer:
→ Bootloader er accessible
→ Kan læse bootloader firmware
→ Kan check for hardcoded limits
```

---

### 3. DUMP BEGGE LAG (Fra Controller #2)

Hvis Controller #2 tillader højere speeds:

**Bootloader dump:**
```
1. Enter DFU mode (command 0x01)
2. Nordic nRF Connect app
3. DFU → Read firmware
4. Save bootloader.bin
```

**Application dump:**
```
1. GraphQL query firmware URL
2. Download .zip
3. Extract app_firmware.bin
```

**Sammenlign med Controller #1:**
→ Find bytes der er forskellige
→ Patch Controller #1 firmware
→ Flash til Controller #1

---

## 💡 TEORI: HVORFOR 38 KM/H?

**38 km/h = 0x26 i hex**

**Mulige steder denne værdi er hardcoded:**

### I Bootloader:
```c
#define MAX_SPEED_KMH 38
```

### I Application:
```c
if (speed_command > MAX_SPEED_KMH) {
    speed_command = MAX_SPEED_KMH;  // Cap at 38
}
```

### I Motor Controller:
```
Motor firmware har egen 38 km/h limit
```

---

## ⚡ QUICK TEST (GØR DETTE FØRST!)

```kotlin
// Connect til Controller #2
gatt.connect(controller2_mac)

// Send 60 km/h command
val cmd = byteArrayOf(0xA2.toByte(), 0x3C, 0x00)
gatt.writeCharacteristic(cmd)

delay(3000)

// Læs speedometer
val speed = gatt.readCharacteristic(telemetry)
println("Controller #2 actual speed: $speed km/h")

// Hvis speed > 38:
//   → Controller #2 har unlocked firmware!
//   → Dump firmware fra Controller #2
//   → Flash til Controller #1
//   → DONE!
```

---

## 🔬 ADVANCED: FIND LIMIT I FIRMWARE

Hvis du dumper firmware:

```bash
# Søg efter 0x26 (38 decimal)
xxd firmware.bin | grep "0026"

# Søg efter speed validation kode
strings firmware.bin | grep -i "speed\|limit\|max"

# Find alle steder 38 forekommer
xxd firmware.bin | awk '/26 00/ {print NR": "$0}'
```

**Patch:**
```bash
# Hvis du finder: "26 00" på offset 0x1234
# Patch til 50 km/h (0x32):
echo "32 00" | xxd -r -p - | dd of=firmware_patched.bin bs=1 seek=$((0x1234)) conv=notrunc
```

---

## ⚠️ KRITISK SPØRGSMÅL

**Hvad skete der på Controller #2?**

1. Send 50 km/h command (0xA2 0x32 0x00)
2. Hvad viser speedometer?
3. Går den over 38 km/h?

**Hvis JA:**
→ Controller #2 firmware er guld!
→ Dump det
→ Flash til Controller #1
→ Profit! 🚀

**Hvis NEJ:**
→ Begge controllers samme limit
→ Skal modificere firmware manually
→ Find og patch 0x26 bytes

---

**TEST CONTROLLER #2 NU! 🔥**

Simpel test uden bloatware:
1. Connect BLE
2. Send: A2 32 00
3. Se speedometer
4. Rapport tilbage!
