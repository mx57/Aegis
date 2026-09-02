package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppSettings
import com.example.data.local.StaveRecord
import com.example.data.local.UserSettings
import com.example.data.model.Rune
import com.example.data.repository.RuneRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RuneViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RuneRepository(application)
    private val database = AppDatabase.getInstance(application)
    private val dao = database.staveDao()
    val appSettings = AppSettings(application)

    private val _runes = MutableStateFlow<List<Rune>>(emptyList())
    val runes: StateFlow<List<Rune>> = _runes.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val userSettings: StateFlow<UserSettings> = appSettings.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings(
            hasCompletedOnboarding = true,
            defaultFuthark = "elder",
            defaultStyle = "ORNAMENTAL",
            darkTheme = true,
            language = "ru"
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
