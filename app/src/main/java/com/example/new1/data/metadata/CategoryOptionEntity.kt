package com.example.new1.data.metadata

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "category_options",
    indices = [Index(value = ["normalized_label"], unique = true)],
)
data class CategoryOptionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "label")
    val label: String,
    @ColumnInfo(name = "normalized_label")
    val normalizedLabel: String,
    @ColumnInfo(name = "position")
    val position: Int,
)
