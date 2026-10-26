package com.example.inventory.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.inventory.ui.components.RoomItem
import com.example.inventory.ui.components.SearchResultItem
import com.example.inventory.data.model.Container
import com.example.inventory.ui.voice.VoiceStatus
import com.example.inventory.ui.voice.VoiceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    onContainerClick: (Long) -> Unit,
    onScanClick: () -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val voiceViewModel: VoiceViewModel = hiltViewModel()
    val voiceState by voiceViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) voiceViewModel.startListening()
    }
    var editor by remember { mutableStateOf<ContainerEditor?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Инвентаризация") },
                actions = {
                    IconButton(onClick = {
                        if (hasAudioPermission) voiceViewModel.startListening()
                        else permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }) {
                        Icon(Icons.Default.Mic, contentDescription = "Голосовая команда")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onScanClick,
                icon = { Icon(Icons.Default.QrCodeScanner, null) },
                text = { Text("Сканировать") }
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
                TreeContent(
                    state = state,
                    viewModel = viewModel,
                    onContainerClick = onContainerClick,
                    onEditContainer = { editor = ContainerEditor(it.roomId, it) },
                    onAddContainer = { editor = ContainerEditor(it, null) }
                )
            } else {
                SearchResults(state)
            }
        }
    }

    editor?.let { current ->
        ContainerEditorDialog(
            editor = current,
            onDismiss = { editor = null },
            onSave = { name ->
                if (current.container == null) viewModel.addContainer(current.roomId, name)
                else viewModel.updateContainer(current.container.copy(name = name))
                editor = null
            },
            onDelete = {
                current.container?.let { viewModel.deleteContainer(it.id) }
                editor = null
            }
        )
    }

    if (voiceState.status != VoiceStatus.Idle) {
        VoiceCommandDialog(
            state = voiceState,
            onDismiss = voiceViewModel::stopListening
        )
    }
}

@Composable
private fun VoiceCommandDialog(
    state: com.example.inventory.ui.voice.VoiceUiState,
    onDismiss: () -> Unit
) {
    val title = when (state.status) {
        VoiceStatus.Listening -> "Слушаю команду"
        VoiceStatus.Processing -> "Обработка команды"
        VoiceStatus.Success -> "Команда выполнена"
        VoiceStatus.Error -> "Ошибка"
        VoiceStatus.Idle -> "Голосовая команда"
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.status == VoiceStatus.Listening) {
                    Text("Скажите: «перемести вещь в комнату или контейнер»")
                }
                if (state.recognizedText.isNotBlank()) {
                    Text("Распознано: ${state.recognizedText}")
                }
                if (state.message.isNotBlank()) Text(state.message)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Закрыть") }
        }
    )
}

@Composable
private fun TreeContent(
    state: InventoryUiState,
    viewModel: InventoryViewModel,
    onContainerClick: (Long) -> Unit,
    onEditContainer: (Container) -> Unit,
    onAddContainer: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 96.dp     // место под FAB, чтобы кнопка не перекрывала контент
        ),
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
                onContainerToggle = viewModel::onContainerToggle,
                onContainerClick = onContainerClick,
                onEditContainer = onEditContainer,
                onAddContainer = onAddContainer
            )
        }
    }
}

@Composable
private fun SearchResults(state: InventoryUiState) {
    if (state.results.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 96.dp),   // поднимаем текст выше FAB
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ничего не найдено по запросу «${state.query}»",
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(state.results, key = { it.thing.id }) { result ->
            SearchResultItem(result)
        }
    }
}

private data class ContainerEditor(val roomId: Long, val container: Container?)

@Composable
private fun ContainerEditorDialog(
    editor: ContainerEditor,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember(editor) { mutableStateOf(editor.container?.name.orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editor.container == null) "Новый контейнер" else "Изменить контейнер") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onSave(name.trim()) }, enabled = name.isNotBlank()) { Text("Сохранить") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (editor.container != null) {
                    TextButton(onClick = onDelete) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text("Отмена") }
            }
        }
    )
}
