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
- Alle UUID'er har fast `ff01` identifier på position 2
- Region koder: `38`=København, `40`=Aarhus, `50`=Odense
- Batch numre i 4. segment tillader fleet management
- Muliggør geografisk filtrering uden database lookup!

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

## 📊 UUID Struktur

```
550e8400-ff01-3801-0042-a1b2c3d4e5f6
│        │    │  │ │    └─ Device ID & metadata
│        │    │  │ └────── Batch #66 (0x0042)
│        │    │  └───────── Sub-region 01
│        │    └──────────── Region 38 (København)
│        └───────────────── ff01 (FAST identifier)
└────────────────────────── Deployment ID
```

### Region Koder

| Kode | By | Distrikt |
|------|---------|----------|
| `38` | København | Central |
| `39` | København | Nord |
| `3a` | København | Syd/Vest |
| `40` | Aarhus | Central |
| `41` | Aarhus | Nord |
| `50` | Odense | Central |

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

## 🎉 Nøgle Funktioner

✅ **UUID Parser** - Ekstrahér region, distrikt, batch fra scooter UUID
✅ **Geografisk Filtrering** - Find scootere per by/region uden database
✅ **Fleet Management** - Administrér scooter fleets på tværs af regioner
✅ **Batch Tracking** - Gruppér scootere efter deployment batch
✅ **Firmware Updates** - Prioriteret rollout strategi
✅ **Fleet Analytics** - Batteri statistik, regional distribution
✅ **GraphQL Integration** - Ready-to-use resolvers
✅ **Database Optimization** - Computed columns for hurtig lookup

## 🧪 Test Resultat

```
Test Resultat: 15/15 tests bestået
🎉 Alle tests bestået!
```

## 📝 Licens

Dette projekt er reverse-engineered fra Augment APK til uddannelsesformål og intern brug.

## 👤 Udviklet af

Reverse engineering og dokumentation udført gennem APK analyse.
