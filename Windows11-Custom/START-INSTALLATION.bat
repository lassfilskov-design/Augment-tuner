@echo off
echo ================================================
echo  Windows 11 Custom Installation Launcher
echo ================================================
echo.
echo Dette script vil starte PowerShell som Administrator
echo og kore alle optimerings scripts.
echo.
echo Tryk en tast for at fortsaette...
pause >nul

:: Check for admin rights
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo.
    echo FEJL: Dette script skal kores som Administrator!
    echo.
    echo Hojreklik pa denne .bat fil og vaelg "Run as administrator"
    echo.
    pause
    exit /b 1
)

:: Kør PowerShell script
echo.
echo Starter installation...
echo.

PowerShell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Scripts\INSTALL-ALL.ps1"

echo.
echo.
pause
