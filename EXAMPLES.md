# 📚 Augment Scanner - Praktiske Eksempler

## Quick Start Eksempler

### 1. Scan en enkelt APK fil

```bash
# Basic scan
python scanner.py augment_scooter.apk

# Output:
# [*] Scanning APK: augment_scooter.apk
# [*] Extracting APK...
# [*] Scanning directory...
# [+] Report saved to: scan_results/augment_scooter_report.json
#
# ================================================================================
# SECURITY SCAN RESULTS: augment_scooter.apk
# ================================================================================
# [!] Passwords/Credentials: 12
# [!] URLs Found: 45
# [!] Backend Endpoints: 23
# [!] Sensitive Files: 8
# [!] Red Flags: 15
```

### 2. Start web interface

```bash
python web_app.py

# Åbn browser på http://localhost:5000
# Træk og slip din APK fil
# Se resultater i real-time!
```

### 3. Brug CLI tool

```bash
# Scan
./augment-scan scan my_app.apk

# Extract firmware
./augment-scan extract firmware.bin

# Decompile APK
./augment-scan decompile my_app.apk

# Batch scan
./augment-scan batch ./apk_folder/

# Start web
./augment-scan web --port 8080
```

## Avancerede Use Cases

### Find API Endpoints fra Augment App

```bash
# 1. Download Augment app APK fra device eller APK mirror
adb pull /data/app/com.augment.scooter/base.apk augment.apk

# 2. Scan APK
python scanner.py augment.apk

# 3. Se backend endpoints
cat scan_results/augment_report.json | jq '.findings.backends'

# Output eksempel:
# [
#   {
#     "file": "resources.arsc/res/values/strings.xml",
#     "endpoint": "https://api.augment.eco/v1/scooters",
#     "line": 42
#   },
#   {
#     "file": "classes.dex",
#     "endpoint": "/api/v2/unlock",
#     "line": 1337
#   }
# ]
```

### Find Hardcoded API Keys

```bash
# Scan og filter efter passwords
python scanner.py app.apk

# Se kun passwords i JSON
cat scan_results/app_report.json | jq '.findings.passwords'

# Output eksempel:
# [
#   {
#     "file": "res/values/config.xml",
#     "match": "api_key=\"sk_live_abc123xyz\"",
#     "value": "sk_live_abc123xyz",
#     "line": 15
#   }
# ]
```

### Map Hele Backend Infrastruktur

```python
# custom_scan.py
from scanner import SecurityScanner
import json

scanner = SecurityScanner()
report = scanner.scan_apk("augment.apk")

# Extract alle unikke URLs
urls = set()
for finding in report['findings']['urls']:
    urls.add(finding['url'])

# Extract alle backend endpoints
endpoints = set()
for finding in report['findings']['backends']:
    endpoints.add(finding['endpoint'])

# Print backend map
print("=== BACKEND MAP ===")
print("\nBase URLs:")
for url in sorted(urls):
    if 'api' in url.lower() or 'augment' in url.lower():
        print(f"  - {url}")

print("\nAPI Endpoints:")
for endpoint in sorted(endpoints):
    print(f"  - {endpoint}")

# Gem til fil
with open('backend_map.json', 'w') as f:
    json.dump({
        'urls': list(urls),
        'endpoints': list(endpoints)
    }, f, indent=2)
```

### Extract og Analyser Firmware

```bash
# Download firmware update fra scooter
# (Dette er eksempel - faktisk metode afhænger af scooter model)

# Extract firmware
python firmware_tools.py scooter_firmware.bin

# Se firmware type
# [*] Firmware Type: U-Boot Image

# Hvis binwalk er installeret, får du automatisk extraction:
# [*] Extraction Method: binwalk
#     Status: success
#     Output: firmware_extracted/scooter_firmware_binwalk

# Find interessante strings
python -c "
from firmware_tools import FirmwareExtractor, find_interesting_strings

extractor = FirmwareExtractor()
strings = extractor.get_strings('scooter_firmware.bin')
interesting = find_interesting_strings(strings)

print(f'URLs: {len(interesting[\"urls\"])}')
print(f'IPs: {len(interesting[\"ips\"])}')
print(f'Keys: {len(interesting[\"keys\"])}')

# Print første 10 URLs
for url in interesting['urls'][:10]:
    print(url)
"
```

### Batch Scan Multiple APKs

