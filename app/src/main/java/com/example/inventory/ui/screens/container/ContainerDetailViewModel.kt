package com.example.inventory.ui.screens.container

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.classifier.ThingCategory
import com.example.inventory.data.model.Container
import com.example.inventory.data.model.Thing
import com.example.inventory.data.repository.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContainerDetailUiState(
    val container: Container? = null,
    val things: List<Thing> = emptyList(),
    val categories: List<ThingCategory> = emptyList()
)

@HiltViewModel
class ContainerDetailViewModel @Inject constructor(
    private val repository: InventoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val containerId: Long = savedStateHandle["containerId"] ?: 0L

    private val _uiState = MutableStateFlow(ContainerDetailUiState())
    val uiState: StateFlow<ContainerDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                repository.observeContainer(containerId),
                repository.observeThingsInContainer(containerId)
            ) { container, things -> ContainerDetailUiState(container, things, repository.getCategories()) }
                .collect { _uiState.value = it }
        }
    }

    fun addThing(name: String, description: String, categoryId: Long) = viewModelScope.launch {
        repository.createThing(containerId, name, description, categoryId)
    }

    fun updateThing(thing: Thing) = viewModelScope.launch { repository.updateThing(thing) }

    fun deleteThing(thing: Thing) = viewModelScope.launch { repository.deleteThing(thing) }
}
