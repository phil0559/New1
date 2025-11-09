package com.example.new1.data

import kotlinx.coroutines.withContext

class RoomContentRepository(
    private val roomContentDao: RoomContentDao,
    private val dispatcherProvider: CoroutineDispatcherProvider = DefaultCoroutineDispatcherProvider(),
) {
    private val ioDispatcher = dispatcherProvider.io

    suspend fun getByStorageKey(storageKey: String): RoomContentEntity? = withContext(ioDispatcher) {
        roomContentDao.findByStorageKey(storageKey)
    }

    suspend fun listStorageKeys(): List<String> = withContext(ioDispatcher) {
        roomContentDao.listStorageKeys()
    }

    suspend fun upsert(entity: RoomContentEntity) = withContext(ioDispatcher) {
        roomContentDao.upsert(entity)
    }

    suspend fun deleteByStorageKey(storageKey: String) = withContext(ioDispatcher) {
        roomContentDao.deleteByStorageKey(storageKey)
    }
}