```bash
# Opret mappe med APK filer
mkdir apk_collection
# (Kopier APK filer hertil)

# Batch scan
python batch_scan.py apk_collection/ combined_report.json

# Output:
# [*] Found 5 APK files to scan
# [1/5] Scanning app1.apk
# [2/5] Scanning app2.apk
# ...
# [+] Batch scan complete!
#
# === BATCH SUMMARY ===
# Total APKs scanned: 5
# Successful: 5
# Failed: 0
# Total passwords found: 23
# Total URLs found: 156
# Total backends found: 78

# Se combined report
cat combined_report.json | jq '.summary'
```

### Web API Integration

```python
# api_client.py
import requests

# Upload APK via API
files = {'file': open('augment.apk', 'rb')}
response = requests.post('http://localhost:5000/upload', files=files)

if response.ok:
    data = response.json()
    report = data['report']['security_scan']

    print(f"Passwords: {report['summary']['passwords_found']}")
    print(f"URLs: {report['summary']['urls_found']}")
    print(f"Backends: {report['summary']['backends_found']}")

    # Get all API endpoints
    for backend in report['findings']['backends']:
        print(f"Endpoint: {backend['endpoint']}")
else:
    print(f"Error: {response.json()['error']}")

# List alle reports
response = requests.get('http://localhost:5000/reports')
reports = response.json()['reports']

for report in reports:
    print(f"{report['apk']}: {report['summary']}")

# Download specific report
response = requests.get('http://localhost:5000/download/augment_report.json')
with open('downloaded_report.json', 'wb') as f:
    f.write(response.content)
```

### Custom Pattern Detection

```python
# custom_patterns.py
from scanner import SecurityScanner
import re

scanner = SecurityScanner()

# Tilføj Augment-specifikke patterns
scanner.password_patterns.extend([
    r'scooter[_-]?key\s*=\s*["\']([^"\']+)["\']',
    r'unlock[_-]?token\s*=\s*["\']([^"\']+)["\']',
    r'ride[_-]?secret\s*=\s*["\']([^"\']+)["\']',
])

scanner.backend_patterns.extend([
    r'/scooter/[^\s\'"<>]+',
    r'/unlock/[^\s\'"<>]+',
    r'/ride/[^\s\'"<>]+',
])

scanner.red_flags.extend([
    r'bypass.*lock',
    r'free.*ride',
    r'skip.*payment',
])

# Scan med custom patterns
report = scanner.scan_apk('augment.apk')

# Custom analysis
print("\n=== CUSTOM FINDINGS ===")
for pwd in report['findings']['passwords']:
    if 'scooter' in pwd['match'].lower() or 'unlock' in pwd['match'].lower():
        print(f"Scooter-related credential: {pwd['match']}")
```

### Find Bluetooth/BLE Endpoints

```python
# ble_finder.py
from scanner import SecurityScanner

scanner = SecurityScanner()

# Tilføj BLE patterns
scanner.url_patterns.extend([
    r'[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}',  # UUID
])

scanner.backend_patterns.extend([
    r'CHARACTERISTIC_[A-Z_]+',
    r'SERVICE_UUID\s*=\s*["\']([^"\']+)["\']',
])

report = scanner.scan_apk('augment.apk')

# Find BLE UUIDs
print("\n=== BLE SERVICES ===")
for finding in report['findings']['backends'] + report['findings']['urls']:
    value = finding.get('endpoint', finding.get('url', ''))
    if len(value) == 36 and value.count('-') == 4:  # UUID format
        print(f"BLE UUID: {value}")
```

### Security Audit Report Generator

