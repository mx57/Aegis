package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.gemini.GeminiTattooService
import com.example.data.local.AppDatabase
import com.example.data.local.AppSettings
import com.example.data.local.StaveRecord
import com.example.data.local.TattooConceptRecord
import com.example.data.local.UserSettings
import com.example.data.model.Rune
import com.example.data.model.TattooConcept
import com.example.data.repository.RuneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RuneViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RuneRepository(application)
    private val database = AppDatabase.getInstance(application)
    private val dao = database.staveDao()
    private val tattooConceptDao = database.tattooConceptDao()
    private val geminiTattooService = GeminiTattooService()
    val appSettings = AppSettings(application)

    private val _runes = MutableStateFlow<List<Rune>>(emptyList())
    val runes: StateFlow<List<Rune>> = _runes.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isGeneratingTattooConcepts = MutableStateFlow(false)
    val isGeneratingTattooConcepts: StateFlow<Boolean> = _isGeneratingTattooConcepts.asStateFlow()

    private val _generationStatusMessage = MutableStateFlow("")
    val generationStatusMessage: StateFlow<String> = _generationStatusMessage.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError: StateFlow<String?> = _generationError.asStateFlow()

    private val _testConnectionState = MutableStateFlow<TestConnectionStatus>(TestConnectionStatus.Idle)
    val testConnectionState: StateFlow<TestConnectionStatus> = _testConnectionState.asStateFlow()

    fun isGeminiConfigured(customKey: String? = null): Boolean {
        return geminiTattooService.isApiKeyConfigured(customKey ?: userSettings.value.geminiApiKey)
    }

    val tattooConcepts: StateFlow<List<TattooConcept>> = tattooConceptDao.getAll()
        .map { records -> records.map { it.toModel() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userSettings: StateFlow<UserSettings> = appSettings.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings(
            hasCompletedOnboarding = true,
            defaultFuthark = "elder",
            defaultStyle = "ORNAMENTAL",
            darkTheme = true,
            language = "ru",
            animationSpeedMs = 4200
        )
    )

    val favorites: StateFlow<List<StaveRecord>> = dao.getFavorites().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history: StateFlow<List<StaveRecord>> = dao.getAllHistory().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadData()
        seedInitialConceptsIfEmpty()
    }

    private fun seedInitialConceptsIfEmpty() {
        viewModelScope.launch {
            // Check if any concepts exist
            val initial = geminiTattooService.createCuratedConcepts(
                userPrompt = "Защита духа и сакральная гармония",
                placement = "Предплечье (внутренняя сторона)",
                style = "Nordic Dotwork & Blackwork",
                selectedRunes = emptyList(),
                allAvailableRunes = _runes.value,
                isAiGenerated = false
            )
            val records = initial.map { TattooConceptRecord.fromModel(it) }
            tattooConceptDao.insertAll(records)
        }
    }

    fun generateTattooConcepts(
        userPrompt: String,
        placement: String = "Предплечье",
        style: String = "Nordic Dotwork & Blackwork",
        selectedRunes: List<Rune> = emptyList()
    ) {
        viewModelScope.launch {
            _isGeneratingTattooConcepts.value = true
            _generationError.value = null
            _generationStatusMessage.value = "Взывание к мудрости скальдов через Gemini..."

            try {
                val result = geminiTattooService.generateTattooConcepts(
                    userPrompt = userPrompt,
                    placementPreference = placement,
                    stylePreference = style,
                    selectedRunes = selectedRunes,
                    allAvailableRunes = _runes.value,
                    customApiKey = userSettings.value.geminiApiKey
                )

                result.onSuccess { concepts ->
                    if (concepts.isNotEmpty()) {
                        val records = concepts.map { TattooConceptRecord.fromModel(it) }
                        tattooConceptDao.insertAll(records)
                        _generationStatusMessage.value = "Успешно создано ${concepts.size} концепта!"
                    } else {
                        _generationError.value = "Не удалось сгенерировать концепты. Попробуйте изменить запрос."
                    }
                }.onFailure { ex ->
                    _generationError.value = ex.localizedMessage ?: "Неизвестная ошибка генерации"
                }
            } catch (e: Exception) {
                _generationError.value = "Ошибка: ${e.message}"
            } finally {
                _isGeneratingTattooConcepts.value = false
            }
        }
    }

    fun testGeminiApiKey(explicitKey: String? = null) {
        viewModelScope.launch {
            _testConnectionState.value = TestConnectionStatus.Testing
            val keyToTest = explicitKey?.trim() ?: userSettings.value.geminiApiKey
            val result = geminiTattooService.testConnection(keyToTest)
            result.onSuccess { msg ->
                _testConnectionState.value = TestConnectionStatus.Success(msg)
            }.onFailure { ex ->
                _testConnectionState.value = TestConnectionStatus.Error(ex.localizedMessage ?: "Ошибка проверки ключа")
            }
        }
    }

    fun clearTestConnectionState() {
        _testConnectionState.value = TestConnectionStatus.Idle
    }

    fun saveGeminiApiKey(key: String) {
        viewModelScope.launch {
            appSettings.setGeminiApiKey(key)
            testGeminiApiKey(key)
        }
    }

    fun clearGeminiApiKey() {
        viewModelScope.launch {
            appSettings.setGeminiApiKey("")
            _testConnectionState.value = TestConnectionStatus.Idle
        }
    }

    fun toggleFavoriteTattooConcept(conceptId: String, currentIsFavorite: Boolean) {
        viewModelScope.launch {
            tattooConceptDao.setFavorite(conceptId, !currentIsFavorite)
        }
    }

    fun deleteTattooConcept(conceptId: String) {
        viewModelScope.launch {
            tattooConceptDao.deleteById(conceptId)
        }
    }

    fun clearGenerationError() {
        _generationError.value = null
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            val loaded = repository.loadRunes()
            _runes.value = loaded
            _isLoading.value = false
        }
    }

    fun saveToHistory(
        title: String,
        runeIds: List<String>,
        layoutType: String,
        styleType: String,
        seed: Long,
        lineWidth: Float = 3.5f,
        hasFrameCircle: Boolean = true,
        isStencil: Boolean = false,
        isFavorite: Boolean = false
    ) {
        viewModelScope.launch {
            val record = StaveRecord(
                title = title,
                runeIdsCsv = runeIds.joinToString(","),
                layoutType = layoutType,
                styleType = styleType,
                seed = seed,
                lineWidth = lineWidth,
                hasFrameCircle = hasFrameCircle,
                isStencil = isStencil,
                isFavorite = isFavorite
            )
            dao.insert(record)
        }
    }

    fun toggleFavorite(recordId: Long, currentIsFavorite: Boolean) {
        viewModelScope.launch {
            dao.setFavorite(recordId, !currentIsFavorite)
        }
    }

    fun deleteRecord(recordId: Long) {
        viewModelScope.launch {
            dao.deleteById(recordId)
        }
    }

    fun getRunesByIds(ids: List<String>): List<Rune> {
        val currentRunes = _runes.value
        val map = currentRunes.associateBy { it.id }
        return ids.mapNotNull { map[it] }
    }
}

sealed class TestConnectionStatus {
    object Idle : TestConnectionStatus()
    object Testing : TestConnectionStatus()
    data class Success(val message: String) : TestConnectionStatus()
    data class Error(val message: String) : TestConnectionStatus()
}
