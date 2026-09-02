package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stave_records")
data class StaveRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val runeIdsCsv: String,
    val layoutType: String, // "ROW", "BINDRUNE", "CIRCLE", "MIRROR"
    val styleType: String,  // "STRICT", "ORNAMENTAL", "DOTWORK", "BLACKWORK"
    val seed: Long,
    val lineWidth: Float = 3.5f,
    val hasFrameCircle: Boolean = true,
    val isStencil: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
) {
    fun getRuneIdList(): List<String> =
        runeIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
