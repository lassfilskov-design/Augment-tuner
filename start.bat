@echo off
REM ===================================
REM Start Augment Backend Server
REM ===================================

echo.
echo ====================================
echo   STARTER AUGMENT BACKEND
echo ====================================
echo.

REM Check if node_modules exists
if not exist "node_modules" (
    echo [ADVARSEL] Dependencies er ikke installeret!
    echo.
    echo Koerer 'npm install' foerst...
    call npm install
    if %ERRORLEVEL% NEQ 0 (
        echo [FEJL] npm install fejlede!
        pause
        exit /b 1
    )
    echo.
)

REM Check if .env exists
if not exist ".env" (
    echo [FEJL] .env fil mangler!
    echo.
    echo Koer 'setup.bat' foerst for at oprette .env filen
    pause
    exit /b 1
)

REM Start the server
echo Server starter paa http://localhost:4000
echo GraphQL Playground: http://localhost:4000/graphql
echo.
echo Tryk CTRL+C for at stoppe serveren
echo.

call npm run dev

pause
