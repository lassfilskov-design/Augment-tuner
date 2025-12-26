# AUGMENT SCOOTER - RESET & UNLOCK GUIDE

## 🔓 UNLOCK DEEP LINK

```
augment://unlock
```

**Brug:** Dette er sandsynligvis en deep link til at åbne unlock funktioner i appen.

**Test det:**
```bash
# På Android via ADB
adb shell am start -a android.intent.action.VIEW -d "augment://unlock"
```

---

## 🔄 DEVICE RESET COMMANDS

### 1. GraphQL Reset (Backend)

**Mutation:**
```graphql
mutation ResetDeviceOwnership($input: ResetDeviceOwnershipInput!) {
  resetDeviceOwnership(input: $input) {
    id
    name
    serialNumber
    firmwareVersion
    speedLimitKmhMaster
    speedLimitKmhCurrent
  }
}
```

**Input Format:**
```json
{
  "input": {
    "deviceId": "YOUR_DEVICE_ID",
    "serialNumber": "YOUR_SERIAL_NUMBER"
  }
}
```

**Endpoint:**
```
POST https://frbc72oc4h.execute-api.eu-west-1.amazonaws.com/prod/graphql-public
```

**Headers:**
```
Content-Type: application/json
Authorization: Bearer YOUR_JWT_TOKEN
```

---

### 2. Device Ownership Commands

**Claim Device:**
```graphql
mutation ClaimDeviceOwnership($input: ClaimDeviceOwnershipInput!) {
  claimDeviceOwnership(input: $input) {
    id
    ownerId
  }
}
```

---

## 🚀 FIRMWARE LOCK ANALYSE

### Problem: 38 km/h Firmware Lock

**Du rapporterede:**
- Speedometer viser 39 km/h 2 sek efter gas
- Motor "hvisker" den kan mere - rammer begrænseren
- **Firmware locked på 38 km/h** (ikke 39!)

### Test Firmware Limit

Prøv disse BLE kommandoer for at finde firmware loftet:

```kotlin
// Test 40 km/h
byteArrayOf(0xA2, 0x28, 0x00)

// Test 45 km/h (Augment M+ standard)
byteArrayOf(0xA2, 0x2D, 0x00)

// Test 50 km/h
byteArrayOf(0xA2, 0x32, 0x00)

// Test 60 km/h
byteArrayOf(0xA2, 0x3C, 0x00)

// Test NO LIMIT
byteArrayOf(0xA2, 0xFF, 0xFF)
```

**Forventet resultat:**
- Hvis firmware låst på 38 km/h → ingen ændring
- Hvis firmware tillader højere → speedometer stiger

---

## 🔧 LØSNINGER

### Option 1: BLE Reset (Hvis eksisterer)

⚠️ **IKKE FUNDET ENDNU** - Ingen 0xE* eller 0xF* BLE reset kommando fundet i APK.

Mulige command codes (ikke bekræftet):
```
0xE1 ?? ??    Factory Reset (?)
0xF1 ?? ??    Firmware Reset (?)
```

### Option 2: GraphQL Reset

```javascript
// I din custom app
async function resetDevice(deviceId, serialNumber, authToken) {
  const query = `
    mutation ResetDeviceOwnership($input: ResetDeviceOwnershipInput!) {
      resetDeviceOwnership(input: $input) {
        id
        speedLimitKmhMaster
        speedLimitKmhCurrent
      }
    }
  `;

  const variables = {
    input: {
      deviceId: deviceId,
      serialNumber: serialNumber
    }
  };

  const response = await fetch(
    'https://frbc72oc4h.execute-api.eu-west-1.amazonaws.com/prod/graphql-public',
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${authToken}`
      },
      body: JSON.stringify({ query, variables })
    }
  );

  return await response.json();
}
```

### Option 3: Firmware Mod (Se FIRMWARE_MODDING_GUIDE.md)

Hvis firmware er låst på 38 km/h, skal du:

1. **Download firmware** fra Augment backend
2. **Find speed limit bytes** i firmware binær
3. **Patch bytes** til højere værdi
4. **Flash modified firmware** via Nordic DFU

**Hex patterns at søge efter:**
```
26 00      # 38 decimal = 0x26
27 00      # 39 decimal = 0x27
```

---

## 📊 KOMPLET COMMAND OVERSIGT

### BLE Commands (Bekræftet)
```
0xA1 [enable] [max_kmh]              Zero Start
0xA2 [speed_kmh] 0x00                Speed Limit
0xA3 [enable]                        Sport+ Mode
0xA4 [enable]                        Electronic Turbo
0xB1 [enable] [delay_h] [delay_l]    Auto Lock
0xC1 [diameter_l] [diameter_h]       Wheel Size
0xD1 [enable] [hold_time]            Cruise Control
```

### GraphQL Mutations
```graphql
ResetDeviceOwnership        # Reset device til factory
ClaimDeviceOwnership        # Claim en device
checkFirmwareUpgrade        # Check for firmware updates
```

### Deep Links
```
augment://unlock            # Unlock features
```

---

## ⚙️ DIN CONTROLLER

Du nævnte: **"måske jeg skulle prøve på min controller den er spdan lidt mere alt ædende xD"**

**Hvis din controller er mere kraftfuld:**
- Den kan have højere firmware limit
- Test samme BLE commands
- Sammenlign resultater
- Brug den kraftigste controller til testing

---

## 🎯 ANBEFALEDE NÆSTE SKRIDT

1. **Test firmware limit:**
   ```
   Send: A2 2D 00  (45 km/h)
   Send: A2 32 00  (50 km/h)
   Send: A2 FF FF  (No limit)
   ```

2. **Monitor temperatur:**
   ```
   Læs: Service 00006688
   Hold under 70°C motor
   ```

3. **Hvis stadig låst på 38 km/h:**
   - Download firmware
   - Find 0x26 bytes (38 decimal)
   - Patch til højere værdi
   - Flash modified firmware

4. **Backup alt:**
   - Original firmware
   - Device settings
   - Serial number

---

## ⚠️ ADVARSEL

- Firmware modding kan "bricke" controlleren
- Test altid i kontrolleret miljø
- Hold backup af original firmware
- Temperaturoververågning er OBLIGATORISK
- Hastigheder over 25 km/h kan være ulovlige

---

## 🔬 DEBUGGING

**Se aktuel speed limit:**
```
Læs fra service: 00006684 (Settings Read)
Forvent: [0xA2, current_speed, 0x00]
```

**Se firmware version:**
```graphql
query GetDevice($deviceId: ID!) {
  device(id: $deviceId) {
    firmwareVersion
    hardwareVersion
  }
}
```

**Se temperatur:**
```
Læs fra service: 00006688 (Status/Telemetry)
Byte 0 = Motor temp (Celsius decimal)
Byte 1 = Battery temp (Celsius decimal)
```

---

**GOD JAGT! 🚀**

Firmware loftet er der for at beskytte motoren, men med ordentlig
temperaturovervågning og testing kan du finde den sande grænse.
