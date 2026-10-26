package com.example.inventory.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.model.Container
import com.example.inventory.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()
    private val query = MutableStateFlow("")

    init {
        viewModelScope.launch {
            repository.syncInitialData()
        }
        viewModelScope.launch {
            combine(repository.observeContainers(), repository.observeThings(), query) { containers, things, text ->
                val rooms = repository.getRooms()
                val thingsByContainer = things.groupBy { it.containerId }
                val containersByRoom = containers.groupBy { it.roomId }
                val results = if (text.isBlank()) emptyList() else things
                    .filter { it.name.contains(text, ignoreCase = true) }
                    .mapNotNull { thing ->
                        val container = containers.firstOrNull { it.id == thing.containerId } ?: return@mapNotNull null
                        val room = rooms.firstOrNull { it.id == container.roomId } ?: return@mapNotNull null
                        com.example.inventory.data.model.SearchResult(thing, room.name, container.name)
                    }
                Triple(rooms, containersByRoom, thingsByContainer) to results
            }.collect { (data, results) ->
                _uiState.update {
                    it.copy(
                        rooms = data.first,
                        containersByRoom = data.second,
                        thingsByContainer = data.third,
                        query = query.value,
                        results = results
                    )
                }
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
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

    fun addContainer(roomId: Long, name: String) = viewModelScope.launch {
        repository.createContainer(roomId, name)
    }

    fun updateContainer(container: Container) = viewModelScope.launch {
        repository.updateContainer(container)
    }

    fun deleteContainer(containerId: Long) = viewModelScope.launch {
        repository.deleteContainer(containerId)
    }

}
