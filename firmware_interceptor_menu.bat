@echo off
color 0A
title Augment Firmware Interceptor

:MENU
cls
echo.
echo  ========================================================
echo  █████╗ ██╗   ██╗ ██████╗ ███╗   ███╗███████╗███╗   ██╗████████╗
echo ██╔══██╗██║   ██║██╔════╝ ████╗ ████║██╔════╝████╗  ██║╚══██╔══╝
echo ███████║██║   ██║██║  ███╗██╔████╔██║█████╗  ██╔██╗ ██║   ██║
echo ██╔══██║██║   ██║██║   ██║██║╚██╔╝██║██╔══╝  ██║╚██╗██║   ██║
echo ██║  ██║╚██████╔╝╚██████╔╝██║ ╚═╝ ██║███████╗██║ ╚████║   ██║
echo ╚═╝  ╚═╝ ╚═════╝  ╚═════╝ ╚═╝     ╚═╝╚══════╝╚═╝  ╚═══╝   ╚═╝
echo  ========================================================
echo            FIRMWARE INTERCEPTOR v1.0
echo  ========================================================
echo.
echo  Hvad vil du goere?
echo.
echo  [1] Install Dependencies (Python + mitmproxy)
echo  [2] Start Firmware Interceptor
echo  [3] Vis Din Computer IP
echo  [4] Vis Setup Guide
echo  [5] Test Installation
echo  [9] Exit
echo.
echo  ========================================================
echo.

set /p choice="Indtast dit valg (1-5 eller 9): "

if "%choice%"=="1" goto INSTALL
if "%choice%"=="2" goto START
if "%choice%"=="3" goto SHOWIP
if "%choice%"=="4" goto GUIDE
if "%choice%"=="5" goto TEST
if "%choice%"=="9" goto EXIT

echo.
echo  [!] Ugyldigt valg! Proev igen...
timeout /t 2 >nul
goto MENU

:INSTALL
cls
echo.
echo  ========================================================
echo  INSTALLATION
echo  ========================================================
echo.
echo  [*] Checker Python installation...
echo.

python --version >nul 2>&1
if errorlevel 1 (
    echo  [X] Python er IKKE installeret!
    echo.
    echo  Download Python her:
    echo  https://www.python.org/downloads/
    echo.
    echo  VIGTIGT: Vaelg "Add Python to PATH" under installation!
    echo.
    pause
    goto MENU
)

echo  [OK] Python er installeret!
python --version
echo.
echo  [*] Installerer mitmproxy og requests...
echo.

pip install --quiet mitmproxy requests

if errorlevel 1 (
    echo.
    echo  [X] Installation fejlede!
    echo  Proev at koere som Administrator
    echo.
    pause
    goto MENU
)

echo.
echo  [OK] Installation gennemfoert!
echo.
pause
goto MENU

:START
cls
echo.
echo  ========================================================
echo  FIRMWARE INTERCEPTOR
echo  ========================================================
echo.

REM Check if mitmproxy is installed
mitmdump --version >nul 2>&1
if errorlevel 1 (
    echo  [X] mitmproxy er ikke installeret!
    echo  Vaelg option [1] for at installere foerst.
    echo.
    pause
    goto MENU
)

echo  [*] Din Computer IP:
echo.
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    echo     %%a
)
echo.
echo  ========================================================
echo  NAESTE SKRIDT PÅ DIN TELEFON:
echo  ========================================================
echo.
echo  1. Settings -^> WiFi -^> Long press netvaerk -^> Modify
echo  2. Advanced -^> Proxy: Manual
echo  3. Hostname: [DIN IP OVENFOR]
echo  4. Port: 8080
echo  5. Save
echo.
echo  6. Åbn browser -^> http://mitm.it
echo  7. Download Android cert
echo  8. Install cert i Settings -^> Security
echo.
echo  9. Åbn Augment app -^> Settings -^> Firmware Update
echo.
echo  ========================================================
echo  PROXY STARTER NU...
echo  ========================================================
echo.
echo  [*] Lytter efter firmware requests...
echo  [*] Tryk Ctrl+C for at stoppe
echo.

mitmdump -s firmware_interceptor.py

echo.
echo  [*] Proxy stoppet.
echo.
pause
goto MENU

:SHOWIP
cls
echo.
echo  ========================================================
echo  DIN COMPUTER IP ADRESSE
echo  ========================================================
echo.
echo  Brug denne IP i Android proxy settings:
echo.

for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr /c:"IPv4"') do (
    echo     %%a
)

echo.
echo  Port: 8080
echo.
pause
goto MENU

:GUIDE
cls
echo.
echo  ========================================================
echo  SETUP GUIDE
echo  ========================================================
echo.
type SETUP_FIRMWARE_INTERCEPTOR.md 2>nul || (
    echo  [!] SETUP_FIRMWARE_INTERCEPTOR.md ikke fundet!
    echo.
    echo  Quick guide:
    echo.
    echo  1. Vaelg [1] for at installere
    echo  2. Vaelg [2] for at starte proxy
    echo  3. Configure Android proxy med din computer IP
    echo  4. Install SSL cert fra http://mitm.it
    echo  5. Trigger firmware check i Augment app
)
echo.
pause
goto MENU

:TEST
cls
echo.
echo  ========================================================
echo  TEST INSTALLATION
echo  ========================================================
echo.

echo  [*] Tester Python...
python --version >nul 2>&1
if errorlevel 1 (
    echo  [X] Python: IKKE INSTALLERET
) else (
    echo  [OK] Python:
    python --version
)
echo.

echo  [*] Tester mitmproxy...
mitmdump --version >nul 2>&1
if errorlevel 1 (
    echo  [X] mitmproxy: IKKE INSTALLERET
) else (
    echo  [OK] mitmproxy:
    mitmdump --version 2>&1 | findstr "Mitmproxy"
)
echo.

echo  [*] Tester firmware_interceptor.py...
if exist firmware_interceptor.py (
    echo  [OK] firmware_interceptor.py fundet
) else (
    echo  [X] firmware_interceptor.py IKKE FUNDET!
)
echo.

echo  [*] Status:
if exist firmware_interceptor.py (
    mitmdump --version >nul 2>&1
    if not errorlevel 1 (
        python --version >nul 2>&1
        if not errorlevel 1 (
            echo  [OK] Alt er klar! Vaelg [2] for at starte.
        )
    )
)
echo.
pause
goto MENU

:EXIT
cls
echo.
echo  ========================================================
echo  Tak fordi du brugte Augment Firmware Interceptor!
echo  ========================================================
echo.
timeout /t 2 >nul
exit

