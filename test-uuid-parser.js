/**
 * Test suite for Augment UUID Parser
 * Kør med: node test-uuid-parser.js
 */

const { AugmentUUIDParser, AugmentFleetManager } = require('./uuid-parser.js');

// ANSI farver til console output
const colors = {
  reset: '\x1b[0m',
  green: '\x1b[32m',
  red: '\x1b[31m',
  yellow: '\x1b[33m',
  blue: '\x1b[34m',
  cyan: '\x1b[36m'
};

function log(msg, color = 'reset') {
  console.log(`${colors[color]}${msg}${colors.reset}`);
}

function assert(condition, message) {
  if (condition) {
    log(`  ✓ ${message}`, 'green');
    return true;
  } else {
    log(`  ✗ ${message}`, 'red');
    return false;
  }
}

// Test data
const testDevices = [
  { id: '1', bt_mac: '550e8400-ff01-3801-0042-a1b2c3d4e5f6', device_name: 'CPH-C-001', firmware_version: 'v2.4.1', battery_percentage: 85 },
  { id: '2', bt_mac: '550e8401-ff01-3900-0010-000000000001', device_name: 'CPH-N-001', firmware_version: 'v2.4.1', battery_percentage: 42 },
  { id: '3', bt_mac: '550e8400-ff01-4000-0001-123456789abc', device_name: 'AAR-C-001', firmware_version: 'v2.3.5', battery_percentage: 95 },
  { id: '4', bt_mac: '550e8402-ff01-5000-00ff-fedcba987654', device_name: 'ODE-C-001', firmware_version: 'v2.3.0', battery_percentage: 12 },
  { id: '5', bt_mac: '550e8400-ff01-3802-0001-111111111111', device_name: 'CPH-C-002', firmware_version: 'v2.4.1', battery_percentage: 67 },
  { id: '6', bt_mac: '550e8401-ff01-3900-0020-222222222222', device_name: 'CPH-N-002', firmware_version: 'v2.5.0', battery_percentage: 15 },
];

let totalTests = 0;
let passedTests = 0;

function runTest(testName, testFn) {
  log(`\n${testName}`, 'cyan');
  try {
    const passed = testFn();
    totalTests++;
    if (passed !== false) passedTests++;
  } catch (e) {
    log(`  ✗ Error: ${e.message}`, 'red');
    totalTests++;
  }
}

// ===== TESTS =====

runTest('Test 1: Parse valid København UUID', () => {
  const parser = new AugmentUUIDParser('550e8400-ff01-3801-0042-a1b2c3d4e5f6');

  assert(parser.isValid() === true, 'UUID er gyldig (har ff01)');
  assert(parser.getDeploymentId() === '550e8400', 'Deployment ID er korrekt');
  assert(parser.getRegionCode() === '38', 'Region kode er 38');
  assert(parser.getSubRegion() === '01', 'Sub-region er 01');
  assert(parser.getBatchNumber() === 66, 'Batch nummer er 66 (0x0042)');
  assert(parser.getBatchHex() === '0042', 'Batch hex er 0042');
  assert(parser.getDeviceId() === 'a1b2c3d4e5f6', 'Device ID er korrekt');
  assert(parser.getCityName() === 'København', 'By navn er København');
  assert(parser.getDistrictName() === 'København Central', 'Distrikt er København Central');
  assert(parser.isInCopenhagen() === true, 'Er i København');
  assert(parser.isInAarhus() === false, 'Er IKKE i Aarhus');
});

runTest('Test 2: Parse Aarhus UUID', () => {
  const parser = new AugmentUUIDParser('550e8400-ff01-4000-0001-123456789abc');

  assert(parser.isValid() === true, 'UUID er gyldig');
  assert(parser.getRegionCode() === '40', 'Region kode er 40');
  assert(parser.getCityName() === 'Aarhus', 'By navn er Aarhus');
  assert(parser.getDistrictName() === 'Aarhus Central', 'Distrikt er Aarhus Central');
  assert(parser.getBatchNumber() === 1, 'Batch nummer er 1');
  assert(parser.isInAarhus() === true, 'Er i Aarhus');
  assert(parser.isInCopenhagen() === false, 'Er IKKE i København');
});

runTest('Test 3: Parse Odense UUID', () => {
  const parser = new AugmentUUIDParser('550e8402-ff01-5000-00ff-fedcba987654');

  assert(parser.isValid() === true, 'UUID er gyldig');
  assert(parser.getRegionCode() === '50', 'Region kode er 50');
  assert(parser.getCityName() === 'Odense', 'By navn er Odense');
  assert(parser.getBatchNumber() === 255, 'Batch nummer er 255 (0x00FF)');
});

runTest('Test 4: Invalid UUID format', () => {
  try {
    const parser = new AugmentUUIDParser('invalid-uuid');
    return false;
  } catch (e) {
    assert(e.message.includes('Invalid UUID format'), 'Kaster fejl for ugyldig format');
  }
});

runTest('Test 5: UUID uden ff01 identifier', () => {
  const parser = new AugmentUUIDParser('550e8400-1234-3801-0042-a1b2c3d4e5f6');
  assert(parser.isValid() === false, 'UUID er ugyldig (mangler ff01)');
});

runTest('Test 6: FleetManager - Find by city', () => {
  const fleet = new AugmentFleetManager(testDevices);

  const copenhagenDevices = fleet.getDevicesByCity('København');
  assert(copenhagenDevices.length === 4, `Finder 4 København scootere (fandt ${copenhagenDevices.length})`);

  const aarhusDevices = fleet.getDevicesByCity('Aarhus');
  assert(aarhusDevices.length === 1, `Finder 1 Aarhus scooter (fandt ${aarhusDevices.length})`);

  const odenserDevices = fleet.getDevicesByCity('Odense');
  assert(odenserDevices.length === 1, `Finder 1 Odense scooter (fandt ${odenserDevices.length})`);
});

