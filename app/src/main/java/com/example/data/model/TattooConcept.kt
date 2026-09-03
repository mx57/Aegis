package com.example.data.model

data class TattooConcept(
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
    fun getRuneIdList(): List<String> =
        runeIdsCsv.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
}
