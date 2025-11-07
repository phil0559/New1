package com.example.new1.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RoomContentEntity::class], version = 1, exportSchema = false)
abstract class RoomContentDatabase : RoomDatabase() {
    abstract fun roomContentDao(): RoomContentDao

    companion object {
        const val DATABASE_NAME: String = "room_content.db"
    }
}
