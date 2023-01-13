package com.junkfood.seal.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class OptionShortcut(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val option: String
)