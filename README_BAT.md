# 🦇 Batch Scripts Guide

## Oversigt

Dette repository indeholder 3 batch scripts til nem setup på Windows:

### 📦 `setup.bat` - Første installation
Kør denne fil FØRST når du downloader projektet.

**Hvad gør den:**
- ✅ Checker om Node.js er installeret
- ✅ Installerer alle npm dependencies
- ✅ Installerer TypeScript globalt
- ✅ Opretter `.env` fil
- ✅ Starter serveren automatisk

**Sådan bruger du den:**
1. Åbn projekt mappen i File Explorer
2. Dobbeltklik på `setup.bat`
3. Følg instruktionerne i vinduet

---

### 🚀 `start.bat` - Start serveren
Brug denne til at starte backend serveren efter første installation.

**Hvad gør den:**
- ✅ Checker om dependencies er installeret
- ✅ Checker om `.env` fil eksisterer
- ✅ Starter development server
- ✅ Åbner GraphQL Playground

**Sådan bruger du den:**
1. Dobbeltklik på `start.bat`
2. Serveren starter på `http://localhost:4000`
3. GraphQL Playground: `http://localhost:4000/graphql`

**Stop serveren:**
Tryk `CTRL+C` i vinduet

---

### 🗄️ `test-db.bat` - Test database forbindelse
Brug denne til at teste om PostgreSQL databasen er konfigureret korrekt.

**Hvad gør den:**
- ✅ Checker om PostgreSQL er installeret
- ✅ Læser credentials fra `.env`
- ✅ Tester forbindelse til databasen
- ✅ Viser database version

**Sådan bruger du den:**
1. Dobbeltklik på `test-db.bat`
2. Se om forbindelsen virker
3. Hvis fejl, følg fejlbeskederne

---

## 🎯 Quick Start

### Første gang (helt ny installation):

```
1. Installer PostgreSQL: https://www.postgresql.org/download/windows/
2. Installer Node.js: https://nodejs.org/
3. Download/unzip projektet
4. Dobbeltklik setup.bat
5. Vent til installation er færdig
6. Rediger .env filen (hvis nødvendigt)
7. Opret database i pgAdmin: 'scooter_db'
8. Dobbeltklik start.bat
```

### Dagligt brug:

```
1. Dobbeltklik start.bat
2. Åbn browser: http://localhost:4000/graphql
3. Begynd at kode!
```

---

## 🔧 Fejlfinding

### "npm is not recognized"

**Problem:** Node.js ikke i PATH

**Løsning:**
1. Genstart computeren efter Node.js installation
2. Eller tilføj manuelt til PATH:
   - Windows Search: "Environment Variables"
   - Rediger "Path" variable
   - Tilføj: `C:\Program Files\nodejs\`

### "psql is not recognized"

**Problem:** PostgreSQL CLI ikke i PATH

**Løsning:**
Tilføj PostgreSQL bin mappe til PATH:
```
C:\Program Files\PostgreSQL\16\bin
```
(Skift "16" til din version)

### Database forbindelse fejler

**Tjek:**
1. PostgreSQL service kører:
   - Windows Search: "Services"
   - Find "postgresql-x64-XX"
   - Status: "Running"

2. Database eksisterer:
   - Åbn pgAdmin
   - Se om "scooter_db" findes

3. Credentials i `.env` er korrekte

### Port 4000 allerede i brug

**Løsning:**
Rediger `.env`:
```env
PORT=5000
```

---

## 📝 Manual Setup (hvis batch scripts ikke virker)

### 1. Installer dependencies
```cmd
npm install
npm install -g typescript ts-node
```

### 2. Opret .env fil
```cmd
copy .env.example .env
notepad .env
```

### 3. Start server
```cmd
npm run dev
```

---

## 🎨 Tilpas Scripts

Alle `.bat` filer kan redigeres med Notepad:

1. Højreklik på filen
2. "Edit" eller "Rediger"
3. Gem ændringer
4. Luk Notepad

**Eksempel - ændre standard port:**
Åbn `start.bat` og tilføj:
```batch
set PORT=5000
```

---

## ✨ Tips & Tricks

### Automatisk åbn browser
Tilføj til `start.bat` efter "call npm run dev":
```batch
timeout /t 5
start http://localhost:4000/graphql
```

### Log output til fil
Kør script med output redirection:
```cmd
start.bat > server.log 2>&1
```

### Kør i baggrunden
Start server uden at holde vinduet åbent:
```cmd
start /B npm run dev
```

---

## 🆘 Support

**Fejl med scripts?**
1. Åbn Command Prompt som Administrator
2. Kør script derfra
3. Kopier fejlbesked
4. Se WINDOWS_SETUP.md for detaljeret guide

**Stadig problemer?**
- Check Node.js version: `node --version` (skal være v16+)
- Check npm version: `npm --version`
- Se projekt logs i console
- Tjek PostgreSQL error logs

---

## 📚 Mere Information

- **WINDOWS_SETUP.md** - Detaljeret installation guide
- **INTEGRATION_GUIDE.md** - API integration guide
- **package.json** - Se tilgængelige npm scripts

God fornøjelse! 🚀
