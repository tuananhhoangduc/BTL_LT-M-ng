param([switch]$Screenshots)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $projectRoot
$classes = & "$PSScriptRoot\Build.ps1"
$testClasses = Join-Path $projectRoot 'build\tests'
New-Item -ItemType Directory -Path $testClasses -Force | Out-Null
$tests = @(Get-ChildItem "$projectRoot\tests" -Recurse -Filter *.java -File | ForEach-Object FullName)
& "$env:JAVA_HOME\bin\javac.exe" -encoding UTF-8 -cp $classes -d $testClasses $tests
if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
& "$env:JAVA_HOME\bin\java.exe" -cp "$testClasses;$classes" server.RulesChecks
if ($LASTEXITCODE -ne 0) { throw 'Rules checks failed.' }
& "$env:JAVA_HOME\bin\java.exe" -cp "$testClasses;$classes" server.RoomChecks
if ($LASTEXITCODE -ne 0) { throw 'Room checks failed.' }
& "$env:JAVA_HOME\bin\java.exe" -cp "$testClasses;$classes" client.NetworkChecks
if ($LASTEXITCODE -ne 0) { throw 'Network checks failed.' }
& "$env:JAVA_HOME\bin\java.exe" -cp "$testClasses;$classes" client.AssetChecks --sheet
if ($LASTEXITCODE -ne 0) { throw 'PNG asset checks failed.' }
& "$env:JAVA_HOME\bin\java.exe" -cp "$testClasses;$classes" client.TileReadabilityChecks
if ($LASTEXITCODE -ne 0) { throw 'Tile readability checks failed.' }
$options = @()
if ($Screenshots) { $options += '--screenshots' }
& "$env:JAVA_HOME\bin\java.exe" -cp "$testClasses;$classes" client.PresentationChecks @options
if ($LASTEXITCODE -ne 0) { throw 'Presentation checks failed.' }
