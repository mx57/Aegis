package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Rune
import com.example.engine.StaveLayoutType
import com.example.ui.components.PouchRitualState
import com.example.ui.components.RuneInterpretationCard
import com.example.ui.components.SacredRunePouch
import com.example.ui.components.SacredRuneTablet
import com.example.ui.components.SingleRuneIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

@Composable
fun DivinationScreen(
    allRunes: List<Rune>,
    onNavigateToSketch: (runeIds: List<String>, layoutType: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pouch (1 Rune), 1: Rune of Day, 2: 3 Norns Spread
    val elderRunes = remember(allRunes) { allRunes.filter { it.futhark == "elder" } }

    val todayDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
    val todayDisplayStr = remember {
        SimpleDateFormat("d MMMM yyyy", Locale("ru")).format(Date())
    }

    // Rune of the day derived from day hash
    val runeOfDay = remember(elderRunes, todayDateStr) {
        if (elderRunes.isEmpty()) null
        else {
            val hash = Math.abs(todayDateStr.hashCode())
            elderRunes[hash % elderRunes.size]
        }
    }

    // --- State for Single Rune Pouch Ritual ---
    var singleRitualState by remember { mutableStateOf(PouchRitualState.IDLE) }
    var drawnSingleRune by remember { mutableStateOf<Rune?>(null) }
    var singleReversed by remember { mutableStateOf(false) }

    // Animations for emerging and 3D flip
    val emergeAnim = remember { Animatable(0f) } // 0f = inside pouch, 1f = in front of user
    val flipAnim = remember { Animatable(0f) }   // 0f = back face, 1f = front face (rune)

    // Function to trigger drawing from pouch
    fun drawRuneFromPouch() {
        if (elderRunes.isEmpty()) return
        coroutineScope.launch {
            singleRitualState = PouchRitualState.SHAKING
            emergeAnim.snapTo(0f)
            flipAnim.snapTo(0f)

            // 1. Shaking vibration
            delay(480)

            // Select random rune and orientation
            val r = elderRunes.random()
            drawnSingleRune = r
            singleReversed = Random().nextBoolean()

            // 2. Emerging upwards from pouch mouth
            singleRitualState = PouchRitualState.DRAWING
            emergeAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )

            // 3. 3D Flip reveal
            flipAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
            )

            singleRitualState = PouchRitualState.REVEALED
        }
    }

    // Function to put rune back in pouch
    fun putRuneBack() {
        coroutineScope.launch {
            flipAnim.animateTo(0f, tween(durationMillis = 350, easing = FastOutSlowInEasing))
            emergeAnim.animateTo(0f, tween(durationMillis = 350, easing = FastOutSlowInEasing))
            drawnSingleRune = null
            singleRitualState = PouchRitualState.IDLE
        }
    }

    // --- State for Rune of the Day Ritual ---
    var dayRevealed by remember { mutableStateOf(false) }
    val dayFlipAnim = remember { Animatable(0f) }

    // --- State for 3 Norns Spread ---
    data class NornDrawn(val rune: Rune, val isReversed: Boolean)
    var drawnThreeRunes by remember { mutableStateOf<List<NornDrawn>>(emptyList()) }
    var selectedNornIndex by remember { mutableIntStateOf(0) }
    var isNornsDrawing by remember { mutableStateOf(false) }

    fun drawThreeNorns() {
        if (elderRunes.size < 3) return
        coroutineScope.launch {
            isNornsDrawing = true
            drawnThreeRunes = emptyList()
            val shuffled = elderRunes.shuffled().take(3)
            val results = mutableListOf<NornDrawn>()

            for (r in shuffled) {
                delay(300)
                results.add(NornDrawn(r, Random().nextBoolean()))
                drawnThreeRunes = results.toList()
            }
            isNornsDrawing = false
            selectedNornIndex = 0
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
        // Top Header
        Text(
            text = "Скандинавский Оракул",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Сакральное вопрошание Норн через вытягивание рун из мешочка судьбы.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Mode Selector TabRow
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("🔮 Мешочек (1 руна)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("☀️ Руна дня", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("⚔️ Три Норны", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> {
                // ==========================================
                // 1. SACRED POUCH (1 RUNE SPREAD WITH 3D RITUAL)
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44E5C158)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10131B))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Вытягивание Руны из Мешочка",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (singleRitualState == PouchRitualState.IDLE)
                                "Сосредоточьтесь на вопросе и коснитесь мешочка, чтобы вытянуть руну судьбы."
                            else if (singleRitualState == PouchRitualState.SHAKING)
                                "Шуршат деревянные плашки... Норны выбирают ваш знак."
                            else
                                "Священный знак явлен. Постигните его тайный смысл.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        // --- Interactive Ritual Stage ---
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(270.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (singleRitualState == PouchRitualState.IDLE || singleRitualState == PouchRitualState.SHAKING) {
                                // Pouch ready or shaking
                                SacredRunePouch(
                                    isShaking = singleRitualState == PouchRitualState.SHAKING,
                                    onClick = {
                                        if (singleRitualState == PouchRitualState.IDLE) {
                                            drawRuneFromPouch()
                                        }
                                    }
                                )
                            } else if (drawnSingleRune != null) {
                                // Rune emerging or revealed
                                val r = drawnSingleRune!!
                                val emergeValue = emergeAnim.value
                                val flipValue = flipAnim.value

                                Box(
                                    modifier = Modifier
                                        .offset(y = ((1f - emergeValue) * 60f).dp)
                                        .scale(0.5f + emergeValue * 0.5f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SacredRuneTablet(
                                        rune = r,
                                        isReversed = singleReversed,
                                        flipProgress = flipValue,
                                        size = 145.dp,
                                        onClick = {
                                            if (singleRitualState == PouchRitualState.REVEALED) {
                                                singleReversed = !singleReversed
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Action Buttons Under Ritual Stage
                        if (singleRitualState == PouchRitualState.IDLE) {
                            Button(
                                onClick = { drawRuneFromPouch() },
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                                    .testTag("shake_pouch_button")
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Опустить руку в мешочек", fontWeight = FontWeight.Bold)
                            }
                        } else if (singleRitualState == PouchRitualState.REVEALED && drawnSingleRune != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { drawRuneFromPouch() },
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55E5C158)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Тянуть снова", style = MaterialTheme.typography.labelSmall)
                                }

                                OutlinedButton(
                                    onClick = { putRuneBack() },
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("В мешочек", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }

                // Detailed Handbook Interpretation Section for Single Rune
                if (singleRitualState == PouchRitualState.REVEALED && drawnSingleRune != null) {
                    val r = drawnSingleRune!!
                    Spacer(modifier = Modifier.height(18.dp))
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 4 }
                    ) {
                        RuneInterpretationCard(
                            rune = r,
                            isReversed = singleReversed,
                            onToggleReversed = { singleReversed = !singleReversed },
                            onCreateStave = {
                                onNavigateToSketch(listOf(r.id), StaveLayoutType.ROW.name)
                            },
                            onPutBack = { putRuneBack() }
                        )
                    }
                }
            }

            1 -> {
                // ==========================================
                // 2. RUNE OF THE DAY (DAILY ORACLE SIGN)
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44E5C158)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10131B))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0x22E5C158),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44E5C158))
                        ) {
                            Text(
                                text = "Знак судьбы на $todayDisplayStr",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (runeOfDay != null) {
                            val r = runeOfDay
                            if (!dayRevealed) {
                                // Pouch of the day ready to reveal
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(230.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SacredRunePouch(
                                        onClick = {
                                            coroutineScope.launch {
                                                dayRevealed = true
                                                dayFlipAnim.snapTo(0f)
                                                dayFlipAnim.animateTo(1f, tween(durationMillis = 750, easing = FastOutSlowInEasing))
                                            }
                                        }
                                    )
                                }

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            dayRevealed = true
                                            dayFlipAnim.snapTo(0f)
                                            dayFlipAnim.animateTo(1f, tween(durationMillis = 750, easing = FastOutSlowInEasing))
                                        }
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Открыть руну сегодняшнего дня")
                                }
                            } else {
                                // Tablet revealed
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SacredRuneTablet(
                                        rune = r,
                                        isReversed = false,
                                        flipProgress = dayFlipAnim.value,
                                        size = 140.dp
                                    )
                                }

                                RuneInterpretationCard(
                                    rune = r,
                                    isReversed = false,
                                    onToggleReversed = {},
                                    onCreateStave = {
                                        onNavigateToSketch(listOf(r.id), StaveLayoutType.ROW.name)
                                    },
                                    onPutBack = {
                                        dayRevealed = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            2 -> {
                // ==========================================
                // 3. THREE NORNS SPREAD (URD, VERDANDI, SKULD)
                // ==========================================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44E5C158)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10131B))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Расклад Трех Сестер Судьбы",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Урд (Прошлое / Причина) • Верданди (Настоящее / Действие) • Скульд (Будущее / Исход)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (drawnThreeRunes.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                SacredRunePouch(
                                    isShaking = isNornsDrawing,
                                    onClick = { drawThreeNorns() }
                                )
                            }

                            Button(
                                onClick = { drawThreeNorns() },
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Вытянуть триаду Норн из мешочка")
                            }
                        } else {
                            val positions = listOf(
                                "1. Урд (Истоки)",
                                "2. Верданди (Вызов)",
                                "3. Скульд (Исход)"
                            )

                            // 3 Tablets Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                drawnThreeRunes.forEachIndexed { idx, norn ->
                                    val isSelected = selectedNornIndex == idx
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { selectedNornIndex = idx }
                                            .padding(4.dp)
                                    ) {
                                        Text(
                                            text = positions.getOrElse(idx) { "" },
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFFFFE082) else Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Box(
                                            modifier = if (isSelected) {
                                                Modifier.border(2.dp, Color(0xFFE5C158), RoundedCornerShape(18.dp))
                                            } else Modifier
                                        ) {
                                            SacredRuneTablet(
                                                rune = norn.rune,
                                                isReversed = norn.isReversed,
                                                flipProgress = 1f,
                                                size = 86.dp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = norn.rune.nameRu,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { drawThreeNorns() },
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44E5C158)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Новый расклад", style = MaterialTheme.typography.labelSmall)
                                }

                                Button(
                                    onClick = {
                                        onNavigateToSketch(drawnThreeRunes.map { it.rune.id }, StaveLayoutType.BINDRUNE.name)
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Став из триады", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            // Active Norn Details Card
                            val activeNorn = drawnThreeRunes.getOrNull(selectedNornIndex)
                            if (activeNorn != null) {
                                Spacer(modifier = Modifier.height(18.dp))
                                RuneInterpretationCard(
                                    rune = activeNorn.rune,
                                    isReversed = activeNorn.isReversed,
                                    onToggleReversed = {
                                        val updated = drawnThreeRunes.toMutableList()
                                        updated[selectedNornIndex] = activeNorn.copy(isReversed = !activeNorn.isReversed)
                                        drawnThreeRunes = updated
                                    },
                                    onCreateStave = {
                                        onNavigateToSketch(listOf(activeNorn.rune.id), StaveLayoutType.ROW.name)
                                    },
                                    onPutBack = {
                                        drawnThreeRunes = emptyList()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
