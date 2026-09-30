package com.junkfood.seal.util

import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Resolver tests against a local mock of the Google Drive download flow — the Kotlin
 * counterpart of `vidtoaui/backend/test/e2e.mjs` (confirm-form, quota, login-wall, direct).
 *
 * Uses a hand-rolled loopback HTTP server because `com.sun.net.httpserver` is not on the
 * Android unit-test classpath.
 */
class GoogleDriveResolverTest {

    private lateinit var server: MiniHttpServer
    private lateinit var savedUcBase: String

    private val fileId = "FILE_ID_12345"
    private val binaryBody = "FAKE_MP3_BYTES".toByteArray()

    /** Minimal sequential HTTP/1.1 server bound to 127.0.0.1 with an ephemeral port. */
    private class MiniHttpServer(
        private val handler: (path: String, query: String?) -> MiniResponse
    ) {
        data class MiniResponse(
            val code: Int,
            val contentType: String,
            val body: ByteArray,
        )

        private val socket: ServerSocket = ServerSocket()
        private var closed = false

        val port: Int
            get() = socket.localPort

        init {
            socket.bind(InetSocketAddress("127.0.0.1", 0))
            Thread(
                    {
                        while (!closed) {
                            val client =
                                try {
                                    socket.accept()
                                } catch (e: IOException) {
                                    break
                                }
                            try {
                                handle(client)
                            } catch (ignored: Exception) {
                                // Client may disconnect early (deliberate body discard).
                            }
                        }
                    },
                    "mini-http-server",
                )
                .apply { isDaemon = true }
                .start()
        }

        private fun handle(client: Socket) {
            client.use { s ->
                val input = s.getInputStream().bufferedReader(Charsets.UTF_8)
                val requestLine = input.readLine() ?: return
                while (true) {
                    val line = input.readLine() ?: break
                    if (line.isEmpty()) break
                }
                val target = requestLine.split(" ").getOrNull(1) ?: return
                val uri = URI(target)
                val response = handler(uri.path.orEmpty(), uri.query)
                val statusLine =
                    when (response.code) {
                        200 -> "200 OK"
                        403 -> "403 Forbidden"
                        404 -> "404 Not Found"
                        else -> "${response.code} Status"
                    }
                val head =
                    "HTTP/1.1 $statusLine\r\n" +
                        "Content-Type: ${response.contentType}\r\n" +
                        "Content-Length: ${response.body.size}\r\n" +
                        "Connection: close\r\n\r\n"
                val out = s.getOutputStream()
                out.write(head.toByteArray(Charsets.UTF_8))
                out.write(response.body)
                out.flush()
            }
        }

        fun close() {
            closed = true
            runCatching { socket.close() }
        }
    }

    private fun htmlResponse(html: String): MiniHttpServer.MiniResponse =
        MiniHttpServer.MiniResponse(200, "text/html", html.toByteArray())

    private val binaryResponse =
        MiniHttpServer.MiniResponse(200, "video/mp4", binaryBody)

    @Before
    fun setUp() {
        savedUcBase = GoogleDriveResolver.ucBase
        server =
            MiniHttpServer { _, query ->
                val q = query.orEmpty()
                when {
                    q.contains("quota=1") ->
                        htmlResponse(
                            "<html><body>Quota exceeded. Too many users have accessed this " +
                                "file recently.</body></html>"
                        )

                    q.contains("private=1") ->
                        htmlResponse(
                            "<html><head><title>Sign in</title></head><body>You need access " +
                                "— ServiceLogin accounts.google.com</body></html>"
                        )

                    q.contains("empty=1") ->
                        htmlResponse("<html><body>Nothing here</body></html>")

                    q.contains("direct=1") -> binaryResponse

                    q.contains("confirm=") -> binaryResponse

                    else ->
                        htmlResponse(
                            """
                            <html><body>
                            <form action="http://127.0.0.1:${server.port}/download" method="get">
                              <input type="hidden" name="id" value="$fileId">
                              <input type="hidden" name="export" value="download">
                              <input type="hidden" name="confirm" value="t">
                              <input type="hidden" name="uuid" value="uuid-123">
                            </form>
                            </body></html>
                            """
                                .trimIndent()
                        )
                }
            }
        GoogleDriveResolver.ucBase = "http://127.0.0.1:${server.port}/uc?export=download"
    }

