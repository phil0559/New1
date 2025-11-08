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
        ContentPhotoEntity::class,
        CategoryOptionEntity::class,
        TypeFieldConfigEntity::class,
        TypeDateFormatEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
@TypeConverters(RoomContentTypeConverters::class)
abstract class New1Database : RoomDatabase() {
    abstract fun roomContentDao(): RoomContentDao

    abstract fun establishmentDao(): EstablishmentDao

    abstract fun roomDao(): RoomDao

    abstract fun contentItemDao(): ContentItemDao

    abstract fun contentItemPhotoDao(): ContentItemPhotoDao

    abstract fun contentItemTrackDao(): ContentItemTrackDao

    abstract fun metadataDao(): MetadataDao

    abstract fun contentPhotoDao(): ContentPhotoDao

    companion object {
        const val DATABASE_NAME: String = "room_content.db"
    }
}
