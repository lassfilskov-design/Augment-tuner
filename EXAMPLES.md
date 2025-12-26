# Augment UUID Parser - Eksempler

Praktiske eksempler på hvordan du bruger UUID parseren til at arbejde med Augment scootere.

---

## 📦 Installation og Setup

### JavaScript (Node.js)
```javascript
const { AugmentUUIDParser, AugmentFleetManager } = require('./uuid-parser.js');
```

### TypeScript
```typescript
import { AugmentUUIDParser, AugmentFleetManager } from './uuid-parser';
```

---

## 🎯 Basis Brug

### Parse en enkelt UUID

```javascript
const parser = new AugmentUUIDParser('550e8400-ff01-3801-0042-a1b2c3d4e5f6');

// Check om UUID er gyldig (har ff01 identifier)
console.log(parser.isValid()); // true

// Hent region information
console.log(parser.getDistrictName()); // "København Central"
console.log(parser.getCityName());     // "København"
console.log(parser.getRegionCode());   // "38"

// Hent batch nummer
console.log(parser.getBatchNumber());  // 66 (0x0042 = 66)
console.log(parser.getBatchHex());     // "0042"

// Fuld information
const info = parser.getFullInfo();
console.log(JSON.stringify(info, null, 2));
```

**Output:**
```json
{
  "uuid": "550e8400-ff01-3801-0042-a1b2c3d4e5f6",
  "valid": true,
  "deployment_id": "550e8400",
  "region": {
    "code": "38",
    "sub_region": "01",
    "city": "København",
    "district": "København Central"
  },
  "batch": {
    "number": 66,
    "hex": "0042"
  },
  "device_id": "a1b2c3d4e5f6"
}
```

---

## 🌍 Find Scootere efter Geografisk Område

### Find alle scootere i København

```javascript
const devices = [
  { id: '1', bt_mac: '550e8400-ff01-3801-0001-000000000001', battery_percentage: 85 },
  { id: '2', bt_mac: '550e8401-ff01-3900-0001-000000000002', battery_percentage: 42 },
  { id: '3', bt_mac: '550e8402-ff01-4000-0001-000000000003', battery_percentage: 95 },
  { id: '4', bt_mac: '550e8403-ff01-5000-0001-000000000004', battery_percentage: 12 }
];

const fleet = new AugmentFleetManager(devices);

// Find alle i København (region 38-3f)
const copenhagenScooters = fleet.getDevicesByCity('København');
console.log(`København har ${copenhagenScooters.length} scootere`);

// Find specifikt distrikt (København Central = region 38)
const centralScooters = fleet.getDevicesByRegion('38');
console.log(`København Central: ${centralScooters.length} scootere`);
```

### Manuel filtrering med region check

```javascript
const copenhagenOnly = devices.filter(device => {
  const parser = new AugmentUUIDParser(device.bt_mac);
  return parser.isInCopenhagen();
});
```

---

## 📊 Statistik og Analyse

### Hent region statistik

```javascript
const fleet = new AugmentFleetManager(devices);
const stats = fleet.getRegionStats();

console.log(stats);
```

**Output:**
```javascript
{
  '38': {
    region: 'København Central',
    city: 'København',
    count: 247,
    batches: [1, 2, 5, 8, 12, 15],
    batchCount: 6,
    avgBattery: 67,
    lowBattery: 23,
    devices: ['uuid1', 'uuid2', ...]
  },
  '39': {
    region: 'København Nord',
    city: 'København',
    count: 142,
    batches: [1, 3, 7],
    batchCount: 3,
    avgBattery: 72,
    lowBattery: 8,
    devices: [...]
  }
}
```

### Visualisér statistik

```javascript
const stats = fleet.getRegionStats();

console.log('\n=== FLEET STATISTIK ===\n');

Object.entries(stats).forEach(([code, data]) => {
  console.log(`📍 ${data.region} (${data.city})`);
  console.log(`   Scootere: ${data.count}`);
  console.log(`   Batches: ${data.batchCount} (${data.batches.join(', ')})`);
  console.log(`   Gns. batteri: ${data.avgBattery}%`);
  console.log(`   Lavt batteri (<20%): ${data.lowBattery}`);
  console.log('');
});
```

---

## 🔧 Fleet Management

### Find scootere fra samme batch

```javascript
const fleet = new AugmentFleetManager(devices);

// Find alle scootere fra batch #42
const batch42 = fleet.getDevicesByBatch(42);
console.log(`Batch #42 har ${batch42.length} scootere`);

// List alle scootere i batchen
batch42.forEach(device => {
  const parser = new AugmentUUIDParser(device.bt_mac);
  console.log(`- ${device.id}: ${parser.getDistrictName()}`);
});
```

### Validér hele fleet

```javascript
const validation = fleet.validateFleet();

console.log(`Total scootere: ${validation.total}`);
console.log(`Gyldige: ${validation.valid}`);
console.log(`Ugyldige: ${validation.invalid}`);

