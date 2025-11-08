package com.example.new1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ContentPhotoDao {
    @Query(
        "SELECT * FROM content_photos WHERE owner_type = :ownerType AND owner_id = :ownerId ORDER BY position ASC",
    )
    suspend fun listByOwner(ownerType: String, ownerId: String): List<ContentPhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ContentPhotoEntity>)

    @Query("DELETE FROM content_photos WHERE owner_type = :ownerType AND owner_id = :ownerId")
    suspend fun deleteByOwner(ownerType: String, ownerId: String)

    @Query("SELECT * FROM content_photos WHERE id = :photoId LIMIT 1")
    suspend fun findById(photoId: String): ContentPhotoEntity?

    @Query("DELETE FROM content_photos WHERE id = :photoId")
    suspend fun deleteById(photoId: String)
}
