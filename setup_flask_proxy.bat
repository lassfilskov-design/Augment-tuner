@echo off
REM Augment Flask Proxy Setup Script (Windows)
REM Double-click dette script for at installere Flask proxy

echo ============================================
echo   AUGMENT FLASK PROXY INSTALLER
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

REM Installer dependencies
echo Installerer Flask og dependencies...
echo Dette kan tage et par minutter...
echo.

pip install -r requirements.txt

if errorlevel 0 (
    echo.
    echo [OK] Flask proxy installeret succesfuldt!
    echo.
    echo ============================================
    echo   INSTALLATION FAERDIG!
    echo ============================================
    echo.
    echo Naeste skridt:
    echo 1. Luk dette vindue
    echo 2. Double-click paa 'start_flask_proxy.bat'
    echo 3. Folg instruktionerne der vises
    echo.
    echo Features:
    echo   [OK] Automatisk firmware detection
    echo   [OK] Automatisk firmware download
    echo   [OK] GraphQL query logging
    echo   [OK] Live web dashboard
    echo   [OK] Alt gemmes til captured_data/
    echo.
) else (
    echo.
    echo [X] Installation fejlede!
    echo     Prov at kore manuelt: pip install -r requirements.txt
    echo.
)

pause
