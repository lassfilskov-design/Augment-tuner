# AI BUILDER PROMPT: Augment E-Scooter Backend Clone

## 🎯 MISSION
Build a production-ready GraphQL backend for an e-scooter sharing platform based on the Augment app architecture.

---

## 📋 PROJECT OVERVIEW

**What we're building:**
A complete backend system for managing e-scooters, users, payments, and firmware updates.

**Core functionality:**
- User authentication and management
- E-scooter device registration and control
- Payment processing via Chargebee
- Firmware OTA updates
- Device sharing system
- Real-time status updates

---

## 🛠️ TECH STACK (Use These Exact Technologies)

### Primary Stack
```yaml
Backend Framework: Node.js with TypeScript
API Layer: GraphQL (Apollo Server v4)
Database: PostgreSQL via Supabase
Authentication: Supabase Auth (replaces AWS Cognito)
Real-time: Supabase Realtime + WebSocket server
Hosting: Vercel (serverless functions) or Railway
ORM: Prisma
```

### Integrations
```yaml
Payments: Chargebee SDK
Monitoring: Sentry
Push Notifications: Firebase Admin SDK
Email: Resend or SendGrid
Storage: Supabase Storage (for firmware files)
```

### Development Tools
```yaml
Package Manager: pnpm
Code Quality: ESLint + Prettier
Testing: Vitest + Supertest
API Testing: GraphQL Playground
```

---

## 📐 DATABASE SCHEMA

### Create these Prisma models:

```prisma
// schema.prisma

model User {
  id                String       @id @default(uuid())
  email             String       @unique
  phoneNumber       String?
  countryCode       String       @default("DK")
  language          String       @default("en")
  createdAt         DateTime     @default(now())
  updatedAt         DateTime     @updatedAt
  
  // Integrations
  chargebeeCustomerId String?    @unique
  firstPromoterId     String?
  intercomHash        String?
  
  // Relations
  devices           DeviceUser[]
  subscriptions     Subscription[]
  invoices          Invoice[]
  createdOtps       DeviceSharingOtp[] @relation("OtpCreator")
  consumedOtps      DeviceSharingOtp[] @relation("OtpConsumer")
  
  @@index([email])
}

model Device {
  id                  String       @id @default(uuid())
  serialNumber        String       @unique
  btMac               String?      @unique
  deviceName          String       @default("My Scooter")
  firmwareVersion     String?
  hardwareVersion     String?
  certificationStd    String?
  totalKm             Float        @default(0)
  sharingEnabled      Boolean      @default(false)
  createdAt           DateTime     @default(now())
  updatedAt           DateTime     @updatedAt
  lastConnectedAt     DateTime?
  
  // Relations
  users               DeviceUser[]
  statusLogs          DeviceStatusLog[]
  sharingOtps         DeviceSharingOtp[]
  
  @@index([serialNumber])
  @@index([btMac])
}

model DeviceUser {
  id          String    @id @default(uuid())
  deviceId    String
  userId      String
  accessLevel String    @default("OWNER") // OWNER, SHARED, TEMPORARY
  grantedAt   DateTime  @default(now())
  expiresAt   DateTime?
  grantedBy   String?
  
  device      Device    @relation(fields: [deviceId], references: [id], onDelete: Cascade)
  user        User      @relation(fields: [userId], references: [id], onDelete: Cascade)
  
  @@unique([deviceId, userId])
  @@index([userId])
  @@index([deviceId])
}

model DeviceSharingOtp {
  id          String    @id @default(uuid())
  deviceId    String
  otpCode     String    @unique
  createdBy   String
  createdAt   DateTime  @default(now())
  expiresAt   DateTime
  consumed    Boolean   @default(false)
  consumedAt  DateTime?
  consumedBy  String?
  
  device      Device    @relation(fields: [deviceId], references: [id], onDelete: Cascade)
  creator     User      @relation("OtpCreator", fields: [createdBy], references: [id])
  consumer    User?     @relation("OtpConsumer", fields: [consumedBy], references: [id])
  
  @@index([otpCode])
  @@index([deviceId])
}

model DeviceStatusLog {
  id              String    @id @default(uuid())
  deviceId        String
  batteryLevel    Int       // 0-100
  speedLimit      Int       // km/h
  totalVoltageMv  Int
  cellVoltages    Json      // array of voltages
  temperature     Int?
  tripStartKm     Float?
  loggedAt        DateTime  @default(now())
  
  device          Device    @relation(fields: [deviceId], references: [id], onDelete: Cascade)
  
  @@index([deviceId, loggedAt])
}

model Subscription {
  id                      String    @id @default(uuid())
  userId                  String
  chargebeeSubscriptionId String    @unique
  status                  String    // ACTIVE, CANCELLED, PAUSED
  planId                  String
  currentTermStart        DateTime
  currentTermEnd          DateTime
  nextBillingAt           DateTime?
  createdAt               DateTime  @default(now())
  updatedAt               DateTime  @updatedAt
  
  user                    User      @relation(fields: [userId], references: [id], onDelete: Cascade)
  
  @@index([userId])
  @@index([status])
}

model Invoice {
  id                  String    @id @default(uuid())
  userId              String
  chargebeeInvoiceId  String    @unique
  amount              Float
  currency            String    @default("EUR")
  status              String    // PAID, PENDING, FAILED
  invoiceDate         DateTime
  dueDate             DateTime
  pdfUrl              String?
  createdAt           DateTime  @default(now())
  
  user                User      @relation(fields: [userId], references: [id], onDelete: Cascade)
  
  @@index([userId])
  @@index([status])
}

model FirmwareVersion {
  id                    String    @id @default(uuid())
  version               String    @unique
  hardwareCompatibility String[]
  releaseNotes          Json
  binaryUrl             String
  checksum              String
  releasedAt            DateTime  @default(now())
  mandatory             Boolean   @default(false)
  
  @@index([version])
}
```

