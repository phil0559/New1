package com.example.new1.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomContentRepository(private val roomContentDao: RoomContentDao) {
    suspend fun getByStorageKey(storageKey: String): RoomContentEntity? = withContext(Dispatchers.IO) {
        roomContentDao.findByStorageKey(storageKey)
    }

    suspend fun listStorageKeys(): List<String> = withContext(Dispatchers.IO) {
        roomContentDao.listStorageKeys()
    }

    suspend fun upsert(entity: RoomContentEntity) = withContext(Dispatchers.IO) {
        roomContentDao.upsert(entity)
    }

    suspend fun deleteByStorageKey(storageKey: String) = withContext(Dispatchers.IO) {
        roomContentDao.deleteByStorageKey(storageKey)
    }
}
