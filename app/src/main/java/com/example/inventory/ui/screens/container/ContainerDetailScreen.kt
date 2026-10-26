package com.example.inventory.ui.screens.container

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileUpload
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.inventory.ui.qr.QrGenerator
import com.example.inventory.data.model.Thing
import com.example.inventory.data.classifier.ThingCategory
import com.example.inventory.data.model.InventoryExport
import com.example.inventory.data.model.ContainerExport
import com.example.inventory.data.model.ThingExport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerDetailScreen(
    containerId: Long,
    onBack: () -> Unit,
    viewModel: ContainerDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val container = state.container ?: return
    var editor by remember { mutableStateOf<ThingEditor?>(null) }
    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val export = InventoryExport(
                container = ContainerExport(
                    id = container.id,
                    roomId = container.roomId,
                    name = container.name,
                    qrPayload = container.qrPayload
                ),
                things = state.things.map { thing ->
                    ThingExport(thing.id, thing.name, thing.description, thing.categoryId)
                }
            )
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                it.write(Json { prettyPrint = true }.encodeToString(export))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(container.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        exportLauncher.launch("${container.name.replace(" ", "_")}.json")
                    }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Экспортировать контейнер")
                    }
                    IconButton(onClick = { editor = ThingEditor(null, state.categories) }) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить вещь")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            val bitmap = remember(container.qrPayload) {
                QrGenerator.generate(container.qrPayload, 512)
            }

            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "QR-маркировка",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(12.dp))
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(220.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        container.qrPayload,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                "Содержимое (${state.things.size})",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )

            if (state.things.isEmpty()) {
                Text(
                    "Контейнер пуст",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } else {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.things.forEach { thing ->
                        Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Category, null)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(thing.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${thing.description} · ${state.categories.firstOrNull { it.id == thing.categoryId }?.category.orEmpty()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { editor = ThingEditor(thing, state.categories) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Изменить вещь")
                                }
                                IconButton(onClick = { viewModel.deleteThing(thing) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Удалить вещь")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    editor?.let { current ->
        ThingEditorDialog(
            editor = current,
            onDismiss = { editor = null },
            onSave = { name, description, categoryId ->
                if (current.thing == null) viewModel.addThing(name, description, categoryId)
                else viewModel.updateThing(current.thing.copy(name = name, description = description, categoryId = categoryId))
                editor = null
            },
            onDelete = {
                current.thing?.let(viewModel::deleteThing)
                editor = null
            }
        )
    }
}

private data class ThingEditor(val thing: Thing?, val categories: List<ThingCategory>)

@Composable
private fun ThingEditorDialog(
    editor: ThingEditor,
    onDismiss: () -> Unit,
    onSave: (String, String, Long) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember(editor) { mutableStateOf(editor.thing?.name.orEmpty()) }
    var description by remember(editor) { mutableStateOf(editor.thing?.description.orEmpty()) }
    var categoryId by remember(editor) {
        mutableStateOf(editor.thing?.categoryId ?: editor.categories.firstOrNull()?.id ?: 5L)
    }
    var menuExpanded by remember { mutableStateOf(false) }
    val categoryName = editor.categories.firstOrNull { it.id == categoryId }?.category ?: "Другое"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editor.thing == null) "Новая вещь" else "Изменить вещь") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Название") }, singleLine = true)
                OutlinedTextField(description, { description = it }, label = { Text("Описание") })
                Box {
                    OutlinedButton(onClick = { menuExpanded = true }) { Text("Категория: $categoryName") }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        editor.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.category) },
                                onClick = { categoryId = category.id; menuExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim(), description.trim(), categoryId) },
                enabled = name.isNotBlank()
            ) { Text("Сохранить") }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (editor.thing != null) {
                    TextButton(onClick = onDelete) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text("Отмена") }
            }
        }
    )
}
