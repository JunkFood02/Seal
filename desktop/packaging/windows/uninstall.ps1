<#
.SYNOPSIS
Seal's own uninstaller. Removes the app, its shell integration, and (optionally) its data.

.DESCRIPTION
The MSI/EXE installer can remove the program files, but it knows nothing about what Seal sets up
at runtime: the yt-dlp/ffmpeg PATH entry, the seal:// protocol and URL associations, the
Start-menu uninstall shortcut, and per-user settings/history. This script removes all of it in
one go, then removes the installed product itself (via msiexec when installed from the MSI/EXE
package, or by deleting the install folder for a portable copy).

It is bundled into the packaged app (<install>\app\resources\uninstall.ps1) and reachable from
the "Uninstall Seal" Start-menu shortcut, Apps & Features, or a terminal:

  powershell -NoProfile -ExecutionPolicy Bypass -File uninstall.ps1 [-KeepData] [-Silent]

Only per-user (HKCU) state is touched; no administrator rights are required for the default
per-user install. Downloaded media files are never removed.

.PARAMETER KeepData
Keep per-user settings and download history (%USERPROFILE%\.seal).

.PARAMETER Silent
No prompts; removes the product quietly. App data is removed unless -KeepData is also given.
#>
[CmdletBinding()]
param(
    [switch]$KeepData,
    [switch]$Silent
)

$ErrorActionPreference = 'SilentlyContinue'

# The script ships in <install>\app\resources next to the bundled yt-dlp/ffmpeg binaries.
$resourcesDir = $PSScriptRoot
$installDir = $null
$candidate = Split-Path (Split-Path $resourcesDir -Parent) -Parent
if ($candidate -and (Test-Path (Join-Path $candidate 'Seal.exe'))) { $installDir = $candidate }

if (-not $Silent) {
    Write-Host 'This will remove Seal from this computer.'
    $answer = Read-Host 'Continue? [y/N]'
    if ($answer -notmatch '^[Yy]') {
        Write-Host 'Aborted.'
        exit 1
    }
    if (-not $KeepData) {
        $keep = Read-Host 'Keep settings and download history? [Y/n]'
        if ($keep -notmatch '^[Nn]') { $KeepData = $true }
    }
}

Write-Host 'Stopping Seal...'
Get-Process -Name 'Seal' -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Milliseconds 500

Write-Host 'Removing PATH entry...'
$path = [Environment]::GetEnvironmentVariable('Path', 'User')
if ($path -and $resourcesDir) {
    $parts = $path -split ';' | Where-Object { $_ -and $_ -ne $resourcesDir }
    [Environment]::SetEnvironmentVariable('Path', ($parts -join ';'), 'User')
}

Write-Host 'Removing shell integration...'
$keys = @(
    'HKCU:\Software\Classes\seal',
    'HKCU:\Software\Classes\Seal.URL',
    'HKCU:\Software\Classes\Applications\Seal.exe',
    'HKCU:\Software\Seal',
    'HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\Seal'
)
foreach ($key in $keys) { Remove-Item -Path $key -Recurse -Force -ErrorAction SilentlyContinue }
Remove-ItemProperty -Path 'HKCU:\Software\RegisteredApplications' -Name 'Seal' -ErrorAction SilentlyContinue

Write-Host 'Removing shortcuts...'
$menuDir = Join-Path $env:APPDATA 'Microsoft\Windows\Start Menu\Programs\Seal'
Remove-Item -Path $menuDir -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path (Join-Path ([Environment]::GetFolderPath('Desktop')) 'Seal.lnk') -Force -ErrorAction SilentlyContinue

if (-not $KeepData) {
    Write-Host 'Removing settings and history...'
    Remove-Item -Path (Join-Path $env:USERPROFILE '.seal') -Recurse -Force -ErrorAction SilentlyContinue
}

# Remove the installed product. MSI/EXE installs register a per-user Windows Installer product;
# ask msiexec to remove it so Installer state stays consistent. A portable copy has no product
# entry, so the install folder is deleted directly instead.
$msiRemoved = $false
$uninstallRoots = @(
    'HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall',
    'HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall',
    'HKLM:\Software\WOW6432Node\Microsoft\Windows\CurrentVersion\Uninstall'
)
foreach ($root in $uninstallRoots) {
    Get-ChildItem $root -ErrorAction SilentlyContinue | ForEach-Object {
        $props = Get-ItemProperty $_.PSPath -ErrorAction SilentlyContinue
        if ($props.DisplayName -eq 'Seal' -and $_.PSChildName -match '^\{[0-9A-Fa-f-]+\}$') {
            Write-Host "Uninstalling package $($_.PSChildName)..."
            $flags = if ($Silent) { '/qn' } else { '/qb' }
            Start-Process -FilePath 'msiexec.exe' -ArgumentList "/x $($_.PSChildName) $flags" -Wait
            $script:msiRemoved = $true
        }
    }
}

if (-not $msiRemoved -and $installDir -and (Test-Path $installDir)) {
    Write-Host "Removing $installDir..."
    # Self-delete: this script lives inside the install dir, so hand the removal to a detached
    # cmd that waits a moment for this process to let go of the files first.
    Start-Process -FilePath "$env:ComSpec" `
        -ArgumentList "/c ping -n 3 127.0.0.1 > nul & rd /s /q `"$installDir`"" `
        -WindowStyle Hidden
}

Write-Host 'Seal has been uninstalled.'
if (-not $Silent) { Read-Host 'Press Enter to close' | Out-Null }
