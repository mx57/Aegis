package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.ComposedStave
import com.example.engine.SketchConfig

/**
 * Fullscreen Interactive Sketch Viewer.
 * Opened on tapping the stave preview. Supports fluid pinch-to-zoom (up to 8x),
 * panning, double-tap zoom toggle, animation replay, and export shortcuts.
 */
@Composable
fun FullScreenSketchDialog(
    stave: ComposedStave,
    config: SketchConfig,
    animationKey: Int,
    animationDurationMs: Int = 4000,
    title: String = "Эскиз гальдрастава",
    subtitle: String = "",
    onDismiss: () -> Unit,
    onReplayAnimation: () -> Unit,
    onExportPng: (() -> Unit)? = null,
    onExportSvg: (() -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
            val newScale = (scale * zoomChange).coerceIn(0.8f, 8.0f)
            scale = newScale
            if (scale > 1.0f) {
                val maxOffset = 600f * (scale - 1f)
                offset = Offset(
                    x = (offset.x + offsetChange.x).coerceIn(-maxOffset, maxOffset),
                    y = (offset.y + offsetChange.y).coerceIn(-maxOffset, maxOffset)
                )
            } else {
                offset = Offset.Zero
            }
        }

        val canvasBgColor = if (config.isStencil) {
            Color.White
        } else {
            try {
                Color(android.graphics.Color.parseColor(config.effectiveTheme.bgHex))
            } catch (_: Exception) {
                Color(0xFF0D1117)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(canvasBgColor)
                .testTag("fullscreen_sketch_container")
        ) {
            // Interactive Zoomable Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1.2f) {
                                    scale = 1f
                                    offset = Offset.Zero
                                } else {
                                    scale = 2.8f
                                }
                            }
                        )
                    }
                    .transformable(state = transformState),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    RunicCanvas(
                        stave = stave,
                        config = config,
                        animationKey = animationKey,
                        animationDurationMs = animationDurationMs,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Top Header Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 12.dp, vertical = 28.dp),
                shape = RoundedCornerShape(18.dp),
                color = if (config.isStencil) Color.White.copy(alpha = 0.92f) else Color(0xDD12151D),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (config.isStencil) Color.LightGray else Color(0x33E5C158)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("close_fullscreen_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = if (config.isStencil) Color.Black else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (config.isStencil) Color.Black else MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                        if (subtitle.isNotEmpty()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (config.isStencil) Color.DarkGray else Color.Gray,
                                maxLines = 1
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (scale > 1.05f) {
                            IconButton(
                                onClick = {
                                    scale = 1f
                                    offset = Offset.Zero
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomOutMap,
                                    contentDescription = "Сбросить масштаб",
                                    tint = if (config.isStencil) Color.Black else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onReplayAnimation,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Начертать заново",
                                tint = if (config.isStencil) Color.Black else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Floating Action Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Zoom pill indicator
                Surface(
                    shape = CircleShape,
                    color = if (config.isStencil) Color(0xEEFFFFFF) else Color(0xCC1A1E29),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (config.isStencil) Color.LightGray else Color(0x44E5C158)
                    ),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text(
                        text = if (scale > 1.05f) "Зум: %.1fx • Двойной тап для сброса".format(scale) else "Щипок для зума • Двойной тап для 2.8x",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (config.isStencil) Color.DarkGray else Color(0xFFE5C158),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }

                // Quick Export / Action Buttons in Fullscreen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    if (onExportPng != null) {
                        Button(
                            onClick = onExportPng,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("PNG", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    if (onExportSvg != null) {
                        OutlinedButton(
                            onClick = onExportSvg,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("SVG вектор", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Назад", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
