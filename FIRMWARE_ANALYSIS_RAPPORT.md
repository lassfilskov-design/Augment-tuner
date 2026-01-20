# Augment APK Firmware Analyse Rapport

## Executive Summary
Firmware er **IKKE** gemt direkte i APK'en som billeder eller binære filer. I stedet hentes firmware **runtime via en GraphQL API** når der skal opdateres.

---

## Analysemetode

### 1. Billedanalyse (PNG filer)
**Scannede filer:**
- `src_assets_images_es210_01.png` - 27 KB (ES210 model)
- `src_assets_images_es210_02.png` - 262 KB (ES210 model)
- `src_assets_images_es210_04.png` - 523 KB (ES210 model) ⭐ STØRST
- `src_assets_images_es210_21.png` - 32 KB (ES210 model)
- `src_assets_images_alt_01.png` - 24 KB (Alturo model)
- `src_assets_images_alt_02.png` - 34 KB (Alturo model)

**Resultater:**
✅ Alle billeder er **valide PNG filer** med normal struktur
✅ **Ingen data efter IEND chunk** (almindeligt sted at skjule data)
✅ **Ingen usædvanlige PNG chunks**
✅ Normale komprimeringsratioer for PNG
❌ Ingen steganografi eller skjulte binære data fundet

**Konklusion:** Billederne er normale UI assets uden skjult firmware.

---

### 2. APK Filstruktur Analyse

**Assets mappe indhold:**
```
- index.android.bundle (7.5 MB) - React Native JavaScript bundle
- dexopt/baseline.prof (15 KB) - Android ART profiler data
- dexopt/baseline.profm (861 bytes) - Profiler metadata
```

**Andre filer:**
```
- DebugProbesKt.bin (1.7 KB) - Java class (Kotlin debugging)
- classes.dex til classes5.dex (13MB + 9.8MB + 9.3MB + 7.2MB + 11MB)
- Diverse biblioteker (.so filer ikke undersøgt i detaljer)
```

**Resultater:**
❌ **Ingen .bin, .hex, .img eller .fw firmware filer** fundet i APK'en
❌ Ingen usædvanlige binære filer der kunne være firmware

---

### 3. GraphQL API Analyse ⭐ VIGTIGSTE FUND

**Fundet i `index.android.bundle`:**

#### Firmware Upgrade Query
```graphql
query FirmwareUpgrade($firmware: CheckFirmwareUpgradeInput!, $language: Language) {
  checkFirmwareUpgrade(input: $firmware) {
    # ... returnerer firmware upgrade info
  }
}
```

#### Firmware Funktioner Identificeret:
1. **`checkFirmwareUpgrade`** - Tjekker om der er ny firmware tilgængelig
2. **`handleSendFirmwareChunk`** - Sender firmware chunks til device
3. **`FirmwareUpgradeStart`** - Starter firmware upgrade processen
4. **`sendFirmwareChunk`** - Primær funktion til chunked transfer

#### API Endpoints:
- Bruger **AWS (amazonaws.com)** infrastructure
- GraphQL endpoint (ikke direkte URL fundet i strings)
- Firmware hentes sandsynligvis fra **S3 buckets** eller lignende cloud storage

---

## Teknisk Firmware Flow (Gæt baseret på kode)

```
1. App tjekker firmware version på device
2. GraphQL query: checkFirmwareUpgrade(deviceVersion, model)
3. Server returnerer:
   - Ny firmware tilgængelig? (boolean)
   - Download URL eller chunk info
   - Version nummer
4. App downloader firmware (sandsynligvis i chunks)
5. App sender chunks til device via Bluetooth: sendFirmwareChunk()
6. Device modtager og installerer firmware
```

---

## Model Information

**ES210** = Ældre elscooter model
**ALT (Alturo)** = Din nuværende model
**ES310, ES410, ES510** = Potentielle nyere modeller (ikke bekræftet i APK)

Billeder for begge modeller findes i APK'en til UI formål.

---

## Konklusion & Næste Skridt

### Hvad vi ved nu:
✅ Firmware gemmes **IKKE** i APK'en
✅ Firmware **hentes runtime** fra Augment's server via GraphQL API
✅ Transmission til device sker via **Bluetooth i chunks**
✅ API bruger **AWS infrastructure**

### For at få fat i firmware skal du:

#### Metode 1: Network Sniffing (Anbefalet)
1. **Installér et MITM (Man-in-the-Middle) værktøj:**
   - Charles Proxy
   - mitmproxy
   - Burp Suite

2. **Opsæt Android device til at bruge proxy:**
   - Installér CA certifikat fra proxy værktøj
   - Konfigurér WiFi til at bruge proxy

3. **Trigger firmware update i app:**
   - Åbn Augment app
   - Gå til firmware update funktionen
   - Start update processen
   - **Network traffic vil vise download URL** til firmware filen

4. **Download firmware direkte:**
   - Brug captured URL til at downloade firmware .bin fil
   - Gem til analyse

#### Metode 2: APK Reverse Engineering (Avanceret)
1. **Decompile APK med jadx eller apktool**
2. **Find GraphQL endpoint URL** i decompiled kode
3. **Analysér firmware check/download logik**
4. **Replikér API calls** med curl/Postman:
   ```bash
   curl -X POST https://api.augment.eco/graphql \
     -H "Content-Type: application/json" \
     -d '{"query":"query FirmwareUpgrade(...){...}"}'
   ```

#### Metode 3: Bluetooth Sniffing (Meget Avanceret)
1. **Brug Android HCI snoop log:**
   - Aktivér "Bluetooth HCI snoop log" i Developer Options
   - Gennemfør firmware update
   - Analysér Bluetooth packets i Wireshark
   - **Firmware chunks sendes over Bluetooth** - kan ekstraher

2. **Find OTA (Over-The-Air) Bluetooth Service UUID:**
   - Fra koden: Se efter BLE service UUIDs relateret til OTA
   - Monitor for GATT characteristic writes under update

#### Metode 4: Root + Frida Hooking (Ekspert)
1. Root Android device
2. Brug Frida til at hooke firmware download funktioner
3. Intercept firmware data i memory
4. Dump til fil

---

## Kritiske UUIDs & Identifikatorer (Fra Kode)

**Potentielle firmware-relaterede Bluetooth karakteristika:**
- Søg efter "OTA" eller "FIRMWARE" i Bluetooth GATT services
- Check for Nordic DFU (Device Firmware Update) service
- Look for custom Augment firmware service UUIDs

**Firma info:**
- Brand: Augment Eco
- App bundle: sandsynligvis com.augment.eco eller lignende
- API: sandsynligvis https://api.augment.eco eller AWS endpoint

---

## Filer Genereret Under Analyse

1. **`analyze_images.py`** - Python script til PNG analyse
2. **Dette dokument** - Fuld rapport

---

## Anbefalinger

**Nemmeste metode: MITM Proxy** ⭐
- Kræver kun Charles Proxy/mitmproxy installation
- Ingen root nødvendigt
- Viser firmware download URL direkte
- Tag 30-60 minutter at opsætte første gang

**Hvis du vil have hjælp videre:**
1. Installér mitmproxy
2. Konfigurér Android til at bruge proxy
3. Trigger firmware update
4. Send mig captured traffic - jeg kan hjælpe med at finde firmware URL

---

*Rapport genereret: 2026-01-20*
*Tool: Claude Code*
*Analyseret APK: Augment - Kopi.apk*
