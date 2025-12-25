@echo off
chcp 65001 >nul
color 0E
title Android Connection Diagnostics

echo ========================================================
echo  ANDROID TELEFON DIAGNOSTICERING
echo ========================================================
echo.

echo [*] Checker om ADB er installeret...
echo.

adb version >nul 2>&1
if errorlevel 1 (
    echo [!] ADB er IKKE installeret!
    echo.
    echo [*] Downloader og installerer ADB...
    echo.

    REM Download platform-tools (contains adb)
    echo Downloader Google Platform Tools...
    curl -L -o platform-tools.zip https://dl.google.com/android/repository/platform-tools-latest-windows.zip

    if exist platform-tools.zip (
        echo.
        echo [*] Udpakker...
        tar -xf platform-tools.zip

        REM Add to PATH temporarily
        set PATH=%CD%\platform-tools;%PATH%

        echo.
        echo [OK] ADB installeret!
        echo.

        del platform-tools.zip
    ) else (
        echo [X] Download fejlede. Prøv manuelt:
        echo https://developer.android.com/studio/releases/platform-tools
        pause
        exit
    )
) else (
    echo [OK] ADB er installeret!
    adb version | findstr "Android"
)

echo.
echo ========================================================
echo  CHECKER TELEFON FORBINDELSE
echo ========================================================
echo.

echo [*] Starter ADB server...
adb kill-server >nul 2>&1
adb start-server

echo.
echo [*] Leder efter enheder...
echo.

adb devices

echo.
echo ========================================================
echo  RESULTAT
echo ========================================================
echo.

adb devices | findstr /C:"device" | findstr /V "List" >nul
if errorlevel 1 (
    echo [!] INGEN TELEFON FUNDET!
    echo.
    echo Mulige årsager:
    echo.
    echo  1. USB Debugging ikke aktiveret
    echo     → Settings → Developer Options → USB Debugging
    echo.
    echo  2. USB mode er "Charging only"
    echo     → Skift til "File Transfer" / "MTP"
    echo.
    echo  3. Du har ikke accepteret USB debugging prompt på telefonen
    echo     → Check telefon skærmen for popup
    echo.
    echo  4. USB Drivers mangler
    echo     → Tryk [1] nedenfor for at installere
    echo.
    echo  5. Dårligt USB kabel
    echo     → Prøv et andet kabel
    echo.
    echo  6. USB port problem
    echo     → Prøv en anden USB port
    echo.
    echo.
    echo [1] Installer Universal ADB Drivers
    echo [2] Restart ADB Server
    echo [3] Test igen
    echo [9] Exit
    echo.
    set /p fix="Hvad vil du gøre? "

    if "%fix%"=="1" goto INSTALL_DRIVERS
    if "%fix%"=="2" (
        adb kill-server
        adb start-server
        echo.
        echo [*] ADB server genstartet. Prøv at tilslutte telefon igen.
        pause
        goto :eof
    )
    if "%fix%"=="3" (
        cls
        goto :eof
    )

) else (
    echo [OK] TELEFON FUNDET!
    echo.

    for /f "tokens=1" %%d in ('adb devices ^| findstr /C:"device" ^| findstr /V "List"') do (
        echo Enhed ID: %%d

        echo.
        echo [*] Henter telefon info...
        echo.

        adb -s %%d shell getprop ro.product.manufacturer
        adb -s %%d shell getprop ro.product.model
        adb -s %%d shell getprop ro.build.version.release

        echo.
        echo [*] USB Debugging Status: AKTIV ✓
        echo.
    )

    echo.
    echo ========================================================
    echo  ✓ DU ER KLAR TIL AT BRUGE SETUP SCRIPTS!
    echo ========================================================
    echo.
    echo Nu kan du køre:
    echo  - setup_android_proxy.bat
    echo  - setup_mitm_cert.bat
    echo.
)

echo.
pause
goto :eof

:INSTALL_DRIVERS
cls
echo.
echo ========================================================
echo  INSTALLER UNIVERSAL ADB DRIVERS
echo ========================================================
echo.

echo [*] Downloader Universal ADB Drivers...
echo.

REM Download universal ADB drivers
curl -L -o usb_driver.zip "https://adb.clockworkmod.com/usb_drivers.zip"

if exist usb_driver.zip (
    echo [*] Udpakker...
    tar -xf usb_driver.zip

    echo.
    echo [*] Åbner driver mappe...
    echo.
    echo MANUEL INSTALLATION KRÆVET:
    echo.
    echo 1. Højreklik på "android_winusb.inf"
    echo 2. Vælg "Install"
    echo 3. Tillad installation
    echo.

    start explorer usb_driver

    echo.
    echo Når driver er installeret:
    echo - Frakobl telefonen
    echo - Tilslut den igen
    echo - Kør dette script igen
    echo.

    del usb_driver.zip
) else (
    echo [X] Download fejlede!
    echo.
    echo Installer manuelt fra:
    echo https://developer.android.com/studio/run/win-usb
    echo.
    echo Eller Google efter "[Dit telefon mærke] USB Driver"
    echo.
)

pause
goto :eof
