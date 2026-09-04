package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GeminiArtworkDao {

    @Query("SELECT * FROM gemini_artworks ORDER BY createdAt DESC")
    fun getAll(): Flow<List<GeminiArtworkRecord>>

    @Query("SELECT * FROM gemini_artworks WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<GeminiArtworkRecord>>

    @Query("SELECT * FROM gemini_artworks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): GeminiArtworkRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(artwork: GeminiArtworkRecord)

    @Query("UPDATE gemini_artworks SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM gemini_artworks WHERE id = :id")
    suspend fun deleteById(id: String)
}
