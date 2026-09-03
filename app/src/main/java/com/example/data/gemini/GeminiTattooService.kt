package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.Rune
import com.example.data.model.TattooConcept
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiTattooService {

    companion object {
        private const val TAG = "GeminiTattooService"
        private const val PRIMARY_MODEL = "gemini-3.5-flash"
        private const val FALLBACK_MODEL = "gemini-2.5-flash"
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
                append("Ты — древний скандинавский скальд, рунолог и всемирно признанный мастер татуировки, ")
                append("специализирующийся на аутентичных скандинавских ставах (bindrunes), сакральной геометрии викингов, ")
                append("блэкворке, дотворке и анатомическом расположении эскизов на теле. ")
                append("Твоя задача — создать от 2 до 3 глубоких, художественно детализированных концептов татуировки ")
                append("на основе запроса пользователя. ")
                append("Ответ СТРОГО должен быть валидным JSON-массивом объектов (без внешних тегов, без markdown-блоков, просто [ ... ]).")
            }

            val runeNamesList = if (selectedRunes.isNotEmpty()) {
                selectedRunes.joinToString(", ") { "${it.nameRu} (${it.unicode})" }
            } else {
                "Любые подходящие руны Старшего Футарка (Fehu, Uruz, Thurisaz, Ansuz, Raidho, Kenaz, Gebo, Wunjo, Hagalaz, Nauthiz, Isa, Jera, Eihwaz, Perthro, Algiz, Sowilo, Tiwaz, Berkano, Ehwaz, Mannaz, Laguz, Ingwaz, Dagaz, Othala)"
            }

            val fullUserPrompt = buildString {
                append("Создай 2-3 авторских концепта скандинавской татуировки.\n")
                append("Запрос/намерение клиента: \"$userPrompt\"\n")
                if (placementPreference.isNotBlank() && placementPreference != "Любое") {
                    append("Желаемое место нанесения: $placementPreference\n")
                }
                if (stylePreference.isNotBlank() && stylePreference != "Любой") {
                    append("Желаемый художественный стиль: $stylePreference\n")
                }
                append("Выбранные руны: $runeNamesList\n\n")
                append("Для каждого концепта верни JSON-объект со следующими полями:\n")
                append("1. \"title\": Поэтичное, атмосферное название эскиза (на русском языке, например 'Северный Страж: Щит Альгиз и Тейваз')\n")
                append("2. \"runeIds\": массив строковых идентификаторов рун на английском (например [\"algiz\", \"tiwaz\", \"sowilo\"])\n")
                append("3. \"runesFormatted\": строка с руническими глифами и именами (например 'ᛉ Algiz • ᛏ Tiwaz • ᛋ Sowilo')\n")
                append("4. \"placement\": Анатомическое место на теле и почему оно выбрано (например 'Внутренняя сторона предплечья от сгиба до запястья — раскрытие при рукопожатии и защита действий')\n")
                append("5. \"style\": Название стиля (например 'Скандинавский дотворк с глубоким градиентом и каменной фактурой')\n")
                append("6. \"visualComposition\": Крайне подробное художественное описание эскиза (толщина контуров, плотность точечного градиента dotwork, переплетение центрального вязаного става bindrune, обрамляющие защитные круги с рунической вязью, отрицательное пространство, эффект золотой гравировки)\n")
                append("7. \"sacredMeaning\": Сакральное, эзотерическое значение и магический щит комбинации рун\n")
                append("8. \"masterAdvice\": Практический совет тату-мастера (номера и калибр игл, например 3RL для микродеталей и 7RM для мягкого грейвоша, особенности заживления, контраст с тоном кожи и стойкость через годы)\n")
                append("9. \"recommendedSize\": Рекомендуемый масштаб в сантиметрах (например '15 × 9 см')\n")
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
}
