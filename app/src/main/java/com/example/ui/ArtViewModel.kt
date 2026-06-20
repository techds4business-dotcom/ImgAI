package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.ArtRepository
import com.example.data.GeneratedImage
import com.example.ui.presets.StylePresets
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ArtTab {
    CREATE, EDIT
}

data class ArtUiState(
    val prompt: String = "",
    val selectedPresetId: String = "watercolor", // default watercolor preset is beautiful
    val aspectRatio: String = "1:1",
    val imageSize: String = "1K",
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val errorMessage: String? = null,
    val currentArt: GeneratedImage? = null,
    val historyList: List<GeneratedImage> = emptyList(),
    val activeTab: ArtTab = ArtTab.CREATE,
    val selectedBaseImage: GeneratedImage? = null,
    val editPrompt: String = ""
)

class ArtViewModel(
    application: Application,
    private val repository: ArtRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ArtUiState())
    val uiState: StateFlow<ArtUiState> = _uiState.asStateFlow()

    private var loadingJob: Job? = null

    private val loadingMessages = listOf(
        "Calling the digital muses...",
        "Blending oil pigments on digital canvas...",
        "Splashing dynamic watercolors...",
        "Weaving vintage synthwave dimensions...",
        "Squeezing fresh pixel juice...",
        "Molding three-dimensional clay models...",
        "Unfolding delicate origami shapes...",
        "Academic pencils sketching outline detail...",
        "Harnessing gemini light rays...",
        "Structuring realistic depth shadows...",
        "Rendering masterclass finish..."
    )

    init {
        // Collect history flow
        viewModelScope.launch {
            repository.allImages.collectLatest { list ->
                _uiState.update { it.copy(historyList = list) }
            }
        }
    }

    private fun startLoadingAnimation() {
        loadingJob?.cancel()
        loadingJob = viewModelScope.launch {
            var index = 0
            while (true) {
                _uiState.update { it.copy(loadingMessage = loadingMessages[index]) }
                delay(3000)
                index = (index + 1) % loadingMessages.size
            }
        }
    }

    private fun stopLoadingAnimation() {
        loadingJob?.cancel()
        loadingJob = null
    }

    fun updatePrompt(prompt: String) {
        _uiState.update { it.copy(prompt = prompt) }
    }

    fun updateEditPrompt(prompt: String) {
        _uiState.update { it.copy(editPrompt = prompt) }
    }

    fun selectPreset(presetId: String) {
        _uiState.update { it.copy(selectedPresetId = presetId) }
    }

    fun selectAspectRatio(ratio: String) {
        _uiState.update { it.copy(aspectRatio = ratio) }
    }

    fun selectImageSize(size: String) {
        _uiState.update { it.copy(imageSize = size) }
    }

    fun setTab(tab: ArtTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun selectBaseImage(image: GeneratedImage?) {
        _uiState.update { it.copy(selectedBaseImage = image) }
    }

    fun clearResultArt() {
        _uiState.update { it.copy(currentArt = null) }
    }

    fun generateArt() {
        val state = _uiState.value
        if (state.prompt.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a creative prompt first.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        startLoadingAnimation()

        viewModelScope.launch {
            try {
                val preset = StylePresets.getById(state.selectedPresetId)
                val result = repository.generateImage(
                    prompt = state.prompt,
                    selectedPreset = preset.name,
                    presetPromptSuffix = preset.promptSuffix,
                    aspectRatio = state.aspectRatio,
                    imageSize = state.imageSize
                )
                _uiState.update { it.copy(currentArt = result, isLoading = false) }
            } catch (e: Exception) {
                val userFriendlyError = if (e.message?.contains("API key") == true || e.message?.contains("GEMINI_API_KEY") == true) {
                    "API Key is missing or invalid. Please check your Secrets panel configuration."
                } else if (e.message?.contains("safety") == true || e.message?.contains("unsupported") == true) {
                    "Content was blocked by safety filters or contains unsupported prompt words."
                } else {
                    e.message ?: "Unknown generation error. Please try again."
                }
                _uiState.update { it.copy(errorMessage = userFriendlyError, isLoading = false) }
            } finally {
                stopLoadingAnimation()
            }
        }
    }

    fun editArt() {
        val state = _uiState.value
        val baseImage = state.selectedBaseImage
        if (baseImage == null) {
            _uiState.update { it.copy(errorMessage = "Please select a base image from the history below to edit.") }
            return
        }
        if (state.editPrompt.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please describe the edits you wish to execute.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        startLoadingAnimation()

        viewModelScope.launch {
            try {
                val result = repository.editImage(
                    baseImageRecord = baseImage,
                    editPrompt = state.editPrompt,
                    aspectRatio = state.aspectRatio,
                    imageSize = state.imageSize
                )
                _uiState.update { it.copy(currentArt = result, isLoading = false) }
            } catch (e: Exception) {
                val userFriendlyError = if (e.message?.contains("API key") == true) {
                    "API Key is missing or invalid. Please check your Secrets panel configuration."
                } else {
                    e.message ?: "Unknown editing error. Please try again."
                }
                _uiState.update { it.copy(errorMessage = userFriendlyError, isLoading = false) }
            } finally {
                stopLoadingAnimation()
            }
        }
    }

    fun deleteHistoryItem(image: GeneratedImage) {
        viewModelScope.launch {
            repository.deleteImage(image)
            if (_uiState.value.currentArt?.id == image.id) {
                _uiState.update { it.copy(currentArt = null) }
            }
            if (_uiState.value.selectedBaseImage?.id == image.id) {
                _uiState.update { it.copy(selectedBaseImage = null) }
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _uiState.update {
                it.copy(
                    currentArt = null,
                    selectedBaseImage = null
                )
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

class ArtViewModelFactory(
    private val application: Application,
    private val repository: ArtRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArtViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArtViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
