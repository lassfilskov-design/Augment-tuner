# Windows 11 System Tweaks Script
# Diverse system tweaks for bedre stabilitet og brugervenlighed

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Windows 11 System Tweaks" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Kræver Administrator rettigheder
if (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Warning "Dette script skal køres som Administrator!"
    Break
}

Write-Host "Anvender system tweaks..." -ForegroundColor Yellow
Write-Host ""

# EXPLORER TWEAKS
Write-Host "[1/15] Optimerer File Explorer..." -ForegroundColor Gray

# Vis file extensions
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\Advanced" -Name "HideFileExt" -Type DWord -Value 0 -Force

# Vis skjulte filer
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\Advanced" -Name "Hidden" -Type DWord -Value 1 -Force

# Åbn File Explorer til "This PC" i stedet for Quick Access
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\Advanced" -Name "LaunchTo" -Type DWord -Value 1 -Force

# Deaktiver Quick Access recent files
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer" -Name "ShowRecent" -Type DWord -Value 0 -Force
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer" -Name "ShowFrequent" -Type DWord -Value 0 -Force

# TASKBAR TWEAKS
Write-Host "[2/15] Optimerer Taskbar..." -ForegroundColor Gray

# Deaktiver Search icon på taskbar
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Search" -Name "SearchboxTaskbarMode" -Type DWord -Value 0 -Force

# Deaktiver Task View button
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\Advanced" -Name "ShowTaskViewButton" -Type DWord -Value 0 -Force

# Deaktiver Widgets
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\Advanced" -Name "TaskbarDa" -Type DWord -Value 0 -Force

# Deaktiver Chat icon
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\Advanced" -Name "TaskbarMn" -Type DWord -Value 0 -Force

# PRIVACY TWEAKS (uden at påvirke sikkerhed)
Write-Host "[3/15] Optimerer privacy indstillinger..." -ForegroundColor Gray

# Deaktiver Activity History
Set-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\System" -Name "EnableActivityFeed" -Type DWord -Value 0 -Force
Set-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\System" -Name "PublishUserActivities" -Type DWord -Value 0 -Force
Set-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\System" -Name "UploadUserActivities" -Type DWord -Value 0 -Force

# Deaktiver Location Tracking
Set-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\LocationAndSensors" -Name "DisableLocation" -Type DWord -Value 1 -Force

# Deaktiver Advertising ID
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\AdvertisingInfo" -Name "Enabled" -Type DWord -Value 0 -Force

# NETWORK TWEAKS
Write-Host "[4/15] Optimerer netværk..." -ForegroundColor Gray

# Deaktiver Network Throttling (forbedrer netværks performance)
New-Item -Path "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Multimedia\SystemProfile" -Force | Out-Null
Set-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Multimedia\SystemProfile" -Name "NetworkThrottlingIndex" -Type DWord -Value 0xffffffff -Force

# DISK TWEAKS
Write-Host "[5/15] Optimerer disk indstillinger..." -ForegroundColor Gray

# Deaktiver automatisk defragmentering (skadelig for SSD, unødvendig for moderne systemer)
Disable-ScheduledTask -TaskName "\Microsoft\Windows\Defrag\ScheduledDefrag" -ErrorAction SilentlyContinue

# MAINTENANCE TWEAKS
Write-Host "[6/15] Konfigurerer maintenance..." -ForegroundColor Gray

# Deaktiver Automatic Maintenance wake-up
Set-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Schedule\Maintenance" -Name "WakeUp" -Type DWord -Value 0 -Force

# STARTUP TWEAKS
Write-Host "[7/15] Optimerer startup..." -ForegroundColor Gray

# Deaktiver startup delay for apps
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\Serialize" -Name "StartupDelayInMSec" -Type DWord -Value 0 -Force

# CONTEXT MENU TWEAKS
Write-Host "[8/15] Konfigurerer context menu..." -ForegroundColor Gray

