package com.example.inventory.data.model

data class Container(
    val id: Long,
    val roomId: Long,
    val name: String,
    val qrPayload: String = "INV-CONTAINER-$id",
    val remoteKey: String? = null
)
