# Augment E-Scooter Backend Integration Guide

## 🎯 Formål
Denne guide hjælper dig med at integrere det rigtige Augment API-struktur i dit genererede Workik projekt.

## 📋 Trin 1: Database Schema Integration

### Erstat den genererede schema med den rigtige Augment struktur

I dit projekt skal du opdatere TypeORM entities baseret på `augment-api-schema.json`:

### **src/entities/User.ts**
```typescript
import { Entity, PrimaryGeneratedColumn, Column, OneToMany, CreateDateColumn } from 'typeorm';
import { Device } from './Device';
import { Subscription } from './Subscription';
import { PaymentSource } from './PaymentSource';

@Entity('users')
export class User {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ unique: true })
  email: string;

  @Column()
  password_hash: string;

  @Column({ nullable: true })
  phone: string;

  @Column({ nullable: true })
  cognito_username: string;

  @CreateDateColumn()
  created_at: Date;

  @OneToMany(() => Device, device => device.owner)
  devices: Device[];

  @OneToMany(() => Subscription, subscription => subscription.user)
  subscriptions: Subscription[];

  @OneToMany(() => PaymentSource, paymentSource => paymentSource.user)
  payment_sources: PaymentSource[];
}
```

### **src/entities/Device.ts**
```typescript
import { Entity, PrimaryGeneratedColumn, Column, ManyToOne, OneToMany, CreateDateColumn, JoinColumn } from 'typeorm';
import { User } from './User';
import { DeviceUser } from './DeviceUser';
import { DeviceStatus } from './DeviceStatus';

@Entity('devices')
export class Device {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ unique: true })
  bt_mac: string; // Bluetooth MAC address

  @Column()
  device_name: string;

  @Column({ nullable: true })
  firmware_version: string;

  @Column({ type: 'int', nullable: true })
  battery_percentage: number;

  @Column({ type: 'float', nullable: true })
  battery_voltage: number;

  @Column({ type: 'float', nullable: true })
  cell_voltage: number;

  @Column({ type: 'float', nullable: true })
  speed: number;

  @Column({ type: 'uuid' })
  owner_id: string;

  @ManyToOne(() => User, user => user.devices)
  @JoinColumn({ name: 'owner_id' })
  owner: User;

  @Column({ type: 'boolean', default: false })
  sharing_enabled: boolean;

  @CreateDateColumn()
  created_at: Date;

  @OneToMany(() => DeviceUser, deviceUser => deviceUser.device)
  device_users: DeviceUser[];

  @OneToMany(() => DeviceStatus, status => status.device)
  device_status: DeviceStatus[];
}
```

### **src/entities/DeviceShareOtp.ts**
```typescript
import { Entity, PrimaryGeneratedColumn, Column, ManyToOne, JoinColumn } from 'typeorm';
import { Device } from './Device';

@Entity('device_share_otp')
export class DeviceShareOtp {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column({ type: 'uuid' })
  device_id: string;

  @ManyToOne(() => Device)
  @JoinColumn({ name: 'device_id' })
  device: Device;

  @Column()
  otp_code: string;

  @Column({ type: 'timestamp' })
  expires_at: Date;

  @Column({ type: 'boolean', default: false })
  used: boolean;
}
```

### **src/entities/Firmware.ts**
```typescript
import { Entity, PrimaryGeneratedColumn, Column } from 'typeorm';

@Entity('firmware')
export class Firmware {
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @Column()
  version: string;

  @Column()
  device_type: string; // 'scooter', 'controller', 'battery'

  @Column()
  file_url: string; // S3 URL

  @Column({ type: 'int' })
  file_size: number;

  @Column({ type: 'text', nullable: true })
  changelog: string;

  @Column({ default: 'en' })
  language: string;

  @Column({ type: 'date' })
  release_date: Date;

  @Column({ type: 'boolean', default: true })
  is_stable: boolean;
}
```

## 📋 Trin 2: GraphQL Schema Integration

### Opdater din GraphQL schema baseret på rigtige Augment operationer

