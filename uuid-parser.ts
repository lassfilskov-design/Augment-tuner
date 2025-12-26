/**
 * Augment Scooter UUID Parser - TypeScript Version
 *
 * Parser til at dekode scooter UUID'er og ekstrahere region, distrikt,
 * batch nummer og andre metadata.
 *
 * UUID Format: XXXXXXXX-ff01-XXXX-XXXX-XXXXXXXXXXXX
 */

export interface RegionInfo {
  code: string;
  sub_region: string;
  city: string;
  district: string;
}

export interface BatchInfo {
  number: number;
  hex: string;
}

export interface UUIDInfo {
  uuid: string;
  valid: boolean;
  deployment_id: string;
  region: RegionInfo;
  batch: BatchInfo;
  device_id: string;
}

export interface Device {
  id: string;
  bt_mac: string;
  device_name?: string;
  firmware_version?: string;
  battery_percentage?: number;
  battery_voltage?: number;
  speed?: number;
  owner_id?: string;
}

export interface RegionStats {
  region: string;
  city: string;
  count: number;
  batches: number[];
  batchCount: number;
  avgBattery: number;
  lowBattery: number;
  devices: string[];
}

export interface ValidationResult {
  total: number;
  valid: number;
  invalid: number;
  invalidDevices: Array<{
    id: string;
    bt_mac: string;
    reason: string;
  }>;
}

export interface FirmwareUpdateOptions {
  region?: string;
  maxDevices?: number;
  prioritizeLowBatch?: boolean;
}

export class AugmentUUIDParser {
  private uuid: string;
  private parts: string[];

  constructor(uuid: string) {
    this.uuid = uuid.toLowerCase();
    this.parts = this.uuid.split('-');

    if (this.parts.length !== 5) {
      throw new Error(`Invalid UUID format: ${uuid}`);
    }
  }

  /**
   * Validér om UUID'en er en gyldig Augment scooter UUID
   */
  isValid(): boolean {
    return this.parts[1] === 'ff01';
  }

  /**
   * Hent deployment/producer ID
   */
  getDeploymentId(): string {
    return this.parts[0];
  }

  /**
   * Hent region kode (2 chars)
   */
  getRegionCode(): string {
    return this.parts[2].substring(0, 2);
  }

  /**
   * Hent sub-region kode (2 chars)
   */
  getSubRegion(): string {
    return this.parts[2].substring(2, 4);
  }

  /**
   * Hent batch nummer (decimal)
   */
  getBatchNumber(): number {
    return parseInt(this.parts[3], 16);
  }

  /**
   * Hent batch nummer (hex string)
   */
  getBatchHex(): string {
    return this.parts[3];
  }

  /**
   * Hent device ID
   */
  getDeviceId(): string {
    return this.parts[4];
  }

  /**
   * Konvertér region kode til læsbart distrikt navn
   */
  getDistrictName(): string {
    const regions: Record<string, string> = {
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
   */
  getCityName(): string {
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
   */
  isInCopenhagen(): boolean {
    const code = this.getRegionCode();
    return code >= '38' && code <= '3f';
  }

  /**
   * Check om scooteren er i Aarhus
   */
  isInAarhus(): boolean {
    const code = this.getRegionCode();
    return code >= '40' && code <= '4f';
  }

  /**
   * Hent fuld information om scooteren
   */
  getFullInfo(): UUIDInfo {
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
   */
  toString(): string {
    return `Augment Scooter [${this.getDistrictName()}] Batch #${this.getBatchNumber()}`;
  }
}

/**
 * Hjælpe-funktioner til at arbejde med scooter fleets
 */
export class AugmentFleetManager {
  private devices: Device[];

  constructor(devices: Device[]) {
    this.devices = devices;
  }

  /**
   * Find alle scootere i en specifik by
   */
  getDevicesByCity(city: string): Device[] {
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
   */
  getDevicesByRegion(regionCode: string): Device[] {
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
   */
  getDevicesByBatch(batchNumber: number): Device[] {
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
   */
  getRegionStats(): Record<string, RegionStats> {
    const stats: Record<string, RegionStats> = {};

    this.devices.forEach(device => {
      try {
        const parser = new AugmentUUIDParser(device.bt_mac);
        const regionCode = parser.getRegionCode();

        if (!stats[regionCode]) {
          stats[regionCode] = {
            region: parser.getDistrictName(),
            city: parser.getCityName(),
            count: 0,
            batches: [],
            batchCount: 0,
            avgBattery: 0,
            lowBattery: 0,
            devices: []
          };
        }

        const batchSet = new Set(stats[regionCode].batches);
        batchSet.add(parser.getBatchNumber());
        stats[regionCode].batches = Array.from(batchSet);

        stats[regionCode].count++;
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

    // Beregn gennemsnit
    Object.values(stats).forEach(s => {
      if (s.count > 0) {
        s.avgBattery = Math.round(s.avgBattery / s.count);
      }
      s.batchCount = s.batches.length;
    });

    return stats;
  }

  /**
   * Valider alle UUID'er i fleet
   */
  validateFleet(): ValidationResult {
    const results: ValidationResult = {
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
          reason: e instanceof Error ? e.message : 'Unknown error'
        });
      }
    });

    return results;
  }

  /**
   * Find scootere der skal have firmware opdatering
   */
  getFirmwareUpdateQueue(
    targetVersion: string,
    options: FirmwareUpdateOptions = {}
  ): Device[] {
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

// GraphQL integration helper
export const createUUIDResolvers = () => ({
  Device: {
    parsedUUID: (parent: Device) => {
      try {
        const parser = new AugmentUUIDParser(parent.bt_mac);
        return parser.getFullInfo();
      } catch (e) {
        return null;
      }
    },
    district: (parent: Device) => {
      try {
        const parser = new AugmentUUIDParser(parent.bt_mac);
        return parser.getDistrictName();
      } catch (e) {
        return 'Unknown';
      }
    },
    city: (parent: Device) => {
      try {
        const parser = new AugmentUUIDParser(parent.bt_mac);
        return parser.getCityName();
      } catch (e) {
        return 'Unknown';
      }
    },
    batchNumber: (parent: Device) => {
      try {
        const parser = new AugmentUUIDParser(parent.bt_mac);
        return parser.getBatchNumber();
      } catch (e) {
        return null;
      }
    }
  }
});
