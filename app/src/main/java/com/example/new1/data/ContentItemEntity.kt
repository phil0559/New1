package com.example.new1.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "content_items",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["room_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ContentItemEntity::class,
            parentColumns = ["rank"],
            childColumns = ["parent_rank"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["room_id"]),
        Index(value = ["parent_rank"]),
        Index(value = ["room_id", "rank"], unique = true),
    ],
)
data class ContentItemEntity(
    @PrimaryKey
    @ColumnInfo(name = "rank")
    val rank: Long,
    @ColumnInfo(name = "room_id")
    val roomId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "comment")
    val comment: String?,
    @ColumnInfo(name = "is_container")
    val isContainer: Boolean,
    @ColumnInfo(name = "parent_rank")
    val parentRank: Long?,
    @ColumnInfo(name = "type")
    val type: String?,
    @ColumnInfo(name = "category")
    val category: String?,
    @ColumnInfo(name = "barcode")
    val barcode: String?,
    @ColumnInfo(name = "series")
    val series: String?,
    @ColumnInfo(name = "number")
    val number: String?,
    @ColumnInfo(name = "author")
    val author: String?,
    @ColumnInfo(name = "publisher")
    val publisher: String?,
    @ColumnInfo(name = "edition")
    val edition: String?,
    @ColumnInfo(name = "publication_date")
    val publicationDate: String?,
    @ColumnInfo(name = "summary")
    val summary: String?,
    @ColumnInfo(name = "is_furniture")
    val isFurniture: Boolean,
    @ColumnInfo(name = "is_storage_tower")
    val isStorageTower: Boolean,
    @ColumnInfo(name = "furniture_type")
    val furnitureType: String?,
    @ColumnInfo(name = "furniture_custom_type")
    val furnitureCustomType: String?,
    @ColumnInfo(name = "furniture_levels")
    val furnitureLevels: Int?,
    @ColumnInfo(name = "furniture_columns")
    val furnitureColumns: Int?,
    @ColumnInfo(name = "furniture_has_top")
    val furnitureHasTop: Boolean,
    @ColumnInfo(name = "furniture_has_bottom")
    val furnitureHasBottom: Boolean,
    @ColumnInfo(name = "container_level")
    val containerLevel: Int?,
    @ColumnInfo(name = "container_column")
    val containerColumn: Int?,
)
