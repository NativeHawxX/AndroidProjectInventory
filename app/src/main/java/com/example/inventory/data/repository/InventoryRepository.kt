package com.example.inventory.data.repository

import com.example.inventory.data.classifier.ThingCategory
import com.example.inventory.data.classifier.ThingCategoryRepository
import com.example.inventory.data.local.ContainerEntity
import com.example.inventory.data.local.InventoryDao
import com.example.inventory.data.local.ThingEntity
import com.example.inventory.data.model.Container
import com.example.inventory.data.model.Room
import com.example.inventory.data.model.Thing
import com.example.inventory.data.network.InventoryApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import java.util.Locale

class InventoryRepository @Inject constructor(
    private val dao: InventoryDao,
    private val categoryRepository: ThingCategoryRepository,
    private val api: InventoryApi
) {

    private val rooms = listOf(
        Room(1, "Спальня"),
        Room(2, "Кладовая"),
        Room(3, "Гараж")
    )

    fun getRooms(): List<Room> = rooms

    fun observeContainers(): Flow<List<Container>> = dao.observeContainers().map { list ->
        list.map { it.toModel() }
    }

    fun observeThings(): Flow<List<Thing>> = dao.observeThings().map { list ->
        list.map { it.toModel() }
    }

    fun observeContainer(id: Long): Flow<Container?> = dao.observeContainer(id).map { it?.toModel() }

    fun observeThingsInContainer(id: Long): Flow<List<Thing>> =
        dao.observeThingsInContainer(id).map { list -> list.map { it.toModel() } }

    fun getCategories(): List<ThingCategory> = categoryRepository.getCategories()

    suspend fun syncInitialData() {
        val remoteItems = runCatching { api.getInitialItems() }.getOrNull() ?: return

        remoteItems.forEach { remote ->
            val remoteKey = "${remote.roomId}:${remote.containerName}"
            val container = dao.findContainerByRemoteKey(remoteKey)
                ?: dao.findContainerByName(remote.containerName)?.let { existing ->
                    val linked = existing.copy(remoteKey = remoteKey)
                    dao.updateContainer(linked)
                    linked
                }
                ?: ContainerEntity(
                    roomId = remote.roomId,
                    name = remote.containerName,
                    qrPayload = "",
                    remoteKey = remoteKey
                ).let { draft ->
                    val id = dao.insertContainer(draft)
                    val saved = draft.copy(id = id, qrPayload = "INV-CONTAINER-$id")
                    dao.updateContainer(saved)
                    saved
                }

            val existingThing = dao.findThingByRemoteId(remote.id)
                ?: dao.findThingByName(container.id, remote.thingName)?.let { existing ->
                    val linked = existing.copy(remoteId = remote.id)
                    dao.updateThing(linked)
                    linked
                }
            if (existingThing == null) {
                dao.insertThing(ThingEntity(
                    containerId = container.id,
                    name = remote.thingName,
                    description = remote.description,
                    categoryId = remote.categoryId,
                    remoteId = remote.id
                ))
            } else {
                dao.updateThing(existingThing.copy(
                    containerId = container.id,
                    name = remote.thingName,
                    description = remote.description,
                    categoryId = remote.categoryId
                ))
            }
        }
    }

    suspend fun createContainer(roomId: Long, name: String): Long =
        dao.insertContainer(ContainerEntity(roomId = roomId, name = name, qrPayload = ""))
            .also { id -> dao.updateContainer(ContainerEntity(id, roomId, name, "INV-CONTAINER-$id")) }

    suspend fun updateContainer(container: Container) = dao.updateContainer(container.toEntity())

    suspend fun deleteContainer(id: Long) = dao.deleteContainer(id)

    suspend fun createThing(containerId: Long, name: String, description: String, categoryId: Long) =
        dao.insertThing(ThingEntity(containerId = containerId, name = name, description = description, categoryId = categoryId))

    suspend fun updateThing(thing: Thing) = dao.updateThing(thing.toEntity())

    suspend fun deleteThing(thing: Thing) = dao.deleteThing(thing.toEntity())

    suspend fun moveThing(thingQuery: String, destinationQuery: String): String {
        val thing = dao.findThingByNameLike("%${thingQuery.trim()}%")
            ?: return "Вещь «$thingQuery» не найдена"
        val destination = destinationQuery.trim()
        val room = rooms.firstOrNull { it.name.equals(destination, ignoreCase = true) }
        val target = if (room != null) {
            dao.findFirstContainerByRoomId(room.id)
        } else {
            findContainerByVoiceQuery(destination)
        } ?: return "Место «$destinationQuery» не найдено"

        if (thing.containerId == target.id) {
            return "Вещь уже находится в контейнере «${target.name}»"
        }
        dao.updateThing(thing.copy(containerId = target.id))
        return "Вещь «${thing.name}» перемещена в «${target.name}»"
    }

    private suspend fun findContainerByVoiceQuery(query: String): ContainerEntity? {
        val ignoredWords = setOf("в", "во", "на", "с", "со", "для", "из", "к")
        val queryWords = normalizeWords(query).filter { it !in ignoredWords && it.length >= 4 }
        if (queryWords.isEmpty()) return null

        return dao.getAllContainers()
            .map { container ->
                val nameWords = normalizeWords(container.name)
                val score = queryWords.count { queryWord ->
                    nameWords.any { nameWord ->
                        val prefixLength = minOf(queryWord.length, nameWord.length, 5)
                        prefixLength >= 4 && queryWord.take(prefixLength) == nameWord.take(prefixLength)
                    }
                }
                container to score
            }
            .filter { (_, score) -> score > 0 }
            .maxByOrNull { (_, score) -> score }
            ?.first
    }

    private fun normalizeWords(value: String): List<String> = value
        .lowercase(Locale.ROOT)
        .replace('ё', 'е')
        .split(Regex("[^а-яa-z0-9]+"))
        .filter { it.isNotBlank() }

    private fun ContainerEntity.toModel() = Container(id, roomId, name, qrPayload, remoteKey)
    private fun ThingEntity.toModel() = Thing(id, containerId, name, description, categoryId, remoteId)
    private fun Container.toEntity() = ContainerEntity(id, roomId, name, qrPayload, remoteKey)
    private fun Thing.toEntity() = ThingEntity(id, containerId, name, description, categoryId, remoteId)
}
