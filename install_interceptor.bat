@echo off
echo ================================================
echo Augment Firmware Interceptor - Installation
echo ================================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo [!] Python er ikke installeret!
    echo [*] Download Python fra: https://www.python.org/downloads/
    echo [*] HUSK at vaelge "Add Python to PATH"!
    pause
    exit /b 1
)

echo [+] Python found!
echo.

REM Install mitmproxy
echo [*] Installing mitmproxy...
pip install mitmproxy requests

echo.
echo ================================================
echo Installation Complete!
echo ================================================
echo.
echo Naeste skridt:
echo 1. Koer: start_interceptor.bat
echo 2. Configure din Android telefon (se SETUP_FIRMWARE_INTERCEPTOR.md)
echo 3. Trigger firmware check i Augment app
echo.
pause
