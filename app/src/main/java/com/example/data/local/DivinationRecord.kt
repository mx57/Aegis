package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity storing divination / oracle readings performed by the user.
 * Automatically retains the latest 10 readings for future analysis and stave creation.
 */
@Entity(tableName = "divination_records")
data class DivinationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val spreadType: String, // "POUCH", "DAY", "NORNS"
    val spreadTitleRu: String, // "Мешочек судьбы (1 руна)", "Руна дня", "Три Норны"
    val runeIdsCsv: String, // "fehu" or "uruz,ansuz,raido"
    val reversedFlagsCsv: String, // "0" or "0,1,0"
    val createdAt: Long = System.currentTimeMillis(),
    val questionOrContext: String = "",
    val interpretationSummary: String = "",
    val notes: String = ""
) {
    fun getRuneIdList(): List<String> =
        runeIdsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    fun getReversedList(): List<Boolean> =
        reversedFlagsCsv.split(",").map { it.trim() == "1" || it.trim().equals("true", ignoreCase = true) }
}
