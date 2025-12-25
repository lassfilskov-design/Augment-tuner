# 🛴 Augment Security Scanner

En omfattende security audit tool til analyse af Augment scooter APK filer og firmware. Finder automatisk backends, passwords, URLs, sensitive filer, red flags og meget mere.

## 🎯 Features

- **🔐 Password Detection**: Find hardcoded passwords, API keys, tokens og credentials
- **🌐 URL Discovery**: Extrahér alle URLs, API endpoints og server addresses
- **🔌 Backend Mapping**: Identificér alle backend endpoints og API paths
- **📁 Sensitive Files**: Find .key, .pem, .db, config filer og andre kritiske filer
- **🚩 Red Flags**: Spot security issues som SSL bypass, debug mode, osv.
- **📱 APK Analysis**: Upload og analyser Android APK filer
- **💾 Firmware Extraction**: Udtræk og analyser firmware images
- **🌐 Web Interface**: Brugervenlig web interface til upload og analyse
- **📊 Detaljerede Reports**: JSON reports med alle findings

## 🚀 Quick Start

### Installation

```bash
# Clone repository
git clone <repo-url>
cd Augment-tuner

# Installer Python dependencies
pip install -r requirements.txt

# (Valgfrit) Installer ekstra tools til avanceret analyse
sudo apt install binwalk apktool jadx strings
```

### Metode 1: Web Interface (Anbefalet)

```bash
# Start web server
python web_app.py

# Åbn browser på:
# http://localhost:5000

# Upload din APK fil og få instant resultater!
```

### Metode 2: Command Line

```bash
# Scan en APK fil
python scanner.py augment_scooter.apk

# Analyser firmware
python firmware_tools.py firmware.bin

# Resultater gemmes i scan_results/ mappen
```

## 📖 Detaljeret Brug

### Scanner Script

```bash
python scanner.py <apk_file>
```

**Output:**
- Extraherer APK indhold
- Scanner alle filer for security issues
- Genererer JSON report i `scan_results/`
- Printer summary til console

