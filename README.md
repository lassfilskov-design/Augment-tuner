# Augment E-Scooter Backend System

Komplet backend system til Augment e-scooters med reverse-engineered UUID struktur og API dokumentation.

## 🎯 Projekt Oversigt

Dette projekt indeholder:
- **UUID Parser** - Dekodning af scooter UUID'er til region, distrikt og batch info
- **API Dokumentation** - Komplet GraphQL schema fra Augment backend
- **Integration Guides** - Dansk dokumentation til backend implementation
- **Windows Scripts** - Batch scripts til hurtig setup og test

## 📁 Filer og Dokumentation

### 🔍 UUID Parser (HOVEDOPDAGELSE!)

Efter 2 dages APK reverse engineering opdaget vi UUID strukturen:

| Fil | Beskrivelse |
|-----|-------------|
| `UUID_STRUKTUR.md` | Fuld dokumentation af UUID format og ff01 identifier |
| `uuid-parser.js` | JavaScript implementation af UUID parser |
| `uuid-parser.ts` | TypeScript implementation med type safety |
| `test-uuid-parser.js` | Test suite - 15 comprehensive tests (alle bestået ✓) |
| `EXAMPLES.md` | Praktiske eksempler (GraphQL, React, database) |

**Nøgle Fund:**
- Bluetooth Service UUID: `0000ff01-0000-1000-8000-00805f9b34fb`
- MAC adresse format: `A8:B6:XX:XX:XX:XX` (A8:B6 observeret i Danmark)
- Manufacturer specific data i advertising packet

⚠️ **MULIG TEORI - IKKE VERIFICERET:**
Region koderne (38=København, 40=Aarhus, osv.) og UUID strukturen beskrevet nedenfor er teoretiske hypoteser baseret på begrænsede observationer. De faktiske mønstre kan være anderledes. UUID parseren fungerer som proof-of-concept eksempel, men region mappings er ikke bekræftede.

### 📚 Integration Guides

| Fil | Beskrivelse |
|-----|-------------|
| `INTEGRATION_GUIDE.md` | Dansk guide til backend integration (TypeORM entities, GraphQL) |
| `WINDOWS_SETUP.md` | Windows setup instruktioner for udviklingsmiljø |
| `README_BAT.md` | Dokumentation af Windows batch scripts |

### 🗄️ Schema og Konfiguration

| Fil | Beskrivelse |
|-----|-------------|
| `augment-api-schema.json` | Komplet API schema ekstraheret fra APK |
| `database-schema.json` | Database struktur (devices, users, firmware, etc) |
| `database-schema.csv` | CSV version af database schema |
| `api-config.json` | API endpoints og configuration |

### 🔧 Automatiserings Scripts

| Fil | Beskrivelse |
|-----|-------------|
| `setup.bat` | Initial setup af udviklingsmiljø (Node, PostgreSQL, osv) |
| `start.bat` | Start backend server og services |
| `test-db.bat` | Test database connection og queries |

## 🚀 Quick Start

### 1. Kør Tests
```bash
node test-uuid-parser.js
```

### 2. Parse en Scooter UUID
```javascript
const { AugmentUUIDParser } = require('./uuid-parser.js');

const parser = new AugmentUUIDParser('550e8400-ff01-3801-0042-a1b2c3d4e5f6');

console.log(parser.getDistrictName());  // "København Central"
console.log(parser.getBatchNumber());   // 66
console.log(parser.getCityName());      // "København"
console.log(parser.isValid());          // true (har ff01)
```

### 3. Fleet Management
```javascript
const { AugmentFleetManager } = require('./uuid-parser.js');

const fleet = new AugmentFleetManager(devices);

// Find alle scootere i København
const cphScooters = fleet.getDevicesByCity('København');

// Hent statistik per region
const stats = fleet.getRegionStats();

// Firmware update queue (prioriteret)
const updateQueue = fleet.getFirmwareUpdateQueue('v2.5.0', {
  region: '38',
  maxDevices: 50,
  prioritizeLowBatch: true
});
```

## 📊 UUID Struktur (TEORETISK MODEL)

⚠️ **ADVARSEL**: Følgende struktur er en teoretisk model, ikke verificeret data.

