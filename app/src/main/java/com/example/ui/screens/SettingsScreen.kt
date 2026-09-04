package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.AppSettings
import com.example.engine.SketchStyle
import com.example.ui.viewmodel.RuneViewModel
import com.example.ui.viewmodel.TestConnectionStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: RuneViewModel,
    onBack: () -> Unit,
    onResetOnboarding: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val userSettings by viewModel.userSettings.collectAsState()
    val testConnectionState by viewModel.testConnectionState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var apiKeyInput by remember(userSettings.geminiApiKey) { mutableStateOf(userSettings.geminiApiKey) }
    var isKeyVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки приложения") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Рунический строй по умолчанию",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Выбор исторического строя для транслитерации и генерации",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = userSettings.defaultFuthark == "elder",
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.appSettings.setDefaultFuthark("elder")
                                }
                            },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            label = { Text("Старший Футарк (24)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = userSettings.defaultFuthark == "younger",
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.appSettings.setDefaultFuthark("younger")
                                }
                            },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            label = { Text("Младший Футарк (16)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Animation Speed Setting
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
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
                            text = "Скорость анимации высечения",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val speedSeconds = userSettings.animationSpeedMs / 1000f
                        Text(
                            text = "%.1f сек".format(speedSeconds),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    val speedDescription = when {
                        userSettings.animationSpeedMs <= 2000 -> "⚡ Быстрое начертание (динамично)"
                        userSettings.animationSpeedMs <= 5000 -> "✨ Сбалансированная сакральная резка"
                        userSettings.animationSpeedMs <= 10000 -> "🧘 Медитативное глубокое высечение"
                        userSettings.animationSpeedMs <= 15000 -> "👑 Эпический ритуал (детализированное вырезание)"
                        else -> "🔥 Монументальное плазменное начертание (до 20 сек)"
                    }

                    Text(
                        text = speedDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = userSettings.animationSpeedMs.toFloat().coerceIn(1000f, 20000f),
                        onValueChange = { newMs ->
                            coroutineScope.launch {
                                viewModel.appSettings.setAnimationSpeedMs(newMs.toInt())
                            }
                        },
                        valueRange = 1000f..20000f,
                        steps = 18, // increments of 1000ms
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 с (Быстро)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("10 с (Медитация)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Text("20 с (Эпично)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            // Name Stave Decorative Elements Toggle Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Элементы генерации по имени",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Отключайте декоративные элементы по отдельности или оставьте только чистую руну без лишних обрамлений:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.appSettings.setNameStaveAllElements(false)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🛡️ Только руна", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.appSettings.setNameStaveAllElements(true)
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("✨ Включить всё", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val elements = listOf(
                        Triple("⭕ Внешняя защитная рама", "Защитный двойной круг и священный обод", Pair(AppSettings.KEY_NAME_STAVE_SHOW_FRAME, userSettings.nameStaveShowFrame)),
                        Triple("ᚠ Рунический круг Футарка", "Концентрический венец из 24 рун Старшего Футарка", Pair(AppSettings.KEY_NAME_STAVE_SHOW_RUNERING, userSettings.nameStaveShowRuneRing)),
                        Triple("🌳 Центральная эмблема", "Сакральный знак в ядре става (Иггдрасиль)", Pair(AppSettings.KEY_NAME_STAVE_SHOW_CENTER_EMBLEM, userSettings.nameStaveShowCenterEmblem)),
                        Triple("☀️ Сакральные лучи", "Радиальные вспышки и солнечный ореол силы", Pair(AppSettings.KEY_NAME_STAVE_SHOW_RAY_BURST, userSettings.nameStaveShowRayBurst)),
                        Triple("🔱 Наконечники ветвей", "Трезубцы и копья на внешних полюсах рун", Pair(AppSettings.KEY_NAME_STAVE_SHOW_FINIALS, userSettings.nameStaveShowFinials)),
                        Triple("🌿 Боковые насечки на осях", "Ритмичные насечки на центральных стеблях", Pair(AppSettings.KEY_NAME_STAVE_SHOW_BRANCH_NOTCHES, userSettings.nameStaveShowBranchNotches)),
                        Triple("🛡️ Угловые узлы и вязь", "Скандинавские защитные плетения по углам", Pair(AppSettings.KEY_NAME_STAVE_SHOW_CORNER_ACCENTS, userSettings.nameStaveShowCornerAccents)),
                        Triple("💫 3D-гравировка и свечение", "Объемные тени, светотеневые фаски и аура", Pair(AppSettings.KEY_NAME_STAVE_SHOW_GLOW, userSettings.nameStaveShowGlow))
                    )

                    elements.forEachIndexed { index, (title, desc, keyPair) ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 4.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        viewModel.appSettings.setNameStaveElement(keyPair.first, !keyPair.second)
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = keyPair.second,
                                onCheckedChange = { isChecked ->
                                    coroutineScope.launch {
                                        viewModel.appSettings.setNameStaveElement(keyPair.first, isChecked)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Стиль эскизов по умолчанию",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Художественная стилизация рунических знаков",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SketchStyle.values().forEach { style ->
                            FilterChip(
                                selected = userSettings.defaultStyle == style.name,
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.appSettings.setDefaultStyle(style.name)
                                    }
                                },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                label = { Text(style.titleRu, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(
                    1.dp,
                    if (viewModel.isGeminiConfigured(apiKeyInput))
                        MaterialTheme.colorScheme.outline
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (viewModel.isGeminiConfigured(apiKeyInput))
                        MaterialTheme.colorScheme.surface
                    else
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.VpnKey,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Google Gemini AI (Генератор эскизов)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (viewModel.isGeminiConfigured(apiKeyInput))
                                        "● Ключ активен (gemini-3.5-flash / 2.5-flash)"
                                    else
                                        "⚠️ Требуется API-ключ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (viewModel.isGeminiConfigured(apiKeyInput))
                                        Color(0xFF4CAF50)
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "API-ключ используется для генерации сакральных тату-эскизов и толкования рунических формул с помощью Gemini. Получить бесплатный ключ можно на сайте aistudio.google.com.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = {
                            apiKeyInput = it
                            viewModel.clearTestConnectionState()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("API-ключ Gemini (AIzaSy...)") },
                        placeholder = { Text("Вставьте ваш API-ключ") },
                        singleLine = true,
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = {
                            Icon(
                                Icons.Default.Key,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                    Icon(
                                        imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isKeyVisible) "Скрыть" else "Показать",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        val clip = clipboardManager.getText()?.text?.trim().orEmpty()
                                        if (clip.isNotEmpty()) {
                                            apiKeyInput = clip
                                            Toast.makeText(context, "Вставлено из буфера", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentPaste,
                                        contentDescription = "Вставить",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                viewModel.saveGeminiApiKey(apiKeyInput.trim())
                                Toast.makeText(context, "Ключ успешно сохранен", Toast.LENGTH_SHORT).show()
                            },
                            enabled = apiKeyInput.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Сохранить", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.testGeminiApiKey(apiKeyInput.trim())
                            },
                            enabled = apiKeyInput.isNotBlank() && testConnectionState !is TestConnectionStatus.Testing,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            if (testConnectionState is TestConnectionStatus.Testing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Проверка...", style = MaterialTheme.typography.labelMedium)
                            } else {
                                Text("Проверить связь", style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        if (userSettings.geminiApiKey.isNotBlank() || apiKeyInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    viewModel.clearGeminiApiKey()
                                    apiKeyInput = ""
                                    Toast.makeText(context, "Ключ удален", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Удалить ключ",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    // Test Connection Feedback
                    when (val status = testConnectionState) {
                        is TestConnectionStatus.Success -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1B5E20).copy(alpha = 0.35f),
                                border = BorderStroke(1.dp, Color(0xFF4CAF50)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = status.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFA5D6A7)
                                    )
                                }
                            }
                        }
                        is TestConnectionStatus.Error -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = status.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "О приложении «Рунический Став»",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Версия 1.0.0 (Offline-First)\nВекторный геометрический генератор скандинавских ставов и тату-эскизов.\nРаботает полностью автономно на вашем устройстве без обращения к сети.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.appSettings.setOnboardingDone(false)
                                onResetOnboarding()
                            }
                        },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Повторить просмотр обучения (Онбординг)")
                    }
                }
            }
        }
    }
}
