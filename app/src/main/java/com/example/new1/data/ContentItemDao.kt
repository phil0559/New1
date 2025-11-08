package com.example.new1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentItemDao {
    @Query("SELECT * FROM content_items WHERE room_id = :roomId ORDER BY rank ASC")
    fun observeByRoom(roomId: String): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE parent_rank IS NULL AND room_id = :roomId ORDER BY rank ASC")
    fun observeTopLevelByRoom(roomId: String): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE parent_rank = :parentRank ORDER BY rank ASC")
    fun observeChildren(parentRank: Long): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE rank = :rank LIMIT 1")
    fun observeByRank(rank: Long): Flow<ContentItemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ContentItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ContentItemEntity>)

    @Query("DELETE FROM content_items WHERE rank = :rank")
    suspend fun deleteByRank(rank: Long)

    @Query("DELETE FROM content_items WHERE room_id = :roomId")
    suspend fun deleteByRoom(roomId: String)

    @Query("DELETE FROM content_items WHERE parent_rank = :parentRank")
    suspend fun deleteByParent(parentRank: Long)
}
