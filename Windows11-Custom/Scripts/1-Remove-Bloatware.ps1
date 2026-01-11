# Windows 11 Bloatware Removal Script
# Fjerner unødvendige pre-installerede apps for bedre performance og stabilitet

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Windows 11 Bloatware Fjernelse" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Kræver Administrator rettigheder
if (-NOT ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole] "Administrator")) {
    Write-Warning "Dette script skal køres som Administrator!"
    Break
}

# Liste over Windows apps der kan fjernes sikkert
$AppsToRemove = @(
    "Microsoft.BingNews"
    "Microsoft.BingWeather"
    "Microsoft.GetHelp"
    "Microsoft.Getstarted"
    "Microsoft.Microsoft3DViewer"
    "Microsoft.MicrosoftOfficeHub"
    "Microsoft.MicrosoftSolitaireCollection"
    "Microsoft.MixedReality.Portal"
    "Microsoft.Office.OneNote"
    "Microsoft.People"
    "Microsoft.SkypeApp"
    "Microsoft.Todos"
    "Microsoft.WindowsAlarms"
    "Microsoft.WindowsFeedbackHub"
    "Microsoft.WindowsMaps"
    "Microsoft.WindowsSoundRecorder"
    "Microsoft.Xbox.TCUI"
    "Microsoft.XboxApp"
    "Microsoft.XboxGameOverlay"
    "Microsoft.XboxGamingOverlay"
    "Microsoft.XboxIdentityProvider"
    "Microsoft.XboxSpeechToTextOverlay"
    "Microsoft.YourPhone"
    "Microsoft.ZuneMusic"
    "Microsoft.ZuneVideo"
    "MicrosoftTeams"
    "Microsoft.GamingApp"
    "Disney.37853FC22B2CE"
    "SpotifyAB.SpotifyMusic"
    "Clipchamp.Clipchamp"
)

Write-Host "Fjerner unødvendige Windows Apps..." -ForegroundColor Yellow
Write-Host ""

foreach ($App in $AppsToRemove) {
    Write-Host "Forsøger at fjerne: $App" -ForegroundColor Gray

    # Fjern for nuværende bruger
    Get-AppxPackage -Name $App -ErrorAction SilentlyContinue | Remove-AppxPackage -ErrorAction SilentlyContinue

    # Fjern for alle brugere
    Get-AppxPackage -Name $App -AllUsers -ErrorAction SilentlyContinue | Remove-AppxPackage -AllUsers -ErrorAction SilentlyContinue

    # Fjern provisionerede pakker (forhindrer geninstallation)
    Get-AppxProvisionedPackage -Online | Where-Object DisplayName -like $App | Remove-AppxProvisionedPackage -Online -ErrorAction SilentlyContinue
}

Write-Host ""
Write-Host "Bloatware fjernelse fuldført!" -ForegroundColor Green
Write-Host ""
