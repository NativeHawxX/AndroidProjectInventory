package com.example.inventory.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.inventory.ui.components.RoomItem
import com.example.inventory.ui.components.SearchResultItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Инвентаризация") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Поиск вещи по названию") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (state.query.isBlank()) {
                TreeContent(state, viewModel)
            } else {
                SearchResults(state)
            }
        }
    }
}

@Composable
private fun TreeContent(
    state: InventoryUiState,
    viewModel: InventoryViewModel
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.rooms, key = { it.id }) { room ->
            RoomItem(
                room = room,
                containers = state.containersByRoom[room.id].orEmpty(),
                thingsByContainer = state.thingsByContainer,
                expandedRooms = state.expandedRooms,
                expandedContainers = state.expandedContainers,
                onRoomToggle = viewModel::onRoomToggle,
                onContainerToggle = viewModel::onContainerToggle
            )
        }
    }
}

@Composable
private fun SearchResults(state: InventoryUiState) {
    if (state.results.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Ничего не найдено по запросу «${state.query}»")
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.results, key = { it.thing.id }) { result ->
            SearchResultItem(result)
        }
    }
}