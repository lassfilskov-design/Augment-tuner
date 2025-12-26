# 🔍 FIND HEX STRUCTURE FOR SCOOTER #3

## PROBLEM: Scooter #3 bruger ANDEN HEX FORMAT!

**Hypoteser:**
1. **Byte order ændret** - Little/Big endian forskel
2. **Andre command codes** - Ikke 0xA1-A4 men 0xB1-B4?
3. **Ekstra bytes** - 4-byte format i stedet for 2-3 bytes?
4. **Checksum tilføjet** - CRC eller XOR checksum?
5. **Forskellige UUIDs** - Anden service UUID?

---

## 🧪 TEST 1: FIND HVILKE SERVICES ER TILGÆNGELIGE

### Via nRF Connect:

```
Scan Scooter #3:

Standard Augment services (Scooter #1 & #2):
✓/✗ 00006680 - Hovedkontrol
✓/✗ 00006681 - Sekundær
✓/✗ 00006682 - Ekstra
✓/✗ 00006683 - Settings Write ← VIGTIG!
✓/✗ 00006684 - Settings Read  ← VIGTIG!
✓/✗ 00006685 - Data sync
✓/✗ 00006687 - Features
✓/✗ 00006688 - Telemetry

Scooter #3 har måske ANDRE services:
? ________ - Ukendt service 1
? ________ - Ukendt service 2
? ________ - Ukendt service 3
```

**HVIS Scooter #3 har ANDRE UUIDs → helt anden protocol!**

---

## 🧪 TEST 2: READ CURRENT SETTINGS

### Test hvis 6684 findes:

```kotlin
// Prøv at læse nuværende indstillinger
val read = ble.read("00006684-0000-1000-8000-00805f9b34fb")

// Hvad kommer der tilbage?
// Scooter #1/2: [26 00 00 01 ...] (3-4 bytes)
// Scooter #3:    [?? ?? ?? ??]
```

**NOTER PRÆCIS HVAD DU FÅR TILBAGE!**

---

## 🧪 TEST 3: PRØV STANDARD FORMAT FØRST

### Test 1: Sport+ OFF (simpleste kommando)

```kotlin
// Standard format (Scooter #1/2):
val cmd = byteArrayOf(0xA3.toByte(), 0x00)

ble.write("00006683-0000-1000-8000-00805f9b34fb", cmd)

// Virker det?
// ✅ Success → Scooter accepterer standard format
// ❌ Error → Prøv andet format
```

**Hvis dette IKKE virker → Scooter #3 bruger andet format!**

---

## 🧪 TEST 4: PRØV ALTERNATIVE FORMATER

### Format A: Omvendt Byte Order

```kotlin
// Original: [0xA3, 0x00]
// Test:     [0x00, 0xA3]

val cmd = byteArrayOf(0x00, 0xA3.toByte())
ble.write("00006683-...", cmd)
```

### Format B: 4-Byte Format

```kotlin
// Original: [0xA3, 0x00]
// Test:     [0xA3, 0x00, 0x00, 0x00]

val cmd = byteArrayOf(0xA3.toByte(), 0x00, 0x00, 0x00)
ble.write("00006683-...", cmd)
```

### Format C: Med Checksum

```kotlin
// Original: [0xA3, 0x00]
// Test:     [0xA3, 0x00, checksum]

val checksum = (0xA3 + 0x00) and 0xFF
val cmd = byteArrayOf(0xA3.toByte(), 0x00, checksum.toByte())
ble.write("00006683-...", cmd)
```

### Format D: Med XOR Checksum

```kotlin
// Original: [0xA3, 0x00]
// Test:     [0xA3, 0x00, xor]

val xor = 0xA3 xor 0x00
val cmd = byteArrayOf(0xA3.toByte(), 0x00, xor.toByte())
ble.write("00006683-...", cmd)
```

### Format E: Andre Command Codes

```kotlin
// Måske ikke 0xA3 men 0xB3?
val cmd = byteArrayOf(0xB3.toByte(), 0x00)
ble.write("00006683-...", cmd)

// Eller 0x01A3 (16-bit)?
val cmd = byteArrayOf(0x01, 0xA3.toByte())
ble.write("00006683-...", cmd)
```

---

## 🧪 TEST 5: REVERSE ENGINEERING VIA READ

### Strategy:

**1. Forbind til Scooter #3 via original Augment app**

**2. Sæt Sport+ til ON i appen**

**3. Læs værdien tilbage via nRF Connect:**
```kotlin
val value = ble.read("00006684-...-9b34fb")
// Hvad står der?
// Sammenlign med Scooter #1/2
```

**4. Sæt Sport+ til OFF i appen**

**5. Læs igen:**
```kotlin
val value = ble.read("00006684-...-9b34fb")
// Hvad ændrede sig?
```

**FIND FORSKELLEN!**

```
Sport+ ON:  [A3 01] → Scooter #1/2
Sport+ ON:  [?? ??] → Scooter #3

Sport+ OFF: [A3 00] → Scooter #1/2
Sport+ OFF: [?? ??] → Scooter #3
```

---

## 🧪 TEST 6: SNIFF BLE TRAFFIC

### Method A: nRF Sniffer

**1. Installer nRF Sniffer:**
```bash
# Download fra Nordic Semi:
# https://www.nordicsemi.com/Products/Development-tools/nRF-Sniffer-for-Bluetooth-LE
```

