package com.example.inventory.data.model

import kotlinx.serialization.Serializable

@Serializable
data class InventoryExport(
    val container: ContainerExport,
    val things: List<ThingExport>
)

@Serializable
data class ContainerExport(
    val id: Long,
    val roomId: Long,
    val name: String,
    val qrPayload: String
)

@Serializable
data class ThingExport(
    val id: Long,
    val name: String,
    val description: String,
    val categoryId: Long
)
