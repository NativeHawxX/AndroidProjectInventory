package com.example.inventory.data.network

import retrofit2.http.GET

interface InventoryApi {
    @GET("inventory")
    suspend fun getInitialItems(): List<RemoteInventoryItem>
}
