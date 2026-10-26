package com.example.inventory.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceRepositoryImpl(context: Context) : VoiceRepository {
    private val recognizer = if (SpeechRecognizer.isRecognitionAvailable(context)) {
        SpeechRecognizer.createSpeechRecognizer(context)
    } else null
    private var textToSpeech: TextToSpeech? = null
    private var listening = false

    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("ru", "RU")
            }
        }
    }

    override fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit) {
        val speechRecognizer = recognizer
        if (speechRecognizer == null) {
            onError("Распознавание речи недоступно на устройстве")
            return
        }
        listening = true
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                if (!listening) return
                listening = false
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (text.isNullOrBlank()) onError("Не удалось распознать команду")
                else onResult(text)
            }

            override fun onError(error: Int) {
                if (!listening) return
                listening = false
                val message = when (error) {
                    SpeechRecognizer.ERROR_CLIENT ->
                        "Сервис распознавания речи завершил сеанс. Проверьте микрофон."
                    SpeechRecognizer.ERROR_AUDIO -> "Не удалось получить звук с микрофона"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        "Нет разрешения на использование микрофона"
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        "Сервис распознавания речи недоступен"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Речь не распознана"
                    else -> "Ошибка распознавания речи"
                }
                onError(message)
            }
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onPartialResults(partialResults: Bundle?) = Unit
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ru-RU")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
        }
        speechRecognizer.startListening(intent)
    }

    override fun stopListening() {
        listening = false
        recognizer?.cancel()
    }

    override fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "inventory_voice")
    }

    override fun release() {
        listening = false
        recognizer?.cancel()
        recognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