if (validation.invalid > 0) {
  console.log('\n⚠️ Ugyldige enheder:');
  validation.invalidDevices.forEach(d => {
    console.log(`- ${d.id} (${d.bt_mac}): ${d.reason}`);
  });
}
```

---

## 🔄 Firmware Updates

### Prioriteret firmware update queue

```javascript
const updateQueue = fleet.getFirmwareUpdateQueue('v2.5.0', {
  region: '38',           // Kun København Central
  maxDevices: 50,         // Max 50 scootere ad gangen
  prioritizeLowBatch: true  // Start med ældste scootere
});

console.log(`Opdaterer ${updateQueue.length} scootere:`);

updateQueue.forEach((device, index) => {
  const parser = new AugmentUUIDParser(device.bt_mac);
  console.log(`${index + 1}. ${device.id} - Batch #${parser.getBatchNumber()}`);
});

// Kør opdateringer
for (const device of updateQueue) {
  await updateFirmware(device.id, 'v2.5.0');
  console.log(`✓ Opdateret ${device.id}`);
}
```

### Canary deployment (test på få enheder først)

```javascript
// Find de første 5 scootere fra laveste batch i København
const canaryDevices = fleet.getFirmwareUpdateQueue('v2.5.0', {
  region: '38',
  maxDevices: 5,
  prioritizeLowBatch: true
});

console.log('🐤 Canary deployment:');
canaryDevices.forEach(d => {
  const parser = new AugmentUUIDParser(d.bt_mac);
  console.log(`- ${d.id}: Batch #${parser.getBatchNumber()}`);
});

// Test opdatering på canary devices
await deployCanary(canaryDevices, 'v2.5.0');

// Hvis success, fortsæt med resten
if (canarySuccess) {
  const remaining = fleet.getFirmwareUpdateQueue('v2.5.0', {
    region: '38',
    maxDevices: 100,
    prioritizeLowBatch: true
  });
  await deployToAll(remaining, 'v2.5.0');
}
```

---

## 🗺️ GraphQL Integration

### Backend resolvers (TypeScript)

```typescript
import { AugmentUUIDParser, createUUIDResolvers } from './uuid-parser';

// Tilføj UUID parsing til GraphQL schema
const resolvers = {
  Query: {
    devices: async () => {
      return await db.devices.findMany();
    },

    devicesByCity: async (_, { city }) => {
      const allDevices = await db.devices.findMany();
      return allDevices.filter(d => {
        try {
          const parser = new AugmentUUIDParser(d.bt_mac);
          return parser.getCityName() === city;
        } catch {
          return false;
        }
      });
    },

    devicesByRegion: async (_, { regionCode }) => {
      const allDevices = await db.devices.findMany();
      return allDevices.filter(d => {
        try {
          const parser = new AugmentUUIDParser(d.bt_mac);
          return parser.getRegionCode() === regionCode;
        } catch {
          return false;
        }
      });
    },

    fleetStats: async () => {
      const devices = await db.devices.findMany();
      const fleet = new AugmentFleetManager(devices);
      return fleet.getRegionStats();
    }
  },

  // Tilføj computed fields til Device type
  ...createUUIDResolvers()
};
```

### GraphQL schema definition

```graphql
type Device {
  id: ID!
  bt_mac: String!
  device_name: String
  firmware_version: String
  battery_percentage: Int

  # Computed fields fra UUID parser
  parsedUUID: UUIDInfo
  district: String
  city: String
  batchNumber: Int
}

type UUIDInfo {
  uuid: String!
  valid: Boolean!
  deployment_id: String!
  region: RegionInfo!
  batch: BatchInfo!
  device_id: String!
}

type RegionInfo {
  code: String!
  sub_region: String!
  city: String!
  district: String!
}

type BatchInfo {
  number: Int!
  hex: String!
}

type RegionStats {
  region: String!
  city: String!
  count: Int!
  batches: [Int!]!
  batchCount: Int!
  avgBattery: Int!
  lowBattery: Int!
  devices: [String!]!
}

type Query {
  devices: [Device!]!
  devicesByCity(city: String!): [Device!]!
  devicesByRegion(regionCode: String!): [Device!]!
  fleetStats: [RegionStats!]!
}
```

### Frontend GraphQL queries

```graphql
# Hent alle scootere med parsed UUID info
query GetDevicesWithRegion {
  devices {
    id
    bt_mac
    device_name
    battery_percentage
    city
    district
    batchNumber
    parsedUUID {
      region {
        code
        city
        district
      }
      batch {
        number
      }
    }
  }
}

# Hent scootere i en specifik by
query GetCopenhagenDevices {
  devicesByCity(city: "København") {
    id
    device_name
    battery_percentage
    district
  }
}

# Hent fleet statistik
query GetFleetStats {
  fleetStats {
    region
    city
    count
    avgBattery
    lowBattery
    batchCount
  }
}
```

---

## 📱 React Frontend Eksempel

### Display scooter på kort med region info

```tsx
import { AugmentUUIDParser } from './uuid-parser';

