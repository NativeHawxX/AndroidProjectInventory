package com.example.inventory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.inventory.data.model.Container
import com.example.inventory.data.model.Room
import com.example.inventory.data.model.Thing

@Composable
fun RoomItem(
    room: Room,
    containers: List<Container>,
    thingsByContainer: Map<Long, List<Thing>>,
    expandedRooms: Set<Long>,
    expandedContainers: Set<Long>,
    onRoomToggle: (Long) -> Unit,
    onContainerToggle: (Long) -> Unit,
    onContainerClick: (Long) -> Unit,
    onEditContainer: (Container) -> Unit,
    onAddContainer: (Long) -> Unit
) {
    val expanded = room.id in expandedRooms

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRoomToggle(room.id) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.MeetingRoom, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = room.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                 Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess
                    else Icons.Default.ExpandMore,
                     contentDescription = null
                 )
                IconButton(onClick = { onAddContainer(room.id) }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить контейнер")
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(
                        start = 32.dp, end = 16.dp, bottom = 8.dp
                    )
                ) {
                    containers.forEach { container ->
                        ContainerItem(
                            container = container,
                            things = thingsByContainer[container.id].orEmpty(),
                            expanded = container.id in expandedContainers,
                            onToggle = { onContainerToggle(container.id) },
                            onClick = { onContainerClick(container.id) },
                            onEdit = { onEditContainer(container) }
                        )
                    }
                }
            }
        }
    }
}
