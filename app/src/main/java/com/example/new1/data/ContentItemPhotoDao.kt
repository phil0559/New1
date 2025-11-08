package com.example.new1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentItemPhotoDao {
    @Query("SELECT * FROM content_item_photos WHERE item_rank = :itemRank ORDER BY position ASC")
    fun observeByItem(itemRank: Long): Flow<List<ContentItemPhotoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(photos: List<ContentItemPhotoEntity>)

    @Query("DELETE FROM content_item_photos WHERE item_rank = :itemRank")
    suspend fun deleteByItem(itemRank: Long)
}
