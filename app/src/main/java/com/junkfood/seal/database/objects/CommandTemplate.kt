package com.junkfood.seal.database.objects

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class CommandTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val name: String,
    val template: String,
)