    @After
    fun tearDown() {
        GoogleDriveResolver.ucBase = savedUcBase
        server.close()
    }

    private fun driveUrl(query: String = ""): String =
        "https://drive.google.com/file/d/$fileId/view$query"

    @Test
    fun followsModernConfirmFormToDirectUrl() {
        val resolved = GoogleDriveResolver.resolve(driveUrl())
        assertTrue("resolved=$resolved", resolved.contains("/download"))
        assertTrue("resolved=$resolved", resolved.contains("confirm=t"))
        assertTrue("resolved=$resolved", resolved.contains("uuid=uuid-123"))
        assertTrue("resolved=$resolved", resolved.contains("id=$fileId"))
    }

    @Test
    fun appendsResourceKeyToFormUrl() {
        val resolved = GoogleDriveResolver.resolve("${driveUrl()}?resourcekey=RESOURCE_KEY_ABC")
        assertTrue("resolved=$resolved", resolved.contains("resourcekey=RESOURCE_KEY_ABC"))
    }

    @Test
    fun returnsDirectUrlWhenBinaryServedImmediately() {
        GoogleDriveResolver.ucBase =
            "http://127.0.0.1:${server.port}/uc?export=download&direct=1"
        val input = "https://drive.usercontent.google.com/download?id=$fileId&direct=1"
        val resolved = GoogleDriveResolver.resolve(input)
        assertTrue("resolved=$resolved", resolved.contains("direct=1"))
    }

    @Test
    fun quotaPageThrowsQuotaExceeded() {
        GoogleDriveResolver.ucBase =
            "http://127.0.0.1:${server.port}/uc?export=download&quota=1"
        try {
            GoogleDriveResolver.resolve(driveUrl())
            fail("expected DriveError.QuotaExceeded")
        } catch (expected: DriveError.QuotaExceeded) {
            // expected
        }
    }

    @Test
    fun loginWallThrowsNotAccessible() {
        GoogleDriveResolver.ucBase =
            "http://127.0.0.1:${server.port}/uc?export=download&private=1"
        try {
            GoogleDriveResolver.resolve(driveUrl())
            fail("expected DriveError.NotAccessible")
        } catch (expected: DriveError.NotAccessible) {
            // expected
        }
    }

    @Test
    fun htmlWithoutFormOrTokenThrowsNotFound() {
        GoogleDriveResolver.ucBase =
            "http://127.0.0.1:${server.port}/uc?export=download&empty=1"
        try {
            GoogleDriveResolver.resolve(driveUrl())
            fail("expected DriveError.NotFound")
        } catch (expected: DriveError.NotFound) {
            // expected
        }
    }

    @Test
    fun folderLinksThrowFolderError() {
        try {
            GoogleDriveResolver.resolve("https://drive.google.com/drive/folders/FOLDER_ID_12345")
            fail("expected DriveError.Folder")
        } catch (expected: DriveError.Folder) {
            // expected
        }
    }

    @Test
    fun nonDriveUrlsPassThroughUnchanged() {
        val youtube = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
        assertEquals(youtube, GoogleDriveResolver.resolve(youtube))
    }

    @Test
    fun parsesLegacyConfirmTokensFromHtml() {
        assertEquals(
            "abc123",
            GoogleDriveResolver.extractConfirmToken(
                """<a href="/uc?export=download&confirm=abc123">x</a>"""
            ),
        )
        assertEquals(
            "xyz",
            GoogleDriveResolver.extractConfirmToken("""{"confirm": "xyz"}"""),
        )
        assertEquals(
            "tok_1",
            GoogleDriveResolver.extractConfirmToken("""confirm_tok_1 more"""),
        )
    }

    @Test
    fun ignoresFormsWithNonDownloadActions() {
        val html =
            """
            <form action="https://accounts.google.com/ServiceLogin" method="post">
              <input type="hidden" name="id" value="$fileId">
            </form>
            """
                .trimIndent()
        assertEquals(null, GoogleDriveResolver.parseDownloadForm(html))
    }
}
