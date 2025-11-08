package com.example.new1.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.new1.data.metadata.CategoryOptionEntity
import com.example.new1.data.metadata.MetadataDao
import com.example.new1.data.metadata.TypeDateFormatEntity
import com.example.new1.data.metadata.TypeFieldConfigEntity

@Database(
    entities = [
        RoomContentEntity::class,
        EstablishmentEntity::class,
        RoomEntity::class,
        ContentItemEntity::class,
        ContentItemTrackEntity::class,
        ContentItemPhotoEntity::class,
        CategoryOptionEntity::class,
        TypeFieldConfigEntity::class,
        TypeDateFormatEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
@TypeConverters(RoomContentTypeConverters::class)
abstract class RoomContentDatabase : RoomDatabase() {
    abstract fun roomContentDao(): RoomContentDao

    abstract fun metadataDao(): MetadataDao

    companion object {
        const val DATABASE_NAME: String = "room_content.db"
    }
}
