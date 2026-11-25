package com.junkfood.seal.util

import com.junkfood.seal.util.GoogleDriveUrl.Target
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Mirrors the proven web backend test matrix (`vidtoaui/backend/test/driveUrl.test.mjs`). */
class GoogleDriveUrlTest {

    private val fileId = "FILE_ID_12345"
    private val folderId = "FOLDER_ID_12345"
    private val resourceKey = "RESOURCE_KEY_ABC"

    private fun assertTarget(
        input: String?,
        expectedId: String,
        expectedType: Target.Type,
        expectedResourceKey: String? = null,
    ) {
        val target = GoogleDriveUrl.extractDriveTarget(input)
        requireNotNull(target) { "expected a target for $input" }
        assertEquals("id for $input", expectedId, target.id)
        assertEquals("type for $input", expectedType, target.type)
        assertEquals("resourceKey for $input", expectedResourceKey, target.resourceKey)
    }

    @Test
    fun parsesEverySupportedShareLinkShape() {
        assertTarget(
            "https://drive.google.com/file/d/$fileId/view",
            fileId,
            Target.Type.FILE,
        )
        assertTarget(
            "https://drive.google.com/file/d/$fileId/view?usp=sharing",
            fileId,
            Target.Type.FILE,
        )
        assertTarget(
            "https://drive.google.com/file/d/$fileId/preview",
            fileId,
            Target.Type.FILE,
        )
        assertTarget("https://drive.google.com/open?id=$fileId", fileId, Target.Type.FILE)
        assertTarget("https://drive.google.com/uc?id=$fileId", fileId, Target.Type.FILE)
        assertTarget(
            "https://drive.google.com/uc?export=download&id=$fileId",
            fileId,
            Target.Type.FILE,
        )
        assertTarget(
            "https://drive.google.com/uc?export=view&id=$fileId",
            fileId,
            Target.Type.FILE,
        )
        assertTarget(
            "https://drive.usercontent.google.com/download?id=$fileId&export=download",
            fileId,
            Target.Type.FILE,
        )
        assertTarget(
            "https://drive.google.com/file/d/$fileId/view?resourcekey=$resourceKey",
            fileId,
            Target.Type.FILE,
            resourceKey,
        )
        assertTarget(
            "https://drive.google.com/file/d/$fileId/view?usp=sharing&resourcekey=$resourceKey",
            fileId,
            Target.Type.FILE,
            resourceKey,
        )
        assertTarget(
            "https://drive.google.com/drive/folders/$folderId",
            folderId,
            Target.Type.FOLDER,
        )
        assertTarget(
            "https://drive.google.com/drive/folders/$folderId?usp=sharing",
            folderId,
            Target.Type.FOLDER,
        )
        assertTarget(
            "https://drive.google.com/drive/u/0/folders/$folderId",
            folderId,
            Target.Type.FOLDER,
        )
        assertTarget("https://drive.google.com/file/d/$fileId", fileId, Target.Type.FILE)
        assertTarget("https://drive.google.com/file/d/$fileId/edit", fileId, Target.Type.FILE)
        assertTarget(
            "https://drive.google.com/open?id=$fileId&resourcekey=$resourceKey",
            fileId,
            Target.Type.FILE,
            resourceKey,
        )
        assertTarget(
            "https://www.drive.google.com/file/d/$fileId/view",
            fileId,
            Target.Type.FILE,
        )
        assertTarget(fileId, fileId, Target.Type.FILE)
    }

    @Test
    fun rejectsNonDriveAndMalformedLinks() {
        assertNull(GoogleDriveUrl.extractDriveTarget("https://example.com/file/d/$fileId/view"))
        assertNull(GoogleDriveUrl.extractDriveTarget("https://youtube.com/watch?v=$fileId"))
        assertNull(GoogleDriveUrl.extractDriveTarget("https://drive.google.com/file/d/short/x"))
        assertNull(GoogleDriveUrl.extractDriveTarget("https://drive.google.com/drive/folders/"))
        assertNull(GoogleDriveUrl.extractDriveTarget("not a url"))
        assertNull(GoogleDriveUrl.extractDriveTarget(""))
        assertNull(GoogleDriveUrl.extractDriveTarget(null))
    }

    @Test
    fun backCompatHelpersMatchWebBehaviour() {
        assertEquals(fileId, GoogleDriveUrl.extractFileId("https://drive.google.com/file/d/$fileId/view"))
        assertNull(GoogleDriveUrl.extractFileId("https://drive.google.com/drive/folders/$folderId"))
        assertTrue(
            GoogleDriveUrl.isDriveUrl(
                "https://drive.usercontent.google.com/download?id=$fileId"
            )
        )
        assertFalse(GoogleDriveUrl.isDriveUrl("https://example.com/x"))
    }

    @Test
    fun bareIdFlagIsSetOnlyForBareIds() {
        assertTrue(
            requireNotNull(GoogleDriveUrl.extractDriveTarget(fileId)).bare
        )
        assertFalse(
            requireNotNull(
                GoogleDriveUrl.extractDriveTarget("https://drive.google.com/file/d/$fileId/view")
            )
            .bare
        )
    }
}
