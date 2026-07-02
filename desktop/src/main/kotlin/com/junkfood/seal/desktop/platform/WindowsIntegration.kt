package com.junkfood.seal.desktop.platform

import java.io.File

/**
 * One-time, per-user Windows shell integration for the packaged app. Runs on first launch (the
 * jpackage MSI/EXE installers can't run custom actions) and only ever writes to HKCU:
 *
 * - registers the `seal://` protocol plus http/https URL associations ("Open with", Settings >
 *   Default apps), so links can be shared to Seal from browsers and other apps
 * - creates an "Uninstall Seal" Start-menu shortcut pointing at the bundled uninstall script, and
 *   an Apps & Features entry for it when the installer didn't already register one — giving Seal
 *   its own uninstaller that also cleans up everything this integration writes (see
 *   `desktop/packaging/windows/uninstall.ps1`)
 *
 * No-ops when not on Windows or when running unpackaged (`./gradlew :desktop:run`).
 */
object WindowsIntegration {

    private val isWindows: Boolean
        get() = System.getProperty("os.name")?.lowercase()?.contains("win") == true

    /** The packaged launcher (Seal.exe); null when running unpackaged via gradle/java. */
    private val launcherExecutable: File?
        get() =
            ProcessHandle.current()
                .info()
                .command()
                .orElse(null)
                ?.let(::File)
                ?.takeIf { it.name.equals("Seal.exe", ignoreCase = true) }

    /** Returns true when the integration was applied; false if unsupported here or it failed. */
    fun register(): Boolean {
        if (!isWindows) return false
        val exe = launcherExecutable ?: return false
        val resourcesDir = System.getProperty("compose.application.resources.dir") ?: return false
        val uninstallScript = File(resourcesDir, "uninstall.ps1").takeIf { it.isFile }
        return runScript(buildScript(exe, uninstallScript))
    }

    private fun runScript(script: String): Boolean =
        runCatching {
                // -File instead of -Command: the script is too large to pass safely as one
                // argument, and this avoids a second layer of quoting/escaping.
                val file =
                    File.createTempFile("seal-shell-integration", ".ps1").apply {
                        writeText(script)
                        deleteOnExit()
                    }
                val process =
                    ProcessBuilder(
                            "powershell",
                            "-NoProfile",
                            "-NonInteractive",
                            "-ExecutionPolicy",
                            "Bypass",
                            "-File",
                            file.absolutePath,
                        )
                        .redirectErrorStream(true)
                        .start()
                process.waitFor() == 0
            }
            .getOrDefault(false)

    private fun psQuote(value: String): String = "'" + value.replace("'", "''") + "'"

