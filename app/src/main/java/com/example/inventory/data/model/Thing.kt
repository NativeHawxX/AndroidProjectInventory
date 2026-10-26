package com.example.inventory.data.model

data class Thing(
    val id: Long,
    val containerId: Long,
    val name: String,
    val description: String,
    val categoryId: Long = 5,
    val remoteId: Long? = null
)
