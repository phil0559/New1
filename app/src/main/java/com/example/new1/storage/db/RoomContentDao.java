package com.example.new1.storage.db;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RoomContentDao {
    @Query("SELECT * FROM room_content WHERE storage_key = :storageKey LIMIT 1")
    RoomContentEntity findByStorageKey(@NonNull String storageKey);

    @Query("SELECT storage_key FROM room_content")
    List<String> listStorageKeys();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(@NonNull RoomContentEntity entity);

    @Query("DELETE FROM room_content WHERE storage_key = :storageKey")
    void deleteByStorageKey(@NonNull String storageKey);
}
