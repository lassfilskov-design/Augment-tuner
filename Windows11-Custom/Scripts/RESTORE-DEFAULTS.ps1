# Windows 11 Restore Defaults Script
# Gendanner Windows til standard indstillinger

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Windows 11 - Gendan Standard Indstillinger" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Kræver Administrator rettigheder
if (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Warning "Dette script skal køres som Administrator!"
    Break
}

Write-Host "Dette script vil gendanne Windows til standard indstillinger." -ForegroundColor Yellow
Write-Host ""
Write-Host "ANBEFALINGER:" -ForegroundColor Cyan
Write-Host "  1. Brug Windows System Restore (hurtigere og sikrere)" -ForegroundColor White
Write-Host "  2. Søg 'Create a restore point' i Start Menu" -ForegroundColor White
Write-Host "  3. Klik 'System Restore' og vælg restore point" -ForegroundColor White
Write-Host ""
Write-Host "Vil du fortsætte med manuel restore? [Y/N]: " -ForegroundColor Yellow -NoNewline
$confirmation = Read-Host

if ($confirmation -ne 'Y' -and $confirmation -ne 'y') {
    Write-Host ""
    Write-Host "Restore afbrudt. Brug System Restore i stedet." -ForegroundColor Green
    Read-Host "Tryk Enter for at afslutte"
    exit
}

Write-Host ""
Write-Host "Gendanner indstillinger..." -ForegroundColor Yellow
Write-Host ""

# Re-enable services
Write-Host "Genaktiverer services..." -ForegroundColor Gray
$ServicesToEnable = @(
    "DiagTrack",
    "dmwappushservice",
    "SysMain",
    "WSearch"
)

foreach ($Service in $ServicesToEnable) {
    Set-Service -Name $Service -StartupType Automatic -ErrorAction SilentlyContinue
    Write-Host "  ✓ $Service" -ForegroundColor DarkGreen
}

# Re-enable telemetry
Write-Host ""
Write-Host "Genaktiverer telemetri..." -ForegroundColor Gray
Set-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\DataCollection" -Name "AllowTelemetry" -Type DWord -Value 3 -Force

# Re-enable hibernation
Write-Host ""
Write-Host "Genaktiverer hibernation..." -ForegroundColor Gray
powercfg -h on

# Restore balanced power plan
Write-Host ""
Write-Host "Gendanner Balanced power plan..." -ForegroundColor Gray
powercfg -setactive 381b4222-f694-41f0-9685-ff5bb260df2e

# Re-enable Windows 11 context menu
Write-Host ""
Write-Host "Gendanner Windows 11 context menu..." -ForegroundColor Gray
Remove-Item -Path "HKCU:\Software\Classes\CLSID\{86ca1aa0-34aa-4e8b-a509-50c905bae2a2}" -Recurse -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "Standard indstillinger gendannet!" -ForegroundColor Green
Write-Host "Genstart din computer for at ændringerne træder i kraft." -ForegroundColor Yellow
Write-Host ""
Read-Host "Tryk Enter for at afslutte"
