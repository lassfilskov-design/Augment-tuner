/**
 * Augment Scooter UUID Parser
 *
 * Parser til at dekode scooter UUID'er og ekstrahere region, distrikt,
 * batch nummer og andre metadata.
 *
 * UUID Format: XXXXXXXX-ff01-XXXX-XXXX-XXXXXXXXXXXX
 *              ^        ^    ^    ^    ^
 *              |        |    |    |    └─ Device ID & metadata
 *              |        |    |    └────── Batch nummer
 *              |        |    └─────────── Region + Sub-region
 *              |        └──────────────── Fast identifier (ff01)
 *              └───────────────────────── Deployment ID
 */

class AugmentUUIDParser {
  constructor(uuid) {
    this.uuid = uuid.toLowerCase();
    this.parts = this.uuid.split('-');

    if (this.parts.length !== 5) {
      throw new Error(`Invalid UUID format: ${uuid}`);
    }
  }

  /**
   * Validér om UUID'en er en gyldig Augment scooter UUID
   * @returns {boolean}
   */
  isValid() {
    return this.parts[1] === 'ff01';
  }

  /**
   * Hent deployment/producer ID
   * @returns {string}
   */
  getDeploymentId() {
    return this.parts[0];
  }

  /**
   * Hent region kode (2 chars)
   * @returns {string}
   */
  getRegionCode() {
    return this.parts[2].substring(0, 2);
  }

  /**
   * Hent sub-region kode (2 chars)
   * @returns {string}
   */
  getSubRegion() {
    return this.parts[2].substring(2, 4);
  }

  /**
   * Hent batch nummer (decimal)
   * @returns {number}
   */
  getBatchNumber() {
    return parseInt(this.parts[3], 16);
  }

  /**
   * Hent batch nummer (hex string)
   * @returns {string}
   */
  getBatchHex() {
    return this.parts[3];
  }

  /**
   * Hent device ID
   * @returns {string}
   */
  getDeviceId() {
    return this.parts[4];
  }

  /**
   * Konvertér region kode til læsbart distrikt navn
   * @returns {string}
   */
  getDistrictName() {
    const regions = {
      '38': 'København Central',
      '39': 'København Nord',
      '3a': 'København Syd/Vest',
      '3b': 'København Øst',
      '40': 'Aarhus Central',
      '41': 'Aarhus Nord',
      '42': 'Aarhus Syd',
      '50': 'Odense Central',
      '51': 'Odense Nord',
      '60': 'Aalborg',
      '70': 'Esbjerg'
    };

    const code = this.getRegionCode();
    return regions[code] || `Ukendt region (${code})`;
  }

  /**
   * Hent by navn fra region kode
   * @returns {string}
   */
  getCityName() {
    const code = this.getRegionCode();

    if (code >= '38' && code <= '3f') return 'København';
    if (code >= '40' && code <= '4f') return 'Aarhus';
    if (code >= '50' && code <= '5f') return 'Odense';
    if (code >= '60' && code <= '6f') return 'Aalborg';
    if (code >= '70' && code <= '7f') return 'Esbjerg';

    return 'Ukendt';
  }

  /**
   * Check om scooteren er i København
   * @returns {boolean}
   */
  isInCopenhagen() {
    const code = this.getRegionCode();
    return code >= '38' && code <= '3f';
  }

  /**
   * Check om scooteren er i Aarhus
   * @returns {boolean}
   */
  isInAarhus() {
    const code = this.getRegionCode();
    return code >= '40' && code <= '4f';
  }

  /**
   * Hent fuld information om scooteren
   * @returns {object}
   */
  getFullInfo() {
    return {
      uuid: this.uuid,
      valid: this.isValid(),
      deployment_id: this.getDeploymentId(),
      region: {
        code: this.getRegionCode(),
        sub_region: this.getSubRegion(),
        city: this.getCityName(),
        district: this.getDistrictName()
      },
      batch: {
        number: this.getBatchNumber(),
        hex: this.getBatchHex()
      },
      device_id: this.getDeviceId()
    };
  }

  /**
   * Formatér som læsbar string
   * @returns {string}
   */
  toString() {
    return `Augment Scooter [${this.getDistrictName()}] Batch #${this.getBatchNumber()}`;
  }
}

/**
 * Hjælpe-funktioner til at arbejde med scooter fleets
 */
class AugmentFleetManager {
  constructor(devices) {
    this.devices = devices;
  }

  /**
   * Find alle scootere i en specifik by
   * @param {string} city - By navn (København, Aarhus, etc)
   * @returns {Array}
   */
  getDevicesByCity(city) {
    return this.devices.filter(device => {
      try {
        const parser = new AugmentUUIDParser(device.bt_mac);
        return parser.getCityName() === city;
      } catch (e) {
        return false;
      }
    });
  }

  /**
   * Find alle scootere i et specifikt distrikt
   * @param {string} regionCode - Region kode (38, 40, etc)
   * @returns {Array}
   */
  getDevicesByRegion(regionCode) {
    return this.devices.filter(device => {
      try {
        const parser = new AugmentUUIDParser(device.bt_mac);
        return parser.getRegionCode() === regionCode;
      } catch (e) {
        return false;
      }
    });
  }

