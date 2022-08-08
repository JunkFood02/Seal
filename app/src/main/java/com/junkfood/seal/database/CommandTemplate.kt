package com.junkfood.seal.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CommandTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val template: String
)