    private fun buildScript(exe: File, uninstallScript: File?): String {
        val version = System.getProperty("jpackage.app-version") ?: "1.0.0"
        return """
            ${'$'}ErrorActionPreference = 'Stop'
            ${'$'}exe = ${psQuote(exe.absolutePath)}
            ${'$'}open = '"' + ${'$'}exe + '" "%1"'

            function Set-DefaultValue(${'$'}key, ${'$'}value) {
                New-Item -Path ${'$'}key -Force | Out-Null
                Set-ItemProperty -Path ${'$'}key -Name '(default)' -Value ${'$'}value
            }

            # seal:// protocol handler
            Set-DefaultValue 'HKCU:\Software\Classes\seal' 'URL:Seal link'
            Set-ItemProperty -Path 'HKCU:\Software\Classes\seal' -Name 'URL Protocol' -Value ''
            Set-DefaultValue 'HKCU:\Software\Classes\seal\DefaultIcon' (${'$'}exe + ',0')
            Set-DefaultValue 'HKCU:\Software\Classes\seal\shell\open\command' ${'$'}open

            # ProgId used by the http/https associations below
            Set-DefaultValue 'HKCU:\Software\Classes\Seal.URL' 'Seal download link'
            Set-DefaultValue 'HKCU:\Software\Classes\Seal.URL\DefaultIcon' (${'$'}exe + ',0')
            Set-DefaultValue 'HKCU:\Software\Classes\Seal.URL\shell\open\command' ${'$'}open

            # "Open with" browse target
            Set-DefaultValue 'HKCU:\Software\Classes\Applications\Seal.exe\shell\open\command' ${'$'}open
            Set-ItemProperty -Path 'HKCU:\Software\Classes\Applications\Seal.exe' -Name 'FriendlyAppName' -Value 'Seal'

            # Default-apps registration so Windows offers Seal as a handler for shared web links
            New-Item -Path 'HKCU:\Software\Seal\Capabilities\URLAssociations' -Force | Out-Null
            Set-ItemProperty -Path 'HKCU:\Software\Seal\Capabilities' -Name 'ApplicationName' -Value 'Seal'
            Set-ItemProperty -Path 'HKCU:\Software\Seal\Capabilities' -Name 'ApplicationDescription' -Value 'Video/audio downloader powered by yt-dlp'
            Set-ItemProperty -Path 'HKCU:\Software\Seal\Capabilities\URLAssociations' -Name 'http' -Value 'Seal.URL'
            Set-ItemProperty -Path 'HKCU:\Software\Seal\Capabilities\URLAssociations' -Name 'https' -Value 'Seal.URL'
            New-Item -Path 'HKCU:\Software\RegisteredApplications' -Force | Out-Null
            Set-ItemProperty -Path 'HKCU:\Software\RegisteredApplications' -Name 'Seal' -Value 'Software\Seal\Capabilities'

            ${'$'}uninstall = ${psQuote(uninstallScript?.absolutePath ?: "")}
            if (${'$'}uninstall -and (Test-Path ${'$'}uninstall)) {
                # Start-menu shortcut next to the launcher's own entry (menuGroup 'Seal')
                ${'$'}menuDir = Join-Path ${'$'}env:APPDATA 'Microsoft\Windows\Start Menu\Programs\Seal'
                New-Item -ItemType Directory -Path ${'$'}menuDir -Force | Out-Null
                ${'$'}shell = New-Object -ComObject WScript.Shell
                ${'$'}shortcut = ${'$'}shell.CreateShortcut((Join-Path ${'$'}menuDir 'Uninstall Seal.lnk'))
                ${'$'}shortcut.TargetPath = "${'$'}env:SystemRoot\System32\WindowsPowerShell\v1.0\powershell.exe"
                ${'$'}shortcut.Arguments = '-NoProfile -ExecutionPolicy Bypass -File "' + ${'$'}uninstall + '"'
                ${'$'}shortcut.WorkingDirectory = Split-Path ${'$'}uninstall
                ${'$'}shortcut.IconLocation = ${'$'}exe + ',0'
                ${'$'}shortcut.Description = 'Uninstall Seal'
                ${'$'}shortcut.Save()

                # Apps & Features entry — only when the MSI/EXE installer didn't register one, so
                # the app never shows up twice in the installed-apps list.
                ${'$'}roots = @(
                    'HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall',
                    'HKLM:\Software\Microsoft\Windows\CurrentVersion\Uninstall'
                )
                ${'$'}existing = Get-ChildItem ${'$'}roots -ErrorAction SilentlyContinue |
                    Where-Object { (${'$'}_ | Get-ItemProperty -ErrorAction SilentlyContinue).DisplayName -eq 'Seal' }
                if (-not ${'$'}existing) {
                    ${'$'}arp = 'HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\Seal'
                    New-Item -Path ${'$'}arp -Force | Out-Null
                    Set-ItemProperty -Path ${'$'}arp -Name 'DisplayName' -Value 'Seal'
                    Set-ItemProperty -Path ${'$'}arp -Name 'DisplayVersion' -Value ${psQuote(version)}
                    Set-ItemProperty -Path ${'$'}arp -Name 'Publisher' -Value 'Seal'
                    Set-ItemProperty -Path ${'$'}arp -Name 'DisplayIcon' -Value (${'$'}exe + ',0')
                    Set-ItemProperty -Path ${'$'}arp -Name 'InstallLocation' -Value (Split-Path ${'$'}exe)
                    Set-ItemProperty -Path ${'$'}arp -Name 'UninstallString' -Value ('powershell.exe -NoProfile -ExecutionPolicy Bypass -File "' + ${'$'}uninstall + '"')
                    Set-ItemProperty -Path ${'$'}arp -Name 'NoModify' -Value 1 -Type DWord
                    Set-ItemProperty -Path ${'$'}arp -Name 'NoRepair' -Value 1 -Type DWord
                }
            }
            """
            .trimIndent()
    }
}
