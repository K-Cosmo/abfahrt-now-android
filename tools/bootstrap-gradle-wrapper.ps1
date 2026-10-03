$ErrorActionPreference = "Stop"

$GradleVersion = "9.6.0"
$ExpectedSha256 = "497c8c2a7e5031f6aa847f88104aa80a93532ec32ee17bdb8d1d2f67a194a9c7"
$ProjectRoot = Split-Path -Parent $PSScriptRoot
$WrapperDir = Join-Path $ProjectRoot "gradle\wrapper"
$WrapperJar = Join-Path $WrapperDir "gradle-wrapper.jar"
$DownloadUrl = "https://services.gradle.org/distributions/gradle-$GradleVersion-wrapper.jar"

New-Item -ItemType Directory -Force -Path $WrapperDir | Out-Null
$tempFile = Join-Path ([System.IO.Path]::GetTempPath()) "gradle-$GradleVersion-wrapper-$([guid]::NewGuid().ToString('N')).jar"

try {
    Write-Host "Downloading official Gradle $GradleVersion Wrapper JAR..."
    Invoke-WebRequest -Uri $DownloadUrl -OutFile $tempFile -UseBasicParsing

    $actual = (Get-FileHash -Path $tempFile -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actual -ne $ExpectedSha256) {
        throw "Wrapper checksum mismatch. Expected $ExpectedSha256 but got $actual. File was not installed."
    }

    Move-Item -Force $tempFile $WrapperJar
    Write-Host "Wrapper installed and verified: $WrapperJar"
    Write-Host "SHA-256: $actual"
} finally {
    if (Test-Path $tempFile) {
        Remove-Item -Force $tempFile
    }
}
