# Windows 11 Services Optimization Script
# Deaktiverer unødvendige services for bedre performance
# BEMÆRK: Sikkerhedsrelaterede services røres IKKE

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Windows 11 Services Optimering" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Kræver Administrator rettigheder
if (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Warning "Dette script skal køres som Administrator!"
    Break
}

Write-Host "Optimerer Windows Services..." -ForegroundColor Yellow
Write-Host "Sikkerhedsrelaterede services bevares!" -ForegroundColor Green
Write-Host ""

# Liste over services der kan deaktiveres sikkert
# Disse er primært telemetri, diagnostics og unødvendige features
$ServicesToDisable = @{
    "DiagTrack" = "Connected User Experiences and Telemetry"
    "dmwappushservice" = "Device Management Wireless Application Protocol"
    "SysMain" = "Superfetch (kan forårsage disk thrashing)"
    "WSearch" = "Windows Search (reducerer baggrunds disk aktivitet)"
    "XblAuthManager" = "Xbox Live Auth Manager"
    "XblGameSave" = "Xbox Live Game Save"
    "XboxGipSvc" = "Xbox Accessory Management Service"
    "XboxNetApiSvc" = "Xbox Live Networking Service"
    "MapsBroker" = "Downloaded Maps Manager"
    "lfsvc" = "Geolocation Service"
    "RetailDemo" = "Retail Demo Service"
    "WerSvc" = "Windows Error Reporting"
    "Fax" = "Fax Service"
    "fhsvc" = "File History Service"
    "RemoteRegistry" = "Remote Registry"
    "TabletInputService" = "Touch Keyboard and Handwriting Panel"
    "wisvc" = "Windows Insider Service"
    "WMPNetworkSvc" = "Windows Media Player Network Sharing"
    "WpcMonSvc" = "Parental Controls"
    "PcaSvc" = "Program Compatibility Assistant"
    "WalletService" = "WalletService"
    "CDPSvc" = "Connected Devices Platform Service"
    "PhoneSvc" = "Phone Service"
    "OneSyncSvc" = "Sync Host Service"
}

$counter = 0
$total = $ServicesToDisable.Count

foreach ($Service in $ServicesToDisable.GetEnumerator()) {
    $counter++
    Write-Host "[$counter/$total] Behandler: $($Service.Value)" -ForegroundColor Gray

    $serviceObj = Get-Service -Name $Service.Key -ErrorAction SilentlyContinue

    if ($serviceObj) {
        try {
            # Stop service hvis den kører
            if ($serviceObj.Status -eq 'Running') {
                Stop-Service -Name $Service.Key -Force -ErrorAction SilentlyContinue
            }

            # Deaktiver service
            Set-Service -Name $Service.Key -StartupType Disabled -ErrorAction Stop
            Write-Host "  ✓ Deaktiveret: $($Service.Key)" -ForegroundColor DarkGreen
        }
        catch {
            Write-Host "  ✗ Kunne ikke deaktivere: $($Service.Key)" -ForegroundColor DarkYellow
        }
    }
    else {
        Write-Host "  - Service ikke fundet: $($Service.Key)" -ForegroundColor DarkGray
    }
}

Write-Host ""
Write-Host "Services optimering fuldført!" -ForegroundColor Green
Write-Host ""
Write-Host "VIGTIGE SERVICES DER BEVARES:" -ForegroundColor Cyan
Write-Host "  - Windows Defender (sikkerhed)" -ForegroundColor White
Write-Host "  - Windows Update (sikkerhed)" -ForegroundColor White
Write-Host "  - Firewall (sikkerhed)" -ForegroundColor White
Write-Host "  - Network services (konnektivitet)" -ForegroundColor White
Write-Host ""
