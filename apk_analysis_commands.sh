#!/bin/bash
# APK Reverse Engineering Commands for Augment Scooter App

# ============================================
# 1. EXTRACT APK
# ============================================

# Extract APK with apktool
apktool d augment.apk -o augment-extracted

# Or manually extract with unzip
unzip augment.apk -d augment-extracted

# ============================================
# 2. FIND BLE SERVICE UUIDs
# ============================================

# Search for all BLE service UUIDs
strings augment-extracted/assets/index.android.bundle | grep -E "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}"

# Search for specific Augment services
strings augment-extracted/assets/index.android.bundle | grep "6680\|6683\|6684\|6688"

# ============================================
# 3. FIND SPEED LIMIT REFERENCES
# ============================================

# Find speed limit variables
strings augment-extracted/assets/index.android.bundle | grep -i "speedlimit"

# Find specific speed variables
strings augment-extracted/assets/index.android.bundle | grep "speedLimitKmhMaster\|speedLimitKmhCurrent"

# Find speed setting function
strings augment-extracted/assets/index.android.bundle | grep "setConnectedDeviceSpeed"

# ============================================
# 4. FIND WHEEL SIZE REFERENCES
# ============================================

# Search for wheel configuration
strings augment-extracted/assets/index.android.bundle | grep -i "wheel\|diameter\|circumference"

# ============================================
# 5. FIND LOCK/UNLOCK COMMANDS
# ============================================

# Find lock-related strings
strings augment-extracted/assets/index.android.bundle | grep -E "lock|unlock" | grep -v "http\|css"

# Find hex lock commands
strings augment-extracted/assets/index.android.bundle | grep "lockHX\|unlockHX"

# ============================================
# 6. FIND HEX PATTERNS & COMMANDS
# ============================================

# Extract all hex values
strings augment-extracted/assets/index.android.bundle | grep -E "0x[0-9A-Fa-f]{2}"

# Find buffer encoding functions
strings augment-extracted/assets/index.android.bundle | grep "toBuf\|toBuffer"

# Find device settings buffer function
strings augment-extracted/assets/index.android.bundle | grep "deviceSettingsToBuf"

# ============================================
# 7. FIND CHARACTERISTIC OPERATIONS
# ============================================

# Find BLE characteristic operations
strings augment-extracted/assets/index.android.bundle | grep "characteristic" | head -30

# Find write operations
strings augment-extracted/assets/index.android.bundle | grep "writeCharacteristic"

# Find read operations
strings augment-extracted/assets/index.android.bundle | grep "readCharacteristic"

# ============================================
# 8. ANALYZE SPECIFIC VALUES
# ============================================

# Search for specific speed values (25, 39, 45 km/h)
strings augment-extracted/assets/index.android.bundle | grep -E "\b25\b|\b39\b|\b45\b" | grep -i "km\|speed"

# Search for wheel sizes (254mm = 10", 279mm = 11")
strings augment-extracted/assets/index.android.bundle | grep -E "\b254\b|\b279\b"

# ============================================
# 9. DECOMPILE DEX FILES (Advanced)
# ============================================

# Convert DEX to JAR (requires dex2jar)
d2j-dex2jar augment-extracted/classes.dex -o augment-classes.jar

# Decompile JAR with JD-GUI or jadx
jadx augment-extracted/classes.dex -d augment-decompiled

# Search in decompiled Java code
grep -r "speedLimit" augment-decompiled/
grep -r "BleManager" augment-decompiled/

# ============================================
# 10. EXTRACT RESOURCES
# ============================================

# List all XML resources
find augment-extracted/res -name "*.xml" | head -20

# Search AndroidManifest for permissions
grep "permission" augment-extracted/AndroidManifest.xml

# ============================================
# 11. MONITOR BLE TRAFFIC (Live Testing)
# ============================================

# Use Android BLE sniffer (requires rooted device + Wireshark)
# 1. Enable HCI snoop log: Developer Options → Enable Bluetooth HCI snoop log
# 2. Pull log: adb pull /sdcard/btsnoop_hci.log
# 3. Open in Wireshark and filter: bluetooth.uuid == 0x6683

# ============================================
# 12. USEFUL FILTERS & SEARCHES
# ============================================

# Find all functions containing "speed"
strings augment-extracted/assets/index.android.bundle | grep -i "speed" | grep "function\|def\|class"

# Find all error messages (useful for understanding flow)
strings augment-extracted/assets/index.android.bundle | grep -i "error\|fail\|invalid" | head -50

# Find configuration objects
strings augment-extracted/assets/index.android.bundle | grep -E "config|settings|options" | head -50

# ============================================
# NOTES
# ============================================

# All speed values in APK are DECIMAL, converted to hex at BLE transmission
# Wheel sizes are in millimeters (mm) as decimal integers
# Commands follow format: [CommandType, Data...]
# Command types found: 0xA1, 0xA2, 0xB1, 0xC1, 0xD1
