# Windows 11 Custom Installation Guide

Komplet step-by-step guide til at installere og optimere dit Windows 11 system.

---

## 📋 Indholdsfortegnelse

1. [Før Installation](#før-installation)
2. [Download & Forberedelse](#download--forberedelse)
3. [Installation Metoder](#installation-metoder)
4. [Efter Installation](#efter-installation)
5. [Fejlfinding](#fejlfinding)
6. [Verifikation](#verifikation)

---

## 🔍 Før Installation

### System Check

Før du starter, verificér følgende:

#### 1. System Requirements
```
OS: Windows 11 (alle versioner - Home, Pro, Enterprise)
RAM: Minimum 4GB (8GB+ anbefalet)
Disk: Minimum 5GB fri plads
Rettigheder: Administrator adgang påkrævet
```

#### 2. Backup
⚠️ **VIGTIGT:** Selvom scripts er sikre, lav altid backup først!

**Anbefalede backup metoder:**
1. System Restore Point (oprettes automatisk af scriptet)
2. Windows Backup til ekstern disk
3. Cloud backup af vigtige filer
4. Disk image (f.eks. med Macrium Reflect eller Acronis)

#### 3. Opdater Windows
Kør Windows Update først for at sikre dit system er up-to-date:
```
Settings → Windows Update → Check for updates
```

#### 4. Luk vigtige programmer
Luk alle vigtige programmer før du starter:
- Browsers med vigtige tabs
- Office dokumenter
- Spil
- Etc.

---

## 📥 Download & Forberedelse

### Step 1: Download Scripts

1. Download alle filer fra repository
2. Udpak til en mappe, f.eks.: `C:\Windows11-Custom\`

### Step 2: Verificer Filer

Sikr at du har følgende struktur:

```
Windows11-Custom/
├── Scripts/
│   ├── 1-Remove-Bloatware.ps1
│   ├── 2-Performance-Optimization.ps1
│   ├── 3-Optimize-Services.ps1
│   ├── 4-System-Tweaks.ps1
│   ├── INSTALL-ALL.ps1
│   └── RESTORE-DEFAULTS.ps1
├── Docs/
│   ├── DETAILED-CHANGES.md
│   ├── INSTALLATION-GUIDE.md
│   └── FAQ.md
└── README.md
```

### Step 3: Aktivér PowerShell Scripts

Scripts kan muligvis ikke køre pga. execution policy.

**Fix:**
1. Åbn PowerShell som Administrator
2. Kør: `Set-ExecutionPolicy RemoteSigned -Scope CurrentUser`
3. Tryk `Y` for at bekræfte

---

## 🚀 Installation Metoder

### Metode A: Automatisk Installation (Anbefalet)

**Fordele:**
- ✅ Nemmest og hurtigst
- ✅ Opretter automatisk System Restore Point
- ✅ Kører alle scripts i korrekt rækkefølge
- ✅ Brugervenlige prompts og feedback

**Trin:**

1. **Naviger til Scripts mappen**
   ```
   Åbn File Explorer
   Gå til C:\Windows11-Custom\Scripts\
   ```

2. **Kør Master Script**
   ```
   Højreklik på: INSTALL-ALL.ps1
   Vælg: "Run with PowerShell"

   ELLER hvis det ikke virker:

   Højreklik på: INSTALL-ALL.ps1
   Vælg: "Open with" → "Windows PowerShell"
   ```

3. **UAC Prompt**
   ```
   Når du ser User Account Control prompt:
   Klik "Yes" for at give administrator rettigheder
   ```

4. **Bekræft Installation**
   ```
   Læs informationen på skærmen
   Tryk Y og Enter for at fortsætte
   ```

5. **Vent mens scripts kører**
   ```
   Du vil se:
   - System Restore Point oprettes
   - Bloatware fjernes
   - Performance optimeres
   - Services deaktiveres
   - System tweaks anvendes
   ```

6. **Genstart**
   ```
   Når færdig, tryk Y for at genstarte
   ELLER tryk N og genstart manuelt senere
   ```

**Forventet tid:** 5-15 minutter afhængig af system

---

### Metode B: Manuel Installation

**Fordele:**
- ✅ Fuld kontrol over processen
- ✅ Kan springe scripts over
- ✅ Kan køre scripts individuelt over tid

**Trin:**

1. **Opret System Restore Point manuelt**
   ```
   1. Søg "Create a restore point" i Start Menu
   2. Klik "Create" knappen
   3. Navngiv: "Før Windows 11 Optimization"
   4. Vent til færdig
   ```

2. **Åbn PowerShell som Administrator**
   ```
   1. Søg "PowerShell" i Start Menu
   2. Højreklik "Windows PowerShell"
   3. Vælg "Run as administrator"
   ```

3. **Naviger til Scripts mappe**
   ```powershell
   cd C:\Windows11-Custom\Scripts
   ```

4. **Kør scripts i rækkefølge**
   ```powershell
   # Script 1: Fjern Bloatware
   .\1-Remove-Bloatware.ps1

   # Script 2: Performance Optimization
   .\2-Performance-Optimization.ps1

   # Script 3: Optimize Services
   .\3-Optimize-Services.ps1

   # Script 4: System Tweaks
   .\4-System-Tweaks.ps1
   ```

5. **Genstart**
   ```powershell
   Restart-Computer
   ```

**Forventet tid:** 10-20 minutter (inkl. manuel setup)

---

### Metode C: Selektiv Installation

Vil du kun køre specifikke optimeringer? Kør kun de scripts du vil have.

**Eksempler:**

**Kun fjerne bloatware:**
```powershell
.\1-Remove-Bloatware.ps1
```

**Kun performance tweaks:**
```powershell
.\2-Performance-Optimization.ps1
.\3-Optimize-Services.ps1
```

**Kun UI tweaks:**
```powershell
.\4-System-Tweaks.ps1
```

---

## ✅ Efter Installation

### Step 1: Genstart (Vigtigt!)

Mange ændringer træder først i kraft efter genstart:
```
Settings → Power → Restart
```

### Step 2: Verificér Ændringer

Efter genstart, check følgende:

#### File Explorer
```
✓ Kan du se file extensions? (.txt, .pdf, etc.)
✓ Åbner File Explorer til "This PC"?
```

#### Taskbar
```
✓ Er Search box væk?
✓ Er Task View button væk?
✓ Er Widgets væk?
```

#### Performance
```
✓ Er boot hurtigere?
✓ Er Windows mere responsivt?
✓ Bruger Task Manager mindre RAM?
```

### Step 3: Test Vigtige Funktioner

Verificér at alt virker:

```
✓ Internet forbindelse
✓ Lyd
✓ Bluetooth (hvis du bruger det)
✓ Printere (hvis du bruger dem)
✓ Vigtige apps
```

### Step 4: Windows Defender Check

Verificér at sikkerhed stadig er aktiv:
```
Settings → Privacy & security → Windows Security

Check:
✓ Virus & threat protection: ON
✓ Firewall: ON
✓ App & browser control: ON
```

---

## 🔧 Fejlfinding

### Problem 1: Scripts kan ikke køre

**Symptom:**
```
... cannot be loaded because running scripts is disabled on this system
```

**Løsning:**
```powershell
# Åbn PowerShell som Administrator
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser

# Bekræft med Y
```

---

### Problem 2: "Access Denied" fejl

**Symptom:**
```
Access to the registry key is denied
```

**Løsning:**
```
1. Sikr du kører som Administrator
2. Højreklik på PowerShell
3. Vælg "Run as administrator"
```

---

### Problem 3: Nogle apps kom tilbage efter Windows Update

**Symptom:**
Bloatware apps re-installeres

**Løsning:**
```powershell
# Kør bloatware removal script igen
.\1-Remove-Bloatware.ps1
```

**Permanent fix:**
Scripts fjerner allerede provisioned packages, men Windows Update kan nogle gange re-installere dem.

---

### Problem 4: Windows Search virker ikke

**Symptom:**
Start Menu søgning er langsom

**Forventet:**
Dette er normalt - WSearch service er deaktiveret for performance.

**Hvis du vil have hurtig søgning tilbage:**
```powershell
Set-Service -Name "WSearch" -StartupType Automatic
Start-Service -Name "WSearch"
```

---

### Problem 5: Xbox gaming virker ikke

**Symptom:**
Xbox Game Bar / Game Pass virker ikke

**Forventet:**
Xbox services er deaktiveret.

**Hvis du bruger Xbox features:**
```powershell
Set-Service -Name "XblAuthManager" -StartupType Automatic
Set-Service -Name "XblGameSave" -StartupType Automatic
Set-Service -Name "XboxGipSvc" -StartupType Automatic
Set-Service -Name "XboxNetApiSvc" -StartupType Automatic

Start-Service -Name "XblAuthManager"
Start-Service -Name "XblGameSave"
```

---

### Problem 6: System er ustabilt efter installation

**Løsning 1: System Restore**
```
1. Søg "Create a restore point"
2. Klik "System Restore"
3. Vælg restore point fra før optimization
4. Følg wizard
```

**Løsning 2: Restore Script**
```powershell
.\RESTORE-DEFAULTS.ps1
```

---

## 📊 Verifikation

### Performance Metrics

Sammenlign før/efter ved at tjekke:

#### Task Manager
```
Tryk Ctrl+Shift+Esc

Check:
- Memory usage (skal være lavere)
- Disk activity (skal være lavere)
- CPU background usage (skal være lavere)
- Antal background processes (skal være færre)
```

#### Boot Tid
```
1. Åbn Task Manager
2. Gå til "Startup" tab
3. Se "Last BIOS time" - skal være lavere
```

#### Disk Usage
```
Settings → System → Storage

Check frigjort plads (2-5GB mere)
```

---

## 📝 Post-Installation Checklist

Print eller gem denne checklist:

```
□ Genstartet computer
□ Verificeret File Explorer ændringer
□ Verificeret Taskbar ændringer
□ Testet internet forbindelse
□ Testet vigtige apps
□ Verificeret Windows Defender er aktivt
□ Verificeret Windows Update virker
□ Tjekket Task Manager for reduceret ressource forbrug
□ Noteret boot tid forbedring
□ Gemt System Restore Point information
```

---

## 🎯 Næste Skridt

Efter vellykket installation:

1. **Brug systemet normalt i 1-2 dage**
   - Verificér stabilitet
   - Identificér eventuelle problemer

2. **Overvej yderligere tweaks**
   - Se FAQ.md for flere tips
   - Tilpas efter dine behov

3. **Hold scripts**
   - Gem scripts til fremtidig brug
   - Måske skal du re-køre bloatware removal efter store Windows Updates

---

## 🆘 Support

Hvis du oplever problemer:

1. Check [DETAILED-CHANGES.md](DETAILED-CHANGES.md) for at forstå hvad der er ændret
2. Se [FAQ.md](FAQ.md) for almindelige spørgsmål
3. Brug System Restore hvis nødvendigt
4. Kør RESTORE-DEFAULTS.ps1 for at rulle tilbage

---

**Held og lykke med din optimerede Windows 11 installation!** 🚀

---

**Sidste opdatering:** Januar 2026
