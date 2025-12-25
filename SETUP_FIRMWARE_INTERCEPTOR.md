# 🚀 Augment Firmware Interceptor - Installation Guide

## ✅ Step-by-Step Installation (Windows)

### 1️⃣ Install Python & mitmproxy

Åbn PowerShell som Administrator og kør:

```powershell
# Install Python (hvis ikke allerede installeret)
# Download fra: https://www.python.org/downloads/
# HUSK at vælge "Add Python to PATH" under installation!

# Install mitmproxy
pip install mitmproxy
```

### 2️⃣ Start Interceptor

```powershell
# Gå til Augment-tuner mappen
cd "C:\Windows\System32\Augment-tuner"

# Start interceptor
mitmdump -s firmware_interceptor.py

# Du ser nu:
# [*] Augment Firmware Interceptor Started
# [*] Listening for GraphQL requests...
# [*] Proxy running on: 127.0.0.1:8080
```

### 3️⃣ Configure Din Android Telefon

**A. Connect til samme WiFi som din computer**

**B. Find din computer's IP:**
```powershell
# I PowerShell:
ipconfig

# Find "IPv4 Address" under din WiFi adapter
# Eksempel: 192.168.1.100
```

**C. Configure Android Proxy:**
```
Settings → WiFi →
  Long press på dit netværk → Modify Network →
    Advanced Options →
      Proxy: Manual
      Hostname: <din_computer_ip>  (fx 192.168.1.100)
      Port: 8080
      Save
```

**D. Install SSL Certificate:**
```
1. Åbn browser på telefonen
2. Gå til: http://mitm.it
3. Download "Android" certificat
4. Settings → Security → Install from storage
5. Vælg det downloadede certificat
6. Navngiv det "mitmproxy" og install
```

### 4️⃣ Capture Firmware!

```
1. Åbn Augment app på telefonen
2. Gå til Settings → Firmware Update (eller Check for updates)
3. Se din computer terminal - den fanger automatisk requesterne!
4. Firmware bliver automatisk downloaded til: firmware_captures/
```

---

## 🎯 Hvad Sker Der?

Scriptet vil:
- ✅ Intercepte alle GraphQL requests fra Augment appen
- ✅ Finde firmware check requests
- ✅ Extracte firmware download URL
- ✅ **AUTOMATISK DOWNLOADE FIRMWARE!**
- ✅ Gemme alt i `firmware_captures/` mappen
- ✅ Lave quick analyse af firmwaren

---

## 📁 Output Filer

Efter en vellykket capture får du:

```
firmware_captures/
  ├── firmware_request_20251225_143022.json   # GraphQL request
  ├── firmware_response_20251225_143023.json  # GraphQL response med URL
  ├── scooter_fw_v2.5.0.bin                  # ★ FIRMWAREN! ★
  └── scooter_fw_v2.5.0.txt                  # Analysis
```

---

## 🔍 Analyse Firmwaren

Når firmwaren er downloadet:

### Med Binwalk:
```bash
# Install binwalk først
# Dann extract firmware:
binwalk -e scooter_fw_v2.5.0.bin
```

### Find Strings:
```bash
strings scooter_fw_v2.5.0.bin | grep -i "speed\|unlock\|limit"
```

### Reverse Engineering:
- Load filen i Ghidra eller IDA Pro
- Analyser ARM assembly code
- Find motor controller funktioner

---

## ⚠️ Troubleshooting

### Problem: "Connection refused" i appen

**Løsning:**
```powershell
# Check at mitmproxy kører:
netstat -an | findstr "8080"

# Check firewall:
# Windows Defender → Allow an app → Python (tillad både Private og Public)
```

### Problem: SSL Certificate virker ikke

**Løsning:**
```
Android 7+:
  Settings → Security → User credentials →
    Install → Choose mitmproxy cert

Android 11+:
  Skal måske installere Magisk + systemless hosts
```

### Problem: Ingen firmware requests captured

**Løsning:**
```
1. Check at telefon bruger proxy (test på browser: http://mitm.it)
2. Luk og genåbn Augment app
3. Gå til Settings → About → Check for updates
4. Nogle apps kræver SSL pinning bypass (se nedenfor)
```

---

## 🔓 SSL Pinning Bypass (Hvis nødvendigt)

Hvis Augment app bruger SSL pinning:

### Metode 1: Frida (Nemmest)
```bash
# Install Frida
pip install frida-tools

# Download Frida server til Android
# Start Frida:
frida -U -f com.augment.scooter -l ssl-unpinning.js --no-pause
```

### Metode 2: Magisk + TrustMeAlready
```
1. Root telefon med Magisk
2. Install "TrustMeAlready" module
3. Reboot
4. SSL pinning er disabled
```

---

## 🎉 Når Du Har Firmwaren

### Næste Skridt:
1. ✅ Backup original firmware
2. ✅ Reverse engineer med Ghidra/IDA
3. ✅ Find speed limits
4. ✅ Find motor torque curves
5. ✅ Modificer (hvis du vil)
6. ✅ Flash tilbage til scooter

### Modificering Eksempler:
- Øg max hastighed fra 20 km/t til 25 km/t
- Juster motor torque for bedre acceleration
- Disable geo-fencing/speed zones
- Unlock ekstra features

---

## ⚠️ DISCLAIMER

- Kun til educational/research formål
- Modificering kan være ulovlig i dit område
- Kan void warranty
- Kan beskadige scooter
- Brug på egen risiko

---

**Held og lykke! 🚀**

Hvis du støder på problemer, send mig error messages så hjælper jeg!
