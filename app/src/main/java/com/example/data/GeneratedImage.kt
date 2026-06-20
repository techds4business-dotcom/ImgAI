package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generated_images")
data class GeneratedImage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val selectedPreset: String,
    val aspectRatio: String,
    val imageSize: String,
    val localFilePath: String,
    val timestamp: Long,
    val isEdited: Boolean = false,
    val parentId: Int? = null,
    val editPrompt: String? = null
)
