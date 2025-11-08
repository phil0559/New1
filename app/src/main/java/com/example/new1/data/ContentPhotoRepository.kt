package com.example.new1.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentPhotoRepository(
    context: Context,
    private val contentPhotoDao: ContentPhotoDao,
) {
    private val appContext = context.applicationContext

    suspend fun list(ownerType: String, ownerId: String): List<ContentPhotoEntity> =
        withContext(Dispatchers.IO) {
            contentPhotoDao.listByOwner(ownerType, ownerId)
        }

    suspend fun replaceOwnerPhotos(
        ownerType: String,
        ownerId: String,
        payloads: List<ContentPhotoPayload>,
    ) = withContext(Dispatchers.IO) {
        val existing = contentPhotoDao.listByOwner(ownerType, ownerId)
        val incomingIds = payloads.map { it.photoId }.toSet()
        existing.filter { it.id !in incomingIds }.forEach { obsolete ->
            ContentPhotoStorage.delete(appContext, obsolete.fileName)
            contentPhotoDao.deleteById(obsolete.id)
        }
        val entities = payloads.mapIndexed { index, payload ->
            val fileName = payload.fileName ?: ContentPhotoStorage.generateFileName(payload.photoId)
            ContentPhotoStorage.write(appContext, fileName, payload.bytes)
            ContentPhotoEntity(
                id = payload.photoId,
                ownerType = ownerType,
                ownerId = ownerId,
                position = payload.position ?: index,
                fileName = fileName,
            )
        }
        if (entities.isNotEmpty()) {
            contentPhotoDao.upsertAll(entities)
        } else {
            contentPhotoDao.deleteByOwner(ownerType, ownerId)
        }
    }

    suspend fun readPhoto(photoId: String): ByteArray? = withContext(Dispatchers.IO) {
        val entity = contentPhotoDao.findById(photoId) ?: return@withContext null
        ContentPhotoStorage.read(appContext, entity.fileName)
    }

    suspend fun deletePhoto(photoId: String) = withContext(Dispatchers.IO) {
        val entity = contentPhotoDao.findById(photoId) ?: return@withContext
        ContentPhotoStorage.delete(appContext, entity.fileName)
        contentPhotoDao.deleteById(photoId)
    }
}

data class ContentPhotoPayload(
    val photoId: String,
    val bytes: ByteArray,
    val position: Int? = null,
    val fileName: String? = null,
)
