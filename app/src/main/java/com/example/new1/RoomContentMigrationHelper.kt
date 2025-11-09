package com.example.new1

import android.content.Context
import kotlinx.coroutines.runBlocking

object RoomContentMigrationHelper {
    @JvmStatic
    fun migrateRoomContent(
        context: Context,
        oldEstablishmentName: String?,
        newEstablishmentName: String?,
        oldRoomName: String?,
        newRoomName: String?,
    ) {
        val repository = ViewModelDependencies.roomContentRepository(context)
        val canonicalOldKey = RoomContentStorage.buildKey(oldEstablishmentName, oldRoomName)
        val canonicalNewKey = RoomContentStorage.buildKey(newEstablishmentName, newRoomName)
        if (canonicalOldKey == canonicalNewKey) {
            return
        }
        runBlocking {
            val legacyOldKey = buildLegacyKey(oldEstablishmentName, oldRoomName)
            val entity = repository.getByStorageKey(canonicalOldKey)
                ?: repository.getByStorageKey(legacyOldKey)
            if (entity != null) {
                val originalKey = entity.storageKey
                val updated = entity.copy(
                    storageKey = canonicalNewKey,
                    establishment = trimmedOrNull(newEstablishmentName),
                    room = trimmedOrNull(newRoomName),
                )
                repository.upsert(updated)
                if (originalKey != canonicalNewKey) {
                    repository.deleteByStorageKey(originalKey)
                }
            }
        }
    }

    private fun trimmedOrNull(value: String?): String? {
        val trimmed = value?.trim()
        return trimmed?.takeIf { it.isNotEmpty() }
    }

    private fun buildLegacyKey(establishmentName: String?, roomName: String?): String {
        return LEGACY_PREFIX + legacySanitize(establishmentName) + "_" + legacySanitize(roomName)
    }

    private fun legacySanitize(value: String?): String {
        if (value == null) {
            return LEGACY_DEFAULT_TOKEN
        }
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            return LEGACY_DEFAULT_TOKEN
        }
        return trimmed.replace("[^A-Za-z0-9]".toRegex(), "_")
    }

    private const val LEGACY_PREFIX = "room_content_"
    private const val LEGACY_DEFAULT_TOKEN = "default"
}
