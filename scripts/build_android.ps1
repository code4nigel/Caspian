<#
.SYNOPSIS
Build a Caspian Flow or Caspian Mobile APK and copy it into versions/android.
.DESCRIPTION
Missing or blank arguments are prompted for. devDebug and prodRelease select the
existing Android debug and release builds. Version overrides apply to this build.
.EXAMPLE
.\scripts\build_android.ps1
.EXAMPLE
.\scripts\build_android.ps1 -AppType flow -Variant devDebug -ExplicitVersionCode 263 -ExplicitVersionName 1.3.52-BetaC
#>
[CmdletBinding()]
param(
    [string]$AppType,
    [string]$Variant,
    [string]$ExplicitVersionCode,
    [string]$ExplicitVersionName
)

$ErrorActionPreference = 'Stop'

function Read-BuildOption {
    param([string]$Value, [string]$Prompt, [string[]]$Options)

    if ([string]::IsNullOrWhiteSpace($Value)) {
        do {
            $Value = (Read-Host "$Prompt [$($Options -join ' or ')]").Trim()
        } until ($Value -in $Options)
    }
    $Value = $Value.Trim()
    if ($Value -notin $Options) {
        throw "$Prompt must be one of $($Options -join ', ')."
    }
    $Options | Where-Object { $_ -eq $Value }
}

$AppType = Read-BuildOption $AppType 'App type' @('flow', 'mobile')
$Variant = Read-BuildOption $Variant 'Variant' @('devDebug', 'prodRelease')

if ([string]::IsNullOrWhiteSpace($ExplicitVersionCode)) {
    $ExplicitVersionCode = Read-Host 'Version code'
}
[int]$versionCode = 0
if (-not [int]::TryParse($ExplicitVersionCode, [ref]$versionCode) -or
    $versionCode -lt 1 -or $versionCode -gt 2100000000) {
    throw 'Version code must be an integer between 1 and 2100000000.'
}

if ([string]::IsNullOrWhiteSpace($ExplicitVersionName)) {
    $ExplicitVersionName = Read-Host 'Version name'
}
$versionName = $ExplicitVersionName.Trim()
if ($versionName -notmatch '^[A-Za-z0-9][A-Za-z0-9._+-]*$' -or $versionName.EndsWith('.')) {
    throw 'Version name must start with a letter or digit and may contain letters, digits, periods, underscores, plus signs, and hyphens. It cannot end with a period.'
}

$repoRoot = Split-Path -Parent $PSScriptRoot
$appName = @{ flow = 'Caspian-Flow'; mobile = 'Caspian-Mobile' }[$AppType]
$projectFolder = @{ flow = 'betaC'; mobile = 'Caspian-Android' }[$AppType]
$projectPath = Join-Path $repoRoot $projectFolder
$buildType = @{ devDebug = 'Debug'; prodRelease = 'Release' }[$Variant]
$gradleArgs = @(
    ":app:assemble$buildType"
    "-PcaspianVersionCode=$versionCode"
    "-PcaspianVersionName=$versionName"
)

Push-Location -LiteralPath $projectPath
try {
    & .\gradlew.bat @gradleArgs
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle failed for $appName $Variant (exit code $LASTEXITCODE)."
    }
} finally {
    Pop-Location
}

$outputDir = Join-Path $projectPath "app/build/outputs/apk/$($buildType.ToLowerInvariant())"
$metadata = Get-Content -LiteralPath (Join-Path $outputDir 'output-metadata.json') -Raw | ConvertFrom-Json
$apk = @($metadata.elements | Where-Object {
    $_.outputFile -like '*.apk' -and @($_.filters).Count -eq 0 -and
    $_.versionCode -eq $versionCode -and $_.versionName -ceq $versionName
})
if ($apk.Count -ne 1) {
    throw 'Gradle did not produce a single APK with the requested version.'
}

$destinationDir = Join-Path $repoRoot "versions/android/$AppType"
[void][System.IO.Directory]::CreateDirectory($destinationDir)
$destination = Join-Path $destinationDir "$appName-$Variant-$versionCode-$versionName.apk"
Copy-Item -LiteralPath (Join-Path $outputDir $apk[0].outputFile) -Destination $destination
if ($apk[0].outputFile -like '*-unsigned.apk') {
    Write-Warning 'The release APK is unsigned. Sign it with your Android release key before installing or distributing it.'
}
Write-Host "Saved $destination"
