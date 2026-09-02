package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StaveDao {

    @Query("SELECT * FROM stave_records ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<StaveRecord>>

    @Query("SELECT * FROM stave_records WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavorites(): Flow<List<StaveRecord>>

    @Query("SELECT * FROM stave_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): StaveRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: StaveRecord): Long

    @Update
    suspend fun update(record: StaveRecord)

    @Query("UPDATE stave_records SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM stave_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM stave_records WHERE isFavorite = 0")
    suspend fun clearNonFavoriteHistory()
}