**src/graphql/schema.graphql**
```graphql
type Query {
  # Device Queries (fra rigtige Augment API)
  meDevices: [Device!]!
  checkDeviceOwnership(deviceId: ID!): Boolean!
  deviceQuery(deviceId: ID!): Device
  deviceSubUsers(deviceId: ID!): [DeviceUser!]!

  # Firmware Queries
  firmwareUpgrade(deviceType: String!, currentVersion: String!): Firmware

  # Payment Queries
  mePaymentSources: [PaymentSource!]!
  meManagePayment: ChargebeeHostedPage!
}

type Mutation {
  # Device Mutations (fra rigtige Augment API)
  renameDevice(deviceId: ID!, newName: String!): Device!
  changeDeviceSharingSettings(deviceId: ID!, enabled: Boolean!): Device!
  shareDeviceOtp(deviceId: ID!): DeviceShareOtp!
  consumeShareDeviceOtp(otpCode: String!): Device!
  claimDeviceOwnership(btMac: String!): Device!
  resetDeviceOwnership(deviceId: ID!): Boolean!
  revokeAccessToOwnersDevice(deviceId: ID!, userId: ID!): Boolean!

  # Device Status
  addDeviceStatus(deviceId: ID!, batteryLevel: Int!, speed: Float!, location: JSON!): DeviceStatus!

  # Payment Mutations
  assignPaymentSourceRole(paymentSourceId: ID!, role: String!): PaymentSource!
  deletePaymentSource(paymentSourceId: ID!): Boolean!
}

type Device {
  id: ID!
  bt_mac: String!
  device_name: String!
  firmware_version: String
  battery_percentage: Int
  battery_voltage: Float
  cell_voltage: Float
  speed: Float
  owner: User!
  sharing_enabled: Boolean!
  created_at: String!
}

type DeviceShareOtp {
  id: ID!
  device_id: ID!
  otp_code: String!
  expires_at: String!
  used: Boolean!
}

type Firmware {
  id: ID!
  version: String!
  device_type: String!
  file_url: String!
  file_size: Int!
  changelog: String
  language: String!
  release_date: String!
  is_stable: Boolean!
}

type PaymentSource {
  id: ID!
  user_id: ID!
  payment_type: String!
  chargebee_customer_id: String!
  chargebee_payment_source_id: String!
  is_primary: Boolean!
  role: String
}

type ChargebeeHostedPage {
  id: String!
  url: String!
  state: String!
}

scalar JSON
```

## 📋 Trin 3: Environment Variables Setup

### Opdater din `.env` fil med rigtige integration keys

```env
# Database
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_USER=postgres
DATABASE_PASSWORD=yourpassword
DATABASE_NAME=scooter_db

# GraphQL API
GRAPHQL_ENDPOINT=https://frbc72oc4h.execute-api.eu-west-1.amazonaws.com/prod/graphql-public
PORT=4000

# JWT Authentication
JWT_SECRET=your-super-secret-jwt-key-change-this
JWT_EXPIRES_IN=7d

# AWS Services (Region: eu-west-1)
AWS_REGION=eu-west-1
AWS_ACCESS_KEY_ID=your-aws-access-key
AWS_SECRET_ACCESS_KEY=your-aws-secret-key
AWS_S3_BUCKET_NAME=augment-firmware-bucket

# AWS Cognito
COGNITO_USER_POOL_ID=your-cognito-pool-id
COGNITO_CLIENT_ID=your-cognito-client-id
COGNITO_REGION=eu-west-1

# Chargebee Payment Integration
CHARGEBEE_SITE=your-site-name
CHARGEBEE_API_KEY=your-chargebee-api-key

# Firebase Cloud Messaging
FIREBASE_PROJECT_ID=your-firebase-project-id
FIREBASE_PRIVATE_KEY=your-firebase-private-key
FIREBASE_CLIENT_EMAIL=your-firebase-client-email

# Sentry Error Tracking
SENTRY_DSN=your-sentry-dsn

# MQTT Broker (for BLE gateway communication)
MQTT_BROKER_URL=mqtt://broker.hivemq.com
MQTT_PORT=1883
MQTT_USERNAME=
MQTT_PASSWORD=

# Bluetooth Service UUIDs (fra Augment APK)
BLE_SERVICE_UUID_PRIMARY=00006680-0000-1000-8000-00805f9b34fb
BLE_SERVICE_UUID_SECONDARY=00006681-0000-1000-8000-00805f9b34fb
```

## 📋 Trin 4: Bluetooth Integration

### BLE Service UUIDs (fra Augment APK analyse)

Integrer disse Bluetooth UUIDs i din IoT service:

**src/services/bluetooth/constants.ts**
```typescript
export const BLUETOOTH_SERVICES = {
  PRIMARY: '00006680-0000-1000-8000-00805f9b34fb',
  SECONDARY: '00006681-0000-1000-8000-00805f9b34fb',
  CONTROL: '00006683-0000-1000-8000-00805f9b34fb',
  STATUS: '00006684-0000-1000-8000-00805f9b34fb',
  FIRMWARE: '00006685-0000-1000-8000-00805f9b34fb',
  BATTERY: '00006687-0000-1000-8000-00805f9b34fb',
  GPS: '00006688-0000-1000-8000-00805f9b34fb',
  DEVICE_INFO: '0000d101-0000-1000-8000-00805f9b34fb',
  OTA_SERVICE_1: '0000fed7-0000-1000-8000-00805f9b34fb',
  OTA_SERVICE_2: '0000fed8-0000-1000-8000-00805f9b34fb',
};

export const DEVICE_COMMANDS = {
  LOCK: 'lock',
  UNLOCK: 'unlock',
  SET_SPEED_LIMIT: 'setSpeedLimit',
  SET_LIGHTS: 'setLights',
  GET_STATUS: 'getStatus',
  START_OTA_UPDATE: 'startOtaUpdate',
  RESET_DEVICE: 'resetDevice',
};
```

