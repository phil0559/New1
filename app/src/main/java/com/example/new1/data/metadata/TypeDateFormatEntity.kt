package com.example.new1.data.metadata

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "type_date_formats",
    primaryKeys = ["type_normalized_label"],
    indices = [Index(value = ["type_label"])],
)
data class TypeDateFormatEntity(
    @ColumnInfo(name = "type_label")
    val typeLabel: String,
    @ColumnInfo(name = "type_normalized_label")
    val typeNormalizedLabel: String,
    @ColumnInfo(name = "date_format")
    val dateFormat: String,
)
