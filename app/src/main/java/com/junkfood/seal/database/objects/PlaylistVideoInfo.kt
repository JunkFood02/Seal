package com.junkfood.seal.database.objects

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["playlistId", "videoId"],
    foreignKeys = [
        ForeignKey(entity = Playlist::class, parentColumns = ["id"], childColumns = ["playlistId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = DownloadedVideoInfo::class, parentColumns = ["id"], childColumns = ["videoId"], onDelete = ForeignKey.CASCADE),
    ]
)
data class PlaylistVideoInfo(
    val playlistId: Int,
    val videoId: Int,
    val addedAt: Long = System.currentTimeMillis(),
)
