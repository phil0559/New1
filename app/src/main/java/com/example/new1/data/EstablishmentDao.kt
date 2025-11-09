package com.example.new1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EstablishmentDao {
    @Query("SELECT * FROM establishments ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<EstablishmentEntity>>

    @Query("SELECT * FROM establishments ORDER BY name COLLATE NOCASE ASC")
    suspend fun listAll(): List<EstablishmentEntity>

    @Query("SELECT * FROM establishments WHERE id = :establishmentId LIMIT 1")
    fun observeById(establishmentId: String): Flow<EstablishmentEntity?>

    @Query("SELECT COUNT(*) FROM establishments")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(establishment: EstablishmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(establishments: List<EstablishmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAllSync(establishments: List<EstablishmentEntity>)

    @Query("DELETE FROM establishments WHERE id = :establishmentId")
    suspend fun deleteById(establishmentId: String)

    @Query("DELETE FROM establishments")
    suspend fun clear()
}
