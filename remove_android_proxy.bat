@echo off
chcp 65001 >nul
color 0C
title Remove Android Proxy

echo ========================================================
echo  FJERN PROXY FRA ANDROID
echo ========================================================
echo.
echo Dette script fjerner proxy fra din Android telefon
echo.
pause

echo.
echo [*] Fjerner proxy...

REM Remove proxy
adb shell settings delete global http_proxy
adb shell settings delete global global_http_proxy_host
adb shell settings delete global global_http_proxy_port

echo.
echo ✓ Proxy er fjernet!
echo.
echo Din telefon bruger nu normal internet forbindelse
echo.
pause
