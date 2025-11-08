package com.example.new1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentItemTrackDao {
    @Query("SELECT * FROM content_item_tracks WHERE item_rank = :itemRank ORDER BY position ASC")
    fun observeByItem(itemRank: Long): Flow<List<ContentItemTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tracks: List<ContentItemTrackEntity>)

    @Query("DELETE FROM content_item_tracks WHERE item_rank = :itemRank")
    suspend fun deleteByItem(itemRank: Long)
}
