package com.example.new1.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        RoomContentEntity::class,
        EstablishmentEntity::class,
        RoomEntity::class,
        ContentItemEntity::class,
        ContentItemTrackEntity::class,
        ContentItemPhotoEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(RoomContentTypeConverters::class)
abstract class RoomContentDatabase : RoomDatabase() {
    abstract fun roomContentDao(): RoomContentDao

    companion object {
        const val DATABASE_NAME: String = "room_content.db"
    }
}
