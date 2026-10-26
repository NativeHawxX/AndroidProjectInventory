package com.example.inventory.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ContainerEntity::class, ThingEntity::class],
    version = 2,
    exportSchema = false
)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE containers ADD COLUMN remoteKey TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE things ADD COLUMN remoteId INTEGER DEFAULT NULL")
            }
        }
    }
}
