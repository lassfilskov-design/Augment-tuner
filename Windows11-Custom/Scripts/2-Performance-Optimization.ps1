# Windows 11 Performance Optimization Script
# Optimerer system indstillinger for bedre performance og stabilitet

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Windows 11 Performance Optimering" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Kræver Administrator rettigheder
if (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Warning "Dette script skal køres som Administrator!"
    Break
}

Write-Host "Anvender performance optimeringer..." -ForegroundColor Yellow
Write-Host ""

# Deaktiver telemetri og data collection (forbedrer performance)
Write-Host "[1/12] Deaktiverer telemetri..." -ForegroundColor Gray
Set-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\DataCollection" -Name "AllowTelemetry" -Type DWord -Value 0 -Force
Set-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Windows\CurrentVersion\Policies\DataCollection" -Name "AllowTelemetry" -Type DWord -Value 0 -Force

# Deaktiver Windows Tips og forslag
Write-Host "[2/12] Deaktiverer Windows Tips..." -ForegroundColor Gray
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\ContentDeliveryManager" -Name "SoftLandingEnabled" -Type DWord -Value 0 -Force
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\ContentDeliveryManager" -Name "SubscribedContent-338389Enabled" -Type DWord -Value 0 -Force

# Deaktiver Background Apps (sparer ressourcer)
Write-Host "[3/12] Optimerer background apps..." -ForegroundColor Gray
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\BackgroundAccessApplications" -Name "GlobalUserDisabled" -Type DWord -Value 1 -Force

# Optimér Visual Effects for bedst performance
Write-Host "[4/12] Optimerer visuelle effekter..." -ForegroundColor Gray
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\VisualEffects" -Name "VisualFXSetting" -Type DWord -Value 2 -Force

# Deaktiver animationer (hurtigere respons)
Set-ItemProperty -Path "HKCU:\Control Panel\Desktop\WindowMetrics" -Name "MinAnimate" -Type String -Value "0" -Force

# Deaktiver transparency effects
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Themes\Personalize" -Name "EnableTransparency" -Type DWord -Value 0 -Force

# Optimér paging executive
Write-Host "[5/12] Optimerer memory management..." -ForegroundColor Gray
Set-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\Session Manager\Memory Management" -Name "DisablePagingExecutive" -Type DWord -Value 1 -Force

# Optimer system responsiveness
Set-ItemProperty -Path "HKLM:\SOFTWARE\Microsoft\Windows NT\CurrentVersion\Multimedia\SystemProfile" -Name "SystemResponsiveness" -Type DWord -Value 10 -Force

# Deaktiver Superfetch/SysMain (kan forårsage disk thrashing)
Write-Host "[6/12] Optimerer disk performance..." -ForegroundColor Gray
Set-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\Session Manager\Memory Management\PrefetchParameters" -Name "EnableSuperfetch" -Type DWord -Value 0 -Force

# Deaktiver Windows Search indexering på alle drev (reducerer disk aktivitet)
Write-Host "[7/12] Optimerer search indexering..." -ForegroundColor Gray
Set-ItemProperty -Path "HKLM:\SOFTWARE\Policies\Microsoft\Windows\Windows Search" -Name "AllowCortana" -Type DWord -Value 0 -Force

# Deaktiver Game DVR og Game Bar
Write-Host "[8/12] Deaktiverer Game DVR..." -ForegroundColor Gray
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\GameDVR" -Name "AppCaptureEnabled" -Type DWord -Value 0 -Force
Set-ItemProperty -Path "HKCU:\System\GameConfigStore" -Name "GameDVR_Enabled" -Type DWord -Value 0 -Force

# Deaktiver Auto-play
Write-Host "[9/12] Deaktiverer AutoPlay..." -ForegroundColor Gray
Set-ItemProperty -Path "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\AutoplayHandlers" -Name "DisableAutoplay" -Type DWord -Value 1 -Force

# Hurtigere boot tid - reducer menu timeout
Write-Host "[10/12] Optimerer boot tid..." -ForegroundColor Gray
bcdedit /timeout 3

# Deaktiver Hibernate (frigør diskplads lig med RAM størrelse)
Write-Host "[11/12] Deaktiverer hibernation..." -ForegroundColor Gray
powercfg -h off

# Sæt power plan til High Performance
Write-Host "[12/12] Sætter power plan til High Performance..." -ForegroundColor Gray
powercfg -duplicatescheme e9a42b02-d5df-448d-aa00-03f14749eb61
powercfg -setactive 8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c

# Deaktiver Fast Startup (kan forårsage problemer)
Set-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\Session Manager\Power" -Name "HiberbootEnabled" -Type DWord -Value 0 -Force

Write-Host ""
Write-Host "Performance optimering fuldført!" -ForegroundColor Green
Write-Host "BEMÆRK: Nogle ændringer kræver genstart for at træde i kraft." -ForegroundColor Yellow
Write-Host ""
