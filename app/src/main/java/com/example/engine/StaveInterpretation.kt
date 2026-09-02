package com.example.engine

import com.example.data.model.Rune

data class RuneRole(
    val runeName: String,
    val roleTitle: String,
    val contextMeaning: String
)

data class StaveInterpretationData(
    val title: String,
    val summary: String,
    val runeRoles: List<RuneRole>,
    val strokeOrderAdvice: String,
    val activationSteps: List<String>,
    val carryingAdvice: String,
    val deactivationAdvice: String,
    val disclaimer: String
)

object StaveInterpretation {

    fun generate(runes: List<Rune>, layoutType: StaveLayoutType): StaveInterpretationData {
        if (runes.isEmpty()) {
            return StaveInterpretationData(
                title = "Пустой став",
                summary = "Выберите или введите руны для составления става.",
                runeRoles = emptyList(),
                strokeOrderAdvice = "",
                activationSteps = emptyList(),
                carryingAdvice = "",
                deactivationAdvice = "",
                disclaimer = ""
            )
        }

        val title = when {
            runes.size == 1 -> "Одиночная руна: ${runes.first().nameRu}"
            layoutType == StaveLayoutType.BINDRUNE -> "Священная биндруна (${runes.joinToString(" + ") { it.nameRu }})"
            layoutType == StaveLayoutType.CIRCLE -> "Круговой обережный став (${runes.size} рун)"
            layoutType == StaveLayoutType.MIRROR -> "Зеркальный щит (${runes.joinToString("–") { it.nameRu }})"
            else -> "Руническая формула: ${runes.joinToString(" • ") { it.nameRu }}"
        }

        val roles = runes.mapIndexed { index, rune ->
            val roleTitle = when (index) {
                0 -> "Исток и Первопричина"
                runes.size - 1 -> "Врата Реализации и Итог"
                1 -> if (runes.size == 3) "Вектор Движения" else "Связующая Нить"
                else -> "Укрепляющий Узел"
            }
            val context = "${rune.keywordsRu.take(2).joinToString(", ")}. В данном ставе направляет энергию на ${rune.magicUse.lowercase()}."
            RuneRole(rune.nameRu, roleTitle, context)
        }

        val summary = buildString {
            append("Став объединяет энергии ")
            append(runes.joinToString(", ") { "${it.nameRu} (${it.keywordsRu.firstOrNull() ?: ""})" })
            append(". ")
            when (layoutType) {
                StaveLayoutType.BINDRUNE -> append("Единый вертикальный стержень связывает вибрации всех знаков в неразрывный фокус личной воли.")
                StaveLayoutType.CIRCLE -> append("Круговая геометрия запирает защитное поле и распределяет влияние равномерно по всем сферам жизни.")
                StaveLayoutType.MIRROR -> append("Двусторонняя зеркальная симметрия многократно отражает негативные импульсы и стабилизирует результат.")
                StaveLayoutType.ROW -> append("Классический строчный порядок задает поступательное развитие намерения от истока к воплощению.")
                StaveLayoutType.VEGVISIR -> append("Восьмилучевой компас Вегвизира ведет сквозь любые штормы и препятствия к истинной цели.")
                StaveLayoutType.AEGISHJALMUR -> append("Шлем Ужаса (Агисхьяльм) создает несокрушимый круговой щит с тройными вилами концентрации силы.")
                StaveLayoutType.CROSS_STAVE -> append("Сакральный крестовой став балансирует четыре стороны света и концентрирует защиту в центре.")
                StaveLayoutType.STELE_OBELISK -> append("Стела-обелиск концентрирует руническую силу в нерушимом монолите, устремленном сквозь миры к победе.")
            }
        }

        val strokeOrder = when (layoutType) {
            StaveLayoutType.BINDRUNE -> "1. Сначала наносится центральный вертикальный стержень сверху вниз (призыв нисходящей энергии Асгарда). 2. Затем последовательно прорисовываются ветви и углы каждой руны от центра наружу."
            StaveLayoutType.CIRCLE -> "1. Наносится центральный солярный крест. 2. Очерчивается внешний контур по часовой стрелке (посолонь). 3. Вписываются руны по кругу, начиная с верхней точки."
            else -> "1. Каждая руна чертится сверху вниз. 2. Вертикальные стойки чертятся в первую очередь, диагональные отростки — следом, слева направо."
        }

        val activationSteps = listOf(
            "Очищение разума: Сядьте в тихом месте, сосредоточьтесь на дыхании и ясно сформулируйте своё чистое намерение без частицы «не».",
            "Оговор (Формула воли): Вслух или шепотом назовите имена всех рун става и утвердите цель: «Силою рун (имена) пусть свершится (цель) во благо мне и миру».",
            "Активация дыханием (Önd): Поднесите ладони со ставом к губам и трижды сделайте теплый, осознанный выдох на символ, вдыхая в него жизнь.",
            "Закрепление: Поблагодарите скандинавских богов или стихии за содействие и завершите мысленно ритуал."
        )

        val carryingAdvice = "Рекомендуется носить как талисман из дерева, камня или металла близко к телу (на шее, запястье), либо наносить на кожу смываемыми красками или натуральным маслом."
        val deactivationAdvice = "Когда намерение исполнится или срок действия става истечет, его деактивируют со словами благодарности: деревянную плашку сжигают, бумажный эскиз предают земле или огню, а с тела смывают проточной водой."

        val disclaimer = "По традиции считается, что руны — это многовековой символический инструмент самопознания и фокусировки воли. Став не дает безусловных гарантий и не заменяет практических действий, медицинского или юридического содействия."

        return StaveInterpretationData(
            title = title,
            summary = summary,
            runeRoles = roles,
            strokeOrderAdvice = strokeOrder,
            activationSteps = activationSteps,
            carryingAdvice = carryingAdvice,
            deactivationAdvice = deactivationAdvice,
            disclaimer = disclaimer
        )
    }
}
