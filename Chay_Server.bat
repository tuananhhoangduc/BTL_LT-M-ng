@echo off
cd /d "%~dp0"
title Color Match Arena - Server
echo ====================================================
echo Dang khoi dong Server Dau Tri Sac Mau (Port 5000)...
echo ====================================================
echo.
java -cp ".;lib/mysql-connector-j.jar" server.ColorMatchServer
if errorlevel 1 (
    echo.
    echo [LOI] Khong the chay Server. Vui long kiem tra MySQL trong XAMPP da BAT (Start).
    pause
)
