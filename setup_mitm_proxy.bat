@echo off
REM Augment Firmware MITM Proxy Setup Script (Windows)
REM Double-click dette script for at installere mitmproxy

echo ============================================
echo   AUGMENT FIRMWARE MITM PROXY INSTALLER
echo ============================================
echo.

REM Check om Python er installeret
python --version >nul 2>&1
if errorlevel 1 (
    echo [X] Python er ikke installeret!
    echo     Installer Python forst: https://www.python.org/downloads/
    echo.
    pause
    exit /b 1
)

echo [OK] Python fundet
echo.

REM Check om pip er installeret
pip --version >nul 2>&1
if errorlevel 1 (
    echo [X] pip er ikke installeret!
    echo     Geninstaller Python med "Add to PATH" option
    echo.
    pause
    exit /b 1
)

echo [OK] pip fundet
echo.

REM Installer mitmproxy
echo Installerer mitmproxy...
echo Dette kan tage et par minutter...
echo.

pip install mitmproxy

if errorlevel 0 (
    echo.
    echo [OK] mitmproxy installeret succesfuldt!
    echo.
    echo ============================================
    echo   INSTALLATION FAERDIG!
    echo ============================================
    echo.
    echo Naeste skridt:
    echo 1. Luk dette vindue
    echo 2. Double-click paa 'start_mitm_proxy.bat'
    echo 3. Folg instruktionerne der vises
    echo.
) else (
    echo.
    echo [X] Installation fejlede!
    echo     Prov at kore manuelt: pip install mitmproxy
    echo.
)

pause
