package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.data.model.Rune
import com.example.engine.SketchConfig
import com.example.engine.SketchStyle
import com.example.engine.StaveComposer
import com.example.engine.StaveLayoutType
import com.example.ui.components.BodySilhouetteCanvas
import com.example.ui.components.BodyZone
import com.example.ui.components.RunicCanvas
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TryOnScreen(
    runeIds: List<String>,
    layoutTypeName: String,
    seed: Long,
    styleName: String,
    allRunes: List<Rune>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val runeMap = remember(allRunes) { allRunes.associateBy { it.id } }
    val runes = remember(runeIds, runeMap) { runeIds.mapNotNull { runeMap[it] } }

    val layoutType = remember(layoutTypeName) {
        try { StaveLayoutType.valueOf(layoutTypeName) } catch (_: Exception) { StaveLayoutType.BINDRUNE }
    }
    val sketchStyle = remember(styleName) {
        try { SketchStyle.valueOf(styleName) } catch (_: Exception) { SketchStyle.ORNAMENTAL }
    }

    var selectedZone by remember { mutableStateOf(BodyZone.FOREARM) }
    var userPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Transform Gesture States
    var offset by remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableFloatStateOf(1.0f) }
    var rotation by remember { mutableFloatStateOf(0f) }

    // Tattoo visual tuning
    var opacity by remember { mutableFloatStateOf(0.85f) }
    var isBlackInk by remember { mutableStateOf(true) }

    // Zero-permission Android Photo Picker for user's own photo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            userPhotoUri = uri
        }
    }

    val config = remember(sketchStyle, seed) {
        SketchConfig(
            style = sketchStyle,
            lineWidth = 3.6f,
            hasFrameCircle = true,
            seed = seed
        )
    }

    val composedStave = remember(runes, layoutType, seed) {
        StaveComposer.compose(runes, layoutType, seed)
    }

    // Dynamic size in cm calculation
    val estimatedSizeCm = remember(scale, selectedZone) {
        val baseSizeCm = selectedZone.zoneWidthCm * 0.6f
        val calculated = baseSizeCm * scale
        String.format(java.util.Locale.US, "%.1f", calculated)
    }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Примерка на теле") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            offset = Offset.Zero
                            scale = 1.0f
                            rotation = 0f
                        }
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Сбросить позицию")
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
            // Interactive Fitting Stage Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.82f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Background Layer: Silhouette or Photo
                if (userPhotoUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(context)
                                .data(userPhotoUri)
                                .crossfade(true)
                                .build()
                        ),
                        contentDescription = "Фото пользователя",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    BodySilhouetteCanvas(
                        zone = selectedZone,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Foreground Layer: Interactive Stave with Gesture Detection
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            rotationZ = rotation,
                            alpha = opacity
                        )
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, rotate ->
                                offset += pan
                                scale = (scale * zoom).coerceIn(0.35f, 3.0f)
                                rotation += rotate
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    RunicCanvas(
                        stave = composedStave,
                        config = config,
                        overrideColor = if (isBlackInk) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                    )
                }

                // Size Badge Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Размер: ~$estimatedSizeCm × $estimatedSizeCm см",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Перемещайте, масштабируйте двумя пальцами и вращайте став",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Body Zones Selector
            Text(
                text = "Анатомическая зона:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(BodyZone.values()) { zone ->
                    FilterChip(
                        selected = userPhotoUri == null && selectedZone == zone,
                        onClick = {
                            selectedZone = zone
                            userPhotoUri = null
                        },
                        shape = RoundedCornerShape(16.dp),
                        label = { Text(zone.titleRu) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User Photo Button & Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Своё фото из галереи", style = MaterialTheme.typography.labelMedium)
                }

                if (userPhotoUri != null) {
                    OutlinedButton(
                        onClick = { userPhotoUri = null },
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.weight(0.6f)
                    ) {
                        Text("Силуэт")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Appearance Controls: Opacity & Ink Color
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Прозрачность татуировки на коже: ${(opacity * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = opacity,
                        onValueChange = { opacity = it },
                        valueRange = 0.2f..1.0f
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBlackInk) "Пигмент: Чёрный тату-инкер" else "Пигмент: Золотой оберег",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedButton(
                            onClick = { isBlackInk = !isBlackInk },
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Icon(Icons.Default.InvertColors, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isBlackInk) "Золото" else "Чёрный")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
