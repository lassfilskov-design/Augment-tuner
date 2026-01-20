# 🔍 MITM Proxy Guide - Fang Augment Firmware URL

## Quick Start (3 skridt)

### 1️⃣ Installér mitmproxy

**Linux/Mac:**
```bash
# Gør script executable først (kun første gang)
chmod +x setup_mitm_proxy.sh
chmod +x start_mitm_proxy.sh

# Kør installer
./setup_mitm_proxy.sh
```

**Windows:**
- Double-click på `setup_mitm_proxy.bat`

---

### 2️⃣ Start mitmproxy

**Linux/Mac:**
```bash
./start_mitm_proxy.sh
```

**Windows:**
- Double-click på `start_mitm_proxy.bat`

---

### 3️⃣ Konfigurér din Android telefon

#### A) Sæt WiFi Proxy
1. Åbn **WiFi indstillinger**
2. Hold finger på dit netværk → **Modificer netværk**
3. Vælg **Avancerede indstillinger**
4. Proxy: **Manuel**
   - **Proxy hostname:** Din computer IP (vises i terminal)
   - **Proxy port:** `8080`
5. **Gem**

#### B) Installér Certifikat
1. Åbn **Chrome** på telefonen
2. Gå til: `http://mitm.it`
3. Download **Android certifikat**
4. Installér certifikatet (følg Android's guide)

---

## 🎯 Fang Firmware URL

### Trin 1: Åbn mitmweb Interface
På din computer, åbn browser:
```
http://127.0.0.1:8081
```

Du ser nu alle HTTP/HTTPS requests fra din telefon!

### Trin 2: Trigger Firmware Update
1. Åbn **Augment app** på telefonen
2. Gå til **Indstillinger** → **Firmware Update** (eller lignende)
3. Klik **Check for update** / **Update firmware**

### Trin 3: Find Firmware URL
I mitmweb interface ser du:

**GraphQL Request:**
```
POST https://api.augment.eco/graphql
{
  "query": "query checkFirmwareUpgrade(...) { ... }",
  "variables": { ... }
}
```

**Response kan indeholde:**
```json
{
  "data": {
    "checkFirmwareUpgrade": {
      "available": true,
      "version": "1.2.3",
      "downloadUrl": "https://augment-firmware.s3.amazonaws.com/alturo_v1.2.3.bin"
    }
  }
}
```

🎯 **DER ER DIN FIRMWARE URL!**

### Trin 4: Download Firmware
Kopier URL'en og download:
```bash
wget "https://augment-firmware.s3.amazonaws.com/alturo_v1.2.3.bin"

# Eller
curl -O "https://augment-firmware.s3.amazonaws.com/alturo_v1.2.3.bin"
```

---

## 📊 Hvad skal du lede efter?

### GraphQL Queries
```
checkFirmwareUpgrade
getFirmwareInfo
downloadFirmware
```

### URL Patterns
```
*.amazonaws.com/*.bin
*.augment.eco/*.bin
*/firmware/*
*/ota/*
```

### File Extensions
```
.bin - Binary firmware
.hex - Intel HEX format
.img - Image file
.fw  - Firmware file
```

---

## 🛠 Troubleshooting

### "HTTPS fejl" på telefon?
- Certifikatet er ikke installeret korrekt
- Geninstallér certifikat fra http://mitm.it

### Ser ingen traffic i mitmweb?
- Check at proxy IP og port er korrekt
- Check at computer og telefon er på samme WiFi
- Genstart Augment app'en

### "mitmweb: command not found"?
- Kør setup scriptet igen
- Eller installér manuelt: `pip3 install mitmproxy`

### Kan ikke finde firmware URL?
- App'en downloader måske firmware i chunks
- Kig efter **alle** requests til amazonaws.com
- Check for Base64 encoded data i responses

---

## 🔒 Når du er færdig

1. **Stop mitmproxy:** Luk terminal/cmd vinduet
2. **Fjern proxy fra telefon:**
   - WiFi indstillinger → Modificer netværk
   - Proxy: **Ingen**
   - Gem

---

## 💡 Tips & Tricks

### Filtrer Traffic
I mitmweb kan du filtrere requests:
```
~d augment.eco        # Kun Augment API
~d amazonaws.com      # Kun AWS
~u firmware           # URLs med "firmware"
~m POST               # Kun POST requests
```

### Gem Alt Traffic
```bash
# Start mitmproxy med logging
mitmweb --set stream_large_bodies=1 --set save_stream_file=traffic.mitm
```

### Export Captured Data
I mitmweb:
1. Vælg en request
2. Klik **Export** → **cURL**
3. Kopier kommandoen for at gentage request

---

## 📝 Næste Skridt Efter Download

Når du har firmware .bin filen:

1. **Analysér firmware:**
   ```bash
   file firmware.bin
   strings firmware.bin | less
   hexdump -C firmware.bin | less
   ```

2. **Reverse engineering:**
   - Ghidra
   - Binary Ninja
   - IDA Pro

3. **Find encryption keys:**
   - Søg efter strings
   - Find crypto algorithms
   - Analysér update procedure

---

**Held og lykke! 🚀**

*Hvis du støder på problemer, åbn en issue eller spørg mig!*
