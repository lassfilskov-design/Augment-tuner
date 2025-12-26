═══════════════════════════════════════════════════════════════
  AUGMENT FIRMWARE MODDING GUIDE
  Download → Modify → Flash via OTA
═══════════════════════════════════════════════════════════════

## 🎯 WORKFLOW OVERVIEW

```
┌─────────────────────────────────────────────────────────────┐
│  1. DOWNLOAD firmware fra Augment backend (GraphQL API)    │
│  2. MODIFICER .bin fil (hex editor / patches)              │
│  3. FLASH tilbage via OTA (Nordic DFU over BLE)            │
│  4. TEST på scooter                                         │
│  5. Hvis fejl: Gentag med fresh firmware                   │
└─────────────────────────────────────────────────────────────┘
```

═══════════════════════════════════════════════════════════════
STEP 1: DOWNLOAD FIRMWARE
═══════════════════════════════════════════════════════════════

## Method A: Intercept Augment App (Nemmest)

### Tools du skal bruge:
- **mitmproxy** eller **Charles Proxy**
- Rooted Android telefon ELLER iOS med jailbreak
- Augment app installeret

### Setup mitmproxy:
```bash
# Install mitmproxy
pip install mitmproxy

# Start proxy på port 8080
mitmproxy -p 8080

# Eller brug GUI version
mitmweb -p 8080
```

### Configure telefon til at bruge proxy:
1. WiFi Settings → Manual Proxy
2. Server: Din computer's IP
3. Port: 8080
4. Install mitmproxy CA certificate

### Trigger firmware download i Augment app:
1. Åbn Augment app
2. Gå til Settings → Firmware Update
3. Check for updates
4. Se proxy output for AWS S3 URL

### Du vil se:
```
GET https://s3.amazonaws.com/augment-firmware/nrf52840_vX.X.X.bin
```

### Download firmware:
```bash
# Gem URL og download direkte
curl -O "https://s3.amazonaws.com/augment-firmware/nrf52840_vX.X.X.bin"
```

---

## Method B: Direct GraphQL Query (Avanceret)

### Find GraphQL endpoint i APK:
```bash
# Fra tidligere APK analyse
grep -r "graphql" apk-extracted/
grep -r "checkFirmwareUpgrade" apk-extracted/
```

### GraphQL Query structure (fundet i APK):
```graphql
query FirmwareUpgrade($firmware: CheckFirmwareUpgradeInput!, $language: Language) {
  checkFirmwareUpgrade(input: $firmware) {
    version
    url
    changelog
    mandatory
  }
}
```

### Input variables:
```json
{
  "firmware": {
    "currentVersion": "1.0.0",
    "hardwareVersion": "nrf52840",
    "deviceId": "YOUR_SCOOTER_MAC"
  },
  "language": "EN"
}
```

### Send request:
```bash
# Find endpoint URL først (eks: api.augment.eco/graphql)
curl -X POST https://api.augment.eco/graphql \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "query": "query FirmwareUpgrade($firmware: CheckFirmwareUpgradeInput!) { checkFirmwareUpgrade(input: $firmware) { version url } }",
    "variables": {
      "firmware": {
        "currentVersion": "1.0.0",
        "hardwareVersion": "nrf52840"
      }
    }
  }'
```

### Response vil indeholde:
```json
{
  "data": {
    "checkFirmwareUpgrade": {
      "version": "2.1.5",
      "url": "https://s3.amazonaws.com/augment-firmware/nrf52840_v2.1.5.bin"
    }
  }
}
```

---

═══════════════════════════════════════════════════════════════
STEP 2: MODIFICER FIRMWARE
═══════════════════════════════════════════════════════════════

## Tools til firmware modding:

### Hex Editors:
- **ImHex** (best, gratis, multi-platform)
- **HxD** (Windows)
- **Hex Fiend** (macOS)
- **xxd** (command line)

### Disassemblers:
- **Ghidra** (gratis, NSA tool!)
- **IDA Free**
- **Radare2** (command line)

---

## A. Find patterns i firmware

### 1. Identify firmware format (Nordic DFU):
```bash
# Check file header
xxd nrf52840_v2.1.5.bin | head -20

# Nordic DFU firmware typisk struktur:
# - SoftDevice (BLE stack)
# - Application code
# - Bootloader
# - Settings page
```