---

## 🔧 IMPLEMENTATION PHASES

### PHASE 1: Foundation (Start Here)

#### 1.1 Project Setup
```bash
# Initialize project
mkdir augment-backend
cd augment-backend
pnpm init
pnpm add -D typescript @types/node tsx prisma
pnpm add @apollo/server graphql graphql-tag
pnpm add @supabase/supabase-js
pnpm add dotenv zod

# Setup TypeScript
npx tsc --init
```

#### 1.2 Create Environment Variables
```env
# .env
DATABASE_URL="postgresql://..."
SUPABASE_URL="https://xxx.supabase.co"
SUPABASE_ANON_KEY="..."
SUPABASE_SERVICE_KEY="..."
CHARGEBEE_SITE="your-site"
CHARGEBEE_API_KEY="..."
SENTRY_DSN="..."
JWT_SECRET="..."
PORT=4000
```

#### 1.3 Initialize Prisma
```bash
npx prisma init
# Copy the schema from above to prisma/schema.prisma
npx prisma migrate dev --name init
npx prisma generate
```

---

### PHASE 2: GraphQL API Structure

#### 2.1 Create GraphQL Schema

**File: `src/schema.ts`**

```graphql
type User {
  id: ID!
  email: String!
  phoneNumber: String
  countryCode: String!
  language: String!
  devices: [DeviceUser!]!
  subscriptions: [Subscription!]!
  createdAt: DateTime!
}

type Device {
  id: ID!
  serialNumber: String!
  btMac: String
  deviceName: String!
  firmwareVersion: String
  hardwareVersion: String
  totalKm: Float!
  sharingEnabled: Boolean!
  lastConnectedAt: DateTime
  users: [DeviceUser!]!
  currentStatus: DeviceStatusLog
}

type DeviceUser {
  id: ID!
  device: Device!
  user: User!
  accessLevel: AccessLevel!
  grantedAt: DateTime!
  expiresAt: DateTime
}

enum AccessLevel {
  OWNER
  SHARED
  TEMPORARY
}

type DeviceSharingOtp {
  id: ID!
  otpCode: String!
  device: Device!
  shareUrl: String!
  expiresAt: DateTime!
  consumed: Boolean!
}

type Subscription {
  id: ID!
  status: SubscriptionStatus!
  planId: String!
  currentTermEnd: DateTime!
  nextBillingAt: DateTime
}

enum SubscriptionStatus {
  ACTIVE
  CANCELLED
  PAUSED
}

type Invoice {
  id: ID!
  amount: Float!
  currency: String!
  status: InvoiceStatus!
  invoiceDate: DateTime!
  pdfUrl: String
}

enum InvoiceStatus {
  PAID
  PENDING
  FAILED
}

type MutationResponse {
  success: Boolean!
  message: String
  code: String
}

# Queries
type Query {
  me: User
  myDevices: [Device!]!
  device(id: ID!): Device
  checkDeviceOwnership(serialNumber: String!): Device
  mySubscriptions: [Subscription!]!
  myInvoices(limit: Int): [Invoice!]!
  firmwareUpgrade(serialNumber: String!): FirmwareVersion
}

# Mutations
type Mutation {
  # Device Management
  claimDevice(serialNumber: String!, btMac: String): ClaimDeviceResponse!
  renameDevice(deviceId: ID!, newName: String!): MutationResponse!
  resetDevice(deviceId: ID!): MutationResponse!
  
  # Sharing
  generateShareOtp(deviceId: ID!, expiresInHours: Int): DeviceSharingOtp!
  consumeShareOtp(otpCode: String!): MutationResponse!
  revokeDeviceAccess(deviceId: ID!, userId: ID!): MutationResponse!
  
  # Device Status
  addDeviceStatus(deviceId: ID!, status: DeviceStatusInput!): MutationResponse!
  
  # Payments
  createCheckoutSession: ChargebeeHostedPage!
  cancelSubscription(subscriptionId: ID!): MutationResponse!
}

type ClaimDeviceResponse {
  success: Boolean!
  message: String
  device: Device
}

type ChargebeeHostedPage {
  id: String!
  url: String!
  state: String!
}

input DeviceStatusInput {
  batteryLevel: Int!
  speedLimit: Int!
  totalVoltageMv: Int!
  cellVoltages: [Int!]!
  temperature: Int
  tripStartKm: Float
}

scalar DateTime
```