```
550e8400-ff01-3801-0042-a1b2c3d4e5f6
│        │    │  │ │    └─ Device ID & metadata (HYPOTESE)
│        │    │  │ └────── Batch #66 (HYPOTESE)
│        │    │  └───────── Sub-region 01 (HYPOTESE)
│        │    └──────────── Region 38 (HYPOTESE)
│        └───────────────── ff01 (Bluetooth Service UUID)
└────────────────────────── Deployment ID (HYPOTESE)
```

### Faktiske Observationer

**Bekræftet:**
- MAC adresse: `A8:B6:66:52:52:40`
- Service UUID: `0000ff01-0000-1000-8000-00805f9b34fb`
- A8:B6 prefix observeret i danske scootere
- Manufacturer data: `0x5266B6A8` (little endian muligvis)

**Ikke bekræftet (Teoretiske region koder):**

| Kode | By | Distrikt | Status |
|------|---------|----------|--------|
| `38` | København | Central | ⚠️ TEORI |
| `39` | København | Nord | ⚠️ TEORI |
| `3a` | København | Syd/Vest | ⚠️ TEORI |
| `40` | Aarhus | Central | ⚠️ TEORI |
| `41` | Aarhus | Nord | ⚠️ TEORI |
| `50` | Odense | Central | ⚠️ TEORI |

## 🔧 Teknisk Stack

- **Backend**: Node.js / TypeScript
- **Database**: PostgreSQL (UUID support)
- **API**: GraphQL (schema i `augment-api-schema.json`)
- **ORM**: TypeORM (entities i `INTEGRATION_GUIDE.md`)
- **Payment**: Chargebee integration
- **Auth**: AWS Cognito

## 📖 Læs Mere

- Se `UUID_STRUKTUR.md` for komplet UUID dokumentation
- Se `EXAMPLES.md` for kode eksempler (GraphQL, React, osv)
- Se `INTEGRATION_GUIDE.md` for backend integration guide (dansk)
- Se `augment-api-schema.json` for komplet API schema

## 🎉 Nøgle Funktioner (Proof-of-Concept)

⚠️ **Note**: Funktionerne nedenfor er proof-of-concept implementeringer baseret på teoretiske modeller.

📋 **UUID Parser** - Eksempel implementation af UUID parsing
📋 **Geografisk Filtrering** - Teoretisk model for region filtrering
📋 **Fleet Management** - Framework til fleet management (kræver verificerede data)
📋 **Batch Tracking** - Koncept for batch gruppering
📋 **Firmware Updates** - Eksempel på prioriteret rollout strategi
📋 **Fleet Analytics** - Statistik framework (kræver reelle region mappings)
📋 **GraphQL Integration** - Eksempel resolvers
📋 **Database Optimization** - Foreslåede optimeringer

## 🧪 Test Resultat

```
Test Resultat: 15/15 tests bestået
🎉 Alle tests bestået!
```

⚠️ **Note**: Tests validerer kun at parseren fungerer med de teoretiske UUID formater. Tests bekræfter IKKE at region mappings er korrekte.

---

## 🔬 Hvad er Fakta vs. Teori?

### ✅ Verificeret (Faktiske Observationer):
- Bluetooth Service UUID: `0000ff01-0000-1000-8000-00805f9b34fb`
- MAC adresse eksempel: `A8:B6:66:52:52:40`
- A8:B6 prefix set i danske enheder
- Manufacturer specific data: `0x5266B6A8`
- GraphQL API schema fra APK
- Database struktur (users, devices, firmware, etc.)

### ⚠️ Teoretisk (Ikke Verificeret):
- UUID region koder (38, 40, 50 osv.)
- By/distrikt mappings (København, Aarhus, osv.)
- Batch nummer position i UUID
- Little endian konvertering hypotese
- Deployment ID struktur

### 📊 Mangler Data:
- Flere MAC adresse eksempler fra forskellige lande
- Verifikation af region kode mønstre
- Batch nummer system dokumentation
- Manufacturer data format specifikation

---

## 📝 Licens

Dette projekt er reverse-engineered fra Augment APK til uddannelsesformål og intern brug.

## 👤 Udviklet af

Reverse engineering og dokumentation udført gennem APK analyse.
