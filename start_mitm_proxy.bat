@echo off
REM Augment Firmware MITM Proxy Starter (Windows)
REM Double-click dette script for at starte mitmproxy

cls
echo ============================================
echo   AUGMENT FIRMWARE MITM PROXY
echo ============================================
echo.

REM Check om mitmproxy er installeret
mitmweb --version >nul 2>&1
if errorlevel 1 (
    echo [X] mitmproxy er ikke installeret!
    echo.
    echo Kor forst 'setup_mitm_proxy.bat' for at installere.
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

echo [OK] mitmproxy klar!
echo.
echo ============================================
echo   INSTRUKTIONER TIL DIN ANDROID TELEFON
echo ============================================
echo.
echo 1. Abn WiFi indstillinger paa din telefon
echo    - Hold finger paa dit WiFi netvaerk
echo    - Vaelg 'Modificer netvaerk' eller 'Avanceret'
echo.
echo 2. Saet Proxy til MANUEL:
echo    - Proxy hostname: %IP_ADDRESS%
echo    - Proxy port: 8080
echo    - Gem aendringer
echo.
echo 3. Abn browser paa telefonen og gaa til:
echo    http://mitm.it
echo.
echo 4. Download og installer certifikat for Android
echo.
echo 5. Abn Augment app og trigger firmware update
echo.
echo 6. Se captured traffic i browser paa:
echo    http://127.0.0.1:8081
echo.
echo ============================================
echo.
echo Starter mitmweb nu...
echo.
echo VIGTIGT: Lad dette vindue vaere aabent!
echo          Luk det FORST naar du er faerdig.
echo.
echo ============================================
echo.

REM Start mitmweb
mitmweb --web-host 127.0.0.1 --web-port 8081

echo.
echo mitmproxy stoppet.
pause
