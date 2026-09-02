package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.Rune
import com.example.data.model.RuneStroke
import com.example.data.model.StrokePoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

sealed class RuneLoadState {
    data object Loading : RuneLoadState()
    data class Success(val runes: List<Rune>) : RuneLoadState()
    data class Error(val message: String) : RuneLoadState()
}

class RuneRepository(private val context: Context) {

    private var cachedRunes: List<Rune>? = null

    suspend fun loadRunes(): List<Rune> = withContext(Dispatchers.IO) {
        cachedRunes?.let { return@withContext it }

        val list = mutableListOf<Rune>()
        try {
            val jsonString = context.assets.open("runes.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getString("id")
                val futhark = obj.optString("futhark", "elder")
                val nameRu = obj.getString("nameRu")
                val nameEn = obj.getString("nameEn")
                val unicode = obj.getString("unicode")
                val phonetic = obj.getString("phonetic")

                val kwArray = obj.getJSONArray("keywordsRu")
                val keywords = mutableListOf<String>()
                for (k in 0 until kwArray.length()) {
                    keywords.add(kwArray.getString(k))
                }

                val divinationDirect = obj.getString("divinationDirect")
                val divinationReversed = obj.getString("divinationReversed")
                val magicUse = obj.getString("magicUse")
                val tattooSymbolism = obj.getString("tattooSymbolism")

                val strokesArray = obj.getJSONArray("strokes")
                val strokesList = mutableListOf<RuneStroke>()
                for (s in 0 until strokesArray.length()) {
                    val pointsArray = strokesArray.getJSONArray(s)
                    val points = mutableListOf<StrokePoint>()
                    for (p in 0 until pointsArray.length()) {
                        val pt = pointsArray.getJSONArray(p)
                        val x = pt.getDouble(0).toFloat()
                        val y = pt.getDouble(1).toFloat()
                        points.add(StrokePoint(x, y))
                    }
                    if (points.isNotEmpty()) {
                        strokesList.add(RuneStroke(points))
                    }
                }

                list.add(
                    Rune(
                        id = id,
                        futhark = futhark,
                        nameRu = nameRu,
                        nameEn = nameEn,
                        unicode = unicode,
                        phonetic = phonetic,
                        keywordsRu = keywords,
                        divinationDirect = divinationDirect,
                        divinationReversed = divinationReversed,
                        magicUse = magicUse,
                        tattooSymbolism = tattooSymbolism,
                        strokes = strokesList
                    )
                )
            }
            cachedRunes = list
            Log.d("RuneRepository", "Loaded ${list.size} runes successfully")
            list
        } catch (e: Exception) {
            Log.e("RuneRepository", "Failed to parse runes.json", e)
            emptyList()
        }
    }

    fun getElderRunes(): List<Rune> = cachedRunes?.filter { it.futhark == "elder" } ?: emptyList()
    fun getYoungerRunes(): List<Rune> = cachedRunes?.filter { it.futhark == "younger" } ?: emptyList()
    fun getRuneById(id: String): Rune? = cachedRunes?.find { it.id == id }
}
