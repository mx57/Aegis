package com.example.data.model

data class StrokePoint(val x: Float, val y: Float)

data class RuneStroke(val points: List<StrokePoint>)

data class Rune(
    val id: String,
    val futhark: String, // "elder" or "younger"
    val nameRu: String,
    val nameEn: String,
    val unicode: String,
    val phonetic: String,
    val keywordsRu: List<String>,
    val divinationDirect: String,
    val divinationReversed: String,
    val magicUse: String,
    val tattooSymbolism: String,
    val strokes: List<RuneStroke>
) {
    val isElder: Boolean get() = futhark == "elder"
}
