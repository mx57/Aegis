package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.engine.SvgStaveRenderer
import com.example.engine.vectorizer.CurveFittingType
import com.example.engine.vectorizer.ImageVectorizer
import com.example.engine.vectorizer.SampleRasterSketches
import com.example.engine.vectorizer.ThresholdMode
import com.example.engine.vectorizer.TracingMode
import com.example.engine.vectorizer.VectorizationResult
import com.example.engine.vectorizer.VectorizerBg
import com.example.engine.vectorizer.VectorizerConfig
import com.example.engine.vectorizer.VectorizerPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VectorizerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Source Bitmap state (defaults to Vegvisir sample)
    var activeSampleId by remember { mutableStateOf("vegvisir") }
    var currentSourceBitmap by remember {
        mutableStateOf<Bitmap?>(SampleRasterSketches.createSampleBitmap("vegvisir"))
    }

    // 2. Configuration & Preset state
    var selectedPreset by remember { mutableStateOf(VectorizerPreset.ULTRA_FIDELITY_100) }
    var config by remember { mutableStateOf(VectorizerConfig.fromPreset(VectorizerPreset.ULTRA_FIDELITY_100)) }

    // 3. Engine result state
    var vectorResult by remember { mutableStateOf<VectorizationResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // 4. View Mode: 0=Vector SVG, 1=Split Compare (Before/After), 2=Binary Mask, 3=Original Raster
    var viewTab by remember { mutableIntStateOf(0) }
    var splitRatio by remember { mutableFloatStateOf(0.50f) }

    // 5. Zoom & Pan State
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // 6. Accordion expanded sections (0: Mode/Layers, 1: Preprocess, 2: Splines/Bézier, 3: Styling)
    var expandedSection by remember { mutableIntStateOf(0) }

    // 7. Modals
    var showXmlDialog by remember { mutableStateOf(false) }
    var showSampleDialog by remember { mutableStateOf(false) }

    // Android Photo Picker launcher (zero permissions needed)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val bitmap = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { stream: InputStream ->
                            BitmapFactory.decodeStream(stream)
                        }
                    }
                    if (bitmap != null) {
                        currentSourceBitmap = bitmap
                        activeSampleId = "custom"
                        Toast.makeText(context, "Изображение успешно загружено", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Trigger vectorization when bitmap or config changes
    fun runVectorization() {
        val src = currentSourceBitmap ?: return
        isProcessing = true
        coroutineScope.launch {
            try {
                val res = ImageVectorizer.vectorize(src, config)
                vectorResult = res
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка векторизации: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isProcessing = false
            }
        }
    }

    // Auto-run when bitmap or config changes
    LaunchedEffect(currentSourceBitmap, config) {
        runVectorization()
    }

    // Sharing action
    fun shareSvg() {
        val res = vectorResult ?: return
        coroutineScope.launch {
            try {
                val file = SvgStaveRenderer.saveSvgForSharing(context, res.svgString, "vectorized_stave.svg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/svg+xml"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Векторный SVG эскиз")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Экспорт SVG файла"))
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка сохранения SVG: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun sharePngPreview() {
        val res = vectorResult ?: return
        coroutineScope.launch {
            try {
                val file = SvgStaveRenderer.savePngForSharing(context, res.vectorPreviewBitmap, "vector_preview.png")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Поделиться PNG превью"))
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка сохранения PNG: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun copySvgXmlToClipboard() {
        val res = vectorResult ?: return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("SVG Vector", res.svgString)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "SVG XML скопирован в буфер обмена (${res.fileSizeKb.roundToInt()} КБ)", Toast.LENGTH_SHORT).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Векторизатор в SVG",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            ) {
                                Text(
                                    text = "100% PRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Конвертация растровых рисунков и эскизов",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSampleDialog = true },
                        modifier = Modifier.testTag("vectorizer_samples_btn")
                    ) {
                        Icon(Icons.Default.Image, contentDescription = "Выбрать образец", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.testTag("vectorizer_pick_photo_btn")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Загрузить фото/эскиз", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { showXmlDialog = true },
                        modifier = Modifier.testTag("vectorizer_code_btn")
                    ) {
                        Icon(Icons.Default.Code, contentDescription = "Просмотр SVG XML", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { shareSvg() },
                        modifier = Modifier.testTag("vectorizer_share_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Экспорт SVG", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Processing indicator
            if (isProcessing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 1. Telemetry and Performance Status Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val simText = vectorResult?.let { "%.1f%%".format(it.similarityPercent) } ?: "—"
                    val pathsText = vectorResult?.let { "${it.pathCount}" } ?: "—"
                    val nodesText = vectorResult?.let { "${it.nodeCount}" } ?: "—"
                    val sizeText = vectorResult?.let { "%.1f КБ".format(it.fileSizeKb) } ?: "—"
                    val timeText = vectorResult?.let { "${it.processingTimeMs}мс" } ?: "—"

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Схожесть", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(simText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Путей", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(pathsText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Узлов", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(nodesText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Размер SVG", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(sizeText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Время", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(timeText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // 2. Interactive Preview Canvas with Split Slider & Zoom
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column {
                    // View tabs
                    ScrollableTabRow(
                        selectedTabIndex = viewTab,
                        edgePadding = 8.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        divider = {}
                    ) {
                        val tabs = listOf(
                            "✨ Векторный SVG",
                            "🌓 До / После (Split)",
                            "🔍 Маска бинаризации",
                            "🖼️ Исходный растр"
                        )
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = viewTab == index,
                                onClick = { viewTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (viewTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }

                    // Main Canvas Area
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.0f)
                            .background(Color(0xFF0A0C10))
                            .clipToBounds()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    zoomScale = (zoomScale * zoom).coerceIn(1.0f, 5.0f)
                                    val maxOffset = 300f * (zoomScale - 1f)
                                    panOffset = Offset(
                                        (panOffset.x + pan.x).coerceIn(-maxOffset, maxOffset),
                                        (panOffset.y + pan.y).coerceIn(-maxOffset, maxOffset)
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val canvasW = maxWidth
                        val canvasH = maxHeight

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = zoomScale
                                    scaleY = zoomScale
                                    translationX = panOffset.x
                                    translationY = panOffset.y
                                }
                        ) {
                            when (viewTab) {
                                0 -> {
                                    // Full Vector Preview
                                    val previewBitmap = vectorResult?.vectorPreviewBitmap
                                    if (previewBitmap != null) {
                                        Image(
                                            bitmap = previewBitmap.asImageBitmap(),
                                            contentDescription = "Vector Preview",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }

                                1 -> {
                                    // Interactive Split Comparison (Before on Left, After on Right)
                                    val srcBitmap = currentSourceBitmap
                                    val vecBitmap = vectorResult?.vectorPreviewBitmap

                                    if (srcBitmap != null && vecBitmap != null) {
                                        // 1. Right side (Vector)
                                        Image(
                                            bitmap = vecBitmap.asImageBitmap(),
                                            contentDescription = "Vectorized",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )

                                        // 2. Left side (Original Raster) clipped to splitRatio
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(splitRatio)
                                                .clipToBounds()
                                        ) {
                                            Image(
                                                bitmap = srcBitmap.asImageBitmap(),
                                                contentDescription = "Original Raster",
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .width(canvasW),
                                                contentScale = ContentScale.Fit
                                            )
                                        }

                                        // Split line divider
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(2.dp)
                                                .offset { IntOffset((canvasW.toPx() * splitRatio).toInt(), 0) }
                                                .background(MaterialTheme.colorScheme.primary)
                                        )

                                        // Split Handle Pill
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary,
                                            shadowElevation = 6.dp,
                                            modifier = Modifier
                                                .size(34.dp)
                                                .offset {
                                                    IntOffset(
                                                        (canvasW.toPx() * splitRatio - 17.dp.toPx()).toInt(),
                                                        (canvasH.toPx() * 0.5f - 17.dp.toPx()).toInt()
                                                    )
                                                }
                                                .pointerInput(Unit) {
                                                    detectDragGestures { change, dragAmount ->
                                                        change.consume()
                                                        val newRatio = splitRatio + (dragAmount.x / canvasW.toPx())
                                                        splitRatio = newRatio.coerceIn(0.05f, 0.95f)
                                                    }
                                                }
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    "◀▶",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                2 -> {
                                    // Binary Mask
                                    val maskBitmap = vectorResult?.binaryMaskBitmap
                                    if (maskBitmap != null) {
                                        Image(
                                            bitmap = maskBitmap.asImageBitmap(),
                                            contentDescription = "Binary Mask",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }

                                3 -> {
                                    // Original Raster
                                    val srcBitmap = currentSourceBitmap
                                    if (srcBitmap != null) {
                                        Image(
                                            bitmap = srcBitmap.asImageBitmap(),
                                            contentDescription = "Original Raster",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                }
                            }
                        }

                        // Zoom Reset Button (top-end corner)
                        if (zoomScale > 1.05f || panOffset != Offset.Zero) {
                            IconButton(
                                onClick = {
                                    zoomScale = 1.0f
                                    panOffset = Offset.Zero
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Сброс зума",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Hint overlay at bottom of canvas
                        Surface(
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                            color = Color.Black.copy(alpha = 0.55f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = if (viewTab == 1) "Тяните ◀▶ для сравнения оригинала и вектора" else "Масштабируйте жестом pinch (до 500%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // 3. Quick Presets Row
            Text(
                text = "Быстрые пресеты векторизации:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 14.dp, top = 8.dp, bottom = 4.dp)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                VectorizerPreset.values().forEach { preset ->
                    val isSelected = selectedPreset == preset
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedPreset = preset
                            config = VectorizerConfig.fromPreset(preset)
                        },
                        label = { Text(preset.titleRu, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Accordion Fine-Tuning Settings
            Text(
                text = "Параметры и тонкая настройка:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
            )

            // Category 1: Tracing Mode & Layers
            SettingAccordionCard(
                title = "1. Алгоритм трассировки и слои",
                subtitle = config.mode.titleRu,
                icon = Icons.Default.Layers,
                isExpanded = expandedSection == 0,
                onToggle = { expandedSection = if (expandedSection == 0) -1 else 0 }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Режим извлечения векторов:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    TracingMode.values().forEach { mode ->
                        val isModeSelected = config.mode == mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isModeSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(
                                1.dp,
                                if (isModeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { config = config.copy(mode = mode) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = mode.titleRu,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isModeSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isModeSelected) {
                                        Spacer(modifier = Modifier.weight(1f))
                                        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = mode.descriptionRu,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    if (config.mode == TracingMode.MULTILAYER_TONAL) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Количество полутоновых слоёв:", style = MaterialTheme.typography.labelSmall)
                            Text("${config.tonalLayers} слоя", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = config.tonalLayers.toFloat(),
                            onValueChange = { config = config.copy(tonalLayers = it.roundToInt()) },
                            valueRange = 2f..8f,
                            steps = 5,
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }

            // Category 2: Preprocessing & Binarization
            SettingAccordionCard(
                title = "2. Препроцессинг и фильтрация растра",
                subtitle = "${config.thresholdMode.titleRu}, контраст ${(config.contrast * 100).toInt()}%",
                icon = Icons.Default.Tune,
                isExpanded = expandedSection == 1,
                onToggle = { expandedSection = if (expandedSection == 1) -1 else 1 }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Метод расчёта порога:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ThresholdMode.values().forEach { tMode ->
                            val isSel = config.thresholdMode == tMode
                            FilterChip(
                                selected = isSel,
                                onClick = { config = config.copy(thresholdMode = tMode) },
                                label = { Text(tMode.titleRu.substringBefore(" ("), style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (config.thresholdMode == ThresholdMode.MANUAL) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Порог яркости (Threshold):", style = MaterialTheme.typography.labelSmall)
                            Text("${config.manualThreshold}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Slider(
                            value = config.manualThreshold.toFloat(),
                            onValueChange = { config = config.copy(manualThreshold = it.roundToInt()) },
                            valueRange = 0f..255f
                        )
                    }

                    // Contrast
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Контрастность изображения:", style = MaterialTheme.typography.labelSmall)
                        Text("%.2fx".format(config.contrast), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = config.contrast,
                        onValueChange = { config = config.copy(contrast = it) },
                        valueRange = 0.5f..2.5f
                    )

                    // Denoise Blur Radius
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Шумоподавление (Denoise):", style = MaterialTheme.typography.labelSmall)
                        Text("${config.denoiseRadius} px", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = config.denoiseRadius.toFloat(),
                        onValueChange = { config = config.copy(denoiseRadius = it.roundToInt()) },
                        valueRange = 0f..4f,
                        steps = 3
                    )

                    // Invert Polarity Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Инверсия полярности", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("Светлые штрихи на тёмном фоне", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = config.invertPolarity,
                            onCheckedChange = { config = config.copy(invertPolarity = it) }
                        )
                    }
                }
            }

            // Category 3: Approximation & Bézier Splines
            SettingAccordionCard(
                title = "3. Аппроксимация и кривые Безье",
                subtitle = "Точность ${(100 - config.detailLevel * 30).roundToInt().coerceIn(90, 100)}%, Безье: ${(config.smoothFactor * 100).toInt()}%",
                icon = Icons.Default.AutoAwesome,
                isExpanded = expandedSection == 2,
                onToggle = { expandedSection = if (expandedSection == 2) -1 else 2 }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Detail Level (Epsilon tolerance)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Уровень детализации (Fidelity):", style = MaterialTheme.typography.labelSmall)
                        val fidelityText = if (config.detailLevel <= 0.03f) "100% (Пиксель в пиксель)" else "%.2f (Сглаживание)".format(config.detailLevel)
                        Text(fidelityText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = config.detailLevel,
                        onValueChange = { config = config.copy(detailLevel = it) },
                        valueRange = 0.02f..0.80f
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Curve Fitting Type
                    Text("Геометрический тип сегментов:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CurveFittingType.values().forEach { cType ->
                            val isSel = config.curveFitting == cType
                            FilterChip(
                                selected = isSel,
                                onClick = { config = config.copy(curveFitting = cType) },
                                label = { Text(cType.titleRu.substringBefore(" ("), style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Smooth Factor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Коэффициент сглаживания Безье:", style = MaterialTheme.typography.labelSmall)
                        Text("${(config.smoothFactor * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = config.smoothFactor,
                        onValueChange = { config = config.copy(smoothFactor = it) },
                        valueRange = 0.0f..1.0f
                    )

                    // Corner preservation angle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Порог сохранения острых углов:", style = MaterialTheme.typography.labelSmall)
                        Text("${config.cornerThresholdAngle.toInt()}°", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = config.cornerThresholdAngle,
                        onValueChange = { config = config.copy(cornerThresholdAngle = it) },
                        valueRange = 25f..110f
                    )

                    // Despeckle Filter (minPathArea)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Фильтр артефактов (Despeckle):", style = MaterialTheme.typography.labelSmall)
                        Text("${config.minPathArea} px", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = config.minPathArea.toFloat(),
                        onValueChange = { config = config.copy(minPathArea = it.roundToInt()) },
                        valueRange = 1f..25f,
                        steps = 23
                    )
                }
            }

            // Category 4: Styling & Colors
            SettingAccordionCard(
                title = "4. Цвета, штрих и экспорт SVG",
                subtitle = "Штрих ${config.strokeWidth}dp, фон ${config.canvasBackground.titleRu}",
                icon = Icons.Default.Palette,
                isExpanded = expandedSection == 3,
                onToggle = { expandedSection = if (expandedSection == 3) -1 else 3 }
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Stroke width
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Толщина штриха линии (для Centerline):", style = MaterialTheme.typography.labelSmall)
                        Text("%.1f dp".format(config.strokeWidth), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = config.strokeWidth,
                        onValueChange = { config = config.copy(strokeWidth = it) },
                        valueRange = 0.5f..8.0f
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Color swatches for stroke
                    Text("Цвет векторного штриха:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    val paletteColors = listOf(
                        "#E5C158" to "Сакральное Золото",
                        "#000000" to "Тату Блэкворк",
                        "#E2E8F0" to "Серебро Валькирий",
                        "#CD7F32" to "Древняя Бронза",
                        "#C83535" to "Охристая Киноварь"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        paletteColors.forEach { (hex, _) ->
                            val isChosen = config.strokeColorHex == hex
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                                    .border(
                                        width = if (isChosen) 3.dp else 1.dp,
                                        color = if (isChosen) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        config = config.copy(strokeColorHex = hex, fillColorHex = hex)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isChosen) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = if (hex == "#000000") Color.White else Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Background selection
                    Text("Фон SVG холста:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        VectorizerBg.values().forEach { bg ->
                            val isSel = config.canvasBackground == bg
                            FilterChip(
                                selected = isSel,
                                onClick = { config = config.copy(canvasBackground = bg) },
                                label = { Text(bg.titleRu.substringBefore(" ("), style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Main Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { shareSvg() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("export_svg_action_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Экспорт SVG", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { copySvgXmlToClipboard() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("copy_xml_action_btn"),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Копировать XML")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { sharePngPreview() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Поделиться PNG", style = MaterialTheme.typography.labelMedium)
                }

                OutlinedButton(
                    onClick = { showXmlDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Просмотр кода", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }

    // Modal Dialog: Sample Chooser
    if (showSampleDialog) {
        AlertDialog(
            onDismissRequest = { showSampleDialog = false },
            title = {
                Text("Выбор тестового образца эскиза", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    SampleRasterSketches.AVAILABLE_SAMPLES.forEach { sample ->
                        val isChosen = activeSampleId == sample.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isChosen) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(
                                1.dp,
                                if (isChosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    activeSampleId = sample.id
                                    currentSourceBitmap = SampleRasterSketches.createSampleBitmap(sample.id)
                                    showSampleDialog = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(sample.titleRu, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(sample.subtitleRu, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(sample.descriptionRu, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSampleDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    // Modal Dialog: Full SVG XML Code Inspector
    if (showXmlDialog) {
        val xmlCode = vectorResult?.svgString ?: ""
        AlertDialog(
            onDismissRequest = { showXmlDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("SVG Векторный Код", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${vectorResult?.pathCount ?: 0} путей, ${vectorResult?.nodeCount ?: 0} узлов, ${xmlCode.length} символов",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            text = {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0D1117),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = xmlCode,
                            color = Color(0xFF58A6FF),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        copySvgXmlToClipboard()
                        showXmlDialog = false
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Копировать XML")
                }
            },
            dismissButton = {
                TextButton(onClick = { showXmlDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }
}

@Composable
private fun SettingAccordionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }
                Icon(
                    if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                content()
            }
        }
    }
}
