package com.example.inventory.data.model

/**
 * Результат поиска: вещь + её точное местоположение.
 */
data class SearchResult(
    val thing: Thing,
    val roomName: String,
    val containerName: String
)