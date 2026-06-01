package com.junkfood.seal.ui.page.settings.network

import com.junkfood.seal.util.DownloadUtil.mergeRuntimeCookies
import com.junkfood.seal.util.DownloadUtil.toCookiesFileContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CookieParserTest {
    @Test
    fun parseNetscapeCookies_acceptsHeaderAndCookieRow() {
        val content =
            "# Netscape HTTP Cookie File\n" +
                "example.com\tFALSE\t/\tTRUE\t1717000000\tsession\tabc123\n"

        val cookies = CookieParser.parseNetscapeCookies(content).getOrThrow()

        assertEquals(1, cookies.size)
        assertEquals(
            Cookie(
                domain = "example.com",
                name = "session",
                value = "abc123",
                includeSubdomains = false,
                path = "/",
                secure = true,
                expiry = 1717000000L,
            ),
            cookies.first(),
        )
    }

    @Test
    fun parseNetscapeCookies_ignoresBlankAndCommentLines() {
        val content =
            "\n" +
                "# A regular comment\n" +
                "example.com\tFALSE\t/\tTRUE\t1717000000\tsession\tabc123\n" +
                "example.org\tTRUE\t/account\tFALSE\t0\tauth\txyz\n"

        val cookies = CookieParser.parseNetscapeCookies(content).getOrThrow()

        assertEquals(2, cookies.size)
        assertEquals("example.com", cookies[0].domain)
        assertEquals("example.org", cookies[1].domain)
    }

    @Test
    fun parseNetscapeCookies_keepsHttpOnlyCookieRows() {
        val content = "#HttpOnly_.example.com\tTRUE\t/\tTRUE\t0\tsession\tsecret\n"

        val cookie = CookieParser.parseNetscapeCookies(content).getOrThrow().single()

        assertEquals(".example.com", cookie.domain)
        assertEquals("session", cookie.name)
        assertEquals("secret", cookie.value)
    }

    @Test
    fun parseNetscapeCookies_acceptsMixedCaseBooleans() {
        val content = "example.com\ttrue\t/\tFalse\t0\tsession\tabc123\n"

        val cookie = CookieParser.parseNetscapeCookies(content).getOrThrow().single()

        assertTrue(cookie.includeSubdomains)
        assertEquals(false, cookie.secure)
    }

    @Test
    fun parseNetscapeCookies_usesLastDuplicateCookieTuple() {
        val content =
            "example.com\tFALSE\t/\tTRUE\t1\tsession\told\n" +
                "example.com\tFALSE\t/\tTRUE\t2\tsession\tnew\n"

        val cookie = CookieParser.parseNetscapeCookies(content).getOrThrow().single()

        assertEquals("new", cookie.value)
        assertEquals(2L, cookie.expiry)
    }

    @Test
    fun parseNetscapeCookies_rejectsRowsWithoutSevenTabSeparatedFields() {
        val content = "example.com FALSE / TRUE 0 session abc123\n"

        val result = CookieParser.parseNetscapeCookies(content)

        assertTrue(result.isFailure)
    }

    @Test
    fun parseNetscapeCookies_rejectsInvalidExpiry() {
        val content = "example.com\tFALSE\t/\tTRUE\tnot-a-number\tsession\tabc123\n"

        val result = CookieParser.parseNetscapeCookies(content)

        assertTrue(result.isFailure)
    }

    @Test
    fun parseNetscapeCookies_roundTripsSerializedCookies() {
        val input =
            "# Netscape HTTP Cookie File\n" +
                "example.com\tFALSE\t/\tTRUE\t1717000000\tsession\tabc123\n" +
                "#HttpOnly_.example.org\tTRUE\t/account\tFALSE\t0\tauth\txyz\n"

        val parsed = CookieParser.parseNetscapeCookies(input).getOrThrow()
        val reparsed = CookieParser.parseNetscapeCookies(parsed.toCookiesFileContent()).getOrThrow()

        assertEquals(parsed, reparsed)
    }

    @Test
    fun mergeRuntimeCookies_keepsImportedCookiesWhenWebViewIsEmpty() {
        val importedCookie = Cookie(domain = "example.com", name = "session", value = "imported")

        val cookies =
            mergeRuntimeCookies(
                webViewCookies = emptyList(),
                importedCookies = listOf(importedCookie),
            )

        assertEquals(listOf(importedCookie), cookies)
    }

    @Test
    fun mergeRuntimeCookies_importedCookieOverridesDuplicateWebViewCookie() {
        val webViewCookie =
            Cookie(domain = "example.com", path = "/", name = "session", value = "webview")
        val importedCookie =
            Cookie(domain = "example.com", path = "/", name = "session", value = "imported")

        val cookies =
            mergeRuntimeCookies(
                webViewCookies = listOf(webViewCookie),
                importedCookies = listOf(importedCookie),
            )

        assertEquals(1, cookies.size)
        assertEquals("imported", cookies.single().value)
    }
}
