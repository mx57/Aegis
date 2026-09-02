package com.example.data.translit

import com.example.data.model.Rune

data class CharMapping(
    val char: Char,
    val runeId: String,
    val runeName: String,
    val note: String? = null
)

data class TransliterationResult(
    val originalText: String,
    val runes: List<Rune>,
    val mappings: List<CharMapping>,
    val notes: List<String>
)

object RuneTransliteration {

    /**
     * Translates input text into runes based on phonetic rules.
     * Skips soft/hard signs (ь, ъ), maps compound letters (я, ю, ё, ц, щ)
     * to dual runes or closest phonetic equivalents with explicit explanations.
     */
    fun transliterate(text: String, allRunes: List<Rune>, preferYounger: Boolean = false): TransliterationResult {
        val targetRunes = allRunes.filter { if (preferYounger) it.futhark == "younger" else it.futhark == "elder" }
        val runeMap = targetRunes.associateBy { it.id }

        val resultRunes = mutableListOf<Rune>()
        val mappings = mutableListOf<CharMapping>()
        val notes = mutableListOf<String>()

        fun findRune(id: String): Rune? = runeMap[id] ?: allRunes.find { it.id.startsWith(id) }

        var i = 0
        val upper = text.uppercase().trim()

        while (i < upper.length) {
            val c = upper[i]
            when (c) {
                ' ' -> {
                    // Space - separator, skip or insert marker
                    i++
                    continue
                }
                'Ь', 'Ъ' -> {
                    notes.add("Знак '$c' не имеет прямого рунического звука и традиционно опускается при начертании става.")
                    i++
                    continue
                }
                'А' -> {
                    val r = findRune(if (preferYounger) "ar_younger" else "ansuz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'Б' -> {
                    val r = findRune(if (preferYounger) "bjarkan_younger" else "berkano")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'В' -> {
                    val r = findRune(if (preferYounger) "ur_younger" else "wunjo")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu, "Звук [В] передан через руну радости/ветра Вуньо"))
                    }
                }
                'Г' -> {
                    val r = findRune(if (preferYounger) "kaun_younger" else "gebo")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'Д' -> {
                    val r = findRune(if (preferYounger) "tyr_younger" else "dagaz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'Е' -> {
                    val r = findRune(if (preferYounger) "iss_younger" else "ehwaz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'Ё' -> {
                    val r1 = findRune(if (preferYounger) "ar_younger" else "jera")
                    val r2 = findRune(if (preferYounger) "oss_younger" else "othala")
                    if (r1 != null && r2 != null) {
                        resultRunes.add(r1); resultRunes.add(r2)
                        mappings.add(CharMapping(c, r1.id, "${r1.nameRu} + ${r2.nameRu}", "Звук [ЙО] передан связкой Йера+Отала"))
                        notes.add("Буква 'Ё' разложена на фонетическую пару [Й + О] (Йера + Отала).")
                    } else if (r2 != null) {
                        resultRunes.add(r2)
                        mappings.add(CharMapping(c, r2.id, r2.nameRu))
                    }
                }
                'Ж' -> {
                    val r = findRune(if (preferYounger) "thurs_younger" else "algiz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu, "Шипящий [Ж] традиционно передается защитной руной Альгиз"))
                        notes.add("Звук 'Ж' отображен руной Альгиз (по схожести архаичного северного звука z/R).")
                    }
                }
                'З' -> {
                    val r = findRune(if (preferYounger) "sol_younger" else "algiz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'И', 'Й' -> {
                    val r = if (c == 'Й') {
                        findRune(if (preferYounger) "ar_younger" else "jera") ?: findRune("isa")
                    } else {
                        findRune(if (preferYounger) "iss_younger" else "isa")
                    }
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'К' -> {
                    val r = findRune(if (preferYounger) "kaun_younger" else "kenaz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'Л' -> {
                    val r = findRune(if (preferYounger) "logr_younger" else "laguz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'М' -> {
                    val r = findRune(if (preferYounger) "madr_younger" else "mannaz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'Н' -> {
                    val r = findRune(if (preferYounger) "naudr_younger" else "nauthiz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'О' -> {
                    val r = findRune(if (preferYounger) "oss_younger" else "othala")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'П' -> {
                    val r = findRune(if (preferYounger) "bjarkan_younger" else "perthro")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'Р' -> {
                    val r = findRune(if (preferYounger) "reid_younger" else "raidho")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'С' -> {
                    val r = findRune(if (preferYounger) "sol_younger" else "sowilo")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'Т' -> {
                    val r = findRune(if (preferYounger) "tyr_younger" else "tiwaz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'У' -> {
                    val r = findRune(if (preferYounger) "ur_younger" else "uruz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'Ф' -> {
                    val r = findRune(if (preferYounger) "fe_younger" else "fehu")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'Х' -> {
                    val r = findRune(if (preferYounger) "hagall_younger" else "hagalaz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'Ц' -> {
                    val r1 = findRune(if (preferYounger) "tyr_younger" else "tiwaz")
                    val r2 = findRune(if (preferYounger) "sol_younger" else "sowilo")
                    if (r1 != null && r2 != null) {
                        resultRunes.add(r1); resultRunes.add(r2)
                        mappings.add(CharMapping(c, r1.id, "${r1.nameRu} + ${r2.nameRu}", "Звук [Ц] записан как [Т + С]"))
                        notes.add("Буква 'Ц' не имеет прямой руны и передана классическим сочетанием Тейваз + Соуло [Т+С].")
                    }
                }
                'Ч' -> {
                    val r = findRune(if (preferYounger) "kaun_younger" else "kenaz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu, "Глухой мягкий звук передан руной Кеназ"))
                        notes.add("Звук 'Ч' передан руной Кеназ (звонкий факел сознания).")
                    }
                }
                'Ш' -> {
                    val r = findRune(if (preferYounger) "sol_younger" else "sowilo")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu, "Шипящий [Ш] выражен солнечной Соуло"))
                    }
                }
                'Щ' -> {
                    val r1 = findRune(if (preferYounger) "sol_younger" else "sowilo")
                    val r2 = findRune(if (preferYounger) "kaun_younger" else "kenaz")
                    if (r1 != null && r2 != null) {
                        resultRunes.add(r1); resultRunes.add(r2)
                        mappings.add(CharMapping(c, r1.id, "${r1.nameRu} + ${r2.nameRu}", "Звук [Щ] передан парой [С + К/Ч]"))
                        notes.add("Сложный звук 'Щ' передан связкой Соуло + Кеназ.")
                    }
                }
                'Ы' -> {
                    val r = findRune(if (preferYounger) "iss_younger" else "isa")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu, "Гласный [Ы] отождествлен с руной Иса"))
                    }
                }
                'Э' -> {
                    val r = findRune(if (preferYounger) "iss_younger" else "ehwaz")
                    if (r != null) {
                        resultRunes.add(r)
                        mappings.add(CharMapping(c, r.id, r.nameRu))
                    }
                }
                'Ю' -> {
                    val r1 = findRune(if (preferYounger) "ar_younger" else "jera")
                    val r2 = findRune(if (preferYounger) "ur_younger" else "uruz")
                    if (r1 != null && r2 != null) {
                        resultRunes.add(r1); resultRunes.add(r2)
                        mappings.add(CharMapping(c, r1.id, "${r1.nameRu} + ${r2.nameRu}", "Звук [Ю] передан парой Йера + Уруз [Й+У]"))
                        notes.add("Буква 'Ю' представлена как дифтонг [Й + У] (Йера + Уруз).")
                    }
                }
                'Я' -> {
                    val r1 = findRune(if (preferYounger) "ar_younger" else "jera")
                    val r2 = findRune(if (preferYounger) "ar_younger" else "ansuz")
                    if (r1 != null && r2 != null) {
                        resultRunes.add(r1); resultRunes.add(r2)
                        mappings.add(CharMapping(c, r1.id, "${r1.nameRu} + ${r2.nameRu}", "Звук [Я] передан парой Йера + Ансуз [Й+А]"))
                        notes.add("Буква 'Я' представлена как дифтонг [Й + А] (Йера + Ансуз).")
                    }
                }
                else -> {
                    // Latin fallback or unmapped
                    val latinMatch = targetRunes.find { it.phonetic.equals(c.toString(), ignoreCase = true) }
                    if (latinMatch != null) {
                        resultRunes.add(latinMatch)
                        mappings.add(CharMapping(c, latinMatch.id, latinMatch.nameRu))
                    }
                }
            }
            i++
        }

        return TransliterationResult(
            originalText = text,
            runes = resultRunes,
            mappings = mappings,
            notes = notes
        )
    }
}