**Hvad findes:**
- Hardcoded passwords (password=, api_key=, secret=, token=)
- URLs (http://, https://, ws://, mqtt://, ftp://)
- Backend endpoints (/api/, /v1/, baseUrl=, API_URL=)
- Sensitive files (.key, .pem, .db, config.json, .env)
- Red flags (debug=true, SSL bypass, security TODOs)

### Firmware Tools

```bash
python firmware_tools.py <firmware_file>
```

**Features:**
- Automatisk firmware type detection
- ZIP/APK extraction
- Binwalk integration (hvis installeret)
- String extraction og analyse
- Find URLs, IPs, emails, keys i firmware

**Understøttede formater:**
- APK (Android Package)
- ZIP archives
- ELF binaries
- Android DEX
- U-Boot images
- GZIP/BZIP2/XZ/LZMA compressed
- RAW firmware images

### Web Interface

```bash
python web_app.py
```

**Features:**
- Drag & drop APK upload
- Real-time scanning progress
- Visuelt dashboard med findings
- Download JSON reports
- Se historik af scans

**Endpoints:**
- `GET /` - Main upload interface
- `POST /upload` - Upload og scan fil
- `GET /reports` - List alle reports
- `GET /report/<filename>` - Hent specifik report
- `GET /download/<filename>` - Download report som JSON

## 🔍 Hvad Finder Værktøjet?

### 1. Passwords & Credentials

Patterns der matches:
```
password = "..."
api_key = "..."
secret = "..."
token = "..."
auth_token = "..."
private_key = "..."
client_secret = "..."
access_token = "..."
encryption_key = "..."
aws_secret = "..."
db_password = "..."
```

### 2. URLs & Endpoints

Typer:
- HTTP/HTTPS URLs
- WebSocket URLs (ws://, wss://)
- MQTT endpoints
- FTP addresses
- API endpoints (/api/*, /v1/*, etc.)
- Base URLs og server addresses

### 3. Backend Infrastructure

Finder:
- API endpoint paths
- Server URLs og base addresses
- Backend configuration
- API versioning schemes
- Microservice endpoints

### 4. Sensitive Files

Filtyper:
- Private keys (.key, .pem, .p12)
- Keystores (.jks, .keystore)
- Databases (.db, .sqlite)
- Config files (config.json, secrets.json)
- Environment files (.env)
- Credential files

### 5. Security Red Flags

Issues:
- `debug = true` (Debug mode enabled)
- `ssl_verify = false` (SSL verification disabled)
- `allowBackup = true` (Android backup enabled)
- `debuggable = true` (App debuggable)
- `usesCleartextTraffic = true` (Cleartext traffic allowed)
- SSL bypass code (TrustAllCerts, HostnameVerifier)
- Security TODOs/FIXMEs i kode

## 📊 Example Output

```json
{
  "apk": "augment_scooter.apk",
  "summary": {
    "passwords_found": 12,
    "urls_found": 45,
    "backends_found": 23,
    "sensitive_files_found": 8,
    "red_flags_found": 15
  },
  "findings": {
    "passwords": [
      {
        "file": "res/values/strings.xml",
        "match": "api_key=\"sk_live_abc123...\"",
        "line": 42
      }
    ],
    "urls": [
      {
        "file": "classes.dex",
        "url": "https://api.augment.eco/v1/scooters",
        "line": 1337
      }
    ],
    "backends": [
      {
        "file": "com/augment/network/ApiClient.java",
        "endpoint": "/api/v2/unlock",
        "line": 89
      }
    ]
  }
}
```

## 🛠️ Avanceret Brug

### Custom Patterns

Tilpas scanner.py til at finde custom patterns:

```python
scanner = SecurityScanner()

# Tilføj custom password pattern
scanner.password_patterns.append(r'my_secret\s*=\s*["\']([^"\']+)["\']')

# Tilføj custom URL pattern
scanner.url_patterns.append(r'mqtt://[^\s]+')

scanner.scan_apk("my_app.apk")
```

### Batch Scanning

```python
from scanner import SecurityScanner
from pathlib import Path

scanner = SecurityScanner()

# Scan alle APK filer i en mappe
apk_dir = Path("apk_collection")
for apk_file in apk_dir.glob("*.apk"):
    print(f"Scanning {apk_file}")
    scanner.scan_apk(str(apk_file))
```

### API Integration

```python
import requests

# Upload via API
files = {'file': open('app.apk', 'rb')}
response = requests.post('http://localhost:5000/upload', files=files)
report = response.json()

print(f"Found {report['report']['security_scan']['summary']['passwords_found']} passwords")
```

## 🔧 Installation af Ekstra Tools

### Binwalk (Firmware extraction)

```bash
# Ubuntu/Debian
sudo apt install binwalk

# macOS
brew install binwalk

# Fra source
git clone https://github.com/ReFirmLabs/binwalk
cd binwalk
python setup.py install
```

### APKTool (APK decompilation)

```bash
# Ubuntu/Debian
sudo apt install apktool

# Manual installation
wget https://raw.githubusercontent.com/iBotPeaches/Apktool/master/scripts/linux/apktool
wget https://bitbucket.org/iBotPeaches/apktool/downloads/apktool_2.9.0.jar
chmod +x apktool
sudo mv apktool apktool_2.9.0.jar /usr/local/bin/
```

### JADX (Java decompiler)

```bash
# Download latest release
wget https://github.com/skylot/jadx/releases/download/v1.4.7/jadx-1.4.7.zip
unzip jadx-1.4.7.zip -d jadx
sudo mv jadx /opt/
sudo ln -s /opt/jadx/bin/jadx /usr/local/bin/jadx
```

## 📁 Projekt Struktur

```
Augment-tuner/
├── scanner.py              # Main security scanner
├── firmware_tools.py       # Firmware extraction tools
├── web_app.py             # Web interface
├── templates/
│   └── index.html         # Web UI
├── requirements.txt       # Python dependencies
├── uploads/               # Uploaded files (auto-created)
├── scan_results/          # Scan reports (auto-created)
├── firmware_extracted/    # Extracted firmware (auto-created)
└── decompiled/           # Decompiled APKs (auto-created)
```

## 🎓 Brug Cases

### 1. Reverse Engineering af Augment Scooter App

```bash
# Download Augment app APK
# Scan for backend endpoints
python scanner.py augment.apk

# Check resultater for API endpoints
cat scan_results/augment_report.json | jq '.findings.backends'
```

### 2. Find Hardcoded API Keys

```bash
python scanner.py app.apk
# Se passwords sektion i output for API keys
```

### 3. Map Backend Infrastructure

Web interface viser alle fundne:
- API endpoints
- Server URLs
- WebSocket connections
- MQTT brokers

### 4. Security Audit

Find alle security issues:
- Hardcoded credentials
- SSL bypass code
- Debug mode enabled
- Sensitive data i plaintext

## ⚠️ Disclaimer

Dette værktøj er til **educational** og **authorized security testing** formål.

- Brug KUN på apps/firmware du har rettigheder til at analysere
- Reverse engineering kan være underlagt licensing agreements
- Respektér privacy og data protection laws
- Brug ansvarligt

## 🤝 Contributing

Bidrag er velkomne! Features der kan tilføjes:

- [ ] Support for iOS IPA files
- [ ] Integration med VirusTotal
- [ ] ML-based pattern detection
- [ ] Automated exploit detection
- [ ] API fuzzing capabilities
- [ ] Bluetooth/BLE endpoint discovery
- [ ] Certificate pinning detection

## 📝 License

MIT License - Se LICENSE fil for detaljer

## 🔗 Resources

- [APKTool Documentation](https://ibotpeaches.github.io/Apktool/)
- [Binwalk Documentation](https://github.com/ReFirmLabs/binwalk)
- [JADX Decompiler](https://github.com/skylot/jadx)
- [OWASP Mobile Security](https://owasp.org/www-project-mobile-security/)

---

**Made with ❤️ for Augment scooter security research**
