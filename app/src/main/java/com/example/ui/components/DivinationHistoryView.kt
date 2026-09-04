package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DivinationRecord
import com.example.data.model.Rune
import com.example.engine.StaveLayoutType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DivinationHistoryView(
    records: List<DivinationRecord>,
    allRunes: List<Rune>,
    onDeleteRecord: (DivinationRecord) -> Unit,
    onClearAll: () -> Unit,
    onNavigateToSketch: (runeIds: List<String>, layoutType: String) -> Unit,
    onStartDivination: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Очистить историю гаданий?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Text("Все сохраненные расклады (до 10 последних записей) будут удалены из летописи.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Удалить всё", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Отмена")
                }
            },
            containerColor = Color(0xFF141722),
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header Section ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0x33E5C158)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF10131B))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x22E5C158))
                            .border(1.dp, Color(0x44E5C158), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color(0xFFFFE082),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Летопись Оракула",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (records.isNotEmpty()) {
                                "Сохранено ${records.size} из 10 последних раскладов"
                            } else {
                                "Журнал пуст"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                if (records.isNotEmpty()) {
                    TextButton(
                        onClick = { showClearConfirmDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Очистить всё",
                            tint = Color(0xFFFF8A80),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Очистить",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF8A80)
                        )
                    }
                }
            }
        }

        // --- Empty State ---
        if (records.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                border = BorderStroke(1.dp, Color(0x22E5C158)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10131B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0x18E5C158))
                            .border(1.dp, Color(0x33E5C158), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color(0xFFFFE082),
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Летопись пока пуста",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Каждое ваше обращение к рунам (из Мешочка, Руна Дня или Расклад Трех Норн) автоматически сохраняется здесь. Вы сможете возвращаться к знакам и создавать ставы.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onStartDivination,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Сделать первое гадание")
                    }
                }
            }
        } else {
            // --- Records List ---
            records.forEachIndexed { index, record ->
                DivinationRecordCard(
                    record = record,
                    allRunes = allRunes,
                    index = index + 1,
                    onDelete = { onDeleteRecord(record) },
                    onCreateStave = { runeIds, layout ->
                        onNavigateToSketch(runeIds, layout)
                    }
                )
            }
        }
    }
}

@Composable
private fun DivinationRecordCard(
    record: DivinationRecord,
    allRunes: List<Rune>,
    index: Int,
    onDelete: () -> Unit,
    onCreateStave: (runeIds: List<String>, layout: String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var isExpanded by remember { mutableStateOf(false) }
    var isCopied by remember { mutableStateOf(false) }

    val formattedDate = remember(record.createdAt) {
        try {
            SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru")).format(Date(record.createdAt))
        } catch (_: Exception) {
            "Недавно"
        }
    }

    val runeIds = remember(record.runeIdsCsv) { record.getRuneIdList() }
    val reversedFlags = remember(record.reversedFlagsCsv) { record.getReversedList() }

    val runesInSpread = remember(runeIds, allRunes) {
        runeIds.mapNotNull { id -> allRunes.find { it.id == id } }
    }

    val spreadBadge = when (record.spreadType) {
        "POUCH" -> Pair("🔮 Мешочек", Color(0xFF64B5F6))
        "DAY" -> Pair("☀️ Руна дня", Color(0xFFFFD54F))
        "NORNS" -> Pair("⚔️ Три Норны", Color(0xFF81C784))
        else -> Pair("📜 Расклад", Color(0xFFE5C158))
    }

    val nornPositions = listOf("1. Урд (Истоки)", "2. Верданди (Действие)", "3. Скульд (Исход)")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, Color(0x33E5C158)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10131B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Row: Type badge, Date and Delete icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = spreadBadge.second.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, spreadBadge.second.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = spreadBadge.first,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = spreadBadge.second,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Удалить запись",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Runes in reading row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = if (runesInSpread.size == 1) Arrangement.Start else Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                runesInSpread.forEachIndexed { idx, rune ->
                    val isRev = reversedFlags.getOrElse(idx) { false }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    ) {
                        if (record.spreadType == "NORNS") {
                            Text(
                                text = nornPositions.getOrElse(idx) { "" },
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = Color(0xFFFFE082),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        // Tablet representation
                        SacredRuneTablet(
                            rune = rune,
                            isReversed = isRev,
                            flipProgress = 1f,
                            size = if (runesInSpread.size == 1) 78.dp else 68.dp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = rune.nameRu,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = if (isRev) "Перевернутая" else "Прямая",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = if (isRev) Color(0xFFFF8A80) else Color(0xFFA5D6A7)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interpretation summary block
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { isExpanded = !isExpanded },
                color = Color(0xFF161B26),
                border = BorderStroke(1.dp, Color(0x22E5C158))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Толкование оракула",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                            tint = Color(0xFFFFE082),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = record.interpretationSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                        lineHeight = 18.sp
                    )

                    if (isExpanded && record.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = record.notes,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val layout = if (record.spreadType == "NORNS") {
                            StaveLayoutType.BINDRUNE.name
                        } else {
                            StaveLayoutType.ROW.name
                        }
                        onCreateStave(runeIds, layout)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Создать став",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        val shareText = buildString {
                            appendLine("📜 ${record.spreadTitleRu}")
                            appendLine("📅 $formattedDate")
                            appendLine()
                            appendLine("Руны: ${runesInSpread.joinToString(", ") { it.nameRu }}")
                            appendLine()
                            appendLine(record.interpretationSummary)
                            if (record.notes.isNotBlank()) {
                                appendLine()
                                appendLine(record.notes)
                            }
                        }
                        clipboardManager.setText(AnnotatedString(shareText))
                        isCopied = true
                        coroutineScope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0x44E5C158))
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = "Копировать толкование",
                        tint = if (isCopied) Color(0xFFA5D6A7) else Color(0xFFFFE082),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCopied) "Скопировано!" else "Копировать",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCopied) Color(0xFFA5D6A7) else Color(0xFFFFE082)
                    )
                }
            }
        }
    }
}
