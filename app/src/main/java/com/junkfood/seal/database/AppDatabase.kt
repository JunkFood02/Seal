package com.junkfood.seal.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DownloadedVideoInfo::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoInfoDao(): VideoInfoDao
}