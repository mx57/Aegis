package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Rune
import com.example.engine.RuneLoreRepository

/**
 * Rich, comprehensive interpretation view from the sacred rune handbook.
 * Shows direct vs reversed aspects, deities, elements, practical advice,
 * spheres of life (business, love), magical properties, and tattoo symbolism.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RuneInterpretationCard(
    rune: Rune,
    isReversed: Boolean,
    onToggleReversed: () -> Unit,
    onCreateStave: () -> Unit,
    onPutBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lore = RuneLoreRepository.getLore(rune.id)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("rune_interpretation_card"),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44E5C158)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF141722)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // 1. Header: Name, Phonetics, Element & Aett
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${rune.nameRu} • ${rune.nameEn}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = rune.unicode,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFE082)
                        )
                    }

                    Text(
                        text = "${lore.aett} • Стихия: ${lore.element}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Покровитель: ${lore.deity} • Звук: [${rune.phonetic}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                // Position badge (Direct or Reversed) with toggle button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isReversed) Color(0x33FF6E40) else Color(0x33E5C158),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isReversed) Color(0xFFFF6E40) else Color(0xFFE5C158)
                    ),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    if (isReversed) Color(0xFFFF6E40) else Color(0xFFE5C158),
                                    CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isReversed) "Перевёрнутое" else "Прямое",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isReversed) Color(0xFFFF9E80) else Color(0xFFFFE082)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Keywords badges
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                rune.keywordsRu.forEach { kw ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x22E5C158))
                            .border(0.5.dp, Color(0x44E5C158), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = kw,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFFE082)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0x22E5C158))
            Spacer(modifier = Modifier.height(14.dp))

            // 3. Oracle Voice (Divination message from handbook)
            Text(
                text = "Голос Оракула (${if (isReversed) "Перевёрнутое положение" else "Прямое положение"}):",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isReversed) rune.divinationReversed else rune.divinationDirect,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Practical Advice of the Norns
            InterpretationSection(
                icon = Icons.Default.AutoAwesome,
                iconColor = Color(0xFFE5C158),
                title = "Совет Оракула:",
                content = if (isReversed) lore.adviceReversed else lore.adviceDirect
            )

            // 5. Warning of the Norns
            InterpretationSection(
                icon = Icons.Default.Warning,
                iconColor = Color(0xFFFFB74D),
                title = "Предостережение:",
                content = lore.warning
            )

            // 6. Spheres of Life
            InterpretationSection(
                icon = Icons.Default.Work,
                iconColor = Color(0xFF81D4FA),
                title = "Дела, карьера и достаток:",
                content = lore.businessAspect
            )

            InterpretationSection(
                icon = Icons.Default.Favorite,
                iconColor = Color(0xFFFF8A80),
                title = "Отношения и союз сердец:",
                content = lore.loveAspect
            )

            // 7. Magic and Tattoo
            InterpretationSection(
                icon = Icons.Default.Shield,
                iconColor = Color(0xFFA5D6A7),
                title = "Магическое применение:",
                content = rune.magicUse
            )

            InterpretationSection(
                icon = Icons.Default.Brush,
                iconColor = Color(0xFFCE93D8),
                title = "Символика в татуировке:",
                content = rune.tattooSymbolism
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions: Reverse Toggle, Put Back, Create Stave
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onToggleReversed,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x44E5C158)),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isReversed) "К прямому" else "К обратному",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                OutlinedButton(
                    onClick = onPutBack,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("В мешочек", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onCreateStave,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Создать амулет с этой руной", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun InterpretationSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    content: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1B1E2B),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0x22FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(18.dp)
                    .padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = iconColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