## 📋 Trin 5: Chargebee Integration

### Setup Chargebee resolvers

**src/graphql/resolvers/payment.resolvers.ts**
```typescript
import { chargebee } from '../../services/chargebee';

export const paymentResolvers = {
  Query: {
    mePaymentSources: async (_, __, { user }) => {
      if (!user) throw new Error('Not authenticated');

      // Fetch from Chargebee
      const customer = await chargebee.customer.retrieve(user.chargebee_customer_id).request();
      return customer.customer.payment_sources;
    },

    meManagePayment: async (_, __, { user }) => {
      if (!user) throw new Error('Not authenticated');

      const hostedPage = await chargebee.hosted_page.manage_payment_sources({
        customer: { id: user.chargebee_customer_id }
      }).request();

      return hostedPage.hosted_page;
    },
  },

  Mutation: {
    assignPaymentSourceRole: async (_, { paymentSourceId, role }, { user }) => {
      // Implementation
    },

    deletePaymentSource: async (_, { paymentSourceId }, { user }) => {
      // Implementation
    },
  },
};
```

## 📋 Trin 6: Firmware OTA Updates

### S3 Integration for firmware files

**src/services/firmware/firmware.service.ts**
```typescript
import AWS from 'aws-sdk';

const s3 = new AWS.S3({
  region: process.env.AWS_REGION,
  accessKeyId: process.env.AWS_ACCESS_KEY_ID,
  secretAccessKey: process.env.AWS_SECRET_ACCESS_KEY,
});

export class FirmwareService {
  async uploadFirmware(file: Buffer, version: string, deviceType: string): Promise<string> {
    const key = `firmware/${deviceType}/${version}.bin`;

    await s3.putObject({
      Bucket: process.env.AWS_S3_BUCKET_NAME!,
      Key: key,
      Body: file,
      ContentType: 'application/octet-stream',
    }).promise();

    return s3.getSignedUrl('getObject', {
      Bucket: process.env.AWS_S3_BUCKET_NAME!,
      Key: key,
      Expires: 3600, // 1 hour
    });
  }

  async checkForUpdates(deviceType: string, currentVersion: string) {
    // Query database for newer firmware
    const firmware = await Firmware.findOne({
      where: {
        device_type: deviceType,
        is_stable: true,
      },
      order: {
        release_date: 'DESC',
      },
    });

    if (firmware && firmware.version > currentVersion) {
      return firmware;
    }

    return null;
  }
}
```

## 📋 Trin 7: Testing

### Test dine endpoints

```bash
# Start serveren
npm run dev

# Test GraphQL endpoint
curl -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ meDevices { id device_name battery_percentage } }"}'
```

## 🔗 Reference Files

Se disse filer i `/home/user/Augment-tuner/`:
- `augment-api-schema.json` - Komplet database struktur
- `api-config.json` - Alle API endpoints og service konfiguration
- `database-schema.csv` - CSV format af schema

## 📱 Mobile App Integration

For at forbinde en React Native app til dit backend:

1. **GraphQL Client Setup**
```typescript
import { ApolloClient, InMemoryCache } from '@apollo/client';

const client = new ApolloClient({
  uri: 'http://localhost:4000/graphql',
  cache: new InMemoryCache(),
  headers: {
    authorization: `Bearer ${jwtToken}`,
  },
});
```

2. **Bluetooth Integration** (React Native)
```typescript
import BleManager from 'react-native-ble-manager';

// Scan for devices
BleManager.scan([], 5, true).then(() => {
  console.log('Scanning...');
});

// Connect to scooter
BleManager.connect(deviceId)
  .then(() => {
    return BleManager.retrieveServices(deviceId);
  })
  .then((deviceInfo) => {
    // Read/Write characteristics
  });
```

## 🚀 Næste Skridt

1. ✅ Kopier dit genererede projekt til Linux miljø eller kør det lokalt på Windows
2. ✅ Implementer entities fra denne guide
3. ✅ Opdater GraphQL schema
4. ✅ Konfigurer environment variables
5. ✅ Test endpoints
6. ✅ Integrer Bluetooth gateway
7. ✅ Setup Chargebee og AWS services

Held og lykke! 🎉