### 2. Find speed limit værdier:
```bash
# Søg efter decimal 39 (0x27) og 45 (0x2D)
xxd nrf52840_v2.1.5.bin | grep "27"
xxd nrf52840_v2.1.5.bin | grep "2d"

# Eller brug hex editor's search function
# Search for: 27 00 (little-endian)
# Search for: 2D 00 (little-endian)
```

### 3. Find text strings:
```bash
# Extract all strings
strings nrf52840_v2.1.5.bin > firmware_strings.txt

# Search for interessante strings
grep -i "speed\|limit\|lock\|wheel" firmware_strings.txt
```

---

## B. Patch firmware

### Method 1: Simple hex replacement (ImHex/HxD)

**Eksempel: Øg speed limit fra 39 til 60 km/h**

1. Åbn `.bin` fil i hex editor
2. Search: `27 00` (39 decimal = 0x27)
3. Replace: `3C 00` (60 decimal = 0x3C)
4. Gem som: `nrf52840_v2.1.5_modded.bin`

**⚠️ VIGTIGT:**
- Tag backup før du ændrer!
- Ændr IKKE firmware størrelse
- Nogle bytes kan være checksums - test forsigtig

### Method 2: Script-based patching (Python)

```python
#!/usr/bin/env python3
# patch_firmware.py

def patch_firmware(input_file, output_file, patches):
    with open(input_file, 'rb') as f:
        data = bytearray(f.read())

    for offset, old_bytes, new_bytes in patches:
        # Verify old bytes match
        if data[offset:offset+len(old_bytes)] == old_bytes:
            data[offset:offset+len(new_bytes)] = new_bytes
            print(f"✓ Patched at offset {hex(offset)}")
        else:
            print(f"✗ Mismatch at offset {hex(offset)}")

    with open(output_file, 'wb') as f:
        f.write(data)

# Eksempel patches
patches = [
    # (offset, old_bytes, new_bytes)
    (0x1234, b'\x27\x00', b'\x3C\x00'),  # Speed 39 → 60
    (0x5678, b'\x01', b'\x00'),          # Disable auto-lock
]

patch_firmware('nrf52840_v2.1.5.bin', 'nrf52840_modded.bin', patches)
```

### Method 3: Ghidra disassembly (Advanced)

1. Load firmware i Ghidra
2. Analyze ARM Cortex-M4 code
3. Find speed limit function
4. Patch assembly instructions
5. Export modified binary

---

## C. Find patch locations (hvis du ikke kender offset)

### Brug pattern matching:
```python
#!/usr/bin/env python3
# find_patterns.py

def find_pattern(filename, pattern):
    with open(filename, 'rb') as f:
        data = f.read()

    matches = []
    offset = 0
    while True:
        offset = data.find(pattern, offset)
        if offset == -1:
            break
        matches.append(offset)
        offset += 1

    return matches

# Find alle instances af speed limit (39 = 0x27)
pattern = b'\x27\x00'  # Little-endian
matches = find_pattern('nrf52840_v2.1.5.bin', pattern)

for match in matches:
    print(f"Found at offset: {hex(match)}")
```

---

═══════════════════════════════════════════════════════════════
STEP 3: FLASH VIA OTA (Nordic DFU)
═══════════════════════════════════════════════════════════════

## Tools:

### Option A: nRF Connect App (Nemmest)
- Download fra Play Store / App Store
- Gratis, officiel Nordic tool
- GUI interface

### Option B: nrfutil (Command line)
- Mere kontrol
- Automation friendly
- Python tool

### Option C: Custom Android app
- Integrer i din egen app
- Nordic BLE Library

---

## A. Using nRF Connect App

### 1. Prepare DFU package:
```bash
# Install nrfutil
pip install nrfutil

# Create DFU package (.zip)
nrfutil pkg generate \
  --hw-version 52 \
  --sd-req 0x00 \
  --application nrf52840_modded.bin \
  --application-version 1 \
  nrf52840_modded_dfu.zip
```

