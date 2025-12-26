@echo off
chcp 65001 >nul
title Augment Tuner - Installation
color 0A

echo.
echo ═══════════════════════════════════════════════════════
echo    AUGMENT TUNER RACER EDITION - Installation
echo ═══════════════════════════════════════════════════════
echo.

:MENU
echo [1] Installer APK til telefon (via ADB)
echo [2] Byg app med Gradle
echo [3] Åbn i Android Studio
echo [4] Check ADB forbindelse
echo [5] Afslut
echo.
set /p choice="Vælg (1-5): "

if "%choice%"=="1" goto INSTALL_APK
if "%choice%"=="2" goto BUILD_APP
if "%choice%"=="3" goto OPEN_STUDIO
if "%choice%"=="4" goto CHECK_ADB
if "%choice%"=="5" goto END

goto MENU

:INSTALL_APK
echo.
echo [*] Søger efter APK fil...
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo [✓] APK fundet!
    echo [*] Installer på telefon...
    adb install -r app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo [✓] Installation færdig!
) else (
    echo [!] Ingen APK fundet. Byg først med option [2]
)
pause
goto MENU

:BUILD_APP
echo.
echo [*] Bygger app...
echo [*] Dette kan tage et par minutter...
call gradlew.bat assembleDebug
echo.
echo [✓] Build færdig!
pause
goto MENU

:OPEN_STUDIO
echo.
echo [*] Åbner projekt i Android Studio...
start "" "studio64.exe" .
goto MENU

:CHECK_ADB
echo.
echo [*] Checker ADB forbindelse...
adb devices
echo.
echo [!] Hvis din telefon ikke vises:
echo    1. Tjek USB-kabel er tilsluttet
echo    2. Slå USB-debugging til
echo    3. Accept tillid til computer på telefonen
pause
goto MENU

:END
echo.
echo [✓] Farvel!
timeout /t 2 >nul
exit