---

### PHASE 3: Resolvers Implementation

#### 3.1 User Queries

**File: `src/resolvers/user.ts`**

```typescript
import { PrismaClient } from '@prisma/client'
import { GraphQLError } from 'graphql'

const prisma = new PrismaClient()

export const userResolvers = {
  Query: {
    me: async (_: any, __: any, context: any) => {
      if (!context.user) {
        throw new GraphQLError('Not authenticated', {
          extensions: { code: 'UNAUTHENTICATED' }
        })
      }
      
      return await prisma.user.findUnique({
        where: { id: context.user.id }
      })
    },
    
    myDevices: async (_: any, __: any, context: any) => {
      if (!context.user) {
        throw new GraphQLError('Not authenticated', {
          extensions: { code: 'UNAUTHENTICATED' }
        })
      }
      
      const deviceUsers = await prisma.deviceUser.findMany({
        where: { userId: context.user.id },
        include: { device: true }
      })
      
      return deviceUsers.map(du => du.device)
    }
  }
}
```

#### 3.2 Device Mutations

**File: `src/resolvers/device.ts`**

```typescript
export const deviceResolvers = {
  Mutation: {
    claimDevice: async (_: any, args: any, context: any) => {
      if (!context.user) {
        throw new GraphQLError('Not authenticated', {
          extensions: { code: 'UNAUTHENTICATED' }
        })
      }
      
      const { serialNumber, btMac } = args
      
      // Check if device exists
      let device = await prisma.device.findUnique({
        where: { serialNumber }
      })
      
      if (!device) {
        // Create new device
        device = await prisma.device.create({
          data: {
            serialNumber,
            btMac,
            deviceName: 'My Scooter'
          }
        })
      }
      
      // Check if already claimed
      const existingOwner = await prisma.deviceUser.findFirst({
        where: {
          deviceId: device.id,
          accessLevel: 'OWNER'
        }
      })
      
      if (existingOwner) {
        return {
          success: false,
          message: 'Device already claimed by another user',
          device: null
        }
      }
      
      // Claim device
      await prisma.deviceUser.create({
        data: {
          deviceId: device.id,
          userId: context.user.id,
          accessLevel: 'OWNER'
        }
      })
      
      return {
        success: true,
        message: 'Device claimed successfully',
        device
      }
    },
    
    renameDevice: async (_: any, args: any, context: any) => {
      if (!context.user) {
        throw new GraphQLError('Not authenticated')
      }
      
      const { deviceId, newName } = args
      
      // Verify ownership
      const deviceUser = await prisma.deviceUser.findFirst({
        where: {
          deviceId,
          userId: context.user.id,
          accessLevel: 'OWNER'
        }
      })
      
      if (!deviceUser) {
        throw new GraphQLError('Device not found or access denied', {
          extensions: { code: 'FORBIDDEN' }
        })
      }
      
      await prisma.device.update({
        where: { id: deviceId },
        data: { deviceName: newName }
      })
      
      return {
        success: true,
        message: 'Device renamed successfully'
      }
    }
  }
}
```

#### 3.3 Sharing System

**File: `src/resolvers/sharing.ts`**