**2. Start sniffer mens du connecter med original Augment app**

**3. Capture BLE packets når du ændrer settings**

**4. Analyser hex data:**
```
Packet: Write Request
Handle: 0x003C (service 6683)
Value:  [A3 01]  ← DETTE ER FORMATET!
```

### Method B: Android BLE Log

**1. Enable Bluetooth HCI snoop på Android:**
```
Settings → Developer Options → Enable Bluetooth HCI snoop log
```

**2. Connect med Augment app og ændr settings**

**3. Hent log:**
```bash
adb bugreport
```

**4. Åbn .btsnoop filen i Wireshark:**
```bash
wireshark btsnoop_hci.log
```

**5. Filter på GATT writes:**
```
Wireshark filter: btatt.opcode == 0x12
```

---

## 📊 SYSTEMATISK TEST MATRIX

Test alle kombinationer:

```
Command   | Format           | Scooter #1/2 | Scooter #3
----------|------------------|--------------|------------
Sport+ ON | [A3 01]          | ✅           | ❓
          | [01 A3]          | ❌           | ❓
          | [A3 01 00 00]    | ❌           | ❓
          | [A3 01 A2]       | ❌           | ❓
          | [B3 01]          | ❌           | ❓
          | [01 00 00 A3]    | ❌           | ❓
----------|------------------|--------------|------------
Speed 40  | [A2 28 00]       | ✅           | ❓
          | [00 28 A2]       | ❌           | ❓
          | [A2 00 28]       | ❌           | ❓
          | [28 00 A2]       | ❌           | ❓
```

**TEST HVER KOMBINATION OG NOTER RESULTAT!**

---

## 🔍 EXPECTED PATTERNS

### Pattern 1: Little-Endian vs Big-Endian

```
Standard:        [cmd, value_low, value_high]
Scooter #3 maybe: [cmd, value_high, value_low]

Eksempel Speed 40 km/h (0x28):
Standard:    [A2 28 00]
Scooter #3?: [A2 00 28]  ← Omvendt byte order
```

### Pattern 2: CRC8 Checksum

```
Standard:        [cmd, value]
Scooter #3 maybe: [cmd, value, checksum]

Eksempel Sport+ ON:
Standard:    [A3 01]
Scooter #3?: [A3 01 A2]  ← CRC8(A3 + 01) = A2
```

### Pattern 3: Length Prefix

```
Standard:        [cmd, value]
Scooter #3 maybe: [length, cmd, value]

Eksempel Sport+ ON:
Standard:    [A3 01]
Scooter #3?: [02 A3 01]  ← Length = 2 bytes
```

### Pattern 4: Fixed Header

```
Standard:        [cmd, value]
Scooter #3 maybe: [header, cmd, value]

Eksempel Sport+ ON:
Standard:    [A3 01]
Scooter #3?: [FF A3 01]  ← Header = 0xFF
```

---

## 🎯 QUICK TEST SCRIPT

```kotlin
// Test alle formater hurtigt:

val formats = listOf(
    byteArrayOf(0xA3, 0x01),              // Standard
    byteArrayOf(0x01, 0xA3),              // Reversed
    byteArrayOf(0xA3, 0x01, 0x00, 0x00),  // 4-byte
    byteArrayOf(0xA3, 0x01, 0xA2),        // + Checksum
    byteArrayOf(0x02, 0xA3, 0x01),        // + Length
    byteArrayOf(0xFF, 0xA3, 0x01),        // + Header
    byteArrayOf(0xB3, 0x01),              // Alt command
    byteArrayOf(0x00, 0xA3, 0x01, 0x00),  // Padded
)

formats.forEachIndexed { index, cmd ->
    println("Testing format $index: ${cmd.joinToString(" ") { "%02X".format(it) }}")

    try {
        ble.write(SERVICE_UUID, cmd)
        Thread.sleep(100)
        val read = ble.read(READ_UUID)
        println("✅ Success! Read back: ${read.joinToString(" ") { "%02X".format(it) }}")
    } catch (e: Exception) {
        println("❌ Failed: ${e.message}")
    }

    Thread.sleep(500)
}
```

---

## 📝 RESULTAT TRACKING

**Udfyld denne tabel:**

```
Dato: __________
Scooter #3 MAC: __________
Firmware version: __________

Test 1 - Standard [A3 01]:
Result: ☐ Success ☐ Failed
Error: _______________________

Test 2 - Reversed [01 A3]:
Result: ☐ Success ☐ Failed
Error: _______________________

Test 3 - 4-byte [A3 01 00 00]:
Result: ☐ Success ☐ Failed
Error: _______________________

[etc...]

WINNING FORMAT:
[___ ___ ___ ___]

NOTES:
_________________________________
_________________________________
```

---

## 🚀 NÆSTE SKRIDT

**1. Test basic read først:**
```
Kan du læse fra service 6684?
Ja/Nej: ____
Hvis ja, hvad får du: [___ ___ ___ ___]
```

**2. Test standard format:**
```
Virker [A3 01]?
Ja/Nej: ____
Hvis nej, hvad er fejlen: _______________
```

**3. Hvis standard IKKE virker:**
```
→ Kør systematic test matrix ovenfor
→ Noter ALLE resultater
→ Find det format der virker!
```

**KOM TILBAGE MED RESULTATERNE! 🔍**
