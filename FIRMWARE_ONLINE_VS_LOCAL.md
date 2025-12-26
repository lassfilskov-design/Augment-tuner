# AUGMENT - ONLINE VS LOCAL FIRMWARE

## 🌐 DU OPDAGEDE: TO FIRMWARE KILDER

```
┌──────────────────────────────────────┐
│   ONLINE FIRMWARE                    │
│   ↓                                  │
│   Augment Backend (GraphQL)          │
│   - AWS S3 storage                   │
│   - Firmware version API             │
│   - Speed limit settings             │
└──────────────────────────────────────┘
           ↓ Download via app
           ↓
┌──────────────────────────────────────┐
│   LOCAL FIRMWARE                     │
│   ↓                                  │
│   Controller (nRF52 chip)            │
│   - Flashed firmware.bin             │
│   - Hardcoded limits                 │
│   - Actual motor control             │
└──────────────────────────────────────┘
```

---

## 📊 FORSKELLEN

### ONLINE FIRMWARE (Augment Backend)

**Lokation:**
```
AWS S3 bucket via GraphQL
https://frbc72oc4h.execute-api.eu-west-1.amazonaws.com/prod/graphql-public
```

**Indhold:**
- Firmware binaries (.bin, .zip)
- Firmware version metadata
- Speed limit settings per region/device
- Feature flags (Sport+, Turbo, etc)

**GraphQL queries:**
```graphql
query checkFirmwareUpgrade($deviceId: ID!) {
  checkFirmwareUpgrade(deviceId: $deviceId) {
    version
    downloadUrl
    speedLimitKmhMaster
    features
  }
}
```

**Properties:**
- ✓ Kan ændres af Augment
- ✓ Region-specific (EU vs US)
- ✓ Device-specific settings
- ✗ Kræver internet for at hente

---

### LOCAL FIRMWARE (På Controlleren)

**Lokation:**
```
nRF52 Flash memory
Flashed via Nordic DFU
```

**Indhold:**
- Compiled firmware binary
- Hardcoded speed limits
- Motor control algorithms
- BLE GATT services

**Properties:**
- ✓ Virker offline
- ✓ Kan modificeres direkte
- ✗ Kræver DFU flash for at ændre
- ✗ Kan "bricke" controller ved fejl

---

## 🔥 HVORFOR DET ER VIGTIGT

### Problem: Mismatch mellem online og local

**Scenario A: Online limit > Local limit**
```
Online firmware: speedLimitKmhMaster = 45 km/h
Local firmware:  Hardcoded MAX_SPEED = 38 km/h

→ App viser 45 km/h som max
→ Men controller ignorerer over 38 km/h
→ DU ER SANDSYNLIGVIS HER! ← 38 km/h locked
```

**Scenario B: Online limit < Local limit**
```
Online firmware: speedLimitKmhMaster = 25 km/h (EU legal)
Local firmware:  Capable of 60 km/h

→ App begrænser til 25 km/h
→ Men controller KAN køre 60 km/h
→ Custom app kan unlåse!
```

---

## 🎯 CONTROLLER #1 VS #2 FORKLARING

**Controller #1:**
```
Online: speedLimitKmhMaster = 39 km/h (?)
Local:  Hardcoded limit = 38 km/h ← LOCKED!

→ Uanset BLE command, stuck at 38 km/h
```

**Controller #2:**
```
Online: speedLimitKmhMaster = 45 km/h (?)
Local:  Hardcoded limit = 60 km/h (?)

→ Kan nå højere hastigheder!
→ "mere alt ædende" controller
```

---

## 🚀 HVORDAN TJEKKE FORSKELLEN

### 1. Check ONLINE firmware settings

```graphql
query GetDevice($deviceId: ID!) {
  device(id: $deviceId) {
    id
    serialNumber
    firmwareVersion
    speedLimitKmhMaster
    speedLimitKmhCurrent
  }
}
```

**Kør for begge controllers:**

```javascript
// Controller #1
{
  deviceId: "controller_1_id",
  serialNumber: "SERIAL_1"
}

// Controller #2
{
  deviceId: "controller_2_id",
  serialNumber: "SERIAL_2"
}
```

**Sammenlign:**
- `speedLimitKmhMaster` - Backend's max limit
- `speedLimitKmhCurrent` - Nuværende aktiv limit
- `firmwareVersion` - Firmware version

---

### 2. Check LOCAL firmware limits

**Test direkte via BLE (UDEN app):**

```kotlin
// Connect direkte til controller (ingen Augment app)
gatt.connect(controller_mac)

// Send 60 km/h command
val cmd = byteArrayOf(0xA2.toByte(), 0x3C, 0x00)
gatt.writeCharacteristic(service_6683, cmd)

// Læs faktisk hastighed
delay(3000)
val actualSpeed = readTelemetry()

println("Local firmware max: $actualSpeed km/h")
```

**Dette bypasser online limits!**

---

## 💡 RESET COMMAND FORMÅL

**Nu giver det mening!**

**Reset command bruges til at:**

1. **Sync online → local**
   ```
   Download online firmware settings
   Flash til local controller
   ```

