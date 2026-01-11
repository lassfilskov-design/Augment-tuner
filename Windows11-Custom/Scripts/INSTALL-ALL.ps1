# Windows 11 Custom Installation - Master Script
# Kører alle optimerings scripts i korrekt rækkefølge

Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "  WINDOWS 11 CUSTOM INSTALLATION & OPTIMIZATION" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Dette script vil optimere dit Windows 11 system for:" -ForegroundColor Yellow
Write-Host "  ✓ Maksimal stabilitet" -ForegroundColor Green
Write-Host "  ✓ Bedre performance" -ForegroundColor Green
Write-Host "  ✓ Reduceret ressource forbrug" -ForegroundColor Green
Write-Host "  ✓ Fjernelse af bloatware" -ForegroundColor Green
Write-Host ""
Write-Host "VIGTIG INFORMATION:" -ForegroundColor Red
Write-Host "  - Sikkerhedsindstillinger påvirkes IKKE" -ForegroundColor White
Write-Host "  - Windows Defender bevares" -ForegroundColor White
Write-Host "  - Firewall bevares" -ForegroundColor White
Write-Host "  - Windows Update bevares" -ForegroundColor White
Write-Host ""

# Kræver Administrator rettigheder
if (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Host "FEJL: Dette script skal køres som Administrator!" -ForegroundColor Red
    Write-Host "Højreklik på filen og vælg 'Run as Administrator'" -ForegroundColor Yellow
    Write-Host ""
    Read-Host "Tryk Enter for at afslutte"
    Break
}

# Bekræftelse
Write-Host "Er du sikker på du vil fortsætte?" -ForegroundColor Yellow
Write-Host "Tryk [Y] for at fortsætte, eller [N] for at afbryde: " -ForegroundColor White -NoNewline
$confirmation = Read-Host

if ($confirmation -ne 'Y' -and $confirmation -ne 'y') {
    Write-Host ""
    Write-Host "Installation afbrudt." -ForegroundColor Red
    Write-Host ""
    Read-Host "Tryk Enter for at afslutte"
    exit
}

Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "  STARTER OPTIMERING" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""

$scriptPath = Split-Path -Parent $MyInvocation.MyCommand.Path
$totalSteps = 4
$currentStep = 0

# Opret System Restore Point
Write-Host "Opretter System Restore Point..." -ForegroundColor Cyan
try {
    Enable-ComputerRestore -Drive "C:\"
    Checkpoint-Computer -Description "Før Windows 11 Custom Optimization" -RestorePointType "MODIFY_SETTINGS"
    Write-Host "✓ System Restore Point oprettet" -ForegroundColor Green
}
catch {
    Write-Host "⚠ Kunne ikke oprette System Restore Point" -ForegroundColor Yellow
    Write-Host "  Fortsætter alligevel..." -ForegroundColor Gray
}
Write-Host ""

# Script 1: Fjern Bloatware
$currentStep++
Write-Host "======================================================" -ForegroundColor DarkCyan
Write-Host "  STEP $currentStep/$totalSteps: BLOATWARE REMOVAL" -ForegroundColor DarkCyan
Write-Host "======================================================" -ForegroundColor DarkCyan
Write-Host ""
& "$scriptPath\1-Remove-Bloatware.ps1"
Write-Host ""
Start-Sleep -Seconds 2

# Script 2: Performance Optimization
$currentStep++
Write-Host "======================================================" -ForegroundColor DarkCyan
Write-Host "  STEP $currentStep/$totalSteps: PERFORMANCE OPTIMIZATION" -ForegroundColor DarkCyan
Write-Host "======================================================" -ForegroundColor DarkCyan
Write-Host ""
& "$scriptPath\2-Performance-Optimization.ps1"
Write-Host ""
Start-Sleep -Seconds 2

# Script 3: Services Optimization
$currentStep++
Write-Host "======================================================" -ForegroundColor DarkCyan
Write-Host "  STEP $currentStep/$totalSteps: SERVICES OPTIMIZATION" -ForegroundColor DarkCyan
Write-Host "======================================================" -ForegroundColor DarkCyan
Write-Host ""
& "$scriptPath\3-Optimize-Services.ps1"
Write-Host ""
Start-Sleep -Seconds 2

# Script 4: System Tweaks
$currentStep++
Write-Host "======================================================" -ForegroundColor DarkCyan
Write-Host "  STEP $currentStep/$totalSteps: SYSTEM TWEAKS" -ForegroundColor DarkCyan
Write-Host "======================================================" -ForegroundColor DarkCyan
Write-Host ""
& "$scriptPath\4-System-Tweaks.ps1"
Write-Host ""

# Færdig
Write-Host ""
Write-Host "======================================================" -ForegroundColor Green
Write-Host "  OPTIMERING FULDFØRT!" -ForegroundColor Green
Write-Host "======================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Hvad der er blevet gjort:" -ForegroundColor Cyan
Write-Host "  ✓ Fjernet bloatware apps" -ForegroundColor White
Write-Host "  ✓ Optimeret performance indstillinger" -ForegroundColor White
Write-Host "  ✓ Deaktiveret unødvendige services" -ForegroundColor White
Write-Host "  ✓ Anvendt system tweaks" -ForegroundColor White
Write-Host ""
Write-Host "VIGTIG PÅMINDELSE:" -ForegroundColor Yellow
Write-Host "  • Genstart din computer for at alle ændringer træder i kraft" -ForegroundColor White
Write-Host "  • Sikkerhedsfeatures (Defender, Firewall, Updates) er bevaret" -ForegroundColor White
Write-Host "  • System Restore Point er oprettet hvis du vil rulle tilbage" -ForegroundColor White
Write-Host ""
Write-Host "Vil du genstarte nu? [Y/N]: " -ForegroundColor Yellow -NoNewline
$restart = Read-Host

if ($restart -eq 'Y' -or $restart -eq 'y') {
    Write-Host ""
    Write-Host "Genstarter om 10 sekunder..." -ForegroundColor Cyan
    Write-Host "Tryk Ctrl+C for at afbryde" -ForegroundColor Gray
    Start-Sleep -Seconds 10
    Restart-Computer -Force
}
else {
    Write-Host ""
    Write-Host "Genstart manuelt når du er klar." -ForegroundColor Cyan
    Write-Host ""
    Read-Host "Tryk Enter for at afslutte"
}