runTest('Test 7: FleetManager - Find by region', () => {
  const fleet = new AugmentFleetManager(testDevices);

  const region38 = fleet.getDevicesByRegion('38');
  assert(region38.length === 2, `Region 38 har 2 scootere (fandt ${region38.length})`);

  const region39 = fleet.getDevicesByRegion('39');
  assert(region39.length === 2, `Region 39 har 2 scootere (fandt ${region39.length})`);
});

runTest('Test 8: FleetManager - Find by batch', () => {
  const fleet = new AugmentFleetManager(testDevices);

  const batch1 = fleet.getDevicesByBatch(1);
  assert(batch1.length === 2, `Batch #1 har 2 scootere (fandt ${batch1.length})`);

  const batch66 = fleet.getDevicesByBatch(66);
  assert(batch66.length === 1, `Batch #66 har 1 scooter (fandt ${batch66.length})`);
});

runTest('Test 9: FleetManager - Region statistics', () => {
  const fleet = new AugmentFleetManager(testDevices);
  const stats = fleet.getRegionStats();

  assert(Object.keys(stats).length > 0, 'Statistik indeholder data');
  assert(stats['38'] !== undefined, 'Statistik for region 38 findes');
  assert(stats['38'].count === 2, `Region 38 har 2 scootere (har ${stats['38']?.count})`);
  assert(stats['38'].city === 'København', 'Region 38 er København');
  assert(stats['39'].avgBattery > 0, `Gennemsnit batteri beregnet (${stats['39']?.avgBattery}%)`);
});

runTest('Test 10: FleetManager - Fleet validation', () => {
  const fleet = new AugmentFleetManager(testDevices);
  const validation = fleet.validateFleet();

  assert(validation.total === 6, `Total antal enheder er 6 (har ${validation.total})`);
  assert(validation.valid === 6, `Alle 6 enheder er gyldige (har ${validation.valid})`);
  assert(validation.invalid === 0, `Ingen ugyldige enheder (har ${validation.invalid})`);
});

runTest('Test 11: FleetManager - Firmware update queue', () => {
  const fleet = new AugmentFleetManager(testDevices);

  // Find enheder der ikke har v2.5.0
  const queue = fleet.getFirmwareUpdateQueue('v2.5.0', {
    maxDevices: 10,
    prioritizeLowBatch: true
  });

  assert(queue.length === 5, `5 enheder skal opdateres (fandt ${queue.length})`);
  assert(queue[0].id !== '6', 'Enhed #6 har allerede v2.5.0');

  // Check at de er sorteret efter batch nummer
  const parser1 = new AugmentUUIDParser(queue[0].bt_mac);
  const parser2 = new AugmentUUIDParser(queue[1].bt_mac);
  assert(
    parser1.getBatchNumber() <= parser2.getBatchNumber(),
    'Enheder er sorteret efter batch (lav til høj)'
  );
});

runTest('Test 12: FleetManager - Region-filtered firmware queue', () => {
  const fleet = new AugmentFleetManager(testDevices);

  const copenhagenQueue = fleet.getFirmwareUpdateQueue('v2.5.0', {
    region: '38',
    maxDevices: 10
  });

  assert(copenhagenQueue.length === 2, `2 København Central enheder skal opdateres (fandt ${copenhagenQueue.length})`);

  copenhagenQueue.forEach(device => {
    const parser = new AugmentUUIDParser(device.bt_mac);
    assert(
      parser.getRegionCode() === '38',
      `Enhed ${device.id} er fra region 38`
    );
  });
});

runTest('Test 13: toString() formattering', () => {
  const parser = new AugmentUUIDParser('550e8400-ff01-3801-0042-a1b2c3d4e5f6');
  const str = parser.toString();

  assert(
    str.includes('København Central'),
    'toString() indeholder distrikt navn'
  );
  assert(
    str.includes('66'),
    'toString() indeholder batch nummer'
  );
});

runTest('Test 14: getFullInfo() komplet data', () => {
  const parser = new AugmentUUIDParser('550e8400-ff01-3801-0042-a1b2c3d4e5f6');
  const info = parser.getFullInfo();

  assert(info.uuid !== undefined, 'Info har uuid');
  assert(info.valid !== undefined, 'Info har valid');
  assert(info.deployment_id !== undefined, 'Info har deployment_id');
  assert(info.region !== undefined, 'Info har region');
  assert(info.batch !== undefined, 'Info har batch');
  assert(info.device_id !== undefined, 'Info har device_id');
  assert(info.region.code === '38', 'Region kode er korrekt');
  assert(info.batch.number === 66, 'Batch nummer er korrekt');
});

runTest('Test 15: Case insensitivity', () => {
  const parserLower = new AugmentUUIDParser('550e8400-ff01-3801-0042-a1b2c3d4e5f6');
  const parserUpper = new AugmentUUIDParser('550E8400-FF01-3801-0042-A1B2C3D4E5F6');

  assert(
    parserLower.getRegionCode() === parserUpper.getRegionCode(),
    'Case insensitive parsing virker'
  );
});

// ===== RESULTAT =====

log('\n' + '='.repeat(50), 'blue');
log(`\nTest Resultat: ${passedTests}/${totalTests} tests bestået`, passedTests === totalTests ? 'green' : 'red');

if (passedTests === totalTests) {
  log('\n🎉 Alle tests bestået!', 'green');
} else {
  log(`\n⚠️  ${totalTests - passedTests} tests fejlede`, 'red');
  process.exit(1);
}

log('\n' + '='.repeat(50), 'blue');
