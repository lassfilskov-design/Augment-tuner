@echo off
REM ===================================
REM Test PostgreSQL Database Connection
REM ===================================

echo.
echo ====================================
echo   TESTER DATABASE FORBINDELSE
echo ====================================
echo.

REM Check if psql is available
where psql >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [FEJL] PostgreSQL CLI (psql) er ikke fundet!
    echo.
    echo Sikr at PostgreSQL er installeret og 'bin' mappen er i PATH
    echo Typisk placering: C:\Program Files\PostgreSQL\XX\bin
    echo.
    pause
    exit /b 1
)

echo [OK] PostgreSQL CLI fundet
psql --version
echo.

REM Read .env for database credentials
if not exist ".env" (
    echo [FEJL] .env fil mangler!
    echo.
    echo Koer 'setup.bat' foerst
    pause
    exit /b 1
)

echo Laester database credentials fra .env...
echo.

REM Default values
set DB_HOST=localhost
set DB_PORT=5432
set DB_USER=postgres
set DB_NAME=scooter_db

REM Parse .env file (simplified)
for /f "tokens=1,2 delims==" %%a in (.env) do (
    if "%%a"=="DATABASE_HOST" set DB_HOST=%%b
    if "%%a"=="DATABASE_PORT" set DB_PORT=%%b
    if "%%a"=="DATABASE_USER" set DB_USER=%%b
    if "%%a"=="DATABASE_NAME" set DB_NAME=%%b
    if "%%a"=="DATABASE_PASSWORD" set DB_PASSWORD=%%b
)

echo Host: %DB_HOST%
echo Port: %DB_PORT%
echo User: %DB_USER%
echo Database: %DB_NAME%
echo.

echo Tester forbindelse...
echo.

REM Set PGPASSWORD environment variable for non-interactive login
set PGPASSWORD=%DB_PASSWORD%

REM Test connection
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT version();"
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ====================================
    echo   FORBINDELSE SUCCES!
    echo ====================================
    echo.
) else (
    echo.
    echo ====================================
    echo   FORBINDELSE FEJLEDE!
    echo ====================================
    echo.
    echo Tjek:
    echo 1. PostgreSQL service koerer
    echo 2. Database '%DB_NAME%' eksisterer
    echo 3. Credentials i .env er korrekte
    echo.
)

REM Clear password from environment
set PGPASSWORD=

pause
