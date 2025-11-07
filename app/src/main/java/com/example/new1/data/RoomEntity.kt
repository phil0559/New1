package com.example.new1.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rooms",
    foreignKeys = [
        ForeignKey(
            entity = EstablishmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["establishment_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["establishment_id"])]
)
data class RoomEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "establishment_id")
    val establishmentId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "comment")
    val comment: String?,
    @ColumnInfo(name = "photos")
    val photos: List<String>
)
