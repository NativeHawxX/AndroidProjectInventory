package com.example.inventory.data.classifier

import kotlinx.serialization.Serializable

@Serializable
data class ThingCategory(
    val id: Long,
    val category: String,
    val icon: String
)