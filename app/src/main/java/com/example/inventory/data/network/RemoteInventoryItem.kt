package com.example.inventory.data.network

import kotlinx.serialization.Serializable

@Serializable
data class RemoteInventoryItem(
    val id: Long,
    val roomId: Long,
    val containerName: String,
    val thingName: String,
    val description: String,
    val categoryId: Long
)
