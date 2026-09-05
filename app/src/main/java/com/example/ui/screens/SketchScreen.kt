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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.data.gemini.GeminiTattooService
import com.example.data.local.GeminiArtworkRecord
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
import com.example.ui.components.FullScreenArtworkDialog
import com.example.ui.components.FullScreenSketchDialog
import com.example.ui.components.RunicCanvas
import com.example.ui.viewmodel.RuneViewModel
import com.example.ui.viewmodel.TestConnectionStatus
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
    val layout: StaveLayoutType? = null,
    val elementScale: Float = 1.0f
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SketchScreen(
    runeIds: List<String>,
    layoutTypeName: String,
    allRunes: List<Rune>,
    viewModel: RuneViewModel? = null,
    onBack: () -> Unit,
    onNavigateToTryOn: (runeIds: List<String>, layoutType: String, seed: Long, style: String) -> Unit,
    onNavigateToAITattoo: () -> Unit = {},
    onNavigateToVectorizer: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

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
    var elementScale by remember { mutableFloatStateOf(1.0f) }
    var isStencil by remember { mutableStateOf(false) }
    var seed by remember { mutableLongStateOf(4242L) }
    var animTriggerKey by remember { mutableIntStateOf(0) }
    var targetResolution by remember { mutableIntStateOf(2048) }
    var isExporting by remember { mutableStateOf(false) }
    var isFullScreenOpen by remember { mutableStateOf(false) }
    var activeTab by remember { mutableIntStateOf(0) }

    // Gemini Photorealistic Artwork Generation State
    var showGeminiDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var selectedArtworkForDialog by remember { mutableStateOf<GeminiArtworkRecord?>(null) }
    var userStyleNote by remember { mutableStateOf("") }
    var isPromptExpanded by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf("") }
    var showApiKeyText by remember { mutableStateOf(false) }

    val isGeneratingArtwork = viewModel?.isGeneratingArtwork?.collectAsState()?.value ?: false
    val latestGeneratedArtwork = viewModel?.latestGeneratedArtwork?.collectAsState()?.value
    val artworkGenerationError = viewModel?.artworkGenerationError?.collectAsState()?.value
    val testConnectionState = viewModel?.testConnectionState?.collectAsState()?.value ?: TestConnectionStatus.Idle

    LaunchedEffect(latestGeneratedArtwork) {
        if (latestGeneratedArtwork != null) {
            selectedArtworkForDialog = latestGeneratedArtwork
            viewModel?.clearLatestGeneratedArtwork()
        }
    }

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
            StavePreset("Молот Тора (Мьёльнир)", "🔨", "Громовой молот с рунами молний, шипованным поясом и защитным кольцом", SketchStyle.VIKING_CHAIN, CanvasTheme.GOLDEN_EMBER, FrameStyle.SPIKED_CHAIN, FinialType.ARROWS, CenterEmblem.MJOLNIR, CornerStyle.SHIELD_STUDS, branchNotches = true, rayBurst = true, runering = true, lineWidth = 3.6f),
            StavePreset("Ворон Одина (Хугин)", "🦅", "Священный вестник Асгарда с распахнутыми крыльями и сакральным оком", SketchStyle.ORNAMENTAL, CanvasTheme.DARK_SLATE, FrameStyle.CELESTIAL_ASTROLABE, FinialType.SPIRALS, CenterEmblem.RAVEN_ODIN, CornerStyle.NORSE_KNOTS, branchNotches = true, rayBurst = true, runering = true, lineWidth = 3.0f),
            StavePreset("Древо Иггдрасиль", "🌳", "Мировое Древо, 9 миров, сакральные источники Норн и переплетённые ветви", SketchStyle.EMERALD_BRONZE, CanvasTheme.EMERALD_PATINA, FrameStyle.YGGDRASIL_BRANCHES, FinialType.CIRCLES_DOTS, CenterEmblem.YGGDRASIL_TREE, CornerStyle.NORSE_KNOTS, branchNotches = true, rayBurst = true, runering = true, lineWidth = 3.2f),
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
        wobbleAmount, seed, isStencil, hasVolumetricShading, hasTextureGrain, runeChiselDepth, elementScale
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
            runeChiselDepth = runeChiselDepth,
            elementScale = elementScale
        )
    }

    val composedStave by remember(runes, selectedLayout, seed) {
        derivedStateOf {
            StaveComposer.compose(runes, selectedLayout, seed)
        }
    }

    val scrollState = rememberScrollState()

    fun exportPng(res: Int) {
        if (isExporting) return
        isExporting = true
        coroutineScope.launch {
            try {
                val bitmap = SvgStaveRenderer.renderToBitmap(composedStave, config, res)
                val file = SvgStaveRenderer.savePngForSharing(context, bitmap, "runic_stave_${res}px.png")
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
    }

    fun exportSvg() {
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
    }

    if (isFullScreenOpen) {
        FullScreenSketchDialog(
            stave = composedStave,
            config = config,
            animationKey = animTriggerKey,
            animationDurationMs = userSettings.animationSpeedMs,
            title = if (runes.isNotEmpty()) "${selectedLayout.titleRu} (${runes.joinToString(" • ") { it.nameRu }})" else selectedLayout.titleRu,
            subtitle = "${selectedStyle.titleRu} • ${selectedTheme.titleRu}",
            onDismiss = { isFullScreenOpen = false },
            onReplayAnimation = { animTriggerKey++ },
            onExportPng = { exportPng(targetResolution) },
            onExportSvg = { exportSvg() }
        )
    }

    if (selectedArtworkForDialog != null) {
        val currentArtwork = selectedArtworkForDialog!!
        FullScreenArtworkDialog(
            artwork = currentArtwork,
            onDismiss = { selectedArtworkForDialog = null },
            onNavigateToTryOn = {
                selectedArtworkForDialog = null
                onNavigateToTryOn(runeIds, currentArtwork.layoutType, currentArtwork.seed, currentArtwork.styleName)
            },
            onOpenGallery = {
                selectedArtworkForDialog = null
                onNavigateToAITattoo()
            }
        )
    }

    if (isGeneratingArtwork) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF10121A)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Синтез фотореалистичного эскиза",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gemini 2.5 Flash визуализирует детали става, сакральную геометрию и металлическую гравировку. Эскиз автоматически сохраняется в галерею...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFB0BEC5),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    if (artworkGenerationError != null) {
        AlertDialog(
            onDismissRequest = { viewModel?.clearLatestGeneratedArtwork() },
            title = { Text("Сообщение генератора", fontWeight = FontWeight.Bold) },
            text = { Text(artworkGenerationError ?: "") },
            confirmButton = {
                Button(onClick = { viewModel?.clearLatestGeneratedArtwork() }) {
                    Text("Понятно")
                }
            }
        )
    }

    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ключ Gemini API", style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Для фотореалистичной генерации эскизов требуется API-ключ Google AI Studio (бесплатно на aistudio.google.com):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("AIzaSy...") },
                        singleLine = true,
                        visualTransformation = if (showApiKeyText) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showApiKeyText = !showApiKeyText }) {
                                Icon(
                                    if (showApiKeyText) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { viewModel?.testGeminiApiKey(apiKeyInput) },
                            enabled = apiKeyInput.isNotBlank()
                        ) {
                            Text("Проверить", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    when (val status = testConnectionState) {
                        is TestConnectionStatus.Success -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(status.message, style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                        }
                        is TestConnectionStatus.Error -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(status.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                        is TestConnectionStatus.Testing -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Проверка ключа...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        else -> {}
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (apiKeyInput.isNotBlank()) {
                            viewModel?.saveGeminiApiKey(apiKeyInput.trim())
                        }
                        showApiKeyDialog = false
                        showGeminiDialog = true
                    }
                ) {
                    Text("Сохранить и продолжить")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showApiKeyDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showGeminiDialog) {
        val calculatedPrompt = remember(composedStave, config, runes, userStyleNote) {
            GeminiTattooService().buildPhotorealisticPrompt(
                stave = composedStave,
                config = config,
                runes = runes,
                userStyleNote = userStyleNote.ifBlank { null }
            )
        }

        AlertDialog(
            onDismissRequest = { showGeminiDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Фотореалистичный эскиз (Gemini)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "AI-генерация на основе элементов става",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Stave Composition Breakdown Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Параметры става для Gemini:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Эмблема: ${centerEmblem.titleRu} (Масштаб ${(elementScale * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• Руны: ${if (runes.isNotEmpty()) runes.joinToString(" • ") { "${it.unicode} ${it.nameRu}" } else "Геометрический узел"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• Геометрия: ${selectedLayout.titleRu}, ветвей: ${composedStave.strokes.size}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• Обрамление: ${frameStyle.titleRu}, углы: ${cornerStyle.titleRu}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "• Базовый стиль: ${selectedStyle.titleRu}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Художественный акцент стиля:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    // Preset style chips
                    val stylePresets = listOf(
                        "⚜️ Золото и обсидиан" to "3D embossed carved gold, polished obsidian background, rim lighting",
                        "⚔️ Серебро Валькирий" to "Luminescent mithril silver, deep celestial shadow, sharp metallic facets",
                        "🖋️ Блэкворк и дотворк" to "Nordic blackwork tattoo style, fine dotwork stippling, high contrast",
                        "🪨 Рунический камень" to "Ancient Viking runestone, weathered granite relief, carved ochre runes",
                        "🌿 Патинированная бронза" to "Ancient bronze medallion, emerald verdigris patina, museum artifact"
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        stylePresets.forEach { (label, promptAddon) ->
                            val isSelected = userStyleNote == promptAddon
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    userStyleNote = if (isSelected) "" else promptAddon
                                },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Expandable prompt inspection
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isPromptExpanded = !isPromptExpanded },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPromptExpanded) "Скрыть промпт" else "Показать промпт Gemini",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = if (isPromptExpanded) "▲" else "▼",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (isPromptExpanded) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = calculatedPrompt,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(calculatedPrompt))
                                        Toast.makeText(context, "Промпт скопирован", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Копировать", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showGeminiDialog = false
                        viewModel?.generatePhotorealisticSketch(
                            stave = composedStave,
                            config = config,
                            runes = runes,
                            userStyleNote = userStyleNote.ifBlank { null }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Создать эскиз", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showGeminiDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Генератор эскизов SVG", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (viewModel != null && !viewModel.isGeminiConfigured()) {
                                showApiKeyDialog = true
                            } else {
                                showGeminiDialog = true
                            }
                        },
                        modifier = Modifier.testTag("topbar_ai_tattoo_button")
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Фотореалистичный эскиз (Gemini)",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { isFullScreenOpen = true },
                        modifier = Modifier.testTag("topbar_fullscreen_button")
                    ) {
                        Icon(
                            Icons.Default.ZoomOutMap,
                            contentDescription = "На весь экран",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
                .padding(horizontal = 10.dp, vertical = 6.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sketch_preview_card"),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = canvasBgColor)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tap-to-Zoom Interactive Canvas Area (Spacious square preview preventing vertical clipping)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { isFullScreenOpen = true },
                        contentAlignment = Alignment.Center
                    ) {
                        RunicCanvas(
                            stave = composedStave,
                            config = config,
                            animationKey = animTriggerKey,
                            animationDurationMs = userSettings.animationSpeedMs,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Unobtrusive Fullscreen Hint Badge at Top End (Never overlaps top or bottom of stave)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isStencil) Color(0xEEFFFFFF) else Color(0xDD0D1117),
                            border = BorderStroke(1.dp, if (isStencil) Color.LightGray else Color(0x44E5C158)),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ZoomOutMap,
                                    contentDescription = "Во весь экран",
                                    tint = if (isStencil) Color.Black else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Зум",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isStencil) Color.Black else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Compact Title and Description
                    Text(
                        text = if (runes.isNotEmpty()) {
                            "${selectedLayout.titleRu} (${runes.joinToString(" • ") { it.nameRu }})"
                        } else {
                            selectedLayout.titleRu
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isStencil) Color.Black else MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = "${selectedStyle.titleRu} • ${selectedLayout.titleRu}: ${selectedStyle.descriptionRu}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isStencil) Color.DarkGray else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Integrated Compact Control Toolbar (Stepper, Randomize, Magic Draw, Fullscreen)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stepper for variation
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(horizontal = 2.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    seed -= 1L
                                    animTriggerKey++
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowLeft,
                                    contentDescription = "Предыдущая вариация",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "#${Math.abs(seed) % 1000}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                            IconButton(
                                onClick = {
                                    seed += 1L
                                    animTriggerKey++
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    Icons.Default.KeyboardArrowRight,
                                    contentDescription = "Следующая вариация",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Compact action icons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val rng = Random()
                                    seed = rng.nextLong()
                                    animTriggerKey++
                                    if (rng.nextBoolean()) {
                                        finialType = FinialType.values().random()
                                        frameStyle = FrameStyle.values().random()
                                        centerEmblem = CenterEmblem.values().random()
                                        cornerStyle = CornerStyle.values().random()
                                    }
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("randomize_seed_button")
                            ) {
                                Icon(
                                    Icons.Default.Casino,
                                    contentDescription = "Случайная вариация",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { animTriggerKey++ },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("replay_rune_animation_button")
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = "Начертать заново",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { isFullScreenOpen = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("open_fullscreen_button")
                            ) {
                                Icon(
                                    Icons.Default.ZoomOutMap,
                                    contentDescription = "На весь экран",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Element Scale Control Strip (Пропорциональный масштаб элементов става)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("element_scale_quick_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Straighten,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Масштаб элементов:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = {
                                elementScale = (elementScale - 0.10f).coerceIn(0.50f, 1.50f)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("−", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { elementScale = 1.0f }
                        ) {
                            Text(
                                text = "${(elementScale * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                elementScale = (elementScale + 0.10f).coerceIn(0.50f, 1.50f)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Text("+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        listOf(0.75f to "75%", 1.0f to "100%", 1.25f to "125%").forEach { (sc, label) ->
                            val isSel = Math.abs(elementScale - sc) < 0.04f
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable { elementScale = sc }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ✨ Gemini Photorealistic Artwork Generation Hero Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (viewModel != null && !viewModel.isGeminiConfigured()) {
                            showApiKeyDialog = true
                        } else {
                            showGeminiDialog = true
                        }
                    }
                    .testTag("gemini_photorealistic_hero_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                border = BorderStroke(
                    1.5.dp,
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            Color(0xFFFFD54F),
                            MaterialTheme.colorScheme.primary
                        )
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Фотореалистичный эскиз Gemini",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "2.5 Flash",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Синтез по деталям става с автосохранением в галерею",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Compact Category Navigation Row
            val tabs = listOf(
                "🔮 Пресеты",
                "🎨 Стиль & Фон",
                "⚔️ Символы",
                "⚙️ Параметры",
                "📤 Экспорт"
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(tabs.indices.toList()) { idx ->
                    FilterChip(
                        selected = activeTab == idx,
                        onClick = { activeTab = idx },
                        shape = RoundedCornerShape(12.dp),
                        label = { Text(tabs[idx], style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Active Tab Content Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    when (activeTab) {
                        0 -> {
                            // 🔮 Presets & Layouts
                            Text(
                                text = "Готовые сакральные пресеты:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                                            elementScale = p.elementScale
                                            p.layout?.let { selectedLayout = it }
                                            animTriggerKey++
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        border = if (isMatch) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isMatch) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier.width(150.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = p.icon, style = MaterialTheme.typography.titleSmall)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = p.title,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isMatch) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = p.description,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isMatch) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Макет композиции става:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                StaveLayoutType.values().forEach { layout ->
                                    FilterChip(
                                        selected = selectedLayout == layout,
                                        onClick = { selectedLayout = layout },
                                        shape = RoundedCornerShape(12.dp),
                                        label = { Text(layout.titleRu, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }

                        1 -> {
                            // 🎨 Style & Theme
                            Text(
                                text = "Художественный стиль начертания:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                SketchStyle.values().forEach { style ->
                                    FilterChip(
                                        selected = selectedStyle == style,
                                        onClick = { selectedStyle = style },
                                        shape = RoundedCornerShape(12.dp),
                                        label = { Text(style.titleRu, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            Text(
                                text = selectedStyle.descriptionRu,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Атмосфера и материал холста:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                        shape = RoundedCornerShape(12.dp),
                                        label = { Text(theme.titleRu, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }

                        2 -> {
                            // ⚔️ Ornaments & Center Emblems
                            Text(
                                text = "Центральный сакральный символ:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CenterEmblem.values().forEach { ce ->
                                    FilterChip(
                                        selected = centerEmblem == ce,
                                        onClick = { centerEmblem = ce },
                                        shape = RoundedCornerShape(12.dp),
                                        label = { Text(ce.titleRu, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Обрамление става:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FrameStyle.values().forEach { fs ->
                                    FilterChip(
                                        selected = frameStyle == fs && hasFrameCircle,
                                        onClick = {
                                            frameStyle = fs
                                            hasFrameCircle = fs != FrameStyle.NONE
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        label = { Text(fs.titleRu, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Наконечники ветвей:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                FinialType.values().forEach { ft ->
                                    FilterChip(
                                        selected = finialType == ft,
                                        onClick = { finialType = ft },
                                        shape = RoundedCornerShape(12.dp),
                                        label = { Text(ft.titleRu, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Угловые обережные акценты:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CornerStyle.values().forEach { cs ->
                                    FilterChip(
                                        selected = cornerStyle == cs,
                                        onClick = { cornerStyle = cs },
                                        shape = RoundedCornerShape(12.dp),
                                        label = { Text(cs.titleRu, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                        }

                        3 -> {
                            // ⚙️ Fine Parameters
                            // Масштаб элементов става (пропорции символов без нарушения целостности всего рисунка)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Масштаб элементов става:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "Размер рун и символов внутри защитного круга",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    "${(elementScale * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = elementScale,
                                onValueChange = { elementScale = it },
                                valueRange = 0.50f..1.50f,
                                steps = 19
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(0.70f to "Компакт (70%)", 1.0f to "Эталон (100%)", 1.30f to "Крупный (130%)").forEach { (scaleVal, label) ->
                                    FilterChip(
                                        selected = Math.abs(elementScale - scaleVal) < 0.05f,
                                        onClick = { elementScale = scaleVal },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Толщина линий:", style = MaterialTheme.typography.labelMedium)
                                Text("${String.format("%.1f", lineWidth)}px", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = lineWidth,
                                onValueChange = { lineWidth = it },
                                valueRange = 1.5f..8.0f
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Рукотворная неровность:", style = MaterialTheme.typography.labelMedium)
                                Text("${(wobbleAmount * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = wobbleAmount,
                                onValueChange = { wobbleAmount = it },
                                valueRange = 0.0f..0.8f
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("3D Гравировка и тени", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Switch(checked = hasVolumetricShading, onCheckedChange = { hasVolumetricShading = it })
                            }

                            if (hasVolumetricShading) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Глубина резьбы:", style = MaterialTheme.typography.labelSmall)
                                    Text("${String.format("%.1f", runeChiselDepth)}x", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
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
                                Text("Текстура камня/пергамента", style = MaterialTheme.typography.bodySmall)
                                Switch(checked = hasTextureGrain, onCheckedChange = { hasTextureGrain = it })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Сакральное свечение", style = MaterialTheme.typography.bodySmall)
                                Switch(checked = hasGlowEffect, onCheckedChange = { hasGlowEffect = it })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Кольцо Старшего Футарка (24 руны)", style = MaterialTheme.typography.bodySmall)
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
                                Text("Засечки на ветвях", style = MaterialTheme.typography.bodySmall)
                                Switch(checked = hasBranchNotches, onCheckedChange = { hasBranchNotches = it })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Лучистая аура става", style = MaterialTheme.typography.bodySmall)
                                Switch(checked = hasRayBurst, onCheckedChange = { hasRayBurst = it })
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Трафарет для трансфера (Stencil)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = isStencil,
                                    onCheckedChange = {
                                        isStencil = it
                                        if (it) selectedTheme = CanvasTheme.STENCIL
                                    }
                                )
                            }
                        }

                        4 -> {
                            // 📤 Export & Try-on
                            Text(
                                text = "Разрешение PNG:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(1024, 2048, 4096).forEach { res ->
                                    FilterChip(
                                        selected = targetResolution == res,
                                        onClick = { targetResolution = res },
                                        shape = RoundedCornerShape(12.dp),
                                        label = { Text("${res}px", style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { exportPng(targetResolution) },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.weight(1f),
                                    enabled = !isExporting
                                ) {
                                    if (isExporting) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                                    } else {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("PNG (${targetResolution})", style = MaterialTheme.typography.labelMedium)
                                    }
                                }

                                OutlinedButton(
                                    onClick = { exportSvg() },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("SVG вектор", style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (viewModel != null && !viewModel.isGeminiConfigured()) {
                                        showApiKeyDialog = true
                                    } else {
                                        showGeminiDialog = true
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("export_tab_gemini_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Создать фотореалистичный эскиз (Gemini)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    onNavigateToTryOn(runeIds, selectedLayout.name, seed, selectedStyle.name)
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("go_to_tryon_button")
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Перейти к примерке на теле", style = MaterialTheme.typography.labelMedium)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = onNavigateToVectorizer,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("sketch_vectorizer_button")
                            ) {
                                Icon(Icons.Default.Brush, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Векторизатор эскизов в SVG (100% точность)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick bottom actions bar if not on export tab
            if (activeTab != 4) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onNavigateToTryOn(runeIds, selectedLayout.name, seed, selectedStyle.name)
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("quick_tryon_button")
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Примерка на теле", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = { exportPng(targetResolution) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PNG", style = MaterialTheme.typography.labelSmall)
                    }

                    OutlinedButton(
                        onClick = { exportSvg() },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SVG", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
