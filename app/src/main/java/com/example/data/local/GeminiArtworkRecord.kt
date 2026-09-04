package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "gemini_artworks")
data class GeminiArtworkRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val imagePath: String,
    val promptUsed: String,
    val styleName: String,
    val runeNames: String,
    val layoutType: String,
    val centerEmblem: String,
    val frameType: String,
    val elementScale: Float = 1.0f,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
