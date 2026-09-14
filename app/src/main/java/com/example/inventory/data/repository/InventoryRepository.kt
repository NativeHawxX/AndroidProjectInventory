package com.example.inventory.data.repository

import com.example.inventory.data.model.Container
import com.example.inventory.data.model.Room
import com.example.inventory.data.model.SearchResult
import com.example.inventory.data.model.Thing

class InventoryRepository {

    private val rooms = listOf(
        Room(1, "Спальня"),
        Room(2, "Кладовая"),
        Room(3, "Гараж")
    )

    private val containers = listOf(
        Container(1, 1, "Шкаф"),
        Container(2, 1, "Под кроватью"),
        Container(3, 2, "Коробка «Зима»"),
        Container(4, 2, "Коробка «Лето»"),
        Container(5, 3, "Ящик с инструментами"),
        Container(6, 3, "Верхняя полка")
    )

    private val things = listOf(
        Thing(1, 1, "Свитер", "Серый, шерстяной"),
        Thing(2, 1, "Рубашка", "Белая, офисная"),
        Thing(3, 2, "Чемодан", "Большой, синий"),
        Thing(4, 3, "Пуховик", "Чёрный, зимний"),
        Thing(5, 3, "Шапка", "Вязаная"),
        Thing(6, 4, "Плавки", "Синие"),
        Thing(7, 4, "Футболка", "Белая"),
        Thing(8, 5, "Дрель", "Makita"),
        Thing(9, 5, "Отвёртки", "Набор 12 шт."),
        Thing(10, 6, "Ёлочные игрушки", "Коробка")
    )

    fun getRooms(): List<Room> = rooms

    fun getContainers(roomId: Long): List<Container> =
        containers.filter { it.roomId == roomId }

    fun getThings(containerId: Long): List<Thing> =
        things.filter { it.containerId == containerId }

    fun searchThings(query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()
        return things
            .filter { it.name.startsWith(query, true ) }
            .mapNotNull { thing ->
                val container = containers.firstOrNull { it.id == thing.containerId }
                    ?: return@mapNotNull null
                val room = rooms.firstOrNull { it.id == container.roomId }
                    ?: return@mapNotNull null
                SearchResult(thing, room.name, container.name)
            }
    }
}