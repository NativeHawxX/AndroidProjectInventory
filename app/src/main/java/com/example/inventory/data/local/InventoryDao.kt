package com.example.inventory.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM containers ORDER BY name")
    fun observeContainers(): Flow<List<ContainerEntity>>

    @Query("SELECT * FROM things ORDER BY name")
    fun observeThings(): Flow<List<ThingEntity>>

    @Query("SELECT * FROM containers WHERE id = :id")
    fun observeContainer(id: Long): Flow<ContainerEntity?>

    @Query("SELECT * FROM things WHERE containerId = :containerId ORDER BY name")
    fun observeThingsInContainer(containerId: Long): Flow<List<ThingEntity>>

    @Query("SELECT * FROM containers WHERE name = :name LIMIT 1")
    suspend fun findContainerByName(name: String): ContainerEntity?

    @Query("SELECT * FROM containers WHERE remoteKey = :remoteKey LIMIT 1")
    suspend fun findContainerByRemoteKey(remoteKey: String): ContainerEntity?

    @Query("SELECT * FROM things WHERE containerId = :containerId AND name = :name LIMIT 1")
    suspend fun findThingByName(containerId: Long, name: String): ThingEntity?

    @Query("SELECT * FROM things WHERE remoteId = :remoteId LIMIT 1")
    suspend fun findThingByRemoteId(remoteId: Long): ThingEntity?

    @Query("SELECT * FROM things WHERE name LIKE :name LIMIT 1")
    suspend fun findThingByNameLike(name: String): ThingEntity?

    @Query("SELECT * FROM containers WHERE name LIKE :name LIMIT 1")
    suspend fun findContainerByNameLike(name: String): ContainerEntity?

    @Query("SELECT * FROM containers WHERE roomId = :roomId ORDER BY name LIMIT 1")
    suspend fun findFirstContainerByRoomId(roomId: Long): ContainerEntity?

    @Query("SELECT * FROM containers ORDER BY name")
    suspend fun getAllContainers(): List<ContainerEntity>

    @Insert
    suspend fun insertContainer(container: ContainerEntity): Long

    @Update
    suspend fun updateContainer(container: ContainerEntity)

    @Query("DELETE FROM containers WHERE id = :id")
    suspend fun deleteContainer(id: Long)

    @Insert
    suspend fun insertThing(thing: ThingEntity): Long

    @Update
    suspend fun updateThing(thing: ThingEntity)

    @Delete
    suspend fun deleteThing(thing: ThingEntity)
}
