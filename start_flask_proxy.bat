@echo off
REM Augment Flask Proxy Starter (Windows)
REM Double-click dette script for at starte Flask proxy

cls
echo ============================================
echo   AUGMENT FLASK PROXY
echo ============================================
echo.

REM Check om Flask er installeret
python -c "import flask" >nul 2>&1
if errorlevel 1 (
    echo [X] Flask er ikke installeret!
    echo.
    echo Kor forst 'setup_flask_proxy.bat' for at installere.
    echo.
    pause
    exit /b 1
)

REM Find IP adresse
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    set IP_ADDRESS=%%a
    goto :found_ip
)
:found_ip
set IP_ADDRESS=%IP_ADDRESS:~1%

if "%IP_ADDRESS%"=="" (
    set IP_ADDRESS=DIN-COMPUTER-IP
)

echo [OK] Flask proxy klar!
echo.
echo ============================================
echo   ANDROID TELEFON SETUP
echo ============================================
echo.
echo 1. Abn WiFi indstillinger paa din telefon
echo    - Hold finger paa dit WiFi netvaerk
echo    - Vaelg 'Modificer netvaerk' eller 'Avanceret'
echo.
echo 2. Saet Proxy til MANUEL:
echo    - Proxy hostname: %IP_ADDRESS%
echo    - Proxy port: 8888
echo    - Gem aendringer
echo.
echo 3. Abn Augment app og trigger firmware update
echo.
echo ============================================
echo   COMPUTER DASHBOARD
echo ============================================
echo.
echo Abn dashboard i browser:
echo     http://127.0.0.1:5000
echo.
echo Se live captured data:
echo   - Firmware URLs (auto-downloaded!)
echo   - GraphQL queries
echo   - Alle HTTP requests
echo.
echo ============================================
echo.
echo Starter Flask proxy nu...
echo.
echo VIGTIGT: Lad dette vindue vaere aabent!
echo          Luk det FORST naar du er faerdig.
echo.
echo ============================================
echo.

REM Start Flask proxy
python flask_proxy.py

echo.
echo Flask proxy stoppet.
pause
