package com.example.digitaldetox.domain.model

data class App(
    val packageName: String,
    val label: String,
    val blockEnabled: Boolean = false,
    val blockType: BlockType = BlockType.SYSTEM
)

enum class BlockType {
    SYSTEM,
    USER_SELECTED
}