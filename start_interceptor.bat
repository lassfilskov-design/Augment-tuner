@echo off
cls
echo ================================================
echo Augment Firmware Interceptor
echo ================================================
echo.
echo [*] Starting proxy on port 8080...
echo [*] Configure your Android phone to use this proxy
echo.

REM Get local IP
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4 Address"') do (
    set IP=%%a
    goto :found
)
:found
echo [+] Your computer IP: %IP%
echo [+] Use this IP in Android proxy settings!
echo.
echo ================================================
echo [*] Listening for firmware requests...
echo [*] Trigger firmware check in Augment app now!
echo ================================================
echo.

REM Start mitmproxy with our script
mitmdump -s firmware_interceptor.py

pause
