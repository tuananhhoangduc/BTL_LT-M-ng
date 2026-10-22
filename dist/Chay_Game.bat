@echo off
cd /d "%~dp0"
title Color Match Arena - Client
echo Dang khoi dong game...
start "" javaw -jar "%~dp0ColorMatch-Client.jar"
if errorlevel 1 (
    echo.
    echo Dang thu khoi dong bang java thuong...
    java -jar "%~dp0ColorMatch-Client.jar"
    if errorlevel 1 (
        echo.
        echo [LOI] May tinh chua cai dat Java hoac chua them vao PATH.
        echo Vui long cai dat Java tai: https://adoptium.net hoac https://www.java.com
        pause
    )
)
