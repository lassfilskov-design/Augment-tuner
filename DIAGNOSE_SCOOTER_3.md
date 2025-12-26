# 🔍 SCOOTER #3 DIAGNOSTIK

## PROBLEM: Accepterer ikke samme kommandoer!

**Symptomer:**
- ✅ Scooter #1 og #2 accepterer kommandoer
- ❌ Scooter #3 accepterer MEGET FÅ kommandoer
- ❓ Hvilke kommandoer virker?

---

## 🧪 STEP 1: CHECK FIRMWARE VERSION

### Method A: Via BLE (Device Information Service)

```bash
# Connect via nRF Connect og læs:
Service: 0000180A (Device Information)
├─ Manufacturer Name:   [READ]
├─ Model Number:        [READ]
├─ Serial Number:       [READ]
├─ Hardware Revision:   [READ]
├─ Firmware Revision:   [READ] ← VIGTIG!
└─ Software Revision:   [READ]
```

### Method B: Via GraphQL

```graphql
query GetDeviceInfo($deviceId: ID!) {
  device(id: $deviceId) {
    id
    serialNumber
    firmwareVersion        # ← Check denne!
    hardwareRevision
    controllerType
    manufacturingDate
  }
}
```

---

## 🧪 STEP 2: TEST HVILKE SERVICES DER FINDES

**nRF Connect scan:**

```
Standard Augment Services:
✓ 6680 - Hovedkontrol
✓ 6681 - Sekundær
✓ 6682 - Ekstra
✓ 6683 - Settings Write    ← Findes denne?
✓ 6684 - Settings Read      ← Findes denne?
✓ 6685 - Data sync
✓ 6687 - Features
✓ 6688 - Telemetry
✓ 180A - Device Info
✓ FED7/FED8 - DFU           ← Findes disse?
```

**Hvis Scooter #3 MANGLER services → anden controller!**

---

## 🧪 STEP 3: TEST HVILKE KOMMANDOER VIRKER

### Test systematisk:

```kotlin
// Test 1: Read current settings
val read = ble.read("00006684-...-9b34fb")
// Hvis dette fejler → controller er locked

// Test 2: Simpleste write (Sport+ OFF)
val cmd1 = byteArrayOf(0xA3, 0x00)
ble.write("00006683-...-9b34fb", cmd1)
// Virker dette?

// Test 3: Speed limit (samme som nu)
val cmd2 = byteArrayOf(0xA2, 0x26, 0x00)  // 38 km/h
ble.write("00006683-...-9b34fb", cmd2)
// Virker dette?

// Test 4: Zero Start OFF
val cmd3 = byteArrayOf(0xA1, 0x00, 0x00)
ble.write("00006683-...-9b34fb", cmd3)
// Virker dette?
```

**Resultat:**
```
Kommando    | Scooter #1 | Scooter #2 | Scooter #3
----------- | ---------- | ---------- | ----------
0xA1 (Zero) | ✅         | ✅         | ❓
0xA2 (Speed)| ✅         | ✅         | ❓
0xA3 (Sport)| ✅         | ✅         | ❓
0xA4 (Turbo)| ✅         | ✅         | ❓
```

---

## 🧪 STEP 4: MULIGE ÅRSAGER

### Årsag 1: Locked Firmware
**Symptomer:**
- Kan læse services
- Kan ikke skrive til 6683
- Får BLE error: "Write not permitted"

**Løsning:**
```bash
# Send unlock command først
# Via GraphQL eller deep link:
augment://unlock
```

### Årsag 2: Anden Controller Type
**Symptomer:**
- Forskellige services
- Andre UUIDs
- Andre kommando formater

**Check:**
```bash
# Sammenlign MAC addresses:
Scooter #1: XX:XX:XX:XX:XX:01
Scooter #2: XX:XX:XX:XX:XX:02
Scooter #3: XX:XX:XX:XX:XX:03  ← Andet prefix?

# Andet MAC prefix → anden leverandør!
```

### Årsag 3: Faulty Controller
**Symptomer:**
- Random BLE disconnects
- Write succeeds men ingen effekt
- Nogle kommandoer virker, andre ikke

