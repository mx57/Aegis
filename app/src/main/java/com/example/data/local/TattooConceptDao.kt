package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TattooConceptDao {

    @Query("SELECT * FROM tattoo_concepts ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TattooConceptRecord>>

    @Query("SELECT * FROM tattoo_concepts WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<TattooConceptRecord>>

    @Query("SELECT * FROM tattoo_concepts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): TattooConceptRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: TattooConceptRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<TattooConceptRecord>)

    @Query("UPDATE tattoo_concepts SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM tattoo_concepts WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM tattoo_concepts WHERE isFavorite = 0")
    suspend fun clearNonFavorites()
}
