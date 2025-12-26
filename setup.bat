@echo off
REM ===================================
REM Augment E-Scooter Backend Setup
REM ===================================

echo.
echo ====================================
echo   AUGMENT BACKEND SETUP
echo ====================================
echo.

REM Check if Node.js is installed
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [FEJL] Node.js er ikke installeret!
    echo.
    echo Download Node.js fra: https://nodejs.org/
    echo.
    pause
    exit /b 1
)

echo [OK] Node.js er installeret
node --version
echo.

REM Check if npm is available
where npm >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [FEJL] npm er ikke tilgaengelig!
    pause
    exit /b 1
)

echo [OK] npm er tilgaengelig
npm --version
echo.

REM Install dependencies
echo ====================================
echo   INSTALLERER DEPENDENCIES
echo ====================================
echo.

echo Installerer projekt dependencies...
call npm install
if %ERRORLEVEL% NEQ 0 (
    echo [FEJL] npm install fejlede!
    pause
    exit /b 1
)

echo.
echo [OK] Dependencies installeret!
echo.

REM Install global tools
echo Installerer TypeScript globalt...
call npm install -g typescript ts-node
if %ERRORLEVEL% NEQ 0 (
    echo [ADVARSEL] Kunne ikke installere globale tools
    echo Du kan proeve at koere CMD som Administrator
)

echo.
echo [OK] TypeScript installeret!
echo.

REM Check for .env file
if not exist ".env" (
    echo ====================================
    echo   OPRETTER .ENV FIL
    echo ====================================
    echo.

    if exist ".env.example" (
        copy ".env.example" ".env"
        echo [OK] .env fil oprettet fra .env.example
        echo.
        echo VIGTIGT: Rediger .env filen og opdater:
        echo   - DATABASE_PASSWORD
        echo   - DATABASE_USER
        echo   - DATABASE_NAME
        echo.
    ) else (
        echo # Database Configuration > .env
        echo DATABASE_HOST=localhost >> .env
        echo DATABASE_PORT=5432 >> .env
        echo DATABASE_USER=postgres >> .env
        echo DATABASE_PASSWORD=postgres >> .env
        echo DATABASE_NAME=scooter_db >> .env
        echo. >> .env
        echo # Server >> .env
        echo PORT=4000 >> .env
        echo NODE_ENV=development >> .env
        echo. >> .env
        echo # JWT >> .env
        echo JWT_SECRET=change-this-secret-in-production >> .env
        echo JWT_EXPIRES_IN=7d >> .env

        echo [OK] Standard .env fil oprettet!
        echo.
    )
) else (
    echo [OK] .env fil findes allerede
    echo.
)

REM Setup complete
echo ====================================
echo   SETUP KOMPLET!
echo ====================================
echo.
echo Naeste skridt:
echo.
echo 1. Aabn .env filen og opdater database credentials
echo 2. Opret database 'scooter_db' i PostgreSQL
echo 3. Koer 'npm run dev' for at starte serveren
echo.
echo For at starte serveren nu, tryk ENTER
echo For at afslutte, tryk CTRL+C
echo.

pause

REM Start the development server
echo.
echo ====================================
echo   STARTER SERVER
echo ====================================
echo.

call npm run dev

pause
