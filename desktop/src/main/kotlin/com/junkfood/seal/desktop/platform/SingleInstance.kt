package com.junkfood.seal.desktop.platform

import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.util.UUID
import kotlinx.coroutines.channels.Channel

/**
 * Keeps Seal single-instance and lets follow-up launches hand their link to the running window.
 *
 * Shared links always arrive as a brand-new process (protocol handlers and "Open with" spawn a
 * fresh one). The first instance binds a loopback-only socket and advertises it in a lock file;
 * later launches connect, forward their URL (or just a focus request), and exit. A random token
 * in the user-only lock file stops unrelated local processes from injecting links, and a stale
 * lock (crashed instance or recycled port) simply fails the handshake, so the new launch takes
 * over as the primary instance.
 */
class SingleInstance private constructor(private val server: ServerSocket) {

    /** Links forwarded by later launches; an empty string is a plain focus request. */
    val incomingUrls = Channel<String>(Channel.BUFFERED)

    companion object {
        private val lockFile = File(System.getProperty("user.home"), ".seal/instance.lock")

        /**
         * Claims the single-instance role, or forwards [initialUrl] to the instance already
         * holding it. Returns null when the link was handed off and this process should exit.
         */
        fun acquire(initialUrl: String?): SingleInstance? {
            if (forwardToRunningInstance(initialUrl)) return null

            val server = ServerSocket(0, 8, InetAddress.getLoopbackAddress())
            val token = UUID.randomUUID().toString()
            runCatching {
                lockFile.parentFile?.mkdirs()
                lockFile.writeText("${server.localPort}\n$token")
                lockFile.deleteOnExit()
            }
            return SingleInstance(server).apply {
                initialUrl?.let(incomingUrls::trySend)
                Thread { acceptLoop(token) }
                    .apply {
                        name = "seal-single-instance"
                        isDaemon = true
                    }
                    .start()
            }
        }

        private fun forwardToRunningInstance(url: String?): Boolean {
            val lines = runCatching { lockFile.readLines() }.getOrNull() ?: return false
            val port = lines.getOrNull(0)?.trim()?.toIntOrNull() ?: return false
            val token = lines.getOrNull(1)?.trim().takeUnless { it.isNullOrEmpty() } ?: return false
            return runCatching {
                    Socket(InetAddress.getLoopbackAddress(), port).use { socket ->
                        socket.soTimeout = 2000
                        socket.getOutputStream().apply {
                            write("$token ${url.orEmpty()}\n".toByteArray())
                            flush()
                        }
                        socket.getInputStream().bufferedReader().readLine() == "OK"
                    }
                }
                .getOrDefault(false)
        }
    }

    private fun acceptLoop(token: String) {
        while (!server.isClosed) {
            runCatching {
                server.accept().use { client ->
                    client.soTimeout = 2000
                    val line = client.getInputStream().bufferedReader().readLine() ?: return@use
                    if (line != token && !line.startsWith("$token ")) return@use
                    client.getOutputStream().apply {
                        write("OK\n".toByteArray())
                        flush()
                    }
                    incomingUrls.trySend(line.removePrefix(token).trim())
                }
            }
        }
    }
}
