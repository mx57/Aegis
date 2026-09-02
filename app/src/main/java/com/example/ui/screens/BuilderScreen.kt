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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.catalog.IntentionPreset
import com.example.data.catalog.StaveCatalog
import com.example.data.model.Rune
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
fun BuilderScreen(
    viewModel: RuneViewModel,
    allRunes: List<Rune>,
    onNavigateToSketch: (runeIds: List<String>, layoutType: String) -> Unit,
    onNavigateToTryOn: (runeIds: List<String>, layoutType: String, seed: Long, style: String) -> Unit
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0: Intentions, 1: Manual Builder
    val selectedRuneIds = remember { mutableStateOf(listOf("algiz", "thurisaz", "tiwaz")) }
    var selectedLayout by remember { mutableStateOf(StaveLayoutType.BINDRUNE) }
    var staveTitle by remember { mutableStateOf("Став защиты") }
    var isSavedToFav by remember { mutableStateOf(false) }

    val runeMap = remember(allRunes) { allRunes.associateBy { it.id } }
    val currentRunes by remember(selectedRuneIds.value, allRunes) {
        derivedStateOf {
            selectedRuneIds.value.mapNotNull { runeMap[it] }
        }
    }

    val composedStave by remember(currentRunes, selectedLayout) {
        derivedStateOf {
            StaveComposer.compose(currentRunes, selectedLayout)
        }
    }

    val interpretation by remember(currentRunes, selectedLayout) {
        derivedStateOf {
            StaveInterpretation.generate(currentRunes, selectedLayout)
        }
    }

    val userSettings by viewModel.userSettings.collectAsState()
    val defaultSketchStyle = remember(userSettings.defaultStyle) {
        try {
            SketchStyle.valueOf(userSettings.defaultStyle)
        } catch (_: Exception) {
            SketchStyle.ORNAMENTAL
        }
    }

    val sketchConfig = remember(defaultSketchStyle) {
        SketchConfig(
            style = defaultSketchStyle,
            lineWidth = 3.6f,
            hasFrameCircle = true,
            seed = 2024L
        )
    }

    var zoomScale by remember { mutableFloatStateOf(1f) }
    var zoomOffset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        zoomScale = (zoomScale * zoomChange).coerceIn(1f, 5f)
        if (zoomScale > 1f) {
            val maxOffset = 250f * (zoomScale - 1f)
            zoomOffset = Offset(
                x = (zoomOffset.x + offsetChange.x).coerceIn(-maxOffset, maxOffset),
                y = (zoomOffset.y + offsetChange.y).coerceIn(-maxOffset, maxOffset)
            )
        } else {
            zoomOffset = Offset.Zero
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tab Header: Intentions vs Manual
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Каталог намерений", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Ручной конструктор", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeTab == 0) {
            // Preset catalog
            Text(
                text = "Выберите сакральное намерение:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            StaveCatalog.intentions.forEach { preset ->
                IntentionPresetCard(
                    preset = preset,
                    allRunes = allRunes,
                    isSelected = selectedRuneIds.value == preset.runeIds,
                    onSelect = {
                        selectedRuneIds.value = preset.runeIds
                        staveTitle = preset.titleRu
                        isSavedToFav = false
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        } else {
            // Manual construction mode
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Текущая формула (${currentRunes.size} рун):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (selectedRuneIds.value.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    selectedRuneIds.value = emptyList()
                                    isSavedToFav = false
                                }
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = "Сбросить")
                            }
                        }
                    }

                    // Active Rune Chips
                    if (currentRunes.isEmpty()) {
                        Text(
                            text = "Нажмите на руны ниже, чтобы добавить их в став",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(currentRunes) { rune ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SingleRuneIcon(
                                        rune = rune,
                                        size = 20.dp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        strokeWidthDp = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = rune.nameRu,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Удалить",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable {
                                                val list = selectedRuneIds.value.toMutableList()
                                                list.remove(rune.id)
                                                selectedRuneIds.value = list
                                                isSavedToFav = false
                                            }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Добавить руну в став:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Rune selection palette (FlowRow of Elder Futhark)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allRunes.filter { it.futhark == "elder" }.forEach { r ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (selectedRuneIds.value.size < 8) {
                                            selectedRuneIds.value = selectedRuneIds.value + r.id
                                            isSavedToFav = false
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SingleRuneIcon(rune = r, size = 16.dp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = r.nameRu,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Layout Type Selector (Scrollable Row with all geometric styles including Obelisk)
        val layouts = listOf(
            StaveLayoutType.BINDRUNE,
            StaveLayoutType.ROW,
            StaveLayoutType.CIRCLE,
            StaveLayoutType.MIRROR,
            StaveLayoutType.STELE_OBELISK
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(layouts) { layout ->
                val isSelected = selectedLayout == layout
                Card(
                    onClick = { selectedLayout = layout },
                    shape = RoundedCornerShape(16.dp),
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 14.dp),
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

        // Canvas Preview Card with Pinch-to-Zoom and Title & Description below
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive Zoomable Canvas Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (zoomScale > 1.1f) {
                                        zoomScale = 1f
                                        zoomOffset = Offset.Zero
                                    } else {
                                        zoomScale = 2.2f
                                    }
                                }
                            )
                        }
                        .transformable(state = transformState),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = zoomScale
                                scaleY = zoomScale
                                translationX = zoomOffset.x
                                translationY = zoomOffset.y
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentRunes.isEmpty()) {
                            Text(
                                text = "Выберите руны для отображения става",
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

                    // Zoom indicator / reset badge
                    if (zoomScale > 1.05f) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
                                .clickable {
                                    zoomScale = 1f
                                    zoomOffset = Offset.Zero
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ZoomOutMap,
                                contentDescription = "Сбросить масштаб",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "%.1fx".format(zoomScale),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stave Name and Short Description (User requirement: под ним же должно быть название и краткое описание)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = staveTitle.ifEmpty { selectedLayout.titleRu },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentRunes.isEmpty()) {
                            "Выберите руны из каталога или списка выше для сакрального начертания"
                        } else {
                            interpretation.summary
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
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
                    if (selectedRuneIds.value.isNotEmpty()) {
                        onNavigateToSketch(selectedRuneIds.value, selectedLayout.name)
                    }
                },
                enabled = currentRunes.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("builder_sketch_button")
            ) {
                Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Эскиз SVG")
            }

            OutlinedButton(
                onClick = {
                    if (selectedRuneIds.value.isNotEmpty()) {
                        onNavigateToTryOn(selectedRuneIds.value, selectedLayout.name, sketchConfig.seed, sketchConfig.style.name)
                    }
                },
                enabled = currentRunes.isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .weight(1f)
                    .testTag("builder_tryon_button")
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Примерка")
            }

            IconButton(
                onClick = {
                    if (selectedRuneIds.value.isNotEmpty()) {
                        viewModel.saveToHistory(
                            title = staveTitle,
                            runeIds = selectedRuneIds.value,
                            layoutType = selectedLayout.name,
                            styleType = sketchConfig.style.name,
                            seed = sketchConfig.seed,
                            isFavorite = true
                        )
                        isSavedToFav = true
                    }
                },
                enabled = currentRunes.isNotEmpty(),
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

        if (currentRunes.isNotEmpty()) {
            InterpretationCard(data = interpretation)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun IntentionPresetCard(
    preset: IntentionPreset,
    allRunes: List<Rune>,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val runeMap = remember(allRunes) { allRunes.associateBy { it.id } }
    val runes = preset.runeIds.mapNotNull { runeMap[it] }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.titleRu,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Выбрано",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = preset.descriptionRu,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Runes row in preset
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                runes.forEach { r ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        SingleRuneIcon(rune = r, size = 16.dp, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(r.nameRu, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Нанесение: ${preset.recommendedPlacement}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
