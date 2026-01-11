# Frequently Asked Questions (FAQ)

Svar på de mest almindelige spørgsmål om Windows 11 Custom Optimization.

---

## 📑 Indhold

- [Sikkerhed](#sikkerhed)
- [Installation](#installation)
- [Performance](#performance)
- [Funktionalitet](#funktionalitet)
- [Problemer](#problemer)
- [Avanceret](#avanceret)

---

## 🔒 Sikkerhed

### Er det sikkert at bruge disse scripts?

**Ja, absolut.** Scripts er designet med sikkerhed som højeste prioritet:

✅ Windows Defender bevares fuldt funktionelt
✅ Windows Firewall bevares
✅ Windows Update bevares (sikkerhedsopdateringer)
✅ UAC (User Account Control) bevares
✅ BitLocker påvirkes ikke
✅ Ingen malware eller skadelig kode

Scripts fjerner kun bloatware og optimerer indstillinger. Der er ingen sikkerhedsrisiko.

---

### Bliver min computer mere sårbar?

**Nej.** Alle sikkerhedsrelaterede features bevares:
- Antivirus (Windows Defender)
- Firewall
- SmartScreen
- Windows Update
- Secure Boot
- TPM

Det eneste der fjernes er bloatware apps og unødvendige services som ikke har med sikkerhed at gøre.

---

### Kan jeg stadig få sikkerhedsopdateringer?

**Ja!** Windows Update er fuldt funktionelt. Du får stadig:
- Sikkerhedsopdateringer
- Critical updates
- Windows Defender definition updates

Det eneste der er ændret er at automatiske driver updates og Store app updates er deaktiveret - men du kan stadig installere dem manuelt.

---

## 📥 Installation

### Hvordan kører jeg scripts som Administrator?

**Metode 1:**
1. Højreklik på script filen (.ps1)
2. Vælg "Run with PowerShell"
3. Bekræft UAC prompt

**Metode 2:**
1. Søg "PowerShell" i Start Menu
2. Højreklik "Windows PowerShell"
3. Vælg "Run as administrator"
4. Naviger til scripts: `cd C:\Windows11-Custom\Scripts`
5. Kør script: `.\INSTALL-ALL.ps1`

---

### Hvad betyder "cannot be loaded because running scripts is disabled"?

Dette er PowerShell's execution policy der blokerer scripts af sikkerhedsmæssige årsager.

**Fix:**
```powershell
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

Dette tillader lokale scripts at køre, mens downloaded scripts stadig kræver signatur.

---

### Skal jeg køre alle scripts?

**Nej, det er valgfrit.** Du kan vælge hvilke optimeringer du vil have:

**Minimal installation:**
```powershell
.\1-Remove-Bloatware.ps1  # Kun fjern bloatware
```

**Standard installation:**
```powershell
.\INSTALL-ALL.ps1  # Alle optimeringer (anbefalet)
```

**Custom installation:**
Kør kun de scripts du vil have individuelt.

---

### Hvor lang tid tager installationen?

**Automatisk (INSTALL-ALL.ps1):** 5-15 minutter
**Manuel (hvert script individuelt):** 10-20 minutter

Afhænger af:
- Antal apps der skal fjernes
- System hastighed
- SSD vs HDD

---

## ⚡ Performance

### Hvor meget hurtigere bliver min computer?

**Typiske forbedringer:**
- Boot tid: 10-30% hurtigere
- System respons: 5-15% bedre
- RAM forbrug: 200-500MB mindre
- Disk plads: 2-5GB frigjort
- Baggrunds processer: 20-40% færre

**Faktorer:**
- Ældre/langsommere computere ser størst forbedring
- Nye high-end computere ser mindre (men stadig mærkbar) forbedring
- SSD vs HDD gør stor forskel

---

### Vil mine spil køre hurtigere?

**Sandsynligvis ja.** Du kan forvente:
- 5-10% højere FPS pga. færre baggrunds processer
- Mere stabil frametime (mindre stuttering)
- Hurtigere load times (mindre disk aktivitet)

**BEMÆRK:** Xbox Game Bar og Xbox services deaktiveres. Hvis du bruger disse, se "Hvordan genaktiverer jeg Xbox features?"

---

### Hvorfor er Windows Search langsom nu?

Windows Search service (WSearch) er deaktiveret fordi den:
- Bruger 50-150MB RAM konstant
- Forårsager konstant disk aktivitet
- Indexerer filer i baggrunden

**Du kan stadig søge,** det er bare lidt langsommere.

**Hvis du vil have hurtig søgning tilbage:**
```powershell
Set-Service -Name "WSearch" -StartupType Automatic
Start-Service -Name "WSearch"
```

---

## 🔧 Funktionalitet

### Hvilke features mister jeg?

**Apps der fjernes:**
- Xbox apps (Game Bar, Xbox App, etc.)
- Bing apps (News, Weather)
- Pre-installerede spil (Solitaire, etc.)
- Microsoft Teams (consumer version)
- Tips og Feedback apps
- OneDrive fjernes IKKE (du kan fjerne det manuelt hvis ønsket)

**Features der deaktiveres:**
- Windows Search indexering (søgning virker stadig, bare langsommere)
- Game DVR
- Cortana
- Activity History
- Timeline
- Advertising ID

**Alt andet bevares!**

---

### Virker OneDrive stadig?

**Ja.** Scripts rører ikke OneDrive. Det forbliver installeret og funktionelt.

**Hvis du vil fjerne OneDrive:**
```powershell
winget uninstall Microsoft.OneDrive
```

**Eller via Settings:**
Settings → Apps → Installed apps → OneDrive → Uninstall

---

### Hvordan genaktiverer jeg Xbox features?

Hvis du bruger Xbox Game Pass eller Xbox social features:

```powershell
# Re-enable Xbox services
Set-Service -Name "XblAuthManager" -StartupType Automatic
Set-Service -Name "XblGameSave" -StartupType Automatic
Set-Service -Name "XboxGipSvc" -StartupType Automatic
Set-Service -Name "XboxNetApiSvc" -StartupType Automatic

# Start services
Start-Service -Name "XblAuthManager"
Start-Service -Name "XblGameSave"
Start-Service -Name "XboxGipSvc"
Start-Service -Name "XboxNetApiSvc"

# Re-install Xbox apps if removed
winget install "Xbox"
```

---

### Kan jeg stadig bruge Microsoft Store?

**Ja!** Microsoft Store bevares fuldt funktionelt.

Det eneste der er ændret er at automatiske app updates er deaktiveret. Du kan stadig:
- Downloade apps
- Opdatere apps manuelt
- Bruge Store normalt

---

## 🔄 Problemer

### Hvordan ruller jeg ændringerne tilbage?

**Metode 1: System Restore (Anbefalet)**
1. Søg "Create a restore point" i Start Menu
2. Klik "System Restore"
3. Vælg restore point "Før Windows 11 Custom Optimization"
4. Følg wizard
5. Genstart

**Metode 2: Restore Script**
```powershell
.\RESTORE-DEFAULTS.ps1
```
Bemærk: Dette gendanner kun nogle indstillinger, ikke bloatware apps.

**Metode 3: Re-installer Windows**
Ekstrem løsning hvis alt andet fejler.

---

### Nogle apps kom tilbage efter Windows Update

Dette kan ske hvis Microsoft pusher apps som "recommended".

**Løsning:**
```powershell
.\1-Remove-Bloatware.ps1  # Kør bloatware removal igen
```

**Permanent prevention:**
Scripts fjerner allerede provisioned packages, men Microsoft kan nogle gange force-reinstall apps.

---

### Min printer virker ikke mere

**Usandsynligt,** da printer services bevares.

**Troubleshoot:**
1. Check printer forbindelse
2. Genstart printer
3. Check at Print Spooler service kører:
   ```powershell
   Get-Service -Name "Spooler"
   Start-Service -Name "Spooler"
   ```

---

### Bluetooth virker ikke

**Usandsynligt,** da Bluetooth services bevares.

**Troubleshoot:**
```powershell
Get-Service -Name "bthserv"
Start-Service -Name "bthserv"
```

Hvis stadig problemer, brug System Restore.

---

## 🎓 Avanceret

### Kan jeg modificere scripts?

**Ja!** Scripts er open source PowerShell filer.

**Før du modificerer:**
1. Lav backup af original scripts
2. Forstå hvad hver kommando gør
3. Test ændringer i en test miljø først

**Tips:**
- Kommentér linjer ud med `#` for at springe dem over
- Tilføj dine egne tweaks
- Del forbedringer med andre!

---

### Kan jeg bruge dette på en ny Windows installation?

**Ja, perfekt!**

**Best practice:**
1. Installer Windows 11 clean
2. Kør Windows Update
3. Installer kritiske drivers
4. KØR DISSE SCRIPTS
5. Installer dine apps
6. Opret backup

Dette giver den bedste base for et optimeret system.

---

### Hvad er forskellen på DisablePagingExecutive?

`DisablePagingExecutive = 1` betyder:
- Windows kernel holder sig i RAM (swapper ikke til disk)
- Kræver tilstrækkelig RAM (8GB+)
- Giver bedre performance
- Reducerer disk I/O

**Anbefalet hvis:**
- Du har 8GB+ RAM
- Du har SSD

**Deaktiver hvis:**
- Du har <8GB RAM
- Du oplever out-of-memory fejl

---

### Hvordan ser jeg hvilke services der kører?

**Via Task Manager:**
1. Tryk Ctrl+Shift+Esc
2. Gå til "Services" tab

**Via PowerShell:**
```powershell
Get-Service | Where-Object {$_.Status -eq 'Running'} | Select-Object Name, DisplayName
```

**Via Services app:**
1. Tryk Win+R
2. Skriv: `services.msc`
3. Tryk Enter

---

### Kan jeg bruge scripts på Windows 10?

**Delvist.** Mange scripts vil virke, men nogle er specifikt til Windows 11:

**Virker på Windows 10:**
- Bloatware removal (mest)
- Performance optimization
- Services optimization
- Mange system tweaks

**Virker IKKE på Windows 10:**
- Windows 11 context menu tweak
- Nogle taskbar tweaks
- Windows 11-specifikke apps

**Anbefaling:** Test scripts individuelt på Windows 10.

---

### Hvordan laver jeg min egen custom ISO?

Dette kræver mere avanceret arbejde:

**Værktøjer:**
- NTLite (Windows Image editor)
- DISM (Deployment Image Servicing)
- Windows 11 ISO fra Microsoft

**Process (overordnet):**
1. Download Windows 11 ISO
2. Extract ISO
3. Mount install.wim
4. Apply tweaks via DISM/NTLite
5. Integrate scripts
6. Create new ISO

**Dette er avanceret og uden for scope af denne guide.**

---

### Hvordan automatiserer jeg dette for flere computere?

**Via Group Policy (Domain):**
1. Konvertér registry tweaks til GPO
2. Deploy via AD
3. Distribuér scripts via login scripts

**Via MDT/SCCM:**
1. Integrer scripts i deployment task sequence
2. Deploy til computere

**Via Remote PowerShell:**
```powershell
# På hver remote computer
Invoke-Command -ComputerName PC1,PC2,PC3 -FilePath .\INSTALL-ALL.ps1
```

---

## 💡 Tips & Tricks

### Ekstra Tweaks

**Deaktiver Windows Animations:**
```powershell
Set-ItemProperty -Path "HKCU:\Control Panel\Desktop\WindowMetrics" -Name "MinAnimate" -Value "0"
```

**Deaktiver Lock Screen:**
```powershell
Set-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\Personalization" -Name "NoLockScreen" -Value 1
```

**Fjern OneDrive:**
```powershell
winget uninstall Microsoft.OneDrive
```

**Classic Right-Click Menu (Windows 10 style):**
Allerede inkluderet i scripts!

---

### Maintenance

**Månedlig maintenance:**
```powershell
# 1. Kør Disk Cleanup
cleanmgr /d C:

# 2. Check for bloatware
.\1-Remove-Bloatware.ps1

# 3. Update Windows
# Settings → Windows Update
```

**Efter store Windows Updates:**
- Check om bloatware er returneret
- Verificér services stadig er deaktiveret
- Re-kør scripts hvis nødvendigt

---

## 📞 Stadig problemer?

Hvis dit spørgsmål ikke er besvaret her:

1. Check [DETAILED-CHANGES.md](DETAILED-CHANGES.md) for tekniske detaljer
2. Check [INSTALLATION-GUIDE.md](INSTALLATION-GUIDE.md) for step-by-step guide
3. Brug System Restore hvis du oplever problemer
4. Google specifikke fejlmeddelelser

---

**Sidst opdateret:** Januar 2026
