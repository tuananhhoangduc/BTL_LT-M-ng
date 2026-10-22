@echo off
cd /d "%~dp0"
title Color Match Arena - Client
echo Dang khoi dong Client...
java -cp . client.ClientApp
if errorlevel 1 (
    echo.
    echo [LOI] Khong the chay Client.
    pause
)

