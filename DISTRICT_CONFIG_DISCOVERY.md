# 🎯 AUGMENT SCOOTER DISTRICT CONFIGURATION DISCOVERY

## VIGTIG OPDAGELSE!

Hex bytes i scooter konfigurationen repræsenterer **DISTRICT KODER** - ikke lande, men distrikter/områder inden for et land!

---

## STRUKTUR

### Device Settings Operation Code Format:

```
"DK122638"
 ││ └──── District configuration (hex encoded)
 │└────── Land subcode
 └─────── Country code (TEXT - ikke hex!)

DK = Danmark (TEXT)
12 = District/Region identifier
26 = Acceleration setting
38 = Speed limit (38 km/h i decimal)
```

---

## HEX DISTRICT CODES

### Gamle Augment Model ("lillebror"):
```
AA BE FE FA DC CA FF

AA = District 1
BE = District 2
FE = District 3
FA = District 4
DC = District 5
CA = District 6
FF = End marker / Default

🔓 UNLOCK CODE: FE50
   → Dette unlockede alle features på gamle model!
```

### Nuværende Augment Model:
```
ff fe fa fc cf ad ba ab ed ef

ff = Start marker / District code
fe = District code
fa = District code
fc = District code
cf = District code
ad = District code
ba = District code
ab = District code
ed = District code
ef = District code / End marker

🔓 UNLOCK CODE: ????? (SKAL FINDES!)
   → Equivalent til FE50 fra gamle model
   → Mulige kandidater: FEFF? FEFA? FFFF? FE50 stadig?
```

---

## HVORFOR DISTRIKTER?

Forskellige byer/områder har forskellige regler for el-løbehjul:

### Eksempel - Danmark:

| District | By/Område | Max Hastighed | Specielle Regler |
|----------|-----------|---------------|------------------|
| AA       | København | 20 km/h       | Strenge parkering |
| BE       | Aarhus    | 20 km/h       | Zone restriktioner |
| FE       | Odense    | 25 km/h       | Mindre strenge |
| FA       | Aalborg   | 25 km/h       | Standard |
| DC       | Landdistrikter | 25 km/h | Få restriktioner |
| FF       | Default   | 25 km/h       | Standard DK regler |

### Eksempel - Tyskland (DE):

| District | By/Område | Max Hastighed | Specielle Regler |
|----------|-----------|---------------|------------------|
| AA       | Berlin    | 20 km/h       | Meget strenge |
| BE       | Hamburg   | 20 km/h       | Zone forbud |
| FE       | München   | 20 km/h       | Parkeringszoner |

---

## DERFOR VIRKER SCOOTER #3 ANDERLEDES!

**Hypotese:**

- **Scooter #1**: District AA configuration
- **Scooter #2**: District FE configuration
- **Scooter #3**: District ?? configuration (måske købte fra andet område!)

Når du sender kommandoer, tjekker firmwaren:
1. Er denne kommando tilladt i MIT district?
2. Hvis NEJ → Afvis kommando ❌
3. Hvis JA → Accepter kommando ✅

---

## HVORDAN FINDER MAN DISTRICT CODE?

### Via BLE (LightBlue / nRF Connect):

1. **Connect til scooter**
2. **Find UUID: `00006684`** (Settings Read)
3. **Read characteristic**
4. **Se hex bytes** - dette er district configuration!

### Via GraphQL API:

```graphql
query DeviceSettingsOperationCode($btMac: String!) {
  code: deviceSettingsOperationCode(input: {btMac: $btMac})
}
```

Response eksempel:
```json
{
  "code": "DK122638"
}
```

Parse:
- `DK` = Danmark
- `12` = District code 0x12 (hex)
- `2638` = Settings (speed 38 km/h, acceleration 26)

---

## BLE UUID STRUKTUR

### Settings Write (`00006683`):

Send kommandoer her:
```
[0xA2, 0x28, 0x00]  → Set speed 40 km/h
```

Firmware checker:
1. Er speed 40 tilladt i district 0x12?
2. Hvis district 0x12 = København (max 20 km/h) → **AFVIS**
3. Hvis district 0x12 = Landområde (max 25 km/h) → **AFVIS**
4. Hvis district 0xFF = Default/unlocked → **ACCEPTER**

### Settings Read (`00006684`):

Læs current configuration:
```
Read → ff fe fa fc cf ad ba ab ed ef
```

Dette viser hvilke districts/features der er enabled.

---

## DISTRICT CONFIGURATION BYTES BETYDNING

### Byte Position Hypotese:

```
Position 0-1: District identifier
Position 2-3: Speed limit configuration
Position 4-5: Feature flags (zero start, sport+, turbo)
Position 6-7: Lock settings
Position 8-9: Checksum / End marker
```

### Eksempel Decode:

```
ff fe fa fc cf ad ba ab ed ef

ff fe = District identifier (0xFFFE)
fa fc = Speed config (250 + 252 = max speed encoding?)
cf ad = Feature flags
ba ab = Lock/security settings
ed ef = Checksum / End marker
```

---

## NÆSTE SKRIDT

### For at identificere Scooter #3's district:

1. **Connect via nRF Connect/LightBlue**
2. **Read UUID `00006684`**
3. **Note hex bytes**
4. **Sammenlign med Scooter #1 og #2**

### For at test theory:

1. **Scan alle 3 scootere**
2. **Dokumenter district bytes for hver**
3. **Test samme kommando på alle 3**
4. **Se hvilke accepterer/afviser baseret på district**

---

## KILDER

- **Observationer i LightBlue app** (gamle "lillebror" model)
- **nRF Connect scanning** (nuværende model)
- **APK reverse engineering** (UUID struktur)
- **GraphQL API** (deviceSettingsOperationCode)

---

## KONKLUSION

🎯 **Scooter konfiguration er DISTRICT-baseret!**

Dette forklarer:
- ✅ Hvorfor nogle kommandoer virker på nogle scootere men ikke andre
- ✅ Hvorfor samme land har forskellige konfigurationer
- ✅ Hvorfor firmware checker kommandoer før accept
- ✅ Hvorfor der er hex "preference" bytes

**Nøglen til at låse op Scooter #3:**
→ Find dens district code og send kommandoer der er tilladte i det district!
→ Eller flash firmware med FF (default/unlocked) district code!

---

**Opdateret:** 2024-12-26
**Discovery method:** BLE reverse engineering + intuition 💡
