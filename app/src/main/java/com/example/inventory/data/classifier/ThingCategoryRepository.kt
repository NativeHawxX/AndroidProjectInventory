package com.example.inventory.data.classifier

import android.content.Context
import kotlinx.serialization.json.Json

class ThingCategoryRepository(context: Context) {
    private val categories: List<ThingCategory> = context.assets
        .open("thing_categories.json")
        .bufferedReader()
        .use { Json.decodeFromString(it.readText()) }

    fun getCategories(): List<ThingCategory> = categories
}
