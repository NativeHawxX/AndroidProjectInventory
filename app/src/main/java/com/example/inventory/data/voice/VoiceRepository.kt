package com.example.inventory.data.voice

interface VoiceRepository {
    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit)
    fun stopListening()
    fun speak(text: String)
    fun release()
}