```python
# audit_report.py
from scanner import SecurityScanner
from datetime import datetime
import json

def generate_audit_report(apk_path, output_html='audit_report.html'):
    scanner = SecurityScanner()
    report = scanner.scan_apk(apk_path)

    # Calculate severity
    severity_score = 0
    severity_score += report['summary']['passwords_found'] * 10
    severity_score += report['summary']['red_flags_found'] * 5
    severity_score += report['summary']['sensitive_files_found'] * 3

    severity = "LOW"
    if severity_score > 50:
        severity = "CRITICAL"
    elif severity_score > 25:
        severity = "HIGH"
    elif severity_score > 10:
        severity = "MEDIUM"

    # Generate HTML report
    html = f"""
    <html>
    <head><title>Security Audit Report</title></head>
    <body>
        <h1>Security Audit Report</h1>
        <p>APK: {report['apk']}</p>
        <p>Date: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
        <p>Severity: <strong>{severity}</strong> (Score: {severity_score})</p>

        <h2>Summary</h2>
        <ul>
            <li>Passwords/Credentials: {report['summary']['passwords_found']}</li>
            <li>URLs: {report['summary']['urls_found']}</li>
            <li>Backend Endpoints: {report['summary']['backends_found']}</li>
            <li>Sensitive Files: {report['summary']['sensitive_files_found']}</li>
            <li>Red Flags: {report['summary']['red_flags_found']}</li>
        </ul>

        <h2>Critical Findings</h2>
    """

    # Add passwords
    if report['findings']['passwords']:
        html += "<h3>Hardcoded Credentials</h3><ul>"
        for pwd in report['findings']['passwords'][:10]:
            html += f"<li><code>{pwd['file']}:{pwd['line']}</code> - {pwd['match']}</li>"
        html += "</ul>"

    # Add red flags
    if report['findings']['red_flags']:
        html += "<h3>Security Issues</h3><ul>"
        for flag in report['findings']['red_flags'][:10]:
            html += f"<li><code>{flag['file']}:{flag['line']}</code> - {flag['issue']}</li>"
        html += "</ul>"

    html += """
    </body>
    </html>
    """

    with open(output_html, 'w') as f:
        f.write(html)

    print(f"[+] Audit report saved to {output_html}")
    print(f"[!] Severity: {severity} (Score: {severity_score})")

# Generate report
generate_audit_report('augment.apk')
```

## Tips & Tricks

### Tip 1: Find Server IPs

```bash
# Extract strings og find IP addresses
python firmware_tools.py app.apk > /dev/null
strings scan_results/*/strings.txt | grep -E '\b([0-9]{1,3}\.){3}[0-9]{1,3}\b'
```

### Tip 2: Monitor Web Scans

```bash
# Start web interface i background
python web_app.py &

# Watch scan_results directory
watch -n 1 'ls -lth scan_results/ | head -10'
```

### Tip 3: Quick JSON Analysis

```bash
# Find alle unikke URLs
jq -r '.findings.urls[].url' scan_results/*_report.json | sort -u

# Count findings per type
jq '.summary' scan_results/*_report.json

# Find high-risk passwords
jq '.findings.passwords[] | select(.match | contains("admin") or contains("root"))' scan_results/*_report.json
```

### Tip 4: Compare Two APK Versions

```python
# compare_apks.py
from scanner import SecurityScanner
import json

scanner1 = SecurityScanner(output_dir="scan_v1")
report1 = scanner1.scan_apk("app_v1.0.apk")

scanner2 = SecurityScanner(output_dir="scan_v2")
report2 = scanner2.scan_apk("app_v2.0.apk")

# Compare findings
print("\n=== COMPARISON ===")
print(f"Passwords: v1={report1['summary']['passwords_found']} vs v2={report2['summary']['passwords_found']}")
print(f"Red Flags: v1={report1['summary']['red_flags_found']} vs v2={report2['summary']['red_flags_found']}")

# Find new endpoints
urls1 = {f['url'] for f in report1['findings']['urls']}
urls2 = {f['url'] for f in report2['findings']['urls']}

new_urls = urls2 - urls1
removed_urls = urls1 - urls2

print(f"\nNew URLs in v2: {len(new_urls)}")
for url in new_urls:
    print(f"  + {url}")

print(f"\nRemoved URLs from v1: {len(removed_urls)}")
for url in removed_urls:
    print(f"  - {url}")
```

## Troubleshooting

### Problem: "No module named 'flask'"

```bash
# Solution: Install dependencies
pip install -r requirements.txt
```

### Problem: APK extraction fails

```bash
# Solution: Check file is valid APK
file my_app.apk
# Should show: "Zip archive data"

# Try manual extraction
unzip -l my_app.apk
```

### Problem: Binwalk not found

```bash
# Solution: Install binwalk
sudo apt install binwalk

# Or skip binwalk (scanner will use ZIP extraction)
```

### Problem: Web interface port already in use

```bash
# Solution: Use different port
python web_app.py --port 8080

# Or find what's using port 5000
lsof -i :5000
```

## Mere Info

Se [README.md](README.md) for fuld dokumentation.
