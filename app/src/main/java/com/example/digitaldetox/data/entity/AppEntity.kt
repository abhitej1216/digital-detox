package com.example.digitaldetox.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey
    val packageName: String,
    val label: String,
    val blockEnabled: Boolean = false,
    val blockType: BlockType = BlockType.SYSTEM
)

enum class BlockType {
    SYSTEM,
    USER_SELECTED
}