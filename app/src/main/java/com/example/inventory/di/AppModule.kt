package com.example.inventory.di

import android.content.Context
import androidx.room.Room
import com.example.inventory.data.classifier.ThingCategoryRepository
import com.example.inventory.data.local.InventoryDatabase
import com.example.inventory.data.local.InventoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.inventory.data.network.InventoryApi
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import com.example.inventory.data.voice.VoiceRepository
import com.example.inventory.data.voice.VoiceRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideInventoryApi(json: Json): InventoryApi {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
        return Retrofit.Builder()
            // Для эмулятора Android используется 10.0.2.2 — это адрес компьютера.
            // Для физического телефона замените адрес на IP компьютера в Wi-Fi-сети.
            .baseUrl("http://10.0.2.2:3000/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(InventoryApi::class.java)
    }

    @Provides
    @Singleton
    fun provideInventoryDatabase(@ApplicationContext context: Context): InventoryDatabase =
        Room.databaseBuilder(context, InventoryDatabase::class.java, "inventory.db")
            .addMigrations(InventoryDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideInventoryDao(database: InventoryDatabase): InventoryDao = database.inventoryDao()

    @Provides
    @Singleton
    fun provideThingCategoryRepository(@ApplicationContext context: Context): ThingCategoryRepository =
        ThingCategoryRepository(context)

    @Provides
    @Singleton
    fun provideVoiceRepository(@ApplicationContext context: Context): VoiceRepository =
        VoiceRepositoryImpl(context)
}
