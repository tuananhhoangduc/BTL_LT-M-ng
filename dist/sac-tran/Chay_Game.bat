@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
title Sac Tran
set "JAVA_CMD=java"
if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
"%JAVA_CMD%" -Dfile.encoding=UTF-8 -jar "%~dp0SacTran-Client.jar"
if errorlevel 1 (
    echo Khong the mo game. Hay cai JDK 24 va dat JAVA_HOME toi thu muc JDK.
    pause
)
