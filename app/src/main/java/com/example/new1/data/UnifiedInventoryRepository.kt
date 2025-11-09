package com.example.new1.data

import android.content.Context
import androidx.room.withTransaction
import com.example.new1.data.metadata.CategoryOptionEntity
import com.example.new1.data.metadata.MetadataDao
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

/**
 * Repository unifié regroupant l'accès aux différentes tables métier afin
 * d'offrir une orchestration centralisée autour des établissements, des pièces,
 * de l'inventaire et des catégories.
 */
class UnifiedInventoryRepository(
    private val database: New1Database,
    private val establishmentDao: EstablishmentDao = database.establishmentDao(),
    private val roomDao: RoomDao = database.roomDao(),
    private val contentItemDao: ContentItemDao = database.contentItemDao(),
    private val metadataDao: MetadataDao = database.metadataDao(),
    private val dispatcherProvider: CoroutineDispatcherProvider = DefaultCoroutineDispatcherProvider(),
) {

    private val ioDispatcher = dispatcherProvider.io

    private val categoryOptionsState = MutableStateFlow<List<String>>(emptyList())

    /**
     * Flux pour suivre la liste complète des établissements.
     */
    fun observeEstablishments(): Flow<List<EstablishmentEntity>> = establishmentDao.observeAll()

    suspend fun listEstablishments(): List<EstablishmentEntity> = withContext(ioDispatcher) {
        establishmentDao.listAll()
    }

    /**
     * Flux pour suivre un établissement précis.
     */
    fun observeEstablishment(establishmentId: String): Flow<EstablishmentEntity?> =
        establishmentDao.observeById(establishmentId)

    /**
     * Flux listant les pièces rattachées à un établissement.
     */
    fun observeRooms(establishmentId: String): Flow<List<RoomEntity>> =
        roomDao.observeByEstablishment(establishmentId)

    suspend fun listRooms(establishmentId: String): List<RoomEntity> = withContext(ioDispatcher) {
        roomDao.listByEstablishment(establishmentId)
    }

    /**
     * Flux pour suivre une pièce.
     */
    fun observeRoom(roomId: String): Flow<RoomEntity?> = roomDao.observeById(roomId)

    /**
     * Flux pour suivre l'inventaire complet d'une pièce.
     */
    fun observeInventory(roomId: String): Flow<List<ContentItemEntity>> =
        contentItemDao.observeByRoom(roomId)

    /**
     * Flux pour suivre les contenus d'un conteneur.
     */
    fun observeInventoryChildren(parentRank: Long): Flow<List<ContentItemEntity>> =
        contentItemDao.observeChildren(parentRank)

    /**
     * Ajoute ou met à jour un établissement.
     */
    suspend fun upsertEstablishment(entity: EstablishmentEntity) = withContext(ioDispatcher) {
        establishmentDao.upsert(entity)
    }

    /**
     * Supprime un établissement ainsi que les pièces associées.
     */
    suspend fun deleteEstablishment(establishmentId: String) = withContext(ioDispatcher) {
        database.withTransaction {
            roomDao.deleteByEstablishment(establishmentId)
            establishmentDao.deleteById(establishmentId)
        }
    }

    /**
     * Ajoute ou met à jour une pièce.
     */
    suspend fun upsertRoom(entity: RoomEntity) = withContext(ioDispatcher) {
        roomDao.upsert(entity)
    }

    /**
     * Supprime une pièce et l'inventaire associé.
     */
    suspend fun deleteRoom(roomId: String) = withContext(ioDispatcher) {
        database.withTransaction {
            contentItemDao.deleteByRoom(roomId)
            roomDao.deleteById(roomId)
        }
    }

    /**
     * Ajoute ou met à jour un élément d'inventaire.
     */
    suspend fun upsertInventoryItem(entity: ContentItemEntity) = withContext(ioDispatcher) {
        contentItemDao.upsert(entity)
    }

    /**
     * Ajoute ou met à jour plusieurs éléments d'inventaire.
     */
    suspend fun upsertInventory(items: List<ContentItemEntity>) = withContext(ioDispatcher) {
        if (items.isNotEmpty()) {
            contentItemDao.upsertAll(items)
        }
    }

    /**
     * Supprime un élément d'inventaire.
     */
    suspend fun deleteInventoryItem(rank: Long) = withContext(ioDispatcher) {
        contentItemDao.deleteByRank(rank)
    }

    /**
     * Expose en flux les catégories disponibles avec chargement paresseux.
     */
    fun observeCategoryOptions(): Flow<List<String>> =
        categoryOptionsState.asStateFlow().onStart {
            if (categoryOptionsState.value.isEmpty()) {
                categoryOptionsState.value = withContext(ioDispatcher) { loadCategoryOptionsBlocking() }
            }
        }

    /**
     * Recharge explicitement les catégories et retourne la liste mise à jour.
     */
    suspend fun refreshCategoryOptions(): List<String> = withContext(ioDispatcher) {
        val categories = loadCategoryOptionsBlocking()
        categoryOptionsState.value = categories
        categories
    }

    /**
     * Ajoute une catégorie en préservant l'ordre existant.
     */
    suspend fun addCategoryOption(label: String): List<String> = withContext(ioDispatcher) {
        val trimmed = label.trim()
        if (trimmed.isEmpty()) {
            return@withContext categoryOptionsState.value
        }
        val current = ensureCategoryOptionsLoaded()
        if (current.any { it.equals(trimmed, ignoreCase = true) }) {
            return@withContext current
        }
        val updated = current + trimmed
        persistCategoryOptions(updated)
        categoryOptionsState.value = updated
        updated
    }

    /**
     * Renomme une catégorie existante.
     */
    suspend fun renameCategoryOption(oldLabel: String, newLabel: String): List<String> =
        withContext(ioDispatcher) {
            val trimmed = newLabel.trim()
            if (trimmed.isEmpty()) {
                return@withContext categoryOptionsState.value
            }
            val current = ensureCategoryOptionsLoaded()
            val index = current.indexOfFirst { it.equals(oldLabel, ignoreCase = true) }
            if (index < 0) {
                return@withContext current
            }
            if (current.any { it.equals(trimmed, ignoreCase = true) && !it.equals(oldLabel, ignoreCase = true) }) {
                return@withContext current
            }
            val updated = current.toMutableList().also { it[index] = trimmed }.toList()
            persistCategoryOptions(updated)
            categoryOptionsState.value = updated
            updated
        }

    /**
     * Supprime une catégorie.
     */
    suspend fun deleteCategoryOption(label: String): List<String> = withContext(ioDispatcher) {
        val current = ensureCategoryOptionsLoaded()
        val updated = current.filterNot { it.equals(label, ignoreCase = true) }
        if (updated.size == current.size) {
            return@withContext current
        }
        persistCategoryOptions(updated)
        categoryOptionsState.value = updated
        updated
    }

    private fun loadCategoryOptionsBlocking(): List<String> =
        metadataDao.listCategoryOptions().map { it.label }

    private fun buildCategoryEntities(labels: List<String>): List<CategoryOptionEntity> =
        labels.mapIndexed { index, label ->
            val trimmed = label.trim()
            CategoryOptionEntity(
                label = trimmed,
                normalizedLabel = trimmed.lowercase(Locale.getDefault()),
                position = index,
            )
        }

    private fun persistCategoryOptions(labels: List<String>) {
        database.runInTransaction {
            metadataDao.clearCategoryOptions()
            if (labels.isNotEmpty()) {
                metadataDao.insertCategoryOptions(buildCategoryEntities(labels))
            }
        }
    }

    private fun ensureCategoryOptionsLoaded(): List<String> {
        val current = categoryOptionsState.value
        return if (current.isNotEmpty()) {
            current
        } else {
            val loaded = loadCategoryOptionsBlocking()
            categoryOptionsState.value = loaded
            loaded
        }
    }

    companion object {
        @JvmStatic
        fun create(context: Context): UnifiedInventoryRepository {
            val database = New1DatabaseFactory.create(context)
            return UnifiedInventoryRepository(database)
        }
    }
}
