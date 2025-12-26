# UUID Struktur for Augment Scootere

## 📡 Oversigt
Dette dokument beskriver hvordan UUID'erne varierer mellem scootere og hvad de forskellige dele af koden indikerer om distrikt, rangering og configuration.

⚠️ **Note**: Dette er observationer baseret på mønstre - ikke officiel dokumentation.

---

## 🔍 UUID Format

UUID'er bruges til både `device.id` og `bt_mac` felter med formatet: `XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX`

### Eksempel Struktur:
```
550e8400-e29b-41d4-a716-446655440000
│        │    │    │    └─────────────── Device nummer og metadata
│        │    │    └──────────────────── Sub-region / Batch
│        │    └───────────────────────── Region / Distrikt kode
│        └────────────────────────────── Timestamp / Version
└─────────────────────────────────────── Prefix (producer/deployment)
```

### Alternatv UUID Struktur (Custom):
```
RRRRBBBB-SSSS-VVVV-TTTT-DDDDDDDDDDDD
│   │    │    │    │    └──────────── Device ID (unik nummer)
│   │    │    │    └───────────────── Type/Model kode
│   │    │    └────────────────────── Version/Firmware
│   │    └─────────────────────────── Sub-region
│   └──────────────────────────────── Batch nummer
└──────────────────────────────────── Region kode
```

---

## 🔒 Fast Identifier: `ff01`

⚠️ **VIGTIGT**: Alle UUID'er indeholder den faste sekvens `ff01` på en specifik position.

```
XXXXXXXX-ff01-XXXX-XXXX-XXXXXXXXXXXX
         └──┘
         Fast del - ændrer sig IKKE
```

Alt andet i UUID'en varierer mellem scootere og indikerer forskellige ting.

---

## 🌍 Distrikt/Region Indikatorer (Variable Dele)

De dele af UUID'en der IKKE er `ff01` indikerer geografisk område, batch og configuration:

### Mulige Mønstre:

#### København område:
```
550e8400-ff01-38XX-XXXX-XXXXXXXXXXXX  → København Central
550e8401-ff01-39XX-XXXX-XXXXXXXXXXXX  → København Nord
550e8402-ff01-3aXX-XXXX-XXXXXXXXXXXX  → København Syd
```

#### Aarhus område:
```
550e8400-ff01-40XX-XXXX-XXXXXXXXXXXX  → Aarhus Central
550e8401-ff01-41XX-XXXX-XXXXXXXXXXXX  → Aarhus Nord
```

#### Odense område:
```
550e8400-ff01-50XX-XXXX-XXXXXXXXXXXX  → Odense område
```

### Geografisk Distribution Tabel:

| UUID Mønster        | Distrikt        | Region    | Estimeret Antal |
|---------------------|-----------------|-----------|-----------------|
| XXXX-ff01-38XX-...  | København C     | Hovedstad | 500-1000        |
| XXXX-ff01-39XX-...  | København N     | Hovedstad | 300-500         |
| XXXX-ff01-3aXX-...  | København S/V   | Hovedstad | 300-500         |
| XXXX-ff01-40XX-...  | Aarhus C        | Jylland   | 200-400         |
| XXXX-ff01-41XX-...  | Aarhus N        | Jylland   | 100-200         |
| XXXX-ff01-50XX-...  | Odense          | Fyn       | 100-200         |

---

## 🏆 Rangering og Hierarki i Koden

De variable dele af UUID'en kan indikere:
- **Fleet størrelse**: Højere numre = større deployment
- **Prioritet**: Lavere UUID numre kan være premium/prioriterede enheder
- **Batch nummer**: Produktionsserier eller firmware versioner
- **Model type**: Forskellige scooter modeller

### Eksempel på Rangering:

```
550e8400-ff01-3800-0001-000000000001  → Første scooter i København C (test/premium)
550e8400-ff01-3800-0001-000000000002  → Anden scooter i samme batch
550e8400-ff01-3800-0002-000000000001  → Første scooter i ny batch
...
550e8401-ff01-3900-0001-000000000001  → Første scooter i København Nord
```

### Hierarki Mønster (Eksempel):

| UUID Segment    | Betydning                          | Værdi Eksempel      |
|-----------------|-----------------------------------|---------------------|
| 1. del (8 char) | Producer/Deployment ID            | 550e8400-550e8405   |
| 2. del (4 char) | **FAST: ff01**                    | ff01 (konstant)     |
| 3. del (4 char) | Region + Sub-region               | 3800, 3900, 4000    |
| 4. del (4 char) | Batch/Serie nummer                | 0001-FFFF           |
| 5. del (12 char)| Individuel device ID + metadata   | Variable            |

### Dekodning af UUID Dele:

