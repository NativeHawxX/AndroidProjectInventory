package com.example.inventory.ui.screens

import com.example.inventory.data.model.Container
import com.example.inventory.data.model.Room
import com.example.inventory.data.model.SearchResult
import com.example.inventory.data.model.Thing

/**
 * Единое состояние экрана (UDF).
 * View читает это состояние и больше ничего не знает.
 */
data class InventoryUiState(
    val rooms: List<Room> = emptyList(),
    val containersByRoom: Map<Long, List<Container>> = emptyMap(),
    val thingsByContainer: Map<Long, List<Thing>> = emptyMap(),
    val expandedRooms: Set<Long> = emptySet(),
    val expandedContainers: Set<Long> = emptySet(),
    val query: String = "",
    val results: List<SearchResult> = emptyList()
)