**Test:**
```bash
# Se om controller responderer korrekt:
# 1. Send 0xA3 0x00 (Sport+ OFF)
# 2. Læs tilbage fra 6684
# 3. Hvis værdi ikke matcher → faulty!
```

### Årsag 4: Firmware Guard/Protection
**Symptomer:**
- Kun "safe" kommandoer accepteres
- 0xA2 (speed) DENIED
- 0xA3/A4 (acceleration) DENIED
- Kun 0xA1 (zero start) tilladt?

**Muligt:**
```
Nyere firmware har command whitelist:
✅ 0xA1 - Zero Start (sikker)
✅ 0xB1 - Lock (sikker)
✅ 0xC1 - Wheel size (sikker)
❌ 0xA2 - Speed (farlig!)
❌ 0xA3 - Sport+ (farlig!)
❌ 0xA4 - Turbo (farlig!)
```

---

## 🔧 STEP 5: WORKAROUNDS

### Workaround 1: Downgrade Firmware
```bash
# Hvis Scooter #3 har nyere firmware:
# 1. Find firmware fra Scooter #1 eller #2
# 2. Flash til Scooter #3 via DFU
# Se: DFU_RESET_COMMAND.md
```

### Workaround 2: Backend Unlock
```graphql
mutation UnlockDevice($deviceId: ID!) {
  updateDevice(input: {
    id: $deviceId
    speedLimitKmhMaster: 45
    allowCustomSettings: true
  }) {
    id
  }
}
```

### Workaround 3: Swap Controller
```bash
# Hvis Scooter #3 controller er defekt:
# → Tag controller fra Scooter #1 eller #2
# → Installer i Scooter #3
# → Test
```

---

## 📊 NÆSTE SKRIDT

**1. Indsaml info om Scooter #3:**
```bash
# Via nRF Connect:
- MAC address: ___________________
- Services: ______________________
- Firmware version: ______________
- Manufacturer: __________________
```

**2. Test HVILKE kommandoer virker:**
```
Kommando virker:
☐ 0xA1 (Zero Start)
☐ 0xA2 (Speed)
☐ 0xA3 (Sport+)
☐ 0xA4 (Turbo)
☐ 0xB1 (Lock)
☐ 0xC1 (Wheel)

Ingen virker → helt locked/faulty
```

**3. BLE error message:**
```
Hvad er den præcise fejl når du sender kommando?
- "Write not permitted"
- "Insufficient authentication"
- "Invalid handle"
- Ingen fejl, men ingen effekt
- Disconnect
```

---

## 💡 HVIS SCOOTER #3 ER DEFEKT

**Symptomer på faulty controller:**
- ✅ Kan scanne BLE
- ✅ Kan connecte
- ✅ Kan læse services
- ❌ Kan IKKE skrive (eller ingen effekt)
- ❌ Random disconnects
- ❌ Motor responderer ikke på gas

**→ RETURNER TIL AUGMENT!**

**Bevis:**
```bash
# Tag screenshots fra nRF Connect der viser:
1. Services found
2. Write attempt
3. Error message
4. Comparison med working scooter
```

---

## 🚨 HVAD SKAL VI TJEKKE NU?

**Svar disse spørgsmål:**

1. **Hvad er MAC på Scooter #3?**
   ```
   MAC: ___________________
   ```

2. **Kan du scanne services via nRF Connect?**
   ```
   ☐ Ja - hvilke services?
   ☐ Nej - kan ikke connecte
   ```

3. **Hvad sker der når du sender 0xA3 0x00?**
   ```
   ☐ Write success, ingen effekt
   ☐ Write fails med fejl: __________
   ☐ BLE disconnect
   ☐ Andet: _____________________
   ```

4. **Er Scooter #3 ny eller brugt?**
   ```
   ☐ Ny (måske anden hardware revision)
   ☐ Brugt (måske firmware upgraded)
   ```

5. **Kan motoren køre normalt på Scooter #3?**
   ```
   ☐ Ja - motor virker fint
   ☐ Nej - motor virker ikke
   ☐ Delvis - sporadisk problem
   ```

**GIV MIG DISSE SVAR, SÅ KAN VI DIAGNOSTICERE! 🔍**
