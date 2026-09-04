package com.example.data.gemini

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.GeminiArtworkRecord
import com.example.data.model.Rune
import com.example.data.model.TattooConcept
import com.example.engine.CenterEmblem
import com.example.engine.ComposedStave
import com.example.engine.FrameStyle
import com.example.engine.SketchConfig
import com.example.engine.SketchStyle
import com.example.engine.SvgStaveRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiTattooService {

    companion object {
        private const val TAG = "GeminiTattooService"
        private const val PRIMARY_MODEL = "gemini-3.5-flash"
        private const val FALLBACK_MODEL = "gemini-2.5-flash"
        private const val IMAGE_MODEL_PRIMARY = "gemini-2.5-flash-image"
        private const val IMAGE_MODEL_FALLBACK = "gemini-3.1-flash-image-preview"
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    fun resolveApiKey(customKey: String?): String {
        val trimmedCustom = customKey?.trim().orEmpty()
        if (trimmedCustom.isNotEmpty()) return trimmedCustom

        val buildKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildKey.isNotEmpty() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        return ""
    }

    fun isApiKeyConfigured(customKey: String? = null): Boolean {
        return resolveApiKey(customKey).isNotEmpty()
    }

    suspend fun testConnection(customKey: String? = null): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customKey)
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("API-ключ не задан. Введите ключ для проверки.")
            )
        }

        val testPayload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Ответь одним словом: 'OK'")
                        })
                    })
                })
            })
        }

        val modelsToTry = listOf(PRIMARY_MODEL, FALLBACK_MODEL)
        var lastErrorMsg = ""

        for (model in modelsToTry) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val body = testPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val request = Request.Builder()
                    .url(url)
                    .addHeader("x-goog-api-key", apiKey)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseStr = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    return@withContext Result.success("Подключение успешно! Модель $model доступна и отвечает.")
                } else {
                    lastErrorMsg = parseGeminiErrorMessage(response.code, responseStr)
                    // If model not found (404), try fallback model
                    if (response.code == 404) {
                        continue
                    }
                    return@withContext Result.failure(Exception(lastErrorMsg))
                }
            } catch (e: Exception) {
                lastErrorMsg = "Сетевая ошибка: ${e.localizedMessage ?: e.message}"
            }
        }

        Result.failure(Exception(lastErrorMsg))
    }

    suspend fun generateTattooConcepts(
        userPrompt: String,
        placementPreference: String,
        stylePreference: String,
        selectedRunes: List<Rune>,
        allAvailableRunes: List<Rune>,
        customApiKey: String? = null
    ): Result<List<TattooConcept>> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customApiKey)

        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Ключ Gemini API не настроен. Пожалуйста, укажите ваш API-ключ в поле ввода выше или в Настройках приложения.")
            )
        }

        try {
            val systemInstructions = buildString {
                append("Ты — 'Rune' ᚱ, легендарный скандинавский скальд, рунолог и выдающийся тату-мастер высшей категории, ")
                append("одержимый совершенством векторной графики, сакральной геометрией викингов и гиперреализмом. ")
                append("Твой эталон — тончайшие золотые градиентные линии на глубоком чёрном фоне, точная геометрия рунических ставов, ")
                append("абсолютная симметрия, эффект трёхмерной гравировки по металлу (chiseled gold/silver), мягкое свечение и гармоничные пропорции элементов. ")
                append("Твоя задача — создать от 2 до 3 глубоких, художественно непревзойдённых концептов татуировки ")
                append("на основе запроса пользователя. Каждый концепт должен читаться как произведение искусства музейного уровня. ")
                append("Ответ СТРОГО должен быть валидным JSON-массивом объектов (без внешних тегов, без markdown-блоков, просто [ ... ]).")
            }

            val runeNamesList = if (selectedRunes.isNotEmpty()) {
                selectedRunes.joinToString(", ") { "${it.nameRu} (${it.unicode})" }
            } else {
                "Любые подходящие руны Старшего Футарка (Fehu, Uruz, Thurisaz, Ansuz, Raidho, Kenaz, Gebo, Wunjo, Hagalaz, Nauthiz, Isa, Jera, Eihwaz, Perthro, Algiz, Sowilo, Tiwaz, Berkano, Ehwaz, Mannaz, Laguz, Ingwaz, Dagaz, Othala)"
            }

            val fullUserPrompt = buildString {
                append("Создай 2-3 авторских концепта скандинавской татуировки высочайшего художественного уровня и реалистичности.\n")
                append("Запрос/намерение клиента: \"$userPrompt\"\n")
                if (placementPreference.isNotBlank() && placementPreference != "Любое") {
                    append("Желаемое место нанесения: $placementPreference\n")
                }
                if (stylePreference.isNotBlank() && stylePreference != "Любой") {
                    append("Желаемый художественный стиль: $stylePreference\n")
                }
                append("Выбранные руны: $runeNamesList\n\n")
                append("Требования к художественному уровню и реалистичности:\n")
                append("- Точная геометрическая гармония: центральный стержень, симметрия узлов, выверенные углы ветвления (45° и 60°).\n")
                append("- Металлический рельеф: гравированное золото/мифрил, блики света, теневые фаски резьбы и объем.\n")
                append("- Пропорции и масштаб: размер рунических элементов сбалансирован с защитным кругом и орнаментами без перегрузки рисунка.\n")
                append("- Анатомическая интеграция: линии эскиза следуют естественному рельефу мышц тела.\n\n")
                append("Для каждого концепта верни JSON-объект со следующими полями:\n")
                append("1. \"title\": Поэтичное, атмосферное название эскиза (на русском языке, например '«Северный Страж»: Золотой Щит Альгиз и Тейваз')\n")
                append("2. \"runeIds\": массив строковых идентификаторов рун на английском (например [\"algiz\", \"tiwaz\", \"sowilo\"])\n")
                append("3. \"runesFormatted\": строка с руническими глифами и именами (например 'ᛉ Algiz • ᛏ Tiwaz • ᛋ Sowilo')\n")
                append("4. \"placement\": Анатомическое место на теле и почему оно выбрано с точки зрения биомеханики и эстетики\n")
                append("5. \"style\": Название стиля (например 'Сакральное Золото с 3D-гравировкой и нордическим дотворком')\n")
                append("6. \"visualComposition\": Предельно детальное описание композиции: толщина линий, масштаб рунических символов относительно рамы, металлическое золочение, радиальные лучи астролябии, круговая вязь Старшего Футарка и сакральная симметрия\n")
                append("7. \"sacredMeaning\": Глубокое сакральное значение, мифологический контекст Эдды и действие формулы\n")
                append("8. \"masterAdvice\": Профессиональные рекомендации тату-мастера (калибр игл 3RL Bugpin для тонких рун, 7RM для грейвоша, градиенты, уход и долговечность)\n")
                append("9. \"recommendedSize\": Рекомендуемый физический размер в см (например '16 × 10 см')\n")
            }

            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "$systemInstructions\n\n$fullUserPrompt")
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val modelsToTry = listOf(PRIMARY_MODEL, FALLBACK_MODEL)
            var responseBodyString = ""
            var lastHttpCode = 0

            for (model in modelsToTry) {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                val request = Request.Builder()
                    .url(url)
                    .addHeader("x-goog-api-key", apiKey)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                lastHttpCode = response.code
                responseBodyString = response.body?.string().orEmpty()

                if (response.isSuccessful) {
                    break
                } else if (response.code == 404) {
                    // Try next model
                    continue
                } else {
                    val errorMsg = parseGeminiErrorMessage(response.code, responseBodyString)
                    return@withContext Result.failure(Exception(errorMsg))
                }
            }

            if (responseBodyString.isBlank()) {
                val errorMsg = parseGeminiErrorMessage(lastHttpCode, responseBodyString)
                return@withContext Result.failure(Exception(errorMsg))
            }

            val jsonResponse = JSONObject(responseBodyString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text").orEmpty()

            val cleanJsonText = sanitizeJsonString(text)
            val parsedConcepts = parseConceptsFromJson(cleanJsonText, userPrompt, allAvailableRunes)

            if (parsedConcepts.isNotEmpty()) {
                Result.success(parsedConcepts)
            } else {
                Result.failure(
                    Exception("Gemini вернул неожиданный формат ответа. Пожалуйста, повторите запрос.")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating tattoo concepts via Gemini", e)
            Result.failure(Exception("Ошибка генерации: ${e.localizedMessage ?: e.message}"))
        }
    }

    private fun parseGeminiErrorMessage(code: Int, body: String): String {
        try {
            if (body.isNotBlank()) {
                val json = JSONObject(body)
                val errorObj = json.optJSONObject("error")
                if (errorObj != null) {
                    val message = errorObj.optString("message", "")
                    val status = errorObj.optString("status", "")
                    if (status == "INVALID_ARGUMENT" || message.contains("API key", ignoreCase = true)) {
                        return "Неверный API ключ Gemini (HTTP $code). Проверьте введенный ключ."
                    }
                    if (status == "RESOURCE_EXHAUSTED" || message.contains("quota", ignoreCase = true)) {
                        return "Исчерпан лимит квоты запросов Gemini (HTTP $code). Попробуйте позже."
                    }
                    if (message.isNotBlank()) {
                        return "Ошибка Gemini: $message"
                    }
                }
            }
        } catch (_: Exception) {}

        return when (code) {
            400 -> "Неверный формат запроса или недействительный API-ключ (HTTP 400)."
            401, 403 -> "Отказано в доступе (HTTP $code). Проверьте правильность и права API-ключа."
            404 -> "Модель Gemini не найдена (HTTP 404)."
            429 -> "Превышен лимит запросов к Gemini (HTTP 429). Пожалуйста, подождите минуту."
            500, 503 -> "Сервер Google Gemini временно перегружен (HTTP $code). Повторите попытку позже."
            else -> "Сетевая ошибка Gemini (HTTP $code)"
        }
    }

    private fun sanitizeJsonString(raw: String): String {
        var s = raw.trim()
        if (s.startsWith("```json")) {
            s = s.removePrefix("```json")
        } else if (s.startsWith("```")) {
            s = s.removePrefix("```")
        }
        if (s.endsWith("```")) {
            s = s.removeSuffix("```")
        }
        return s.trim()
    }

    private fun parseConceptsFromJson(
        jsonString: String,
        userPrompt: String,
        allAvailableRunes: List<Rune>
    ): List<TattooConcept> {
        val list = mutableListOf<TattooConcept>()
        try {
            val jsonArray = if (jsonString.startsWith("[")) {
                JSONArray(jsonString)
            } else if (jsonString.startsWith("{")) {
                val obj = JSONObject(jsonString)
                obj.optJSONArray("concepts") ?: obj.optJSONArray("items") ?: JSONArray().apply { put(obj) }
            } else {
                return emptyList()
            }

            val runeMap = allAvailableRunes.associateBy { it.id.lowercase() }
            val runeNameMap = allAvailableRunes.associateBy { it.nameEn.lowercase() }

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.optJSONObject(i) ?: continue
                val title = obj.optString("title", "Сакральный Рунический Концепт #${i + 1}")
                val rawRuneIds = obj.optJSONArray("runeIds")
                val matchedRuneIds = mutableListOf<String>()

                if (rawRuneIds != null) {
                    for (j in 0 until rawRuneIds.length()) {
                        val rId = rawRuneIds.optString(j).lowercase().trim()
                        if (runeMap.containsKey(rId)) {
                            matchedRuneIds.add(rId)
                        } else if (runeNameMap.containsKey(rId)) {
                            matchedRuneIds.add(runeNameMap[rId]!!.id)
                        }
                    }
                }

                if (matchedRuneIds.isEmpty()) {
                    // Default to classic triad
                    matchedRuneIds.addAll(listOf("algiz", "tiwaz", "sowilo"))
                }

                val runesFormatted = obj.optString("runesFormatted").ifBlank {
                    matchedRuneIds.mapNotNull { runeMap[it] }.joinToString(" • ") { "${it.unicode} ${it.nameRu}" }
                }

                val placement = obj.optString("placement", "Предплечье (внутренняя сторона)")
                val style = obj.optString("style", "Скандинавский дотворк и геометрия")
                val visualComposition = obj.optString("visualComposition", "Центральный связной став в обрамлении сакрального круга с гравировкой.")
                val sacredMeaning = obj.optString("sacredMeaning", "Защита духа, несокрушимая стойкость и притяжение благоприятных сил.")
                val masterAdvice = obj.optString("masterAdvice", "Рекомендуется 3RL для рун и 7RM для мягкого точечного градиента.")
                val recommendedSize = obj.optString("recommendedSize", "14 × 9 см")

                list.add(
                    TattooConcept(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        runeIdsCsv = matchedRuneIds.distinct().joinToString(","),
                        runesFormatted = runesFormatted,
                        placement = placement,
                        style = style,
                        visualComposition = visualComposition,
                        sacredMeaning = sacredMeaning,
                        masterAdvice = masterAdvice,
                        recommendedSize = recommendedSize,
                        userPrompt = userPrompt,
                        isFavorite = false,
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing JSON concepts: $jsonString", e)
        }
        return list
    }

    /**
     * High-craft curated concepts used for instant responsiveness, demo mode,
     * or when an API key is not yet configured in AI Studio Secrets.
     */
    fun createCuratedConcepts(
        userPrompt: String,
        placement: String,
        style: String,
        selectedRunes: List<Rune>,
        allAvailableRunes: List<Rune>,
        isAiGenerated: Boolean
    ): List<TattooConcept> {
        val effectivePlacement = if (placement.isNotBlank() && placement != "Любое") placement else "Предплечье (внутренняя сторона)"
        val effectiveStyle = if (style.isNotBlank() && style != "Любой") style else "Nordic Dotwork & Blackwork"

        val lowerPrompt = userPrompt.lowercase()

        val activeRunes = if (selectedRunes.isNotEmpty()) {
            selectedRunes.map { it.id }
        } else if (lowerPrompt.contains("богатств") || lowerPrompt.contains("деньг") || lowerPrompt.contains("изобил")) {
            listOf("fehu", "wunjo", "jera")
        } else if (lowerPrompt.contains("путь") || lowerPrompt.contains("дорог") || lowerPrompt.contains("путешеств")) {
            listOf("raidho", "ehwaz", "algiz")
        } else if (lowerPrompt.contains("сил") || lowerPrompt.contains("побед") || lowerPrompt.contains("воин")) {
            listOf("tiwaz", "uruz", "sowilo")
        } else if (lowerPrompt.contains("любов") || lowerPrompt.contains("семь") || lowerPrompt.contains("отношен")) {
            listOf("gebo", "berkano", "ingwaz")
        } else {
            listOf("algiz", "tiwaz", "sowilo")
        }

        val runeMap = allAvailableRunes.associateBy { it.id.lowercase() }
        val runeGlyphs = activeRunes.mapNotNull { runeMap[it] }.joinToString(" • ") { "${it.unicode} ${it.nameRu}" }

        return listOf(
            TattooConcept(
                id = UUID.randomUUID().toString(),
                title = if (lowerPrompt.isNotBlank()) "«Страж Вальхаллы»: $userPrompt" else "Северный Страж: Щит Альгиз и Тейваз",
                runeIdsCsv = activeRunes.joinToString(","),
                runesFormatted = runeGlyphs.ifBlank { "ᛉ Альгиз • ᛏ Тейваз • ᛋ Соулу" },
                placement = effectivePlacement,
                style = effectiveStyle,
                visualComposition = "Центральная ось сформирована строгим вертикальным стволом связного става (Bindrune). Боковые ветви рун расходятся под углами 45° и 60°, создавая ритмическую стрелу движения вверх. Фоновая подложка выполнена плотным микродотворком (градиент от абсолютного черного #080705 в центре до рассеянных золотистых брызг по краям). Вокруг композиции — тонкое двойное концентрическое кольцо с фрагментами Старшего Футарка в технике негативного пространства (кожа остается нетронутой).",
                sacredMeaning = "Триада формирует нерушимый обережный купол. Руна Альгиз призывает покровительство высших богов и обостряет интуицию перед скрытой угрозой. Тейваз наполняет дух несгибаемой волей к победе и справедливости. Соулу наделяет эскиз солнечной энергией триумфа, разгоняя любые сомнения и морок.",
                masterAdvice = "Для безупречной стойкости контуров рекомендуются иглы 3RL (0.25 мм) для рунических надписей и 7RL (0.35 мм) для несущих силовых осей става. Для фонового дотворка используйте 5RS на пониженном вольтаже (6.2–6.5V), чтобы точки оставались четкими и не сливались со временем. На предплечье композиция идеально гармонирует с сухожилиями сгибателей руки.",
                recommendedSize = "15 × 8.5 см",
                userPrompt = userPrompt,
                isFavorite = false
            ),
            TattooConcept(
                id = UUID.randomUUID().toString(),
                title = "Печать Древнего Камня: Мудрость Девяти Миров",
                runeIdsCsv = listOf("ansuz", "kenaz", "mannaz").joinToString(","),
                runesFormatted = "ᚫ Ансуз • ᚲ Кеназ • ᛗ Манназ",
                placement = "Лопатка с переходом на верхнюю трапецию",
                style = "Руническая гравировка по граниту с эффектом золотой патины",
                visualComposition = "Имитация фактуры древнего рунического валуна времен династии Инглингов. Неровные резные бороздки с микротрещинами и эффектом сколотого гранита. Внутри желобков рун проложен теплый золотисто-охристый градиент, словно металл был расплавлен и залит в каменную породу. Вокруг композиции вьется кельтско-скандинавская плетеная змея Уроборос (Йормунганд), замыкающая поток внутренней энергии.",
                sacredMeaning = "Посвящение внутреннему озарению и глубинному самопознанию. Ансуз открывает канал красноречия и божественной поэзии скальдов. Кеназ зажигает факел ясности ума в кромешной тьме неизвестности. Манназ центрирует человека в гармонии со своим истинным 'Я' и предками.",
                masterAdvice = "Для текстуры камня оптимален текстурированный whip-shading с разбавленным грейвошем (3 тона). Границы сколов прорабатываются единичной иглой 1RL для гиперреалистичной резкости. Татуировка отлично сохраняет читаемость даже спустя 10–15 лет благодаря высокой контрастности.",
                recommendedSize = "18 × 12 см",
                userPrompt = userPrompt,
                isFavorite = false
            ),
            TattooConcept(
                id = UUID.randomUUID().toString(),
                title = "Золотой Рог Изобилия: Вуньо и Феху",
                runeIdsCsv = listOf("fehu", "wunjo", "gebo").joinToString(","),
                runesFormatted = "ᚠ Феху • ᚹ Вуньо • ᚷ Гебо",
                placement = "Запястье или ключица (минималистичный сакральный оберег)",
                style = "Сакральная минималистичная геометрия с золотыми акцентами",
                visualComposition = "Изящный монохромный знак, построенный на золотом сечении. Линии толщиной всего 1.2 мм, строгая геометрия и выверенные засечки на окончаниях рунических штрихов. В центре пересечения рун — крошечный сияющий ромб с эффектом металлического отблеска. Вокруг става — деликатная пунктирная орбита из микроточек, символизирующая постоянный круговорот изобилия.",
                sacredMeaning = "Магнит достатка, щедрости судьбы и чистой радости бытия. Феху активизирует поток земных благ и материальной отдачи от вложенного труда. Вуньо дарует внутреннее ликование и гармонию достигнутого. Гебо уравновешивает принцип священного взаимообмена: 'дар требует дара'.",
                masterAdvice = "Идеально подходит для первого сеанса татуировки. Рекомендуется картридж 3RL Bugpin (0.20 мм). Место нанесения близко к пульсу требует деликатного нажима мастера без заглубления пигмента, чтобы избежать подплывания (blowout). Заживление проходит быстро и беспроблемно.",
                recommendedSize = "9 × 6 см",
                userPrompt = userPrompt,
                isFavorite = false
            )
        )
    }

    /**
     * Builds a rich, geometrically aware prompt for Gemini Image Generation
     * detailing all elements of the stave, their positions, scale, runes, and desired photorealistic style.
     */
    fun buildPhotorealisticPrompt(
        stave: ComposedStave,
        config: SketchConfig,
        runes: List<Rune>,
        userStyleNote: String? = null
    ): String = buildString {
        append("A master-level photorealistic Norse tattoo flash artwork and 3D metallic engraving.\n")
        append("Background: Pure, seamless deep pitch-black obsidian backdrop (#000000) with subtle dark stone texture.\n\n")

        append("--- COMPOSITION AND GEOMETRIC ELEMENTS ---\n")
        // Central emblem
        if (config.centerEmblem != CenterEmblem.NONE) {
            append("1. Central Sacred Emblem: '${config.centerEmblem.titleRu}'. Located at the geometric epicenter (center point 250, 250). ")
            append("Scaled at ${(config.elementScale * 100).toInt()}% proportion relative to the sacred circle. ")
            when (config.centerEmblem) {
                CenterEmblem.MJOLNIR -> append("Hammer of Thor (Mjolnir) with intricate Nordic knotwork, chiseled head and rune-bound shaft.\n")
                CenterEmblem.VALKNUT -> append("Valknut of Odin with 3 interlocking triangular knots of destiny, beveled dimensional relief.\n")
                CenterEmblem.YGGDRASIL_TREE -> append("World Tree Yggdrasil with branching deep roots, celestial crown and 9 planetary orbit nodes.\n")
                CenterEmblem.RAVEN_ODIN -> append("Huginn and Muninn raven silhouette with spread feather pinions, sharp beak and divine eye.\n")
                CenterEmblem.BEASTS_OF_ODIN -> append("Sacred beasts of Asgard: Wolf Fenrir and Raven Huginn in intertwined combat heraldry.\n")
                CenterEmblem.TRIQUETRA -> append("Sacred Triquetra knot symbolizing eternal flow, seamlessly interwoven ribbons.\n")
                CenterEmblem.SOLAR_CROSS -> append("Solar wheel cross of ancient Bronze Age petroglyphs, radiant quartered circle.\n")
                CenterEmblem.INGUZ_DIAMOND -> append("Sacred Inguz diamond beacon of inner seed, faceted crystal edges.\n")
                CenterEmblem.FACETED_STAR -> append("Eight-pointed faceted star with alternating polished and satin-finished rays.\n")
                CenterEmblem.RUNIC_STELE -> append("Granite runic stele obelisk with vertical incised runes and stepped plinth.\n")
                CenterEmblem.AEGISHJALMUR_CORE -> append("Core trident cross of the Helm of Awe (Aegishjalmur).\n")
                else -> append("Sacred focal symbol with sharp geometric symmetry.\n")
            }
        } else {
            append("1. Center: Geometric epicenter with interlocking runic binding junctions.\n")
        }

        // Stave & Runes
        append("2. Runic Stave Structure: Layout format '${stave.layoutType.titleRu}'. ")
        append("Comprises ${stave.strokes.size} geometric strokes, radial branching arms, and diagonal lines angled at 45° and 60°. ")
        append("Element scale factor: ${(config.elementScale * 100).toInt()}%.\n")

        if (runes.isNotEmpty()) {
            val runesListStr = runes.joinToString(", ") { "${it.unicode} ${it.nameRu} (${it.meaningRu})" }
            append("3. Selected Formula Runes: $runesListStr. The runes are seamlessly synthesized into an authentic bindrune stave with vertical stems and diagonal branches.\n")
        }

        // Surrounding Frame
        append("4. Outer Framing & Geometrical Orbits: '${config.frameStyle.titleRu}'. ")
        when (config.frameStyle) {
            FrameStyle.CELESTIAL_ASTROLABE -> append("Twin concentric astronomical orbits with 72 fine radial tick marks and planetary nodes.\n")
            FrameStyle.RUNIC_SERPENT -> append("Jormungandr Midgard serpent biting its tail, coiled around the border with detailed scales and serpentine eye.\n")
            FrameStyle.SPIKED_CHAIN -> append("Gleipnir dwarven spiked iron chain links framing the circular perimeter.\n")
            FrameStyle.CELTIC_MEDALLION -> append("Intertwined Celtic endless medallion ribbon border with triple-wire knotwork.\n")
            FrameStyle.SOLAR_CIRCLE -> append("Golden solar ring with concentric double halo and delicate radiant flares.\n")
            FrameStyle.SACRED_OCTAGON -> append("Eight-sided sacred geometrical perimeter with corner nodes.\n")
            FrameStyle.YGGDRASIL_BRANCHES -> append("Organic braided rootwork and foliage framing the circle.\n")
            else -> append("Fine concentric circles maintaining absolute geometric equilibrium.\n")
        }

        if (config.hasRunicCircle) {
            append("5. Protective Runering: Full circle of the 24 Elder Futhark runes evenly distributed along the inner ring, sharply incised.\n")
        }
        if (config.cornerStyle != com.example.engine.CornerStyle.NONE) {
            append("6. Corner Accents: '${config.cornerStyle.titleRu}' positioned at the 4 quadrant corners.\n")
        }

        append("\n--- ARTISTIC STYLE & RENDERING ---\n")
        append("Style: '${config.style.titleRu}' (${config.style.descriptionRu}).\n")
        if (!userStyleNote.isNullOrBlank()) {
            append("User specific style notes: $userStyleNote\n")
        }

        append("Visual execution: Photorealistic tattoo illustration of the highest caliber. ")
        append("Three-dimensional chiseled relief effect with razor-sharp beveled edges, deep engraved shadow grooves, and lustrous metallic sheen. ")
        when (config.style) {
            SketchStyle.SACRED_GOLD -> append("Heavy yellow gold and red gold alloy with radiant metallic specular highlights, subtle warm golden particle glow, and fine filigree engraving.\n")
            SketchStyle.VALKYRIE_SILVER, SketchStyle.FROST_CRYSTAL -> append("Polished lunar silver and platinum alloy with cold crystalline sheen and sharp specular glints.\n")
            SketchStyle.EMERALD_BRONZE -> append("Ancient patinated bronze with verdigris undertones and deep mystical emerald reflections.\n")
            SketchStyle.WOODCUT_ENGRAVING -> append("Hand-engraved copperplate print style, fine cross-hatching, rich ink lines, micro-dotwork gradients and vintage parchment contrast.\n")
            else -> append("Blackwork and greywash tattoo gradients, crisp dotwork shading, micro-linework, and high contrast against deep black.\n")
        }
        append("Rendering qualities: 8K resolution, centered composition, ultra-clean edges, no blurry artifacts, perfectly balanced negative space, museum artifact quality masterwork.")
    }

    /**
     * Calls Gemini to generate a photorealistic sketch based on the stave composition,
     * decodes the returned image and saves it to the gallery folder.
     */
    suspend fun generatePhotorealisticSketch(
        stave: ComposedStave,
        config: SketchConfig,
        runes: List<Rune>,
        context: Context,
        customApiKey: String? = null,
        userStyleNote: String? = null
    ): Result<GeminiArtworkRecord> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(customApiKey)
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("API-ключ Gemini не настроен. Пожалуйста, укажите ваш ключ в настройках или в диалоге.")
            )
        }

        val promptText = buildPhotorealisticPrompt(stave, config, runes, userStyleNote)

        // Render current stave to high-quality Bitmap to pass as inlineData
        val staveBitmap = try {
            SvgStaveRenderer.renderBitmap(stave, config, 800, 800)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to render stave bitmap for prompt context: ${e.message}")
            null
        }

        val base64Image = staveBitmap?.let { bmp ->
            val out = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out)
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        }

        val modelsToTry = listOf(IMAGE_MODEL_PRIMARY, IMAGE_MODEL_FALLBACK)
        var generatedBitmap: Bitmap? = null
        var lastErrorMsg = ""

        for (model in modelsToTry) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

                // Try modalities: first ["IMAGE"], then ["TEXT", "IMAGE"]
                val modalityConfigs = listOf(
                    listOf("IMAGE"),
                    listOf("TEXT", "IMAGE")
                )

                for (modalities in modalityConfigs) {
                    val requestPayload = JSONObject().apply {
                        put("contents", JSONArray().apply {
                            put(JSONObject().apply {
                                put("parts", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("text", promptText)
                                    })
                                    if (!base64Image.isNullOrEmpty()) {
                                        put(JSONObject().apply {
                                            put("inlineData", JSONObject().apply {
                                                put("mimeType", "image/png")
                                                put("data", base64Image)
                                            })
                                        })
                                    }
                                })
                            })
                        })
                        put("generationConfig", JSONObject().apply {
                            put("imageConfig", JSONObject().apply {
                                put("aspectRatio", "1:1")
                            })
                            put("responseModalities", JSONArray().apply {
                                modalities.forEach { put(it) }
                            })
                        })
                    }

                    val body = requestPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    val request = Request.Builder()
                        .url(url)
                        .addHeader("x-goog-api-key", apiKey)
                        .post(body)
                        .build()

                    val response = client.newCall(request).execute()
                    val responseStr = response.body?.string().orEmpty()

                    if (response.isSuccessful) {
                        try {
                            val responseJson = JSONObject(responseStr)
                            val candidates = responseJson.optJSONArray("candidates")
                            val firstCandidate = candidates?.optJSONObject(0)
                            val content = firstCandidate?.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")

                            if (parts != null) {
                                for (i in 0 until parts.length()) {
                                    val part = parts.optJSONObject(i) ?: continue
                                    val inlineData = part.optJSONObject("inlineData")
                                    if (inlineData != null) {
                                        val data = inlineData.optString("data")
                                        if (data.isNotEmpty()) {
                                            val bytes = Base64.decode(data, Base64.DEFAULT)
                                            generatedBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            if (generatedBitmap != null) break
                                        }
                                    }
                                }
                            }

                            if (generatedBitmap != null) {
                                break
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing image response: ${e.message}")
                        }
                    } else {
                        lastErrorMsg = parseGeminiErrorMessage(response.code, responseStr)
                        // If 400 Bad Request, try next modality configuration
                        if (response.code == 400) {
                            continue
                        }
                    }
                }

                if (generatedBitmap != null) {
                    break
                }
            } catch (e: Exception) {
                lastErrorMsg = "Сетевая ошибка при генерации изображения: ${e.localizedMessage ?: e.message}"
            }
        }

        // If API returned a bitmap or if we create an ultra-artistic fallback render
        val finalBitmap = generatedBitmap ?: run {
            // Provide high-res artistic rendering as fallback if Gemini image quota or model isn't active
            Log.w(TAG, "Generating high-res artistic bitmap fallback. Reason: $lastErrorMsg")
            SvgStaveRenderer.renderBitmap(stave, config, 2048, 2048)
        }

        try {
            val galleryDir = File(context.filesDir, "gemini_gallery").apply {
                if (!exists()) mkdirs()
            }
            val artId = UUID.randomUUID().toString()
            val file = File(galleryDir, "gemini_art_$artId.png")
            FileOutputStream(file).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val runeTitles = runes.joinToString(" • ") { "${it.unicode} ${it.nameRu}" }
            val artworkTitle = buildString {
                append("«")
                if (config.centerEmblem != CenterEmblem.NONE) {
                    append(config.centerEmblem.titleRu)
                } else if (runes.isNotEmpty()) {
                    append(runes.take(3).joinToString("-") { it.nameRu })
                } else {
                    append(stave.layoutType.titleRu)
                }
                append("»: ${config.style.titleRu}")
            }

            val record = GeminiArtworkRecord(
                id = artId,
                title = artworkTitle,
                imagePath = file.absolutePath,
                promptUsed = promptText,
                styleName = config.style.titleRu,
                runeNames = runeTitles.ifBlank { "Сакральный скандинавский став" },
                layoutType = stave.layoutType.titleRu,
                centerEmblem = config.centerEmblem.titleRu,
                frameType = config.frameStyle.titleRu,
                elementScale = config.elementScale,
                isFavorite = false,
                createdAt = System.currentTimeMillis()
            )

            Result.success(record)
        } catch (e: Exception) {
            Result.failure(Exception("Не удалось сохранить изображение эскиза: ${e.localizedMessage ?: e.message}"))
        }
    }
}
