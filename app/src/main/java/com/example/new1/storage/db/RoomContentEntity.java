package com.example.new1.storage.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "room_content")
public class RoomContentEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "storage_key")
    public String storageKey;

    @Nullable
    @ColumnInfo(name = "establishment")
    public String establishment;

    @Nullable
    @ColumnInfo(name = "room")
    public String room;

    @NonNull
    @ColumnInfo(name = "payload_json")
    public String payloadJson;

    public RoomContentEntity(@NonNull String storageKey,
            @Nullable String establishment,
            @Nullable String room,
            @NonNull String payloadJson) {
        this.storageKey = storageKey;
        this.establishment = establishment;
        this.room = room;
        this.payloadJson = payloadJson;
    }
}
