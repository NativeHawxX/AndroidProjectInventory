package com.example.inventory.ui.screens

import androidx.lifecycle.ViewModel
import com.example.inventory.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InventoryViewModel(
    private val repository: InventoryRepository = InventoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        val rooms = repository.getRooms()
        val allContainers = rooms.flatMap { repository.getContainers(it.id) }

        _uiState.update {
            it.copy(
                rooms = rooms,
                containersByRoom = rooms.associate { room ->
                    room.id to repository.getContainers(room.id)
                },
                thingsByContainer = allContainers.associate { c ->
                    c.id to repository.getThings(c.id)
                }
            )
        }
    }

    // ---- События (View → ViewModel) ----

    fun onQueryChange(newQuery: String) {
        _uiState.update {
            it.copy(
                query = newQuery,
                results = repository.searchThings(newQuery)
            )
        }
    }

    fun onRoomToggle(roomId: Long) {
        _uiState.update { state ->
            val newSet = state.expandedRooms.toMutableSet()
            if (!newSet.add(roomId)) newSet.remove(roomId)
            state.copy(expandedRooms = newSet)
        }
    }

    fun onContainerToggle(containerId: Long) {
        _uiState.update { state ->
            val newSet = state.expandedContainers.toMutableSet()
            if (!newSet.add(containerId)) newSet.remove(containerId)
            state.copy(expandedContainers = newSet)
        }
    }
}