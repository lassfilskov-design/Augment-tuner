# AUGMENT 8-TEGN IDENTIFIKATIONSKODE

## 🎯 DET ER DIN "deviceSettingsOperationCode"!

**Når du bliver genkendt i Augment systemet får du en 8-tegn kode:**

```
[CC][XX][SSAA]
 ↑   ↑    ↑
 |   |    └─ Speed + Acceleration (4 chars)
 |   └────── Unknown (2 chars)
 └────────── Country Code (2 chars)
```

---

## 📡 **Query fra GraphQL:**

### Method 1: Via GraphQL API

```bash
# 1. Hent auth token først (fra Augment app)
# Se FIRMWARE_MODDING_GUIDE.md

# 2. Query operation code
curl -X POST https://frbc72oc4h.execute-api.eu-west-1.amazonaws.com/prod/graphql-public \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "query DeviceSettingsOperationCode($btMac: String!) { code: deviceSettingsOperationCode(input: {btMac: $btMac}) }",
    "variables": {
      "btMac": "XX:XX:XX:XX:XX:XX"
    }
  }'

# Response:
{
  "data": {
    "code": "DK122638"  # ← Din 8-tegn kode!
  }
}
```

### Eksempel breakdown:

```
Code: "DK122638"
       │  │  └─ 38 = Speed limit (38 km/h)
       │  └──── 26 = Acceleration?
       └─────── DK = Country (Danmark)

Code: "US104540"
       │  │  └─ 40 = Speed limit (40 km/h)
       │  └──── 45 = Acceleration?
       └─────── US = Country (USA)
```

---

## 🧪 **Test: Find DIN kode nu**

```bash
# Indsæt din scooter MAC:
MAC="XX:XX:XX:XX:XX:XX"

# Indsæt din auth token (fra Augment app):
TOKEN="eyJhbGc..."

# Query kode:
curl -X POST https://frbc72oc4h.execute-api.eu-west-1.amazonaws.com/prod/graphql-public \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"query\": \"query DeviceSettingsOperationCode(\$btMac: String!) { code: deviceSettingsOperationCode(input: {btMac: \$btMac}) }\",
    \"variables\": {
      \"btMac\": \"$MAC\"
    }
  }"
```

---

## 📊 **Mulige Country Codes:**

```
DK = Danmark (25 km/h legal, 38 km/h hacked)
SE = Sverige (20 km/h legal)
NO = Norge (20 km/h legal)
DE = Tyskland (20 km/h legal)
FR = Frankrig (25 km/h legal)
NL = Holland (25 km/h legal)
US = USA (varies by state)
UK = UK (15.5 mph / 25 km/h legal)
```

---

## 🔓 **Hvordan ændre koden?**

### Option A: Via GraphQL (hvis muligt)

```graphql
mutation UpdateDeviceSettingsOperationCode($input: UpdateCodeInput!) {
  updateDeviceSettingsOperationCode(input: $input) {
    code
  }
}
```

**Variables:**
```json
{
  "input": {
    "btMac": "XX:XX:XX:XX:XX:XX",
    "code": "DK125045"  # ← Ny kode: DK, 50, 45
  }
}
```

### Option B: Direkte BLE (bedre!)

Hvis koden bare er et "alias" for de reelle indstillinger:

```kotlin
// I stedet for at ændre koden, send direkte kommandoer:

// Speed: 50 km/h (sidste 2 cifre i koden)
ble.setSpeed(50)  // → A2 32 00

// Acceleration: Max (midterste 2 cifre?)
ble.setSportPlus(true)   // → A3 01
ble.setTurbo(true)       // → A4 01
```

---

## ❓ **Hvad betyder midterste 2 cifre?**

Theories:

**Theory 1: Acceleration preset**
```
10 = Eco mode (lav acceleration)
12 = Normal mode
26 = Sport mode
45 = Turbo mode
```

**Theory 2: Hardware revision**
```
10 = Rev. 1.0
12 = Rev. 1.2
26 = Rev. 2.6
```

**Theory 3: Firmware version**
```
10 = v1.0
12 = v1.2
26 = v2.6
```

---

## 🎯 **NÆSTE SKRIDT:**

**1. Find DIN kode:**
```bash
# Kør GraphQL query ovenfor med din MAC
```

**2. Del koden med mig:**
```
Din kode: ________
```

**3. Jeg decipherer den!**

**HAR DU DIN SCOOTER MAC OG AUTH TOKEN KLAR?** 🚀