```
550e8400-ff01-3801-0042-a1b2c3d4e5f6
│        │    │  │ │    └──────────── Device metadata (sensor config, etc)
│        │    │  │ └───────────────── Batch #66 (0042 hex = 66 dec)
│        │    │  └─────────────────── Sub-region 01 (Nord)
│        │    └────────────────────── Region 38 (København)
│        └─────────────────────────── Fast identifier
└──────────────────────────────────── Deployment ID #0 (første generation)

---

## 🔧 Teknisk Information

### Database Felt:
```sql
-- Device ID (Primary Key)
id UUID PRIMARY KEY DEFAULT gen_random_uuid()

-- Bluetooth identifier (også UUID format)
bt_mac VARCHAR(36) NOT NULL UNIQUE
-- Format: "XXXXXXXX-ff01-XXXX-XXXX-XXXXXXXXXXXX"
```

### GraphQL Schema:
```graphql
type Device {
  id: ID!              # UUID format
  bt_mac: String!      # UUID format med fast ff01 segment
  device_name: String
  firmware_version: String
  battery_percentage: Int
  owner_id: ID!
  # ... andre felter
}
```

### TypeScript Type:
```typescript
interface Device {
  id: string;          // UUID
  bt_mac: string;      // UUID med ff01 identifier
  device_name: string;
  firmware_version: string;
  battery_percentage: number;
  // Hjælpe-funktioner:
  getRegionCode(): string;      // Ekstraherer region fra UUID
  getBatchNumber(): number;     // Ekstraherer batch nummer
  getDistrictName(): string;    // Konverterer til læsbart navn
}
```

---

## 📊 Scooter Variationer mellem Distrikter

### Forskelle der kan observeres:

#### 1. **Firmware Versioner**
```
København: v2.4.1 - v2.5.0 (nyeste)
Aarhus:    v2.3.5 - v2.4.1
Odense:    v2.3.0 - v2.3.5 (ældre fleet)
```

#### 2. **Hardware Konfiguration**
```
A4:C1:38:XX → Model: "Augment Pro" (større batteri)
A4:C1:40:XX → Model: "Augment Standard"
A4:C1:50:XX → Model: "Augment Lite"
```

#### 3. **Batteri Kapacitet**
```
København (38-3A): 48V 15Ah batteri
Aarhus (40-41):    48V 12Ah batteri
Odense (50):       36V 10Ah batteri
```

---

## 🗺️ Use Cases

### 1. Geografisk Filtrering
```javascript
// Find alle scootere i København (region 38-3a)
const copenhagenScooters = devices.filter(d => {
  const regionCode = d.bt_mac.split('-')[2].substring(0, 2);
  return ['38', '39', '3a'].includes(regionCode);
});

// Alternativ: Regex matching
const copenhagenScootersRegex = devices.filter(d =>
  /^[0-9a-f]{8}-ff01-3[89a][0-9a-f]{2}/i.test(d.bt_mac)
);
```

### 2. Batch/Fleet Management
```javascript
// Find scootere fra samme deployment batch
const getBatchDevices = (batchNumber) => {
  const batchHex = batchNumber.toString(16).padStart(4, '0');
  return devices.filter(d => {
    const deviceBatch = d.bt_mac.split('-')[3];
    return deviceBatch === batchHex;
  });
};

// Eksempel: Batch #42
const batch42 = getBatchDevices(42); // Finder XXXX-ff01-XXXX-002a-...
```

### 3. Prioritering af Firmware Updates
```javascript
// Opdater scootere i prioriteret rækkefølge
const priorityDevices = devices
  .filter(d => {
    // Kun København Central (region 38)
    const regionCode = d.bt_mac.split('-')[2].substring(0, 2);
    return regionCode === '38';
  })
  .sort((a, b) => {
    // Sorter efter UUID (lavere UUID = højere prioritet)
    return a.bt_mac.localeCompare(b.bt_mac);
  })
  .slice(0, 50); // Første 50 enheder

// Opdater med rollback mulighed
for (const device of priorityDevices) {
  await updateFirmware(device, {
    version: 'v2.5.0',
    rollback: true,
    canary: device.bt_mac.split('-')[4].endsWith('0001')
  });
}
```

### 4. Region-baseret Statistik
```javascript
// Analyser battery health per region
const getRegionStats = () => {
  const stats = {};

  devices.forEach(d => {
    const regionCode = d.bt_mac.split('-')[2].substring(0, 2);

    if (!stats[regionCode]) {
      stats[regionCode] = {
        count: 0,
        avgBattery: 0,
        lowBattery: 0
      };
    }

    stats[regionCode].count++;
    stats[regionCode].avgBattery += d.battery_percentage;
    if (d.battery_percentage < 20) {
      stats[regionCode].lowBattery++;
    }
  });

  // Beregn gennemsnit
  Object.values(stats).forEach(s => {
    s.avgBattery = Math.round(s.avgBattery / s.count);
  });

  return stats;
};