### 2. Flash via app:
1. Open nRF Connect app
2. Scan for scooter (look for service FED7/FED8)
3. Tap "DFU" button
4. Select `nrf52840_modded_dfu.zip`
5. Upload → Wait 2-5 min
6. Done! 🎉

---

## B. Using nrfutil CLI

```bash
# Flash firmware direkte
nrfutil dfu ble \
  -pkg nrf52840_modded_dfu.zip \
  -p /dev/ttyUSB0 \
  -n "Augment Scooter"

# Eller via BLE adapter
nrfutil dfu ble \
  -pkg nrf52840_modded_dfu.zip \
  -ic NRF52 \
  -snr YOUR_DEVICE_SERIAL
```

---

## C. Custom Kotlin Implementation

```kotlin
// In your Android app
class FirmwareFlasher(context: Context) {

    private val dfuManager = DfuServiceInitiator(deviceAddress)
        .setDeviceName("Augment Scooter")
        .setKeepBond(true)
        .setForceDfu(false)
        .setPacketsReceiptNotificationsEnabled(true)
        .setPacketsReceiptNotificationsValue(12)

    fun flashFirmware(zipFilePath: String) {
        dfuManager
            .setZip(Uri.parse(zipFilePath))
            .start(context, DfuService::class.java)
    }
}

// Listen for progress
val dfuProgressListener = object : DfuProgressListener {
    override fun onDeviceConnecting(deviceAddress: String) {
        println("Connecting...")
    }

    override fun onProgressChanged(
        deviceAddress: String,
        percent: Int,
        speed: Float,
        avgSpeed: Float,
        currentPart: Int,
        partsTotal: Int
    ) {
        println("Progress: $percent%")
    }

    override fun onDeviceDisconnected(deviceAddress: String) {
        println("Disconnected")
    }

    override fun onDfuCompleted(deviceAddress: String) {
        println("✓ Firmware flashed!")
    }

    override fun onDfuAborted(deviceAddress: String) {
        println("✗ Flash aborted")
    }

    override fun onError(
        deviceAddress: String,
        error: Int,
        errorType: Int,
        message: String
    ) {
        println("✗ Error: $message")
    }
}
```

---

═══════════════════════════════════════════════════════════════
STEP 4: TESTING & ROLLBACK
═══════════════════════════════════════════════════════════════

## Testing Protocol:

### 1. Initial boot test:
- [ ] Scooter tænder efter flash
- [ ] BLE connection virker
- [ ] Battery level vises korrekt

### 2. Basic functionality:
- [ ] Lock/Unlock virker
- [ ] Motor starter
- [ ] Throttle response normal

### 3. Modified features:
- [ ] Speed limit øget (hvis modificeret)
- [ ] Sport+ mode virker (hvis aktiveret)
- [ ] Zero start virker (hvis aktiveret)

### 4. Safety checks:
- [ ] ⚠️ Temperaturovervågning virker!
- [ ] ⚠️ Nødbremse ved >70°C
- [ ] Batteri cutoff ved lav spænding

---

## Rollback hvis noget går galt:

### Method 1: Flash original firmware
```bash
# Download fresh firmware fra Augment
# Flash via samme OTA process
nrfutil dfu ble -pkg nrf52840_original.zip ...
```

### Method 2: Factory reset via UART
```bash
# Hvis BLE ikke virker, brug UART debug
# Tilslut UART pins på controller board
# Send factory reset command
```

### Method 3: JTAG recovery (sidste udvej)
```bash
# Kræver J-Link debugger
# Flash bootloader + SoftDevice + app
nrfjprog --program nrf52840_original.hex --chiperase --verify
```

---

═══════════════════════════════════════════════════════════════
COMMON PATCHES REFERENCE
═══════════════════════════════════════════════════════════════

## Speed Limit Patches:

### Remove 25 km/h limit (EU models):
```
FIND:    19 00  (25 decimal)
REPLACE: FF 00  (255 = no limit)
```

### Set to 45 km/h (Augment M+):
```
FIND:    27 00  (39 decimal)
REPLACE: 2D 00  (45 decimal)
```

### Set to 60 km/h (experimental):
```
FIND:    27 00
REPLACE: 3C 00  (60 decimal)
```

---

## Zero Start Enable:

