@echo off
chcp 65001 >nul
color 0A
title mitmproxy Certificate Setup

echo ========================================================
echo  MITMPROXY CERTIFICATE INSTALLATION
echo ========================================================
echo.

REM Find mitmproxy certificate
set CERT_PATH=%USERPROFILE%\.mitmproxy\mitmproxy-ca-cert.pem

if not exist "%CERT_PATH%" (
    echo [!] Certifikat ikke fundet!
    echo [*] Starter mitmproxy for at generere certifikat...
    echo.

    REM Start mitmproxy briefly to generate cert
    start /B mitmdump -p 8080
    timeout /t 3 >nul
    taskkill /F /IM mitmdump.exe >nul 2>&1

    timeout /t 2 >nul
)

if exist "%CERT_PATH%" (
    echo [*] Certifikat fundet: %CERT_PATH%
    echo.
    echo [*] Pusher certifikat til telefon...

    REM Push certificate to phone
    adb push "%CERT_PATH%" /sdcard/mitmproxy-ca-cert.pem

    echo.
    echo ========================================================
    echo  ✓ CERTIFIKAT ER UPLOADET TIL TELEFONEN
    echo ========================================================
    echo.
    echo NÆSTE SKRIDT PÅ TELEFONEN:
    echo.
    echo  1. Åbn "Indstillinger" / "Settings"
    echo  2. Gå til "Sikkerhed" / "Security"
    echo  3. Find "Installer certifikat" / "Install certificate"
    echo  4. Vælg "CA-certifikat"
    echo  5. Find filen: mitmproxy-ca-cert.pem
    echo  6. Installer den
    echo.
    echo Eller prøv denne hurtigere metode:
    echo.
    echo PÅ TELEFONEN - Åbn Chrome og gå til:
    echo  http://mitm.it
    echo.
    echo Tryk på Android ikonet og installer
    echo.
) else (
    echo [!] Kunne ikke finde eller generere certifikat
    echo [*] Prøv at starte firmware interceptor først
)

echo.
pause
