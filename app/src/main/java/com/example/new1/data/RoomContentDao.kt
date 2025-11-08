package com.example.new1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoomContentDao {
    @Query("SELECT * FROM room_content WHERE storage_key = :storageKey LIMIT 1")
    suspend fun findByStorageKey(storageKey: String): RoomContentEntity?

    @Query("SELECT storage_key FROM room_content")
    suspend fun listStorageKeys(): List<String>

    @Query("SELECT COUNT(*) FROM room_content")
    fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RoomContentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAllSync(entities: List<RoomContentEntity>)

    @Query("DELETE FROM room_content WHERE storage_key = :storageKey")
    suspend fun deleteByStorageKey(storageKey: String)
}
