package com.example.inventory.ui.voice

enum class VoiceStatus { Idle, Listening, Processing, Success, Error }

data class VoiceUiState(
    val status: VoiceStatus = VoiceStatus.Idle,
    val recognizedText: String = "",
    val message: String = ""
)
