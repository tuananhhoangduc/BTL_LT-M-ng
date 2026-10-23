param([switch]$Server)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $projectRoot
try {
    $classes = & "$PSScriptRoot\Build.ps1"
    if ($Server) {
        & "$env:JAVA_HOME\bin\java.exe" '-Dfile.encoding=UTF-8' -cp "$classes;$projectRoot\lib\mysql-connector-j.jar" server.ColorMatchServer
    } else {
        & "$env:JAVA_HOME\bin\java.exe" '-Dfile.encoding=UTF-8' -jar "$projectRoot\dist\color-match-arena\ColorMatchArena-Client.jar"
    }
    exit $LASTEXITCODE
} catch { Write-Error $_; exit 1 }
