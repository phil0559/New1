package com.example.new1.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "content_item_tracks",
    primaryKeys = ["item_rank", "position"],
    foreignKeys = [
        ForeignKey(
            entity = ContentItemEntity::class,
            parentColumns = ["rank"],
            childColumns = ["item_rank"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["item_rank"])],
)
data class ContentItemTrackEntity(
    @ColumnInfo(name = "item_rank")
    val itemRank: Long,
    @ColumnInfo(name = "position")
    val position: Int,
    @ColumnInfo(name = "value")
    val value: String,
)