# Vis klassisk Windows 10 context menu (hurtigere)
New-Item -Path "HKCU:\Software\Classes\CLSID\{86ca1aa0-34aa-4e8b-a509-50c905bae2a2}" -Force | Out-Null
New-Item -Path "HKCU:\Software\Classes\CLSID\{86ca1aa0-34aa-4e8b-a509-50c905bae2a2}\InprocServer32" -Force | Out-Null
Set-ItemProperty -Path "HKCU:\Software\Classes\CLSID\{86ca1aa0-34aa-4e8b-a509-50c905bae2a2}\InprocServer32" -Name "(Default)" -Value "" -Force

# NOTIFICATION TWEAKS
Write-Host "[9/15] Optimerer notifikationer..." -ForegroundColor Gray

# Deaktiver diverse notifikationer
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Notifications\Settings" -Name "NOC_GLOBAL_SETTING_ALLOW_TOASTS_ABOVE_LOCK" -Type DWord -Value 0 -Force
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Notifications\Settings" -Name "NOC_GLOBAL_SETTING_ALLOW_CRITICAL_TOASTS_ABOVE_LOCK" -Type DWord -Value 0 -Force

# STORAGE SENSE TWEAKS
Write-Host "[10/15] Konfigurerer Storage Sense..." -ForegroundColor Gray

# Enable Storage Sense (automatisk oprydning)
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\StorageSense\Parameters\StoragePolicy" -Name "01" -Type DWord -Value 1 -Force

# Run Storage Sense monthly
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\StorageSense\Parameters\StoragePolicy" -Name "2048" -Type DWord -Value 30 -Force

# WINDOWS UPDATE TWEAKS
Write-Host "[11/15] Optimerer Windows Update..." -ForegroundColor Gray

# Deaktiver automatic driver updates (forhindrer driver problemer)
Set-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\DriverSearching" -Name "SearchOrderConfig" -Type DWord -Value 0 -Force

# Deaktiver automatic app updates fra Store (reducerer baggrunds aktivitet)
Set-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\WindowsStore" -Name "AutoDownload" -Type DWord -Value 2 -Force

# MOUSE TWEAKS
Write-Host "[12/15] Optimerer mouse settings..." -ForegroundColor Gray

# Deaktiver mouse acceleration (bedre præcision)
Set-ItemProperty -Path "HKCU:\Control Panel\Mouse" -Name "MouseSpeed" -Type String -Value "0" -Force
Set-ItemProperty -Path "HKCU:\Control Panel\Mouse" -Name "MouseThreshold1" -Type String -Value "0" -Force
Set-ItemProperty -Path "HKCU:\Control Panel\Mouse" -Name "MouseThreshold2" -Type String -Value "0" -Force

# MEMORY MANAGEMENT
Write-Host "[13/15] Optimerer memory management..." -ForegroundColor Gray

# Disable paging for Windows Kernel (hvis nok RAM)
Set-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\Session Manager\Memory Management" -Name "ClearPageFileAtShutdown" -Type DWord -Value 0 -Force

# PROCESSOR SCHEDULING
Write-Host "[14/15] Optimerer processor scheduling..." -ForegroundColor Gray

# Optimér for background services (mere stabil system performance)
Set-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\PriorityControl" -Name "Win32PrioritySeparation" -Type DWord -Value 24 -Force

# TIME SERVICE
Write-Host "[15/15] Sikrer time synchronization..." -ForegroundColor Gray

# Synkronisér system tid ugentligt (vigtigt for stabilitet)
Set-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Services\W32Time\TimeProviders\NtpClient" -Name "SpecialPollInterval" -Type DWord -Value 604800 -Force

Write-Host ""
Write-Host "System tweaks fuldført!" -ForegroundColor Green
Write-Host "BEMÆRK: Genstart for at alle ændringer træder i kraft." -ForegroundColor Yellow
Write-Host ""
