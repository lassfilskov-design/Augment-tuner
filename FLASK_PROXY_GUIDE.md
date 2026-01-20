# 🚀 Flask Proxy Guide - Automatisk Firmware Capture

## Hvad er forskellen fra MITM?

### MITM Proxy (mitmproxy)
- ✅ Intercepterer **AL** traffic (inkl. HTTPS med certifikat)
- ✅ Ingen coding nødvendigt
- ❌ Kræver certifikat installation på telefon

### Flask Proxy (denne løsning)
- ✅ Automatisk firmware detection og download
- ✅ Pænt web dashboard med live updates
- ✅ Gemmer alt automatisk (logs, firmware, GraphQL queries)
- ✅ Ingen certifikat nødvendigt for HTTP
- ❌ Kun HTTP (for HTTPS skal du bruge mitmproxy)
- ✅ Perfekt til debugging og development

---

## Quick Start

### 1️⃣ Installation

**Linux/Mac:**
```bash
# Gør executable
chmod +x setup_flask_proxy.sh start_flask_proxy.sh

# Installer
./setup_flask_proxy.sh
```

**Windows:**
- Double-click på `setup_flask_proxy.bat`

---

### 2️⃣ Start Flask Proxy

**Linux/Mac:**
```bash
./start_flask_proxy.sh
```

**Windows:**
- Double-click på `start_flask_proxy.bat`

---

### 3️⃣ Konfigurér Android

1. **WiFi Settings:**
   - Hold finger på netværk → Modificer
   - Proxy: **Manuel**
   - Hostname: `<DIN_COMPUTER_IP>` (vises i terminal)
   - Port: `8888`

2. **Åbn Dashboard:**
   - På computer: http://127.0.0.1:5000
   - Se live captured data! 📊

3. **Trigger Firmware Update:**
   - Åbn Augment app
   - Gå til firmware update
   - Tryk update
   - **Se firmware URL dukke op i dashboardet!** 🎯

---

## 🎨 Dashboard Features

### Real-time Stats
- **Total Requests:** Alle HTTP requests fanget
- **Firmware URLs:** Automatisk detected firmware URLs
- **GraphQL Queries:** Alle GraphQL API calls

### Automatisk Firmware Download
Flask proxyen **downloader automatisk** firmware når den finder en URL! 💾

Firmware gemmes i:
```
captured_data/
├── firmware/
│   ├── alturo_v1.2.3.bin
│   ├── es210_v2.1.0.bin
│   └── ...
├── logs/
│   └── proxy_20260120_230145.log
```

### Live Updates
Dashboardet opdaterer automatisk hvert 2. sekund - ingen refresh nødvendigt! 🔄

---

## 🔍 Hvad Captures Flask Proxy?

### 1. Firmware URLs
Alle URLs der matcher:
- `*.bin`, `*.hex`, `*.img`, `*.fw`
- URL indeholder "firmware" eller "ota"
- amazonaws.com med firmware extensions

### 2. GraphQL Queries
Automatisk detection af:
- `/graphql` endpoints
- Requests med `query` eller `mutation` i body
- Gemmes med timestamp og fuld query

### 3. All HTTP Traffic
- Method (GET, POST, etc.)
- Full URL
- Timestamp
- Response analysis

---

## 💡 Pro Tips

### Auto-download Everything
Flask proxyen downloader automatisk alle firmware filer den finder! 🎯

Tjek mappen:
```bash
ls -lh captured_data/firmware/
```

### Search Logs
```bash
# Find firmware URLs i logs
grep "FIRMWARE URL" captured_data/logs/*.log

# Find GraphQL queries
grep "GraphQL" captured_data/logs/*.log
```

### Export Data
Alt gemmes automatisk i `captured_data/` mappen:
- Firmware binaries
- Complete logs
- GraphQL queries
- Request history

---

## 🆚 MITM vs Flask - Hvad skal jeg bruge?

### Brug **MITM** hvis:
- ✅ Augment API bruger HTTPS (sandsynligt)
- ✅ Du vil se **AL** traffic
- ✅ Du ikke vil kode noget

### Brug **Flask** hvis:
- ✅ Du vil have automatisk firmware download
- ✅ Du vil have et pænt dashboard
- ✅ Augment API bruger HTTP (usandsynligt)
- ✅ Du vil have struktureret data output

### Min anbefaling: **Brug BEGGE!** 🎯

1. **Start med Flask proxy** → Se pænt dashboard, få idé om traffic
2. **Hvis HTTPS bloker** → Skift til MITM
3. **Når du finder API endpoints** → Brug begge sammen!

---

## 🔧 Advanced: Kombiner MITM + Flask

Du kan bruge **mitmproxy til HTTPS interception** og **Flask til automatisk processing**:

```bash
# Terminal 1: Start MITM (intercepter HTTPS)
mitmweb --mode upstream:http://127.0.0.1:5000

# Terminal 2: Start Flask (process data)
python3 flask_proxy.py
```

Nu går alt gennem MITM → Flask → Internet → Flask → MITM → App

**ULTIMATE SETUP!** 🚀

---

## 📊 Data Structure

### Captured Requests
```json
{
  "method": "POST",
  "url": "https://api.augment.eco/graphql",
  "timestamp": "2026-01-20 23:15:42"
}
```

### Firmware URLs
```json
{
  "url": "https://augment-firmware.s3.amazonaws.com/alturo_v1.2.3.bin",
  "timestamp": "2026-01-20 23:16:01"
}
```

### GraphQL Queries
```json
{
  "query": {
    "query": "query checkFirmwareUpgrade($input: ...) { ... }",
    "variables": { ... }
  },
  "timestamp": "2026-01-20 23:15:59"
}
```

---

## 🐛 Troubleshooting

### "Flask ikke installeret"
```bash
pip3 install flask flask-cors requests
```

### Ser ingen traffic i dashboard
- Check at proxy IP er korrekt
- Check at port er 8888
- Genstart Augment app'en

### Firmware downloades ikke automatisk
- Check logs: `captured_data/logs/*.log`
- URL er måske HTTPS (brug MITM)
- Firmware sendes måske i chunks via Bluetooth

### Dashboard loader ikke
- Check at Flask kører på port 5000
- Åbn http://127.0.0.1:5000 (ikke localhost)
- Check firewall

---

## 🎉 Success Checklist

- ✅ Flask proxy kører (terminal viser "Running on...")
- ✅ Dashboard åben i browser (http://127.0.0.1:5000)
- ✅ Android proxy konfigureret (WiFi settings)
- ✅ Stats opdaterer når du browser på telefon
- ✅ Trigger firmware update i Augment app
- ✅ Se firmware URL i dashboard! 🎯
- ✅ Firmware automatisk downloaded til `captured_data/firmware/`

---

## 🚀 Næste Skridt Efter Firmware Download

Når du har firmware .bin filen:

```bash
# Analysér firmware
file captured_data/firmware/*.bin
strings captured_data/firmware/*.bin | less

# Find interessante strings
strings captured_data/firmware/*.bin | grep -i "password\|key\|secret"

# Hex dump
hexdump -C captured_data/firmware/*.bin | less

# Reverse engineering (avanceret)
# Brug: Ghidra, Binary Ninja, IDA Pro
```

---

**Held og lykke! 🚀**

*Flask proxyen er klar til at fange alt du behøver!*