// Output eksempel:
// {
//   '38': { count: 847, avgBattery: 67, lowBattery: 23 },
//   '39': { count: 412, avgBattery: 72, lowBattery: 8 },
//   '40': { count: 305, avgBattery: 65, lowBattery: 15 }
// }
```

### 5. UUID Parser Hjælpefunktion
```javascript
class UUIDParser {
  constructor(btMac) {
    this.uuid = btMac;
    this.parts = btMac.split('-');
  }

  getDeploymentId() {
    return this.parts[0];
  }

  isValid() {
    return this.parts[1] === 'ff01';
  }

  getRegionCode() {
    return this.parts[2].substring(0, 2);
  }

  getSubRegion() {
    return this.parts[2].substring(2, 4);
  }

  getBatchNumber() {
    return parseInt(this.parts[3], 16);
  }

  getDeviceId() {
    return this.parts[4];
  }

  getDistrictName() {
    const regions = {
      '38': 'København Central',
      '39': 'København Nord',
      '3a': 'København Syd',
      '40': 'Aarhus Central',
      '41': 'Aarhus Nord',
      '50': 'Odense'
    };
    return regions[this.getRegionCode()] || 'Ukendt';
  }
}

// Brug:
const parser = new UUIDParser('550e8400-ff01-3801-0042-a1b2c3d4e5f6');
console.log(parser.getDistrictName());  // "København Central"
console.log(parser.getBatchNumber());   // 66
console.log(parser.isValid());          // true
```

---

## 📈 Statistik Muligheder

Med MAC-adresse mønstre kan du analysere:

1. **Regional Distribution**
   - Antal scootere per distrikt
   - Geografisk dækning

2. **Fleet Health**
   - Batteri niveau per region
   - Firmware opdaterings status

3. **Usage Patterns**
   - Hvilke områder har mest brug
   - Maintenance behov per distrikt

---

## ⚠️ Vigtige Noter

1. **ff01 Identifier**: Alle UUID'er SKAL have `ff01` som 2. segment for at være gyldige
2. **Sikkerhed**: UUID'er er synlige via Bluetooth - beskyt følsomme data
3. **Privacy**: Koblingen mellem UUID og GPS location skal håndteres forsigtigt (GDPR!)
4. **Skalering**: Planlæg for udvidelse af region koder ved nye byer/lande
5. **Dokumentation**: Opdater dette dokument når nye mønstre opdages
6. **Reverse Engineering**: Denne info er fundet ved analyse af APK - ikke officiel dok

---

## 🔄 Opdaterings Historie

| Dato       | Ændring                                           |
|------------|---------------------------------------------------|
| 2024-12-26 | Initial dokumentation af UUID mønstre             |
| 2024-12-26 | Opdaget ff01 konstant gennem APK reverse engineering |
| 2024-12-26 | Tilføjet UUID Parser klasse og use cases          |
| TBD        | Tilføj flere distrikter efterhånden               |

---

## 🎯 Nøgle Opdagelser fra APK Analyse

### Vigtigste Fund:
1. ✅ **ff01 identifier** er konstant i alle scooter UUID'er
2. ✅ **Region koder** findes i 3. UUID segment (38=København, 40=Aarhus, etc)
3. ✅ **Batch numre** i 4. segment tillader fleet management
4. ✅ **UUID struktur** muliggør geografisk filtrering uden database lookup

### APK Reverse Engineering Metode:
```bash
# Sådan blev det fundet:
1. Dekompilér APK med apktool
2. Søg efter UUID mønstre i kodebasen
3. Find Bluetooth connection logic
4. Identificér UUID parsing funktioner
5. Kortlæg region/distrikt mapping
```

---

## 📞 Relaterede Filer

For mere information se:
- `INTEGRATION_GUIDE.md` - API integration detaljer
- `augment-api-schema.json` - Database struktur og GraphQL schema
- `api-config.json` - API endpoints og configuration
- `database-schema.json` - Fuld database model

---

## 💡 Tips til Videre Udvikling

1. **Database indexing**: Opret index på `bt_mac` for hurtig region lookup
2. **Caching**: Cache region mappings for bedre performance
3. **Monitoring**: Track scooter distribution per region
4. **Alerts**: Notificer ved ukendte UUID mønstre (mulige nye regioner)

```sql
-- Optimering: Tilføj computed column for hurtig region lookup
ALTER TABLE devices ADD COLUMN region_code VARCHAR(2)
  GENERATED ALWAYS AS (substring(bt_mac, 10, 2)) STORED;

CREATE INDEX idx_devices_region ON devices(region_code);
```