interface ScooterMapProps {
  devices: Device[];
}

const ScooterMap: React.FC<ScooterMapProps> = ({ devices }) => {
  const [selectedCity, setSelectedCity] = useState<string>('all');

  // Parse og gruppér scootere
  const devicesByCity = useMemo(() => {
    const grouped: Record<string, Device[]> = {};

    devices.forEach(device => {
      try {
        const parser = new AugmentUUIDParser(device.bt_mac);
        const city = parser.getCityName();

        if (!grouped[city]) {
          grouped[city] = [];
        }
        grouped[city].push(device);
      } catch (e) {
        // Skip invalid UUIDs
      }
    });

    return grouped;
  }, [devices]);

  const filteredDevices = selectedCity === 'all'
    ? devices
    : devicesByCity[selectedCity] || [];

  return (
    <div>
      <select value={selectedCity} onChange={e => setSelectedCity(e.target.value)}>
        <option value="all">Alle byer</option>
        {Object.keys(devicesByCity).map(city => (
          <option key={city} value={city}>
            {city} ({devicesByCity[city].length})
          </option>
        ))}
      </select>

      <Map>
        {filteredDevices.map(device => {
          const parser = new AugmentUUIDParser(device.bt_mac);
          return (
            <Marker
              key={device.id}
              position={device.location}
              icon={getCityIcon(parser.getCityName())}
              tooltip={`${parser.getDistrictName()} - Batch #${parser.getBatchNumber()}`}
            />
          );
        })}
      </Map>
    </div>
  );
};
```

### Fleet statistik dashboard

```tsx
import { AugmentFleetManager } from './uuid-parser';

const FleetDashboard: React.FC<{ devices: Device[] }> = ({ devices }) => {
  const fleet = useMemo(() => new AugmentFleetManager(devices), [devices]);
  const stats = useMemo(() => fleet.getRegionStats(), [fleet]);

  return (
    <div className="grid grid-cols-3 gap-4">
      {Object.entries(stats).map(([code, data]) => (
        <div key={code} className="card">
          <h3>{data.region}</h3>
          <p className="text-2xl font-bold">{data.count} scootere</p>
          <div className="stats">
            <div>Gns. batteri: {data.avgBattery}%</div>
            <div>Batches: {data.batchCount}</div>
            <div className="text-red-600">
              ⚠️ Lavt batteri: {data.lowBattery}
            </div>
          </div>
          <div className="mt-2">
            <BatteryChart average={data.avgBattery} low={data.lowBattery} />
          </div>
        </div>
      ))}
    </div>
  );
};
```

---

## 🔍 Database Queries med UUID Pattern Matching

### PostgreSQL eksempler

```sql
-- Find alle scootere i København (region 38-3f)
SELECT *
FROM devices
WHERE substring(bt_mac, 10, 2) >= '38'
  AND substring(bt_mac, 10, 2) <= '3f';

-- Find scootere fra batch #42 (0x002a)
SELECT *
FROM devices
WHERE substring(bt_mac, 15, 4) = '002a';

-- Opret computed column for hurtig region lookup
ALTER TABLE devices
ADD COLUMN region_code VARCHAR(2)
GENERATED ALWAYS AS (substring(bt_mac, 10, 2)) STORED;

CREATE INDEX idx_devices_region ON devices(region_code);

-- Nu kan du query direkte på region
SELECT * FROM devices WHERE region_code = '38';
```

### MongoDB eksempler

```javascript
// Find scootere i København
db.devices.find({
  bt_mac: { $regex: /^[0-9a-f]{8}-ff01-3[89ab][0-9a-f]{2}/i }
});

// Aggregation pipeline med region parsing
db.devices.aggregate([
  {
    $addFields: {
      region_code: { $substr: ['$bt_mac', 9, 2] }
    }
  },
  {
    $group: {
      _id: '$region_code',
      count: { $sum: 1 },
      avgBattery: { $avg: '$battery_percentage' }
    }
  }
]);
```

---

## ⚡ Performance Tips

### Cache region mappings

```javascript
class CachedUUIDParser extends AugmentUUIDParser {
  static cache = new Map();

  getFullInfo() {
    if (CachedUUIDParser.cache.has(this.uuid)) {
      return CachedUUIDParser.cache.get(this.uuid);
    }

    const info = super.getFullInfo();
    CachedUUIDParser.cache.set(this.uuid, info);
    return info;
  }
}
```

### Batch processing

```javascript
function parseDevicesBatch(devices) {
  return devices.map(device => {
    try {
      const parser = new AugmentUUIDParser(device.bt_mac);
      return {
        ...device,
        region: parser.getRegionCode(),
        city: parser.getCityName(),
        batch: parser.getBatchNumber()
      };
    } catch (e) {
      return device;
    }
  });
}
```

---

## 📞 Se Også

- `UUID_STRUKTUR.md` - Fuld dokumentation af UUID struktur
- `INTEGRATION_GUIDE.md` - API integration guide
- `augment-api-schema.json` - GraphQL schema
