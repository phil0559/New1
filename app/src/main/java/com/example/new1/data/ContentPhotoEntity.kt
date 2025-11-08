package com.example.new1.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entité représentant une photo stockée de manière chiffrée sur le disque.
 * Chaque photo est rattachée à un propriétaire (établissement, salle ou fiche contenu)
 * via un identifiant générique et son type.
 */
@Entity(
    tableName = "content_photos",
    indices = [
        Index(value = ["owner_type", "owner_id", "position"], unique = true),
    ],
)
data class ContentPhotoEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "owner_type")
    val ownerType: String,
    @ColumnInfo(name = "owner_id")
    val ownerId: String,
    @ColumnInfo(name = "position")
    val position: Int,
    @ColumnInfo(name = "file_name")
    val fileName: String,
)
