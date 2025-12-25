@echo off
chcp 65001 >nul
color 0A
title Android Proxy Setup - Augment Interceptor

echo ========================================================
echo  ANDROID PROXY SETUP
echo ========================================================
echo.
echo Dette script sætter proxy op på din Android telefon
echo.
echo VIGTIGT:
echo  1. Tilslut telefon med USB kabel
echo  2. Aktiver USB Debugging på telefonen
echo  3. Accepter USB debugging når telefonen spørger
echo.
pause

echo.
echo [*] Checker om telefon er tilsluttet...
adb devices

echo.
echo [*] Sætter proxy til: 192.168.1.129:8080
echo.

REM Get WiFi SSID
for /f "tokens=2 delims=:" %%a in ('adb shell "cmd wifi status | grep -i ssid"') do set SSID=%%a

echo Dit WiFi: %SSID%
echo.

REM Set proxy via ADB
adb shell settings put global http_proxy 192.168.1.129:8080

echo.
echo ✓ Proxy er sat til: 192.168.1.129:8080
echo.
echo Nu skal du også:
echo  1. Installere mitmproxy certifikat
echo  2. Kør: setup_mitm_cert.bat
echo.
pause
