package com.example.new1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    @Query(
        "SELECT * FROM rooms WHERE establishment_id = :establishmentId ORDER BY name COLLATE NOCASE ASC",
    )
    fun observeByEstablishment(establishmentId: String): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :roomId LIMIT 1")
    fun observeById(roomId: String): Flow<RoomEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(room: RoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rooms: List<RoomEntity>)

    @Query("DELETE FROM rooms WHERE id = :roomId")
    suspend fun deleteById(roomId: String)

    @Query("DELETE FROM rooms WHERE establishment_id = :establishmentId")
    suspend fun deleteByEstablishment(establishmentId: String)
}