```typescript
import { nanoid } from 'nanoid'

export const sharingResolvers = {
  Mutation: {
    generateShareOtp: async (_: any, args: any, context: any) => {
      if (!context.user) {
        throw new GraphQLError('Not authenticated')
      }
      
      const { deviceId, expiresInHours = 24 } = args
      
      // Verify ownership
      const deviceUser = await prisma.deviceUser.findFirst({
        where: {
          deviceId,
          userId: context.user.id,
          accessLevel: 'OWNER'
        }
      })
      
      if (!deviceUser) {
        throw new GraphQLError('Device not found or access denied')
      }
      
      // Generate 6-digit OTP
      const otpCode = nanoid(6).toUpperCase()
      
      const expiresAt = new Date()
      expiresAt.setHours(expiresAt.getHours() + expiresInHours)
      
      const otp = await prisma.deviceSharingOtp.create({
        data: {
          deviceId,
          otpCode,
          createdBy: context.user.id,
          expiresAt
        },
        include: {
          device: true
        }
      })
      
      return {
        ...otp,
        shareUrl: `https://augment.eco/share?code=${otpCode}`
      }
    },
    
    consumeShareOtp: async (_: any, args: any, context: any) => {
      if (!context.user) {
        throw new GraphQLError('Not authenticated')
      }
      
      const { otpCode } = args
      
      const otp = await prisma.deviceSharingOtp.findUnique({
        where: { otpCode },
        include: { device: true }
      })
      
      if (!otp) {
        return {
          success: false,
          message: 'Invalid OTP code',
          code: 'INVALID_OTP'
        }
      }
      
      if (otp.consumed) {
        return {
          success: false,
          message: 'OTP already used',
          code: 'OTP_CONSUMED'
        }
      }
      
      if (new Date() > otp.expiresAt) {
        return {
          success: false,
          message: 'OTP expired',
          code: 'OTP_EXPIRED'
        }
      }
      
      // Mark as consumed
      await prisma.deviceSharingOtp.update({
        where: { id: otp.id },
        data: {
          consumed: true,
          consumedAt: new Date(),
          consumedBy: context.user.id
        }
      })
      
      // Grant access
      await prisma.deviceUser.create({
        data: {
          deviceId: otp.deviceId,
          userId: context.user.id,
          accessLevel: 'SHARED',
          grantedBy: otp.createdBy
        }
      })
      
      return {
        success: true,
        message: 'Device access granted'
      }
    }
  }
}
```

---

### PHASE 4: Authentication with Supabase

**File: `src/auth/context.ts`**

```typescript
import { createClient } from '@supabase/supabase-js'

const supabase = createClient(
  process.env.SUPABASE_URL!,
  process.env.SUPABASE_SERVICE_KEY!
)

export async function createContext({ req }: any) {
  const token = req.headers.authorization?.replace('Bearer ', '')
  
  if (!token) {
    return { user: null }
  }
  
  try {
    const { data: { user }, error } = await supabase.auth.getUser(token)
    
    if (error || !user) {
      return { user: null }
    }
    
    // Get or create user in our database
    let dbUser = await prisma.user.findUnique({
      where: { email: user.email! }
    })
    
    if (!dbUser) {
      dbUser = await prisma.user.create({
        data: {
          email: user.email!,
          phoneNumber: user.phone,
          countryCode: user.user_metadata?.country || 'DK',
          language: user.user_metadata?.language || 'en'
        }
      })
    }
    
    return { user: dbUser }
  } catch (error) {
    console.error('Auth error:', error)
    return { user: null }
  }
}
```

---

### PHASE 5: Chargebee Integration

**File: `src/services/chargebee.ts`**

```typescript
import chargebee from 'chargebee'

chargebee.configure({
  site: process.env.CHARGEBEE_SITE!,
  api_key: process.env.CHARGEBEE_API_KEY!
})

export const chargebeeService = {
  createCustomer: async (user: any) => {
    const result = await chargebee.customer.create({
      email: user.email,
      first_name: user.email.split('@')[0],
      phone: user.phoneNumber,
      locale: user.language
    }).request()
    
    return result.customer.id
  },
  
  createHostedPage: async (customerId: string, planId: string) => {
    const result = await chargebee.hosted_page.checkout_new({
      customer: { id: customerId },
      subscription: { plan_id: planId }
    }).request()
    
    return result.hosted_page
  },
  
  handleWebhook: async (event: any) => {
    switch (event.event_type) {
      case 'subscription_created':
        await handleSubscriptionCreated(event.content.subscription)
        break
      case 'payment_succeeded':
        await handlePaymentSucceeded(event.content.transaction)
        break
      case 'invoice_generated':
        await handleInvoiceGenerated(event.content.invoice)
        break
    }
  }
}

