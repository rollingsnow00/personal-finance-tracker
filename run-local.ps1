$ErrorActionPreference = 'Stop'
$localEnvironment = Join-Path $PSScriptRoot '.local-dev.env'

if (-not (Test-Path -LiteralPath $localEnvironment)) {
    throw "Local credentials file is missing: $localEnvironment"
}

Get-Content -LiteralPath $localEnvironment | ForEach-Object {
    if ($_ -match '^([A-Z_]+)=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process')
    }
}

$env:SPRING_PROFILES_ACTIVE = 'local'
$env:MAVEN_USER_HOME = Join-Path $PSScriptRoot '.maven-cache'
$javaHome = [Environment]::GetEnvironmentVariable('JAVA_HOME', 'Machine')
if ($javaHome) { $env:Path = (Join-Path $javaHome 'bin') + ';' + $env:Path }

& (Join-Path $PSScriptRoot 'mvnw.cmd') spring-boot:run
exit $LASTEXITCODE
