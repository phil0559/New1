package com.example.new1.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "establishments")
data class EstablishmentEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "comment")
    val comment: String?,
    @ColumnInfo(name = "photos")
    val photos: List<String>
)