### Disable kick-to-start requirement:
```
FIND:    00 00 00  (zero start disabled)
REPLACE: 01 06 00  (enabled, max 6 km/h)
```

---

## Auto-Lock Disable:

### Prevent automatic locking:
```
FIND:    01 00 1E 00  (auto-lock enabled, 30s)
REPLACE: 00 00 00 00  (disabled)
```

---

═══════════════════════════════════════════════════════════════
ADVANCED: AUTOMATION SCRIPT
═══════════════════════════════════════════════════════════════

## Complete workflow script:

```bash
#!/bin/bash
# augment_mod_workflow.sh

set -e

FIRMWARE_URL="https://s3.amazonaws.com/augment-firmware/nrf52840_latest.bin"
ORIGINAL_FW="nrf52840_original.bin"
MODDED_FW="nrf52840_modded.bin"
DFU_PKG="nrf52840_modded_dfu.zip"

echo "🔽 Downloading firmware..."
curl -o $ORIGINAL_FW $FIRMWARE_URL

echo "🔧 Patching firmware..."
python3 patch_firmware.py $ORIGINAL_FW $MODDED_FW

echo "📦 Creating DFU package..."
nrfutil pkg generate \
  --hw-version 52 \
  --sd-req 0x00 \
  --application $MODDED_FW \
  --application-version 1 \
  $DFU_PKG

echo "📱 Ready to flash!"
echo "Use nRF Connect app to flash: $DFU_PKG"
echo "Or use: nrfutil dfu ble -pkg $DFU_PKG ..."
```

---

═══════════════════════════════════════════════════════════════
SAFETY & LEGAL
═══════════════════════════════════════════════════════════════

## ⚠️ VIGTIGE ADVARSLER:

### Hardware Safety:
- TEMPERATUROVERVÅGNING skal altid virke
- Test i kontrolleret miljø først
- Brug hjælm og beskyttelse ved test
- Højere hastigheder = højere risiko

### Legal:
- Modificeret firmware kan være ulovligt i dit land
- Kan ugyldiggøre garanti
- Brug kun på privat grund
- Check lokal lovgivning

### Technical:
- Forkert firmware kan "bricke" controlleren
- Tag ALTID backup før modding
- Test ændringer én ad gangen
- Dokumenter dine patches

---

═══════════════════════════════════════════════════════════════
TROUBLESHOOTING
═══════════════════════════════════════════════════════════════

## Problem: OTA flash fejler

### Løsning 1: Check DFU services
```bash
# Scan for DFU services
nrfutil ble-scan

# Should see: FED7, FED8
```

### Løsning 2: Genstart scooter i DFU mode
- Hold power button i 10 sekunder
- Tænd igen mens du holder throttle

### Løsning 3: Check firmware size
```bash
# Nordic DFU har size limits
ls -lh nrf52840_modded.bin
# Should be < 512KB for application
```

---

## Problem: Scooter virker ikke efter flash

### Løsning 1: Reflash original firmware
```bash
# Download fresh fra Augment
# Flash via OTA
```

### Løsning 2: UART factory reset
- Tilslut UART (TX, RX, GND)
- Send reset command

### Løsning 3: JTAG recovery
- Kræver hardware debugger
- Flash komplet firmware bundle

---

═══════════════════════════════════════════════════════════════
RESOURCES & LINKS
═══════════════════════════════════════════════════════════════

## Tools:
- nrfutil: https://github.com/NordicSemiconductor/pc-nrfutil
- nRF Connect: https://www.nordicsemi.com/Products/Development-tools/nrf-connect-for-mobile
- ImHex: https://imhex.werwolv.net/
- Ghidra: https://ghidra-sre.org/
- mitmproxy: https://mitmproxy.org/

## Documentation:
- Nordic DFU: https://infocenter.nordicsemi.com/topic/sdk_nrf5_v17.1.0/lib_bootloader_dfu.html
- nRF52840: https://infocenter.nordicsemi.com/topic/struct_nrf52/struct/nrf52840.html

## Communities:
- r/ElectricScooters
- EUC Forum
- DIY Electric forum

═══════════════════════════════════════════════════════════════
GOOD LUCK! 🚀
═══════════════════════════════════════════════════════════════
