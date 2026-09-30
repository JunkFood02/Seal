package com.junkfood.seal.util

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FfmpegUtilTest {

    @get:Rule val temporaryFolder = TemporaryFolder()

    private fun newBinary(): File = temporaryFolder.newFile("libffmpeg.so")

    private fun newLibDir(withCriticalLib: Boolean = true): File =
        temporaryFolder.newFolder("usr", "lib").apply {
            if (withCriticalLib) {
                assertTrue(File(this, "libavcodec.so").createNewFile())
            }
        }

    @Test
    fun diagnoseReturnsNullWhenBinaryAndLibrariesExist() {
        assertNull(FfmpegUtil.diagnose(newBinary(), newLibDir()))
    }

    @Test
    fun diagnoseReportsMissingBinary() {
        val missingBinary = File(temporaryFolder.root, "libffmpeg.so")
        assertEquals(
            FfmpegUtil.Failure.BINARY_MISSING,
            FfmpegUtil.diagnose(missingBinary, newLibDir()),
        )
    }

    @Test
    fun diagnoseReportsMissingLibDirectory() {
        val missingLibDir = File(temporaryFolder.root, "usr-not-there")
        assertEquals(
            FfmpegUtil.Failure.LIBS_MISSING,
            FfmpegUtil.diagnose(newBinary(), missingLibDir),
        )
    }

    @Test
    fun diagnoseReportsMissingCriticalLibrary() {
        assertEquals(
            FfmpegUtil.Failure.LIBS_MISSING,
            FfmpegUtil.diagnose(newBinary(), newLibDir(withCriticalLib = false)),
        )
    }

    @Test
    fun ldLibraryPathJoinsDirectoriesInOrder() {
        val first = File("first-dir")
        val second = File("second-dir")
        val third = File("third-dir")
        val path = FfmpegUtil.buildLdLibraryPath(first, second, third)
        assertEquals("${first.absolutePath}:${second.absolutePath}:${third.absolutePath}", path)
    }

    @Test
    fun detectsYtdlpMissingFfmpegErrors() {
        assertTrue(
            FfmpegUtil.isMissingFfmpegError(
                "ERROR: Postprocessing: ffmpeg not found. Please install or provide the path using --ffmpeg-location"
            )
        )
        assertTrue(FfmpegUtil.isMissingFfmpegError("failed: --ffmpeg-location is invalid"))
        assertFalse(FfmpegUtil.isMissingFfmpegError("ERROR: HTTP Error 403: Forbidden"))
        assertFalse(FfmpegUtil.isMissingFfmpegError(null))
    }

    @Test
    fun selfTestFailsWhenBinaryDoesNotExist() {
        val missing = File(temporaryFolder.root, "definitely-not-here")
        val result = FfmpegUtil.selfTest(missing, ldLibraryPath = "")
        assertTrue(result.isFailure)
    }
}
