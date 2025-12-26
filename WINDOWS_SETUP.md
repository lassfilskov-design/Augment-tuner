# 🪟 Windows Setup Guide - Augment E-Scooter Backend

## Hurtig Start (5 minutter)

### 1️⃣ Installer PostgreSQL

**Download:**
https://www.postgresql.org/download/windows/

**Under installation:**
- Vælg password: `postgres` (eller hvad du vil)
- Port: `5432` (standard)
- Locale: `Danish, Denmark`

**Efter installation:**
1. Åbn **pgAdmin 4** (installeret med PostgreSQL)
2. Højreklik på "Databases" → "Create" → "Database"
3. Navn: `scooter_db`
4. Klik "Save"

### 2️⃣ Installer Node.js

**Download:**
https://nodejs.org/ (LTS version - v20.x)

**Verificer installation:**
```cmd
node --version
npm --version
```

### 3️⃣ Setup Projekt

**Åbn Command Prompt (CMD):**
```cmd
cd "C:\Users\Filæ\Downloads\default_25_12_26_01_52_23"
```

**Installer dependencies:**
```cmd
npm install
npm install -g typescript ts-node
```

### 4️⃣ Konfigurer .env

**Opret `.env` fil i projekt mappen:**
```env
# Database - OPDATER MED DINE CREDENTIALS
DATABASE_HOST=localhost
DATABASE_PORT=5432
DATABASE_USER=postgres
DATABASE_PASSWORD=postgres
DATABASE_NAME=scooter_db

# Server
PORT=4000
NODE_ENV=development

# JWT
JWT_SECRET=min-super-hemmelige-nøgle-skift-denne-i-produktion
JWT_EXPIRES_IN=7d

# AWS (Valgfrit - til senere)
AWS_REGION=eu-west-1
AWS_ACCESS_KEY_ID=
AWS_SECRET_ACCESS_KEY=
AWS_S3_BUCKET_NAME=

# Chargebee (Valgfrit - til senere)
CHARGEBEE_SITE=
CHARGEBEE_API_KEY=

# Firebase (Valgfrit - til senere)
FIREBASE_PROJECT_ID=
FIREBASE_PRIVATE_KEY=
FIREBASE_CLIENT_EMAIL=
```

### 5️⃣ Start Serveren

```cmd
npm run dev
```

**Output du skal se:**
```
🚀 Server ready at http://localhost:4000/
🗄️  Database connected successfully
```

### 6️⃣ Test API

**Åbn browser:**
```
http://localhost:4000/graphql
```

Du skulle nu se **GraphQL Playground**! 🎉

---

## 🔧 Fejlfinding

### Fejl: "npm is not recognized"

**Løsning:**
1. Genstart CMD efter Node.js installation
2. Eller tilføj til PATH:
   - Søg "Environment Variables" i Windows
   - Find "Path" under "System variables"
   - Tilføj: `C:\Program Files\nodejs\`

### Fejl: "Database connection failed"

**Løsning:**
1. Check PostgreSQL kører:
   - Søg "Services" i Windows
   - Find "postgresql-x64-XX"
   - Status skal være "Running"

2. Verificer credentials i `.env` matcher PostgreSQL

3. Test forbindelse i pgAdmin først

### Fejl: "Port 4000 already in use"

**Løsning:**
Skift port i `.env`:
```env
PORT=5000
```

### Fejl: "Cannot find module 'typescript'"

**Løsning:**
```cmd
npm install -g typescript ts-node
npm install
```

---

## 📝 Næste Skridt

### 1. Integrer Rigtige Augment Schema

Kopier entities fra **INTEGRATION_GUIDE.md** til dit projekt:

```
default_25_12_26_01_52_23/
├── src/
│   ├── entities/
│   │   ├── User.ts          ← Opdater med rigtig struktur
│   │   ├── Device.ts        ← Opdater med BLE MAC, firmware etc.
│   │   ├── Firmware.ts      ← Tilføj denne nye
│   │   ├── DeviceShareOtp.ts ← Tilføj denne nye
│   │   └── ...
```

### 2. Test GraphQL Queries

I GraphQL Playground (`http://localhost:4000/graphql`):

```graphql
# Test 1: Hent alle devices
query {
  meDevices {
    id
    device_name
    battery_percentage
    firmware_version
  }
}

# Test 2: Opret OTP til device sharing
mutation {
  shareDeviceOtp(deviceId: "uuid-her") {
    otp_code
    expires_at
  }
}
```

### 3. Tilføj Bluetooth Gateway (Valgfrit)

For at forbinde til rigtige e-scooters via Bluetooth:

1. Setup en **Raspberry Pi** eller **ESP32** som BLE gateway
2. Den kommunikerer med backend via MQTT
3. Backend sender kommandoer (lock/unlock) via MQTT
4. Gateway sender dem til scooter via Bluetooth

**Simpel MQTT test:**
```cmd
npm install mqtt
```

### 4. Integrer Chargebee Betalinger

1. Opret gratis konto: https://www.chargebee.com/trial/
2. Hent API keys fra dashboard
3. Opdater `.env` med dine keys
4. Test betalings mutations i GraphQL

---

## 🎯 Kom i Gang Med Augment Clone

### Fuldstændig Integration Checklist:

- [ ] ✅ PostgreSQL installeret og kører
- [ ] ✅ Node.js installeret
- [ ] ✅ Projekt dependencies installeret
- [ ] ✅ `.env` konfigureret
- [ ] ✅ Server starter uden fejl
- [ ] ✅ GraphQL Playground tilgængelig
- [ ] 📋 Entities opdateret med Augment schema
- [ ] 📋 GraphQL schema opdateret med rigtige queries/mutations
- [ ] 📋 AWS S3 bucket oprettet til firmware
- [ ] 📋 Chargebee konto oprettet
- [ ] 📋 Firebase projekt oprettet
- [ ] 📋 BLE gateway setup (Raspberry Pi/ESP32)
- [ ] 📋 Mobile app forbundet

---

## 📚 Resourcer

**Vigtige filer i dette repository:**
- `augment-api-schema.json` - Den rigtige Augment database struktur
- `api-config.json` - Alle service integrationer og UUIDs
- `INTEGRATION_GUIDE.md` - Detaljeret integrations guide

**Eksterne guides:**
- TypeORM: https://typeorm.io/
- GraphQL: https://graphql.org/learn/
- Apollo Server: https://www.apollographql.com/docs/apollo-server/
- Chargebee: https://www.chargebee.com/docs/2.0/index.html

---

## 💡 Tips

1. **Start simpelt**: Få backend til at køre først, tilføj features gradvist
2. **Test løbende**: Brug GraphQL Playground til at teste hver ny feature
3. **Commit ofte**: Brug git til at gemme dit arbejde
4. **Læs dokumentationen**: Check `INTEGRATION_GUIDE.md` for detaljer

**Held og lykke! 🚀**

Hvis du støder på problemer, check:
1. Console output for fejlmeddelelser
2. PostgreSQL logs
3. `.env` konfiguration
