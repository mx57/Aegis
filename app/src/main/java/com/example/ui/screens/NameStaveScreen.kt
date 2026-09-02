package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.translit.RuneTransliteration
import com.example.engine.SketchConfig
import com.example.engine.SketchStyle
import com.example.engine.StaveComposer
import com.example.engine.StaveInterpretation
import com.example.engine.StaveLayoutType
import com.example.ui.components.InterpretationCard
import com.example.ui.components.RunicCanvas
import com.example.ui.components.SingleRuneIcon
import com.example.ui.viewmodel.RuneViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NameStaveScreen(
    viewModel: RuneViewModel,
    allRunes: List<com.example.data.model.Rune>,
    onNavigateToSketch: (runeIds: List<String>, layoutType: String) -> Unit,
    onNavigateToTryOn: (runeIds: List<String>, layoutType: String, seed: Long, style: String) -> Unit
) {
    var inputText by remember { mutableStateOf("СИЛА") }
    var preferYounger by remember { mutableStateOf(false) }
    var selectedLayout by remember { mutableStateOf(StaveLayoutType.BINDRUNE) }
    var isSavedToFav by remember { mutableStateOf(false) }

    val translitResult by remember(inputText, allRunes, preferYounger) {
        derivedStateOf {
            RuneTransliteration.transliterate(inputText, allRunes, preferYounger)
        }
    }

    val composedStave by remember(translitResult.runes, selectedLayout) {
        derivedStateOf {
            StaveComposer.compose(translitResult.runes, selectedLayout)
        }
    }

    val interpretation by remember(translitResult.runes, selectedLayout) {
        derivedStateOf {
            StaveInterpretation.generate(translitResult.runes, selectedLayout)
        }
    }

    val userSettings by viewModel.userSettings.collectAsState()

    val sketchConfig = remember(userSettings.defaultStyle) {
        val style = try {
            SketchStyle.valueOf(userSettings.defaultStyle)
        } catch (_: Exception) {
            SketchStyle.ORNAMENTAL
        }
        SketchConfig(
            style = style,
            lineWidth = 3.6f,
            hasFrameCircle = true,
            seed = 1001L
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Став из имени или слова",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Введите имя, намерение или священное слово для рунической трансформации:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                OutlinedTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        isSavedToFav = false
                    },
                    label = { Text("Имя или слово") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("name_input_field"),
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(onClick = { inputText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Очистить")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Futhark toggle chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Строй:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilterChip(
                        selected = !preferYounger,
                        onClick = { preferYounger = false },
                        shape = RoundedCornerShape(16.dp),
                        label = { Text("Старший (24)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    FilterChip(
                        selected = preferYounger,
                        onClick = { preferYounger = true },
                        shape = RoundedCornerShape(16.dp),
                        label = { Text("Младший (16)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }

                // Phonetic Breakdown Chips
                if (translitResult.mappings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Фонетический ряд:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        translitResult.mappings.forEach { map ->
                            val r = allRunes.find { it.id == map.runeId }
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${map.char} → ",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (r != null) {
                                    SingleRuneIcon(
                                        rune = r,
                                        size = 18.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        strokeWidthDp = 1.6.dp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = map.runeName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Notes about compound letters / omitted signs
                if (translitResult.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    translitResult.notes.forEach { note ->
                        Text(
                            text = "ℹ $note",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Live Preview Canvas Card with Geometric Balance aesthetics
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (translitResult.runes.isEmpty()) {
                        Text(
                            text = "Введите текст выше для генерации става",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        RunicCanvas(
                            stave = composedStave,
                            config = sketchConfig,
                            animationDurationMs = userSettings.animationSpeedMs
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${selectedLayout.titleRu.uppercase()} (${selectedLayout.name})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = "Стиль: ${sketchConfig.style.titleRu} • Толщина: ${sketchConfig.lineWidth}px",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Layout Type Selector (Geometric Balance 4-button grid)
        val layouts = listOf(
            StaveLayoutType.BINDRUNE,
            StaveLayoutType.ROW,
            StaveLayoutType.CIRCLE,
            StaveLayoutType.MIRROR
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            layouts.forEach { layout ->
                val isSelected = selectedLayout == layout
                Card(
                    onClick = { selectedLayout = layout },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = layout.titleRu,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    val ids = translitResult.runes.map { it.id }
                    if (ids.isNotEmpty()) {
                        onNavigateToSketch(ids, selectedLayout.name)
                    }
                },
                enabled = translitResult.runes.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("open_sketch_button"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Эскиз SVG", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
                onClick = {
                    val ids = translitResult.runes.map { it.id }
                    if (ids.isNotEmpty()) {
                        onNavigateToTryOn(ids, selectedLayout.name, sketchConfig.seed, sketchConfig.style.name)
                    }
                },
                enabled = translitResult.runes.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1f)
                    .testTag("open_tryon_button")
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Примерка", style = MaterialTheme.typography.labelLarge)
            }

            IconButton(
                onClick = {
                    val ids = translitResult.runes.map { it.id }
                    if (ids.isNotEmpty()) {
                        viewModel.saveToHistory(
                            title = "Став «$inputText»",
                            runeIds = ids,
                            layoutType = selectedLayout.name,
                            styleType = sketchConfig.style.name,
                            seed = sketchConfig.seed,
                            isFavorite = true
                        )
                        isSavedToFav = true
                    }
                },
                enabled = translitResult.runes.isNotEmpty(),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSavedToFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .border(
                        if (isSavedToFav) 0.dp else 1.dp,
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Icon(
                    imageVector = if (isSavedToFav) Icons.Default.Check else Icons.Default.BookmarkBorder,
                    contentDescription = "В избранное",
                    tint = if (isSavedToFav) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stave Interpretation Card
        if (translitResult.runes.isNotEmpty()) {
            InterpretationCard(data = interpretation)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
