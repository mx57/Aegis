package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Rune

enum class QuizFutharkScope(val titleRu: String, val runeCountText: String) {
    ELDER("Старший Футарк", "24 руны"),
    YOUNGER("Младший Футарк", "16 рун"),
    ALL("Все руны (Мастер)", "40 рун")
}

enum class QuizQuestionType(val titleRu: String) {
    RUNE_TO_MEANING("Руна ➔ Значение"),
    MEANING_TO_RUNE("Значение ➔ Руна")
}

data class QuizQuestion(
    val targetRune: Rune,
    val type: QuizQuestionType,
    val options: List<Rune>,
    val correctIndex: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RuneQuizView(
    allRunes: List<Rune>,
    onSelectRuneForBuilder: (runeId: String) -> Unit,
    onBackToCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedScope by remember { mutableStateOf(QuizFutharkScope.ELDER) }
    var selectedQuestionMode by remember { mutableIntStateOf(0) } // 0: Руна -> Значение, 1: Значение -> Руна, 2: Случайный

    // Filter available runes according to scope
    val scopedRunes = remember(allRunes, selectedScope) {
        when (selectedScope) {
            QuizFutharkScope.ELDER -> allRunes.filter { it.futhark == "elder" }
            QuizFutharkScope.YOUNGER -> allRunes.filter { it.futhark == "younger" }
            QuizFutharkScope.ALL -> allRunes
        }
    }

    // Quiz Session State
    val totalQuestionsPerRound = 10
    var currentQuestionNumber by remember { mutableIntStateOf(1) }
    var score by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var maxStreak by remember { mutableIntStateOf(0) }
    var isRoundComplete by remember { mutableStateOf(false) }

    // Current Question State
    var currentQuestion by remember { mutableStateOf<QuizQuestion?>(null) }
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    var isAnswerRevealed by remember { mutableStateOf(false) }

    // Function to generate next question
    fun generateQuestion() {
        if (scopedRunes.size < 4) return
        val target = scopedRunes.random()
        val distractors = scopedRunes.filter { it.id != target.id }.shuffled().take(3)
        val options = (distractors + target).shuffled()
        val correctIdx = options.indexOf(target)

        val qType = when (selectedQuestionMode) {
            0 -> QuizQuestionType.RUNE_TO_MEANING
            1 -> QuizQuestionType.MEANING_TO_RUNE
            else -> if (listOf(true, false).random()) QuizQuestionType.RUNE_TO_MEANING else QuizQuestionType.MEANING_TO_RUNE
        }

        currentQuestion = QuizQuestion(
            targetRune = target,
            type = qType,
            options = options,
            correctIndex = correctIdx
        )
        selectedOptionIndex = null
        isAnswerRevealed = false
    }

    // Reset entire round
    fun resetQuiz() {
        currentQuestionNumber = 1
        score = 0
        currentStreak = 0
        maxStreak = 0
        isRoundComplete = false
        generateQuestion()
    }

    LaunchedEffect(selectedScope, selectedQuestionMode, scopedRunes) {
        resetQuiz()
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header & Scope Selector ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0x33E5C158)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10131B))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0x22E5C158))
                                .border(1.dp, Color(0x44E5C158), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = Color(0xFFFFE082),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Интерактивная Викторина",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Проверка сакральных знаний Футарка",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    // Streak Badge
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (currentStreak > 0) Color(0x28FF6D00) else Color(0x18FFFFFF),
                        border = BorderStroke(1.dp, if (currentStreak > 0) Color(0xFFFF6D00) else Color(0x33FFFFFF))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = if (currentStreak > 0) Color(0xFFFF9100) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$currentStreak",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (currentStreak > 0) Color(0xFFFF9100) else Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Futhark Scope TabRow
                TabRow(
                    selectedTabIndex = selectedScope.ordinal,
                    containerColor = Color(0xFF161B26),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(14.dp))
                ) {
                    QuizFutharkScope.values().forEach { scope ->
                        Tab(
                            selected = selectedScope == scope,
                            onClick = {
                                if (selectedScope != scope) {
                                    selectedScope = scope
                                }
                            },
                            text = {
                                Text(
                                    text = scope.titleRu,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Question Type Mode Selector Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf("🔤 Руна ➔ Значение", "📜 Значение ➔ Руна", "🎲 Смешанный")
                    modes.forEachIndexed { index, title ->
                        val isSel = selectedQuestionMode == index
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) Color(0x33E5C158) else Color(0xFF161B26),
                            border = BorderStroke(1.dp, if (isSel) Color(0xFFE5C158) else Color(0x22E5C158)),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    if (selectedQuestionMode != index) {
                                        selectedQuestionMode = index
                                    }
                                }
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color(0xFFFFE082) else Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Main Quiz Content Area ---
        if (isRoundComplete) {
            // ==========================================
            // ROUND COMPLETION / VICTORY SUMMARY
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, Color(0x44E5C158)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10131B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color(0x22E5C158))
                            .border(2.dp, Color(0xFFE5C158), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    val scorePercentage = (score * 100) / totalQuestionsPerRound
                    val (rankTitle, rankSubtitle) = when {
                        score == 10 -> Pair("🏆 Мастер Эриль", "Безупречное знание священного канона рун!")
                        score >= 8 -> Pair("⚔️ Хранитель Футарка", "Отличное владение значениями и символами!")
                        score >= 6 -> Pair("🌿 Постигающий Тайны", "Хороший результат! Вы уверенно постигаете руны.")
                        else -> Pair("🕯️ Ученик Северной Традиции", "Продолжайте изучать энциклопедию и углублять знания.")
                    }

                    Text(
                        text = rankTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFE082)
                    )

                    Text(
                        text = rankSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Очки", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                text = "$score / $totalQuestionsPerRound",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Точность", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                text = "$scorePercentage%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81C784)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Макс. серия", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                text = "🔥 $maxStreak",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9100)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { resetQuiz() },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Пройти снова")
                        }

                        OutlinedButton(
                            onClick = onBackToCatalog,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0x44E5C158)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("В справочник")
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // ACTIVE QUESTION SCREEN
            // ==========================================
            val q = currentQuestion
            if (q != null) {
                // Progress Bar & Question Counter
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Вопрос $currentQuestionNumber из $totalQuestionsPerRound",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Счет: $score",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF81C784)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { currentQuestionNumber.toFloat() / totalQuestionsPerRound.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFFE5C158),
                        trackColor = Color(0xFF1E2433)
                    )
                }

                // Question Prompt Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0x44E5C158)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10131B))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (q.type == QuizQuestionType.RUNE_TO_MEANING) {
                            // Mode 1: Rune is shown, user guesses meaning
                            SacredRuneTablet(
                                rune = q.targetRune,
                                isReversed = false,
                                flipProgress = 1f,
                                size = 96.dp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "${q.targetRune.nameRu} (${q.targetRune.unicode})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = "Футарк: ${if (q.targetRune.futhark == "elder") "Старший (24)" else "Младший (16)"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Каково главное сакральное значение этой руны?",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = Color(0xFFFFE082)
                            )
                        } else {
                            // Mode 2: Meaning is shown, user guesses rune
                            Surface(
                                shape = CircleShape,
                                color = Color(0x22E5C158),
                                border = BorderStroke(1.dp, Color(0x44E5C158))
                            ) {
                                Text(
                                    text = "Тайный смысл руны",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFFE082),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "«${q.targetRune.keywordsRu.joinToString(", ")}»",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = q.targetRune.divinationDirect.take(130) + if (q.targetRune.divinationDirect.length > 130) "..." else "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Какой руне принадлежит это значение?",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFE082)
                            )
                        }
                    }
                }

                // Options Section
                if (q.type == QuizQuestionType.RUNE_TO_MEANING) {
                    // 4 Meaning Choices (Vertical Stack)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        q.options.forEachIndexed { index, optionRune ->
                            val isSelected = selectedOptionIndex == index
                            val isCorrect = index == q.correctIndex

                            val containerColor by animateColorAsState(
                                targetValue = when {
                                    !isAnswerRevealed -> if (isSelected) Color(0x33E5C158) else Color(0xFF131722)
                                    isCorrect -> Color(0x332E7D32)
                                    isSelected -> Color(0x33C62828)
                                    else -> Color(0xFF10131B)
                                },
                                animationSpec = tween(300),
                                label = "containerColor"
                            )

                            val borderColor by animateColorAsState(
                                targetValue = when {
                                    !isAnswerRevealed -> if (isSelected) Color(0xFFE5C158) else Color(0x22E5C158)
                                    isCorrect -> Color(0xFF4CAF50)
                                    isSelected -> Color(0xFFE53935)
                                    else -> Color(0x18E5C158)
                                },
                                animationSpec = tween(300),
                                label = "borderColor"
                            )

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable(enabled = !isAnswerRevealed) {
                                        selectedOptionIndex = index
                                        isAnswerRevealed = true
                                        if (isCorrect) {
                                            score++
                                            currentStreak++
                                            if (currentStreak > maxStreak) {
                                                maxStreak = currentStreak
                                            }
                                        } else {
                                            currentStreak = 0
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = containerColor,
                                border = BorderStroke(1.5.dp, borderColor)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Letter circle index (A, B, C, D)
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(0x22E5C158)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = ('A'.code + index).toChar().toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFFE082)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Text(
                                            text = optionRune.keywordsRu.joinToString(", "),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isSelected || isCorrect) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isAnswerRevealed && isCorrect) Color(0xFFA5D6A7) else MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 18.sp
                                        )
                                    }

                                    if (isAnswerRevealed) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        if (isCorrect) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Верно",
                                                tint = Color(0xFF81C784),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } else if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Ошибка",
                                                tint = Color(0xFFEF5350),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 4 Rune Choices (Grid 2x2)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (col in 0..1) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (row in 0..1) {
                                    val index = col * 2 + row
                                    val optionRune = q.options[index]
                                    val isSelected = selectedOptionIndex == index
                                    val isCorrect = index == q.correctIndex

                                    val containerColor by animateColorAsState(
                                        targetValue = when {
                                            !isAnswerRevealed -> if (isSelected) Color(0x33E5C158) else Color(0xFF131722)
                                            isCorrect -> Color(0x332E7D32)
                                            isSelected -> Color(0x33C62828)
                                            else -> Color(0xFF10131B)
                                        },
                                        animationSpec = tween(300),
                                        label = "gridContainer"
                                    )

                                    val borderColor by animateColorAsState(
                                        targetValue = when {
                                            !isAnswerRevealed -> if (isSelected) Color(0xFFE5C158) else Color(0x22E5C158)
                                            isCorrect -> Color(0xFF4CAF50)
                                            isSelected -> Color(0xFFE53935)
                                            else -> Color(0x18E5C158)
                                        },
                                        animationSpec = tween(300),
                                        label = "gridBorder"
                                    )

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(18.dp))
                                            .clickable(enabled = !isAnswerRevealed) {
                                                selectedOptionIndex = index
                                                isAnswerRevealed = true
                                                if (isCorrect) {
                                                    score++
                                                    currentStreak++
                                                    if (currentStreak > maxStreak) {
                                                        maxStreak = currentStreak
                                                    }
                                                } else {
                                                    currentStreak = 0
                                                }
                                            },
                                        shape = RoundedCornerShape(18.dp),
                                        color = containerColor,
                                        border = BorderStroke(1.5.dp, borderColor)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            SacredRuneTablet(
                                                rune = optionRune,
                                                isReversed = false,
                                                flipProgress = 1f,
                                                size = 64.dp
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = optionRune.nameRu,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            if (isAnswerRevealed) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                if (isCorrect) {
                                                    Text("✓ Верно", color = Color(0xFF81C784), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                } else if (isSelected) {
                                                    Text("✗ Ошибка", color = Color(0xFFEF5350), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Educational Breakdown Card (appears after answering)
                AnimatedVisibility(
                    visible = isAnswerRevealed,
                    enter = fadeIn() + scaleIn()
                ) {
                    val target = q.targetRune
                    val answeredCorrectly = selectedOptionIndex == q.correctIndex

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, if (answeredCorrectly) Color(0x664CAF50) else Color(0x66E53935)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF131722))
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (answeredCorrectly) "✓ Верный ответ!" else "✗ Значение руны:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (answeredCorrectly) Color(0xFF81C784) else Color(0xFFFF8A80)
                                )

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x22E5C158),
                                    border = BorderStroke(1.dp, Color(0x44E5C158))
                                ) {
                                    Text(
                                        text = "${target.nameRu} (${target.unicode})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFE082),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = target.divinationDirect,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )

                            if (target.tattooSymbolism.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Символика в татуировке: ${target.tattooSymbolism}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Next Button
                            Button(
                                onClick = {
                                    if (currentQuestionNumber >= totalQuestionsPerRound) {
                                        isRoundComplete = true
                                    } else {
                                        currentQuestionNumber++
                                        generateQuestion()
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (currentQuestionNumber >= totalQuestionsPerRound) "Завершить раунд" else "Следующий вопрос",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
