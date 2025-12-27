# BLE Protocol Reverse Engineering - Fra Augment APK

## ✅ Verificeret Information

### Service UUIDs (fra INTEGRATION_GUIDE.md og APK analyse)
```
PRIMARY:     00006680-0000-1000-8000-00805f9b34fb
SECONDARY:   00006681-0000-1000-8000-00805f9b34fb
CONTROL:     00006683-0000-1000-8000-00805f9b34fb  ← LOCK/UNLOCK
STATUS:      00006684-0000-1000-8000-00805f9b34fb
FIRMWARE:    00006685-0000-1000-8000-00805f9b34fb
BATTERY:     00006687-0000-1000-8000-00805f9b34fb
GPS:         00006688-0000-1000-8000-00805f9b34fb
DEVICE_INFO: 0000d101-0000-1000-8000-00805f9b34fb
OTA_1:       0000fed7-0000-1000-8000-00805f9b34fb
OTA_2:       0000fed8-0000-1000-8000-00805f9b34fb
```

###Fields Found in APK
```graphql
password {
  lock
  unlock
  lockHX    # HX = Hex encoded
  unlockHX  # HX = Hex encoded
}
settingsOperationCode
speedLimitKmhMaster
speedLimitKmhCurrent
```

## ⚠️ Teoretisk/Mangler Verifikation

### Karakteristikker (IKKE FUNDET ENDNU)
Under hver service skal der være karakteristikker for READ/WRITE/NOTIFY.

**Standard BLE GATT mønster:**
```
Service: 00006683 (CONTROL)
  ├─ Characteristic: 00006683-XXXX-... (WRITE) → Commands
  └─ Characteristic: 00006683-YYYY-... (NOTIFY) → Responses
```

**Mulige karakteristik UUID formater:**
- `00006683-0001-1000-8000-00805f9b34fb` (WRITE)
- `00006683-0002-1000-8000-00805f9b34fb` (NOTIFY)

### Command Format (SPEKULATIVT)
**Baseret på typiske e-scooter protokoller:**

```python
# Lock Command (spekulativt)
lock_cmd = [0x01, 0x01]  # CMD_LOCK

# Unlock Command (spekulativt)
unlock_cmd = [0x01, 0x00]  # CMD_UNLOCK

# Set Speed Limit (spekulativt)
set_speed_cmd = [0x02, speed_kmh]  # CMD_SET_SPEED

# Request Status (spekulativt)
get_status_cmd = [0x03, 0x00]  # CMD_GET_STATUS
```

**Password/Auth (hvis lockHX/unlockHX betyder hex encoded):**
```python
# Måske skal der sendes en password først?
auth_cmd = [0x00] + password_bytes
```

## 📋 Næste Skridt for at Finde Præcis Protokol

### Metode 1: Deobfuscate JavaScript Bundle
```bash
# Beautify og søg i bundle
cd apk-extracted/assets
npx js-beautify index.android.bundle > bundle-readable.js
grep -A 20 "writeCharacteristic" bundle-readable.js
```

### Metode 2: BLE Sniffing
1. Install nRF Connect på Android
2. Connect til scooter
3. Observer hvilke karakteristikker der opdages
4. Brug Augment app til at lock/unlock
5. Se hvilke bytes der sendes i nRF sniffer logs

### Metode 3: Logcat under App Kørsel
```bash
adb logcat | grep -E "BLE|write|characteristic|6683"
```

### Metode 4: Frida Hooking
Hook `BluetoothGattCharacteristic.setValue()` og log alle writes:
```javascript
Java.perform(function() {
  var Char = Java.use("android.bluetooth.BluetoothGattCharacteristic");
  Char.setValue.overload('[B').implementation = function(value) {
    console.log("BLE Write: " + bytesToHex(value));
    return this.setValue(value);
  };
});
```

## 🔍 Hvad Vi Ved vs Hvad Vi Mangler

### ✅ Vi Ved:
- Service UUIDs
- Kommando navne (lock, unlock, setSpeed)
- App bruger GraphQL til backend
- Password system eksisterer
- Speed limit kan sættes

### ❌ Vi Mangler:
- Karakteristik UUIDs under hver service
- Præcise byte arrays for hver kommando
- Response format
- Checksum/CRC beregning (hvis nogen)
- Encryption/authentication metode

## 💡 Anbefalinger

**BEDSTE LØSNING: BLE Sniffing**
- Download nRF Connect eller Wireshark med BLE adapter
- Capture traffic fra officiel Augment app
- Se præcis hvad der sendes

**ALTERNATV: Test og Gæt**
Når vi har forbundet til scooter:
1. Enumerer alle characteristics under service 00006683
2. Find den med WRITE permission
3. Prøv standard command patterns:
   - `[0x01, 0x01]` for lock
   - `[0x01, 0x00]` for unlock
   - `[0x02, 0x19]` for 25 km/h speed limit
   - osv.

**Dokumentation:** Opdater denne fil når ny info findes!
