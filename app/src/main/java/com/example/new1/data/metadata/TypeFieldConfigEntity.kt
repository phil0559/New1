package com.example.new1.data.metadata

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "type_field_configs",
    primaryKeys = ["type_normalized_label", "field_id"],
    indices = [
        Index(value = ["type_label"]),
        Index(value = ["type_normalized_label", "position"]),
    ],
)
data class TypeFieldConfigEntity(
    @ColumnInfo(name = "type_label")
    val typeLabel: String,
    @ColumnInfo(name = "type_normalized_label")
    val typeNormalizedLabel: String,
    @ColumnInfo(name = "field_id")
    val fieldId: String,
    @ColumnInfo(name = "position")
    val position: Int,
)
