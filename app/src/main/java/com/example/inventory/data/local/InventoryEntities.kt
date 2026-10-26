package com.example.inventory.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "containers",
    indices = [Index("roomId")]
)
data class ContainerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val name: String,
    val qrPayload: String,
    val remoteKey: String? = null
)

@Entity(
    tableName = "things",
    foreignKeys = [
        ForeignKey(
            entity = ContainerEntity::class,
            parentColumns = ["id"],
            childColumns = ["containerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("containerId"), Index("categoryId")]
)
data class ThingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val containerId: Long,
    val name: String,
    val description: String,
    val categoryId: Long,
    val remoteId: Long? = null
)
