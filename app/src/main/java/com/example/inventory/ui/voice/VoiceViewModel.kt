package com.example.inventory.ui.voice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.repository.InventoryRepository
import com.example.inventory.data.voice.VoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val voiceRepository: VoiceRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(VoiceUiState())
    val uiState: StateFlow<VoiceUiState> = _uiState.asStateFlow()

    fun startListening() {
        _uiState.value = VoiceUiState(status = VoiceStatus.Listening)
        voiceRepository.startListening(
            onResult = { text -> processCommand(text) },
            onError = { message ->
                _uiState.value = VoiceUiState(status = VoiceStatus.Error, message = message)
            }
        )
    }

    fun stopListening() {
        voiceRepository.stopListening()
        _uiState.value = VoiceUiState()
    }

    private fun processCommand(text: String) {
        _uiState.value = VoiceUiState(status = VoiceStatus.Processing, recognizedText = text)
        val command = Regex("перемести\\s+(.+?)\\s+в\\s+(.+)", RegexOption.IGNORE_CASE)
            .find(text.trim())
        if (command == null) {
            showError(text, "Скажите: «перемести вещь в комнату или контейнер»")
            return
        }
        val thing = command.groupValues[1]
        val destination = command.groupValues[2]
        viewModelScope.launch {
            val result = inventoryRepository.moveThing(thing, destination)
            val success = result.startsWith("Вещь «") && result.contains("перемещена")
            _uiState.value = VoiceUiState(
                status = if (success) VoiceStatus.Success else VoiceStatus.Error,
                recognizedText = text,
                message = result
            )
            voiceRepository.speak(result)
        }
    }

    private fun showError(text: String, message: String) {
        _uiState.value = VoiceUiState(VoiceStatus.Error, text, message)
        voiceRepository.speak(message)
    }

    override fun onCleared() {
        voiceRepository.release()
        super.onCleared()
    }
}
