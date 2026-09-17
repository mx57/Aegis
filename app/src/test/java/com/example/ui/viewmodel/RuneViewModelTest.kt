package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RuneViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var application: Application
    private lateinit var viewModel: RuneViewModel

    @Before
    fun setUp() = runTest(testDispatcher) {
        Dispatchers.setMain(testDispatcher)
        application = ApplicationProvider.getApplicationContext()
        viewModel = RuneViewModel(application)
        viewModel.appSettings.setGeminiApiKey("")
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        testDispatcher.scheduler.advanceTimeBy(6000L)
        testDispatcher.scheduler.advanceUntilIdle()
        Dispatchers.resetMain()
    }

    @Test
    fun isGeminiConfigured_withValidCustomKey_returnsTrue() = runTest(testDispatcher) {
        val result = viewModel.isGeminiConfigured("custom_valid_api_key_123")
        assertTrue(result)
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        testDispatcher.scheduler.advanceTimeBy(6000L)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun isGeminiConfigured_withNullCustomKeyAndNoSavedKey_returnsFalse() = runTest(testDispatcher) {
        val collectJob = backgroundScope.launch { viewModel.userSettings.collect {} }
        viewModel.appSettings.setGeminiApiKey("")
        viewModel.userSettings.first { it.geminiApiKey.isEmpty() }

        val result = viewModel.isGeminiConfigured(null)
        assertFalse(result)
        collectJob.cancel()
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        testDispatcher.scheduler.advanceTimeBy(6000L)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun isGeminiConfigured_withEmptyCustomKeyAndNoSavedKey_returnsFalse() = runTest(testDispatcher) {
        val collectJob = backgroundScope.launch { viewModel.userSettings.collect {} }
        viewModel.appSettings.setGeminiApiKey("")
        viewModel.userSettings.first { it.geminiApiKey.isEmpty() }

        val result = viewModel.isGeminiConfigured("")
        assertFalse(result)
        collectJob.cancel()
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        testDispatcher.scheduler.advanceTimeBy(6000L)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun isGeminiConfigured_withWhitespaceCustomKeyAndNoSavedKey_returnsFalse() = runTest(testDispatcher) {
        val collectJob = backgroundScope.launch { viewModel.userSettings.collect {} }
        viewModel.appSettings.setGeminiApiKey("")
        viewModel.userSettings.first { it.geminiApiKey.isEmpty() }

        val result = viewModel.isGeminiConfigured("   \t\n  ")
        assertFalse(result)
        collectJob.cancel()
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        testDispatcher.scheduler.advanceTimeBy(6000L)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun isGeminiConfigured_withSavedKeyAndNullCustomKey_returnsTrue() = runTest(testDispatcher) {
        val collectJob = backgroundScope.launch { viewModel.userSettings.collect {} }
        val savedKey = "my_persisted_gemini_key"
        viewModel.appSettings.setGeminiApiKey(savedKey)
        viewModel.userSettings.first { it.geminiApiKey == savedKey }

        val result = viewModel.isGeminiConfigured(null)
        assertTrue(result)
        collectJob.cancel()
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        testDispatcher.scheduler.advanceTimeBy(6000L)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun isGeminiConfigured_withSavedKeyAndEmptyCustomKey_returnsTrue() = runTest(testDispatcher) {
        val collectJob = backgroundScope.launch { viewModel.userSettings.collect {} }
        val savedKey = "my_persisted_gemini_key"
        viewModel.appSettings.setGeminiApiKey(savedKey)
        viewModel.userSettings.first { it.geminiApiKey == savedKey }

        val result = viewModel.isGeminiConfigured("")
        assertTrue(result)
        collectJob.cancel()
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        testDispatcher.scheduler.advanceTimeBy(6000L)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun isGeminiConfigured_withValidCustomKeyOverridingNoSavedKey_returnsTrue() = runTest(testDispatcher) {
        val collectJob = backgroundScope.launch { viewModel.userSettings.collect {} }
        viewModel.appSettings.setGeminiApiKey("")
        viewModel.userSettings.first { it.geminiApiKey.isEmpty() }

        val result = viewModel.isGeminiConfigured("override_custom_key")
        assertTrue(result)
        collectJob.cancel()
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        testDispatcher.scheduler.advanceTimeBy(6000L)
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun isGeminiConfigured_afterClearingSavedKey_returnsFalse() = runTest(testDispatcher) {
        val collectJob = backgroundScope.launch { viewModel.userSettings.collect {} }
        val tempKey = "temp_key"
        viewModel.appSettings.setGeminiApiKey(tempKey)
        viewModel.userSettings.first { it.geminiApiKey == tempKey }
        assertTrue(viewModel.isGeminiConfigured(null))

        viewModel.appSettings.setGeminiApiKey("")
        viewModel.userSettings.first { it.geminiApiKey.isEmpty() }

        val result = viewModel.isGeminiConfigured(null)
        assertFalse(result)
        collectJob.cancel()
        viewModel.viewModelScope.coroutineContext.cancelChildren()
        testDispatcher.scheduler.advanceTimeBy(6000L)
        testDispatcher.scheduler.advanceUntilIdle()
    }
}