  /**
   * Find alle scootere fra samme batch
   * @param {number} batchNumber - Batch nummer
   * @returns {Array}
   */
  getDevicesByBatch(batchNumber) {
    return this.devices.filter(device => {
      try {
        const parser = new AugmentUUIDParser(device.bt_mac);
        return parser.getBatchNumber() === batchNumber;
      } catch (e) {
        return false;
      }
    });
  }

  /**
   * Hent statistik per region
   * @returns {object}
   */
  getRegionStats() {
    const stats = {};

    this.devices.forEach(device => {
      try {
        const parser = new AugmentUUIDParser(device.bt_mac);
        const regionCode = parser.getRegionCode();

        if (!stats[regionCode]) {
          stats[regionCode] = {
            region: parser.getDistrictName(),
            city: parser.getCityName(),
            count: 0,
            batches: new Set(),
            avgBattery: 0,
            lowBattery: 0,
            devices: []
          };
        }

        stats[regionCode].count++;
        stats[regionCode].batches.add(parser.getBatchNumber());
        stats[regionCode].devices.push(device.id);

        if (device.battery_percentage !== undefined) {
          stats[regionCode].avgBattery += device.battery_percentage;
          if (device.battery_percentage < 20) {
            stats[regionCode].lowBattery++;
          }
        }
      } catch (e) {
        // Skip invalid UUIDs
      }
    });

    // Beregn gennemsnit og konvertér Set til Array
    Object.values(stats).forEach(s => {
      if (s.count > 0) {
        s.avgBattery = Math.round(s.avgBattery / s.count);
      }
      s.batches = Array.from(s.batches);
      s.batchCount = s.batches.length;
    });

    return stats;
  }

  /**
   * Valider alle UUID'er i fleet
   * @returns {object}
   */
  validateFleet() {
    const results = {
      total: this.devices.length,
      valid: 0,
      invalid: 0,
      invalidDevices: []
    };

    this.devices.forEach(device => {
      try {
        const parser = new AugmentUUIDParser(device.bt_mac);
        if (parser.isValid()) {
          results.valid++;
        } else {
          results.invalid++;
          results.invalidDevices.push({
            id: device.id,
            bt_mac: device.bt_mac,
            reason: 'Missing ff01 identifier'
          });
        }
      } catch (e) {
        results.invalid++;
        results.invalidDevices.push({
          id: device.id,
          bt_mac: device.bt_mac,
          reason: e.message
        });
      }
    });

    return results;
  }

  /**
   * Find scootere der skal have firmware opdatering
   * @param {string} targetVersion - Target firmware version
   * @param {object} options - Prioriterings optioner
   * @returns {Array}
   */
  getFirmwareUpdateQueue(targetVersion, options = {}) {
    const {
      region = null,
      maxDevices = 100,
      prioritizeLowBatch = true
    } = options;

    let candidates = this.devices.filter(d =>
      d.firmware_version !== targetVersion
    );

    // Filtrér efter region hvis specificeret
    if (region) {
      candidates = candidates.filter(device => {
        try {
          const parser = new AugmentUUIDParser(device.bt_mac);
          return parser.getRegionCode() === region;
        } catch (e) {
          return false;
        }
      });
    }

    // Sortér efter prioritet
    candidates.sort((a, b) => {
      try {
        const parserA = new AugmentUUIDParser(a.bt_mac);
        const parserB = new AugmentUUIDParser(b.bt_mac);

        if (prioritizeLowBatch) {
          // Lavere batch nummer = højere prioritet
          return parserA.getBatchNumber() - parserB.getBatchNumber();
        } else {
          // Højere batch nummer = nyere scooter = højere prioritet
          return parserB.getBatchNumber() - parserA.getBatchNumber();
        }
      } catch (e) {
        return 0;
      }
    });

    return candidates.slice(0, maxDevices);
  }
}

// Export til brug i Node.js
if (typeof module !== 'undefined' && module.exports) {
  module.exports = {
    AugmentUUIDParser,
    AugmentFleetManager
  };
}

// Eksempel brug
if (require.main === module) {
  // Test eksempler
  console.log('=== Augment UUID Parser Test ===\n');

  const testUUIDs = [
    '550e8400-ff01-3801-0042-a1b2c3d4e5f6',  // København Central, Batch 66
    '550e8401-ff01-3900-0010-000000000001',  // København Nord, Batch 16
    '550e8400-ff01-4000-0001-123456789abc',  // Aarhus Central, Batch 1
    '550e8402-ff01-5000-00ff-fedcba987654'   // Odense, Batch 255
  ];

  testUUIDs.forEach(uuid => {
    try {
      const parser = new AugmentUUIDParser(uuid);
      console.log(`UUID: ${uuid}`);
      console.log(`Valid: ${parser.isValid()}`);
      console.log(`District: ${parser.getDistrictName()}`);
      console.log(`City: ${parser.getCityName()}`);
      console.log(`Batch: #${parser.getBatchNumber()} (0x${parser.getBatchHex()})`);
      console.log(`String: ${parser.toString()}`);
      console.log('\nFull Info:', JSON.stringify(parser.getFullInfo(), null, 2));
      console.log('\n---\n');
    } catch (e) {
      console.error(`Error parsing ${uuid}:`, e.message);
    }
  });
}
