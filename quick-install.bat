@echo off
chcp 65001 >nul
title Augment Tuner - Quick Install
color 0E

echo.
echo ═══════════════════════════════════════════════════════
echo    AUGMENT TUNER - QUICK INSTALL
echo ═══════════════════════════════════════════════════════
echo.
echo [!] Sørg for telefonen er tilsluttet via USB
echo [!] USB-debugging skal være slået til
echo.
pause

echo.
echo [1/3] Checker ADB forbindelse...
adb devices
echo.

echo [2/3] Bygger app...
call gradlew.bat assembleDebug

echo.
echo [3/3] Installerer på telefon...
adb install -r app\build\outputs\apk\debug\app-debug.apk

echo.
echo ═══════════════════════════════════════════════════════
echo [✓] FÆRDIG! Appen er nu installeret på din telefon
echo ═══════════════════════════════════════════════════════
echo.
pause