async function handleSubscriptionCreated(subscription: any) {
  await prisma.subscription.create({
    data: {
      userId: /* find user by chargebee customer id */,
      chargebeeSubscriptionId: subscription.id,
      status: subscription.status.toUpperCase(),
      planId: subscription.plan_id,
      currentTermStart: new Date(subscription.current_term_start * 1000),
      currentTermEnd: new Date(subscription.current_term_end * 1000),
      nextBillingAt: subscription.next_billing_at 
        ? new Date(subscription.next_billing_at * 1000) 
        : null
    }
  })
}
```

---

### PHASE 6: Main Server Setup

**File: `src/index.ts`**

```typescript
import { ApolloServer } from '@apollo/server'
import { startStandaloneServer } from '@apollo/server/standalone'
import { readFileSync } from 'fs'
import { createContext } from './auth/context'
import { userResolvers } from './resolvers/user'
import { deviceResolvers } from './resolvers/device'
import { sharingResolvers } from './resolvers/sharing'

const typeDefs = readFileSync('./src/schema.graphql', 'utf-8')

const resolvers = {
  Query: {
    ...userResolvers.Query
  },
  Mutation: {
    ...deviceResolvers.Mutation,
    ...sharingResolvers.Mutation
  }
}

const server = new ApolloServer({
  typeDefs,
  resolvers
})

const { url } = await startStandaloneServer(server, {
  context: createContext,
  listen: { port: parseInt(process.env.PORT || '4000') }
})

console.log(`🚀 Server ready at ${url}`)
```

---

## 🎯 SUCCESS CRITERIA

After implementation, you should be able to:

✅ **User Authentication**
- Sign up with email/password via Supabase
- Login and receive JWT token
- MFA support (Supabase handles this)

✅ **Device Management**
- Claim a device with serial number
- Rename devices
- View device list
- See device status/battery

✅ **Sharing System**
- Generate 6-digit OTP for sharing
- Share device via link
- Consume OTP to gain access
- Revoke access

✅ **Payments**
- Create Chargebee customer
- Generate hosted checkout page
- Webhook handling for subscriptions
- Invoice generation

✅ **API Features**
- GraphQL queries work
- Mutations work
- Authentication required where needed
- Error handling

---

## 🧪 TESTING CHECKLIST

```bash
# Test user creation
mutation {
  signUp(email: "test@example.com", password: "Test1234!") {
    success
    user { id email }
  }
}

# Test device claim
mutation {
  claimDevice(serialNumber: "TEST001", btMac: "AA:BB:CC:DD:EE:FF") {
    success
    device { id deviceName }
  }
}

# Test sharing
mutation {
  generateShareOtp(deviceId: "...", expiresInHours: 24) {
    otpCode
    shareUrl
    expiresAt
  }
}

# Test queries
query {
  me {
    email
    devices { deviceName serialNumber }
  }
}
```

---

## 🚀 DEPLOYMENT

### Option 1: Vercel (Recommended)
```bash
# Install Vercel CLI
pnpm add -g vercel

# Deploy
vercel --prod

# Set environment variables in Vercel dashboard
```

### Option 2: Railway
```bash
# Connect GitHub repo to Railway
# Set environment variables
# Deploy automatically on push
```

---

## 📝 NEXT STEPS AFTER MVP

Once basic backend works:

1. **Real-time Updates** - Add WebSocket for live device status
2. **Firmware System** - Build OTA update endpoints
3. **Admin Dashboard** - Build Next.js admin panel
4. **Analytics** - Add Sentry error tracking
5. **Email Notifications** - Integrate Resend/SendGrid
6. **Mobile App** - Build React Native app to connect

---

## ⚠️ IMPORTANT NOTES

1. **Start Simple**: Build Phase 1-3 first, test thoroughly
2. **Use Supabase Auth**: Simpler than AWS Cognito
3. **Test Payments in Sandbox**: Use Chargebee test mode
4. **Version Control**: Commit after each phase
5. **Environment Variables**: Never commit .env file

---

## 🎬 START BUILDING NOW

Copy this entire prompt to your AI builder (Cursor, v0, Bolt.new) and say:

"Build this backend step by step, starting with Phase 1. 
Ask me questions if anything is unclear. Let's go!"

Good luck! 🚀
