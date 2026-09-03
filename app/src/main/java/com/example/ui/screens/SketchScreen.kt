package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.model.Rune
import com.example.engine.CanvasTheme
import com.example.engine.CenterEmblem
import com.example.engine.ComposedStave
import com.example.engine.CornerStyle
import com.example.engine.FinialType
import com.example.engine.FrameStyle
import com.example.engine.SketchConfig
import com.example.engine.SketchStyle
import com.example.engine.StaveComposer
import com.example.engine.StaveLayoutType
import com.example.engine.SvgStaveRenderer
import com.example.ui.components.RunicCanvas
import kotlinx.coroutines.launch
import java.util.Random

private data class StavePreset(
    val title: String,
    val icon: String,
    val description: String,
    val style: SketchStyle,
    val theme: CanvasTheme = CanvasTheme.DARK_SLATE,
    val frame: FrameStyle,
    val finial: FinialType,
    val center: CenterEmblem,
    val corner: CornerStyle,
    val branchNotches: Boolean,
    val rayBurst: Boolean,
    val runering: Boolean = false,
    val lineWidth: Float,
    val layout: StaveLayoutType? = null
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SketchScreen(
    runeIds: List<String>,
    layoutTypeName: String,
    allRunes: List<Rune>,
    onBack: () -> Unit,
    onNavigateToTryOn: (runeIds: List<String>, layoutType: String, seed: Long, style: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val runeMap = remember(allRunes) { allRunes.associateBy { it.id } }
    val runes = remember(runeIds, runeMap) { runeIds.mapNotNull { runeMap[it] } }

    val initialLayout = remember(layoutTypeName) {
        try {
            StaveLayoutType.valueOf(layoutTypeName)
        } catch (_: Exception) {
            StaveLayoutType.BINDRUNE
        }
    }

    var selectedLayout by remember { mutableStateOf(initialLayout) }
    var selectedStyle by remember { mutableStateOf(SketchStyle.ORNAMENTAL) }
    var selectedTheme by remember { mutableStateOf(CanvasTheme.DARK_SLATE) }
    var frameStyle by remember { mutableStateOf(FrameStyle.SOLAR_CIRCLE) }
    var finialType by remember { mutableStateOf(FinialType.TRIDENT) }
    var centerEmblem by remember { mutableStateOf(CenterEmblem.SOLAR_CROSS) }
    var cornerStyle by remember { mutableStateOf(CornerStyle.NORSE_KNOTS) }

    var lineWidth by remember { mutableFloatStateOf(3.5f) }
    var wobbleAmount by remember { mutableFloatStateOf(0.20f) }
    var hasFrameCircle by remember { mutableStateOf(true) }
    var hasSymmetryAccents by remember { mutableStateOf(true) }
    var hasBranchNotches by remember { mutableStateOf(true) }
    var hasRayBurst by remember { mutableStateOf(false) }
    var hasRunering by remember { mutableStateOf(false) }
    var hasGlowEffect by remember { mutableStateOf(true) }
    var hasVolumetricShading by remember { mutableStateOf(true) }
    var hasTextureGrain by remember { mutableStateOf(true) }
    var runeChiselDepth by remember { mutableFloatStateOf(1.0f) }
    var isStencil by remember { mutableStateOf(false) }
    var seed by remember { mutableLongStateOf(4242L) }
    var animTriggerKey by remember { mutableIntStateOf(0) }
    var targetResolution by remember { mutableIntStateOf(2048) }
    var isExporting by remember { mutableStateOf(false) }

    val appSettings = remember(context) { com.example.data.local.AppSettings(context) }
    val userSettings by appSettings.settingsFlow.collectAsState(
        initial = com.example.data.local.UserSettings(
            hasCompletedOnboarding = true,
            defaultFuthark = "elder",
            defaultStyle = "ORNAMENTAL",
            darkTheme = true,
            language = "ru",
            animationSpeedMs = 4000
        )
    )

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

    val presets = remember {
        listOf(
            StavePreset("Тотем Одина (Волк и Ворон)", "🐺", "Эпический монолит с волками Гери и Фреки, воронами и шипованной цепью", SketchStyle.ODIN_TOTEM, CanvasTheme.GRAPHITE_SKETCH, FrameStyle.SPIKED_CHAIN, FinialType.ARROWS, CenterEmblem.BEASTS_OF_ODIN, CornerStyle.NORSE_KNOTS, branchNotches = true, rayBurst = true, runering = true, lineWidth = 3.2f, layout = StaveLayoutType.STELE_OBELISK),
            StavePreset("Кованая Цепь и Звезда", "⛓️", "Шипованный защитный пояс, гранёная звезда Одина и строгая руническая геометрия", SketchStyle.VIKING_CHAIN, CanvasTheme.CHARCOAL_DARK, FrameStyle.SPIKED_CHAIN, FinialType.TRIDENT, CenterEmblem.FACETED_STAR, CornerStyle.SHIELD_STUDS, branchNotches = true, rayBurst = false, runering = true, lineWidth = 3.4f),
            StavePreset("Гравюра и Кельтский Медальон", "🦅", "Штриховка резцом, медальон с плетением и геральдические звери", SketchStyle.WOODCUT_ENGRAVING, CanvasTheme.GRAPHITE_SKETCH, FrameStyle.CELTIC_MEDALLION, FinialType.SPIRALS, CenterEmblem.BEASTS_OF_ODIN, CornerStyle.NORSE_KNOTS, branchNotches = true, rayBurst = true, runering = true, lineWidth = 2.8f),
            StavePreset("Рунический Монолит", "🗿", "Стела древних скальдов с гранёной звездой и защитным кольцом Футарка", SketchStyle.RUNIC_OBELISK, CanvasTheme.RUNESTONE_GRAY, FrameStyle.CELTIC_MEDALLION, FinialType.CROSSBARS, CenterEmblem.RUNIC_STELE, CornerStyle.RUNIC_BINDS, branchNotches = true, rayBurst = false, runering = true, lineWidth = 3.2f, layout = StaveLayoutType.STELE_OBELISK),
            StavePreset("Сакральное Золото", "👑", "Астролябия планет, лучи и золотая сакральная геометрия", SketchStyle.SACRED_GOLD, CanvasTheme.GOLDEN_EMBER, FrameStyle.CELESTIAL_ASTROLABE, FinialType.CIRCLES_DOTS, CenterEmblem.SOLAR_CROSS, CornerStyle.SUN_RAYS, branchNotches = false, rayBurst = true, runering = true, lineWidth = 2.8f),
            StavePreset("Нордическое Тату", "⚡", "Контрастный тату-стиль с руническим поясом и сакральным Валькнутом", SketchStyle.NORDIC_TATTOO, CanvasTheme.DARK_SLATE, FrameStyle.CELESTIAL_ASTROLABE, FinialType.ARROWS, CenterEmblem.VALKNUT, CornerStyle.NORSE_KNOTS, branchNotches = true, rayBurst = false, runering = true, lineWidth = 3.6f),
            StavePreset("Шлем Ужаса", "🛡️", "Исландский защитный гальдрастав с кольцом рун и ядром силы", SketchStyle.AEGISHJALMUR, CanvasTheme.RUNESTONE_GRAY, FrameStyle.SACRED_OCTAGON, FinialType.TRIDENT, CenterEmblem.AEGISHJALMUR_CORE, CornerStyle.NORSE_KNOTS, branchNotches = true, rayBurst = false, runering = true, lineWidth = 3.6f),
            StavePreset("Вегвизир Одина", "🧭", "Рунический компас 8 направлений с золотым сиянием и кольцом Футарка", SketchStyle.ORNAMENTAL, CanvasTheme.GOLDEN_EMBER, FrameStyle.COMPASS_RAYS, FinialType.TRIDENT, CenterEmblem.SOLAR_CROSS, CornerStyle.SUN_RAYS, branchNotches = true, rayBurst = true, runering = true, lineWidth = 3.4f),
            StavePreset("Серебро Валькирий", "🪽", "Мифриловое сияние небесных дев с узлом трикветра и спиралями", SketchStyle.VALKYRIE_SILVER, CanvasTheme.VALKYRIE_MITHRIL, FrameStyle.CELESTIAL_ASTROLABE, FinialType.SPIRALS, CenterEmblem.TRIQUETRA, CornerStyle.RUNIC_BINDS, branchNotches = false, rayBurst = false, runering = true, lineWidth = 2.6f),
            StavePreset("Северное Сияние", "🌌", "Бирюзовые чары Авроры, витые спирали и рунический круг", SketchStyle.ORNAMENTAL, CanvasTheme.AURORA_NIGHT, FrameStyle.SOLAR_CIRCLE, FinialType.SPIRALS, CenterEmblem.INGUZ_DIAMOND, CornerStyle.NORSE_KNOTS, branchNotches = false, rayBurst = true, runering = true, lineWidth = 3.2f),
            StavePreset("Камень Еллинге", "🪨", "Резной гранит викингов со змеем Мидгарда и узлом Валькнута", SketchStyle.WOODCARVE, CanvasTheme.RUNESTONE_GRAY, FrameStyle.RUNIC_SERPENT, FinialType.CROSSBARS, CenterEmblem.VALKNUT, CornerStyle.RUNIC_BINDS, branchNotches = true, rayBurst = false, runering = true, lineWidth = 3.8f),
            StavePreset("Кельтская вязь", "🌿", "Плетеные сакральные ленты, трикветр и спирали Одина", SketchStyle.CELTIC_KNOT, CanvasTheme.DARK_SLATE, FrameStyle.NORDIC_BRAID, FinialType.SPIRALS, CenterEmblem.TRIQUETRA, CornerStyle.NORSE_KNOTS, branchNotches = false, rayBurst = false, runering = false, lineWidth = 3.2f),
            StavePreset("Дотворк-оберег", "✨", "Сакральная геометрия точек, золотой уголь и ромб Ингуз", SketchStyle.DOTWORK, CanvasTheme.GOLDEN_EMBER, FrameStyle.SOLAR_CIRCLE, FinialType.CIRCLES_DOTS, CenterEmblem.INGUZ_DIAMOND, CornerStyle.SUN_RAYS, branchNotches = false, rayBurst = true, runering = false, lineWidth = 3.0f),
            StavePreset("Манускрипт скальда", "📜", "Древний пергамент с руническим обрамлением и строгой геометрией", SketchStyle.STRICT, CanvasTheme.ANCIENT_PARCHMENT, FrameStyle.SOLAR_CIRCLE, FinialType.DEFAULT, CenterEmblem.SOLAR_CROSS, CornerStyle.NONE, branchNotches = false, rayBurst = false, runering = true, lineWidth = 2.6f),
            StavePreset("Тёмный Блэкворк", "⚔️", "Массивные стрелы Тюра, кованые заклёпки и геометрия", SketchStyle.BLACKWORK, CanvasTheme.DARK_SLATE, FrameStyle.SACRED_OCTAGON, FinialType.ARROWS, CenterEmblem.INGUZ_DIAMOND, CornerStyle.SHIELD_STUDS, branchNotches = true, rayBurst = false, runering = false, lineWidth = 5.2f)
        )
    }

    val config = remember(
        selectedStyle, selectedTheme, lineWidth, hasFrameCircle, frameStyle, finialType, centerEmblem,
        cornerStyle, hasSymmetryAccents, hasBranchNotches, hasRayBurst, hasRunering, hasGlowEffect,
        wobbleAmount, seed, isStencil, hasVolumetricShading, hasTextureGrain, runeChiselDepth
    ) {
        SketchConfig(
            style = selectedStyle,
            theme = selectedTheme,
            lineWidth = lineWidth,
            hasFrameCircle = hasFrameCircle,
            frameStyle = frameStyle,
            finialType = finialType,
            centerEmblem = centerEmblem,
            cornerStyle = cornerStyle,
            hasSymmetryAccents = hasSymmetryAccents,
            hasBranchNotches = hasBranchNotches,
            hasRayBurst = hasRayBurst,
            hasRunering = hasRunering,
            hasGlowEffect = hasGlowEffect,
            wobbleAmount = wobbleAmount,
            seed = seed,
            isStencil = isStencil,
            hasVolumetricShading = hasVolumetricShading,
            hasTextureGrain = hasTextureGrain,
            runeChiselDepth = runeChiselDepth
        )
    }

    val composedStave by remember(runes, selectedLayout, seed) {
        derivedStateOf {
            StaveComposer.compose(runes, selectedLayout, seed)
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Генератор эскизов SVG") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Preview Canvas Card
            val canvasBgColor = if (isStencil) {
                Color.White
            } else {
                try {
                    Color(android.graphics.Color.parseColor(config.effectiveTheme.bgHex))
                } catch (_: Exception) {
                    MaterialTheme.colorScheme.surface
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = canvasBgColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Zoomable Interactive Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
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
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            RunicCanvas(
                                stave = composedStave,
                                config = config,
                                animationKey = animTriggerKey,
                                animationDurationMs = userSettings.animationSpeedMs
                            )
                        }

                        // Zoom indicator badge
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title and short description under preview image (User requirement: под ним же должно быть название и краткое описание)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (runes.isNotEmpty()) {
                                "${selectedLayout.titleRu} (${runes.joinToString(" • ") { it.nameRu }})"
                            } else {
                                selectedLayout.titleRu
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isStencil) Color.Black else MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${selectedStyle.titleRu} • ${selectedLayout.titleRu}: ${selectedStyle.descriptionRu}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isStencil) Color.DarkGray else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { animTriggerKey++ },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("replay_rune_animation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Начертать заново (Магия)")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Variation Stepper & Randomizer Row
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Вариация начертания (#${Math.abs(seed) % 1000}):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    seed -= 1L
                                    animTriggerKey++
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Предыдущая вариация")
                            }
                            IconButton(
                                onClick = {
                                    seed += 1L
                                    animTriggerKey++
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Следующая вариация")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            val rng = Random()
                            seed = rng.nextLong()
                            animTriggerKey++
                            // Randomize complementary ornaments for rich variations
                            if (rng.nextBoolean()) {
                                finialType = FinialType.values().random()
                                frameStyle = FrameStyle.values().random()
                                centerEmblem = CenterEmblem.values().random()
                                cornerStyle = CornerStyle.values().random()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("randomize_seed_button")
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Сгенерировать случайную вариацию")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets Carousel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Готовые сакральные пресеты:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(presets) { p ->
                            val isMatch = selectedStyle == p.style && frameStyle == p.frame && finialType == p.finial
                            Card(
                                onClick = {
                                    selectedStyle = p.style
                                    selectedTheme = p.theme
                                    frameStyle = p.frame
                                    finialType = p.finial
                                    centerEmblem = p.center
                                    cornerStyle = p.corner
                                    hasBranchNotches = p.branchNotches
                                    hasRayBurst = p.rayBurst
                                    hasRunering = p.runering
                                    lineWidth = p.lineWidth
                                    p.layout?.let { selectedLayout = it }
                                    animTriggerKey++
                                },
                                shape = RoundedCornerShape(16.dp),
                                border = if (isMatch) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMatch) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.width(170.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = p.icon, style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = p.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMatch) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = p.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isMatch) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Layout Type Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Макет композиции става:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StaveLayoutType.values().forEach { layout ->
                            FilterChip(
                                selected = selectedLayout == layout,
                                onClick = { selectedLayout = layout },
                                shape = RoundedCornerShape(16.dp),
                                label = { Text(layout.titleRu, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Style Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Художественный стиль начертания:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SketchStyle.values().forEach { style ->
                            FilterChip(
                                selected = selectedStyle == style,
                                onClick = { selectedStyle = style },
                                shape = RoundedCornerShape(16.dp),
                                label = { Text(style.titleRu, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Text(
                        text = selectedStyle.descriptionRu,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Theme Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Атмосфера и материал холста:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CanvasTheme.values().forEach { theme ->
                            FilterChip(
                                selected = selectedTheme == theme,
                                onClick = {
                                    selectedTheme = theme
                                    if (theme == CanvasTheme.STENCIL) {
                                        isStencil = true
                                    } else if (isStencil && theme != CanvasTheme.STENCIL) {
                                        isStencil = false
                                    }
                                    animTriggerKey++
                                },
                                shape = RoundedCornerShape(16.dp),
                                label = { Text(theme.titleRu, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ornaments & Decorations Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Орнаменты и декоративные элементы:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 1. Frame Style
                    Text(
                        text = "Обрамление става:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FrameStyle.values().forEach { fs ->
                            FilterChip(
                                selected = frameStyle == fs && hasFrameCircle,
                                onClick = {
                                    frameStyle = fs
                                    hasFrameCircle = fs != FrameStyle.NONE
                                },
                                shape = RoundedCornerShape(14.dp),
                                label = { Text(fs.titleRu, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Finials
                    Text(
                        text = "Окончания линий (Наконечники):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FinialType.values().forEach { ft ->
                            FilterChip(
                                selected = finialType == ft,
                                onClick = { finialType = ft },
                                shape = RoundedCornerShape(14.dp),
                                label = { Text(ft.titleRu, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Central Sacred Emblem
                    Text(
                        text = "Центральный сакральный символ:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CenterEmblem.values().forEach { ce ->
                            FilterChip(
                                selected = centerEmblem == ce,
                                onClick = { centerEmblem = ce },
                                shape = RoundedCornerShape(14.dp),
                                label = { Text(ce.titleRu, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Corner Accents
                    Text(
                        text = "Угловые обережные акценты:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CornerStyle.values().forEach { cs ->
                            FilterChip(
                                selected = cornerStyle == cs,
                                onClick = { cornerStyle = cs },
                                shape = RoundedCornerShape(14.dp),
                                label = { Text(cs.titleRu, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sliders and Toggles
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Толщина линий: ${String.format("%.1f", lineWidth)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = lineWidth,
                        onValueChange = { lineWidth = it },
                        valueRange = 1.5f..8.0f
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Рукотворная неровность: ${(wobbleAmount * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = wobbleAmount,
                        onValueChange = { wobbleAmount = it },
                        valueRange = 0.0f..0.8f
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Засечки на ветвях става", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = hasBranchNotches, onCheckedChange = { hasBranchNotches = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Лучистая аура става", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = hasRayBurst, onCheckedChange = { hasRayBurst = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Симметричные узлы", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = hasSymmetryAccents, onCheckedChange = { hasSymmetryAccents = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Круг-обрамление", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = hasFrameCircle, onCheckedChange = { hasFrameCircle = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Кольцо Старшего Футарка", style = MaterialTheme.typography.bodyMedium)
                            Text("24 сакральные руны по периметру", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = hasRunering,
                            onCheckedChange = {
                                hasRunering = it
                                animTriggerKey++
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Сакральное свечение (Aura Glow)", style = MaterialTheme.typography.bodyMedium)
                            Text("Мягкий ореол вокруг рун и линий", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = hasGlowEffect, onCheckedChange = { hasGlowEffect = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Объёмная гравировка и тени (3D Volume)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Штриховка, фаски и падающие тени", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = hasVolumetricShading, onCheckedChange = { hasVolumetricShading = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Текстура бумаги и камня (Grain & Vignette)", style = MaterialTheme.typography.bodyMedium)
                            Text("Микро-зернистость пергамента и глубина", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = hasTextureGrain, onCheckedChange = { hasTextureGrain = it })
                    }

                    if (hasVolumetricShading) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Глубина резьбы рун: ${String.format("%.1f", runeChiselDepth)}x",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Slider(
                            value = runeChiselDepth,
                            onValueChange = { runeChiselDepth = it },
                            valueRange = 0.4f..2.2f
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Режим трафарета (Stencil)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Инверсия на белый фон для трансфера", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isStencil,
                            onCheckedChange = {
                                isStencil = it
                                if (it) selectedTheme = CanvasTheme.STENCIL
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Export Settings & Resolution
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Разрешение PNG для экспорта:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1024, 2048, 4096).forEach { res ->
                            FilterChip(
                                selected = targetResolution == res,
                                onClick = { targetResolution = res },
                                shape = RoundedCornerShape(16.dp),
                                label = { Text("${res}px") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (isExporting) return@Button
                                isExporting = true
                                coroutineScope.launch {
                                    try {
                                        val bitmap = SvgStaveRenderer.renderToBitmap(composedStave, config, targetResolution)
                                        val file = SvgStaveRenderer.savePngForSharing(context, bitmap, "runic_stave_${targetResolution}px.png")
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "image/png"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Поделиться эскизом PNG"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Ошибка экспорта: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isExporting = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f),
                            enabled = !isExporting
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PNG ($targetResolution)")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        val svgText = SvgStaveRenderer.renderSvg(composedStave, config)
                                        val file = SvgStaveRenderer.saveSvgForSharing(context, svgText, "runic_stave.svg")
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "image/svg+xml"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Поделиться SVG файлом"))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Ошибка SVG: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SVG вектор")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action to Try-On Screen
            Button(
                onClick = {
                    onNavigateToTryOn(runeIds, selectedLayout.name, seed, selectedStyle.name)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("go_to_tryon_button")
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Перейти к примерке на теле")
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