2. **Factory reset local**
   ```
   Nulstil local firmware til factory defaults
   Fjern custom mods
   ```

3. **Update firmware**
   ```
   Download nyeste firmware fra backend
   Flash via DFU
   ```

**GraphQL ResetDeviceOwnership:**
```graphql
mutation ResetDeviceOwnership($input: ResetDeviceOwnershipInput!) {
  resetDeviceOwnership(input: $input) {
    id
    speedLimitKmhMaster    # ← Online setting
    speedLimitKmhCurrent   # ← Local setting
  }
}
```

**Dette kan:**
- Reset local til match online
- Download fresh firmware
- Clear device pairing

---

## 🔬 DOWNLOAD FIRMWARE FRA BEGGE KILDER

### Download ONLINE firmware

```javascript
async function downloadOnlineFirmware(deviceId, authToken) {
  const query = `
    query checkFirmwareUpgrade($deviceId: ID!) {
      checkFirmwareUpgrade(deviceId: $deviceId) {
        version
        downloadUrl
        releaseNotes
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
        variables: { deviceId }
      })
    }
  );

  const data = await response.json();
  const firmwareUrl = data.data.checkFirmwareUpgrade.downloadUrl;

  // Download .bin or .zip
  const firmwareBlob = await fetch(firmwareUrl);
  return await firmwareBlob.arrayBuffer();
}
```

---

### Download LOCAL firmware (dump fra controller)

**Via Nordic DFU:**

1. **Enter DFU bootloader mode:**
   ```kotlin
   val dfu_service = UUID.fromString("0000FED8-0000-1000-8000-00805f9b34fb")
   val cmd = byteArrayOf(0x01) // Jump to bootloader
   gatt.writeCharacteristic(dfu_service, cmd)
   ```

2. **Use Nordic nRF Connect:**
   ```
   - Connect til controller
   - DFU → "Read firmware"
   - Save as controller_local.bin
   ```

3. **Eller custom read:**
   ```kotlin
   // Read firmware via DFU packets
   // This is complex - Nordic DFU protocol required
   ```

---

## 🧪 SIMPEL TEST (3 STEPS)

### Step 1: Check online limits

```bash
curl -X POST https://frbc72oc4h.execute-api.eu-west-1.amazonaws.com/prod/graphql-public \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query { device(id: \"DEVICE_ID\") { speedLimitKmhMaster speedLimitKmhCurrent } }"
  }'
```

**Noter:**
- Controller #1 online limit: `___` km/h
- Controller #2 online limit: `___` km/h

---

### Step 2: Test local limits (bypass online)

**Disconnect internet!**

```kotlin
// Slå WiFi og mobil data FRA
// Connect kun via BLE

gatt.connect(controller_mac)

// Send 50 km/h
writeCharacteristic(0xA2, 0x32, 0x00)

// Hvad sker der?
```

**Noter:**
- Controller #1 local max: `___` km/h
- Controller #2 local max: `___` km/h

---

### Step 3: Sammenlign

```
Controller #1:
  Online:  ___ km/h
  Local:   ___ km/h
  Locked:  [YES/NO]

Controller #2:
  Online:  ___ km/h
  Local:   ___ km/h
  Locked:  [YES/NO]
```

---

## 🎯 STRATEGI BASERET PÅ RESULTAT

### Resultat A: Online high, Local locked at 38

**Løsning:**
```
1. Download online firmware (højere limit)
2. Flash til local controller via DFU
3. Done!
```

### Resultat B: Online locked, Local capable

**Løsning:**
```
1. Custom app uden online check
2. Send BLE commands direkte
3. Bypass online limits
```

### Resultat C: Begge locked

**Løsning:**
```
1. Download local firmware (dump via DFU)
2. Hex edit: Find 0x26 (38), patch til 0x32 (50)
3. Flash modified firmware tilbage
```

### Resultat D: Controller #2 unlocked

**Løsning:**
```
1. Dump firmware fra Controller #2
2. Flash til Controller #1
3. Begge controllers unlocked!
```

---

## ⚡ GØR DETTE NU

**Simpel 2-minute test:**

```kotlin
// 1. Check online limit (med internet)
val onlineLimit = queryGraphQL("device { speedLimitKmhMaster }")
println("Online limit: $onlineLimit km/h")

// 2. Slå internet FRA

// 3. Check local limit (uden internet)
gatt.connect(controller)
sendSpeedCommand(60)  // 0xA2 0x3C 0x00
val localSpeed = readActualSpeed()
println("Local achieved: $localSpeed km/h")

// 4. Sammenlign
if (localSpeed > 38) {
  println("🎉 Local firmware UNLOCKED!")
  println("→ Online limit holder dig tilbage")
  println("→ Brug custom app uden online check")
} else {
  println("🔒 Local firmware LOCKED at 38 km/h")
  println("→ Skal modificere local firmware")
  println("→ Eller flash fra Controller #2")
}
```

---

**RAPPORT TILBAGE MED RESULTATER! 🔥**

Online limit Controller #1: `___` km/h
Local max Controller #1:    `___` km/h

Online limit Controller #2: `___` km/h
Local max Controller #2:    `___` km/h
