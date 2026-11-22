$ErrorActionPreference = 'Stop'
$env:MAVEN_USER_HOME = Join-Path $PSScriptRoot '.maven-cache'
$javaHome = [Environment]::GetEnvironmentVariable('JAVA_HOME', 'Machine')
if ($javaHome) {
    $env:JAVA_HOME = $javaHome
    $env:Path = (Join-Path $javaHome 'bin') + ';' + $env:Path
}
& (Join-Path $PSScriptRoot 'mvnw.cmd') clean verify
exit $LASTEXITCODE
