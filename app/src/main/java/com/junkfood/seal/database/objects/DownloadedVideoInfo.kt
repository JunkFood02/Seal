package com.junkfood.seal.database.objects

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class DownloadedVideoInfo(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val videoTitle: String,
    val videoAuthor: String,
    val videoUrl: String,
    val thumbnailUrl: String,
    val videoPath: String,
    @ColumnInfo(defaultValue = "Unknown") val extractor: String = "Unknown",
) {
    @Ignore
    constructor() :
        this(
            id = 0,
            videoTitle = "Video",
            videoAuthor = "Author",
            videoUrl = "Url",
            thumbnailUrl = "Thumbnail",
            videoPath = "Path",
            extractor = "Unknown",
        )
}
