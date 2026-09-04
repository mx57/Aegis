package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DivinationDao {

    @Query("SELECT * FROM divination_records ORDER BY createdAt DESC LIMIT 10")
    fun getLatest10(): Flow<List<DivinationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: DivinationRecord): Long

    @Query("DELETE FROM divination_records WHERE id NOT IN (SELECT id FROM divination_records ORDER BY createdAt DESC LIMIT 10)")
    suspend fun pruneOldRecords()

    @Query("DELETE FROM divination_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM divination_records")
    suspend fun clearAll()
}
