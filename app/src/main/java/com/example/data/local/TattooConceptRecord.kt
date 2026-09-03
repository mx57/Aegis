package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.TattooConcept

@Entity(tableName = "tattoo_concepts")
data class TattooConceptRecord(
    @PrimaryKey
    val id: String,
    val title: String,
    val runeIdsCsv: String,
    val runesFormatted: String,
    val placement: String,
    val style: String,
    val visualComposition: String,
    val sacredMeaning: String,
    val masterAdvice: String,
    val recommendedSize: String,
    val userPrompt: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toModel(): TattooConcept = TattooConcept(
        id = id,
        title = title,
        runeIdsCsv = runeIdsCsv,
        runesFormatted = runesFormatted,
        placement = placement,
        style = style,
        visualComposition = visualComposition,
        sacredMeaning = sacredMeaning,
        masterAdvice = masterAdvice,
        recommendedSize = recommendedSize,
        userPrompt = userPrompt,
        isFavorite = isFavorite,
        createdAt = createdAt
    )

    companion object {
        fun fromModel(model: TattooConcept): TattooConceptRecord = TattooConceptRecord(
            id = model.id,
            title = model.title,
            runeIdsCsv = model.runeIdsCsv,
            runesFormatted = model.runesFormatted,
            placement = model.placement,
            style = model.style,
            visualComposition = model.visualComposition,
            sacredMeaning = model.sacredMeaning,
            masterAdvice = model.masterAdvice,
            recommendedSize = model.recommendedSize,
            userPrompt = model.userPrompt,
            isFavorite = model.isFavorite,
            createdAt = model.createdAt
        )
    }
}
