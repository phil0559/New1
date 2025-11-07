package com.example.new1.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_content")
data class RoomContentEntity(
    @PrimaryKey
    @ColumnInfo(name = "storage_key")
    val storageKey: String,
    @ColumnInfo(name = "establishment")
    val establishment: String?,
    @ColumnInfo(name = "room")
    val room: String?,
    @ColumnInfo(name = "payload_json")
    val payloadJson: String,
)
