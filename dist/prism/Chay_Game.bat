@echo off
setlocal
cd /d "%~dp0"
set "JAVA_CMD=java"
if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
"%JAVA_CMD%" -Dfile.encoding=UTF-8 -jar "%~dp0ColorMatch-Client.jar"
if errorlevel 1 (
    echo Khong the mo game. Vui long cai JDK 24 va cau hinh JAVA_HOME.
    pause
)
