package com.example.new1.data

import android.content.Context
import android.util.Base64
import com.example.new1.EstablishmentActivity
import com.example.new1.EstablishmentContentActivity
import com.example.new1.Room
import com.example.new1.RoomContentStorage
import androidx.room.withTransaction
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONException
import java.nio.charset.StandardCharsets
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.Locale
import java.util.UUID

internal object DatabaseInitializer {
    private const val PREFS_NAME = "database_initializer"
    private const val KEY_MIGRATION_COMPLETED = "legacy_migration_completed_v1"
    private const val ROOM_CONTENT_KEY_PREFIX = "room_content_"
    private const val LEGACY_DEFAULT_TOKEN = "default"
    private const val BASE64_FLAGS = Base64.NO_WRAP or Base64.URL_SAFE

    fun initialize(context: Context, database: New1Database) {
        val appContext = context.applicationContext
        val statePrefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (statePrefs.getBoolean(KEY_MIGRATION_COMPLETED, false)) {
            return
        }

        if (hasExistingData(database)) {
            clearLegacyRoomContent(appContext)
            statePrefs.edit().putBoolean(KEY_MIGRATION_COMPLETED, true).apply()
            return
        }

        val migrationState = MigrationState()
        loadLegacyEstablishments(appContext, migrationState)
        loadLegacyRooms(appContext, migrationState)
        val roomContentEntities = loadLegacyRoomContent(appContext, migrationState)

        val establishments = migrationState.establishments.values.toList()
        val rooms = migrationState.rooms.values.toList()

        runBlocking {
            database.withTransaction {
                if (establishments.isNotEmpty()) {
                    database.establishmentDao().upsertAll(establishments)
                }
                if (rooms.isNotEmpty()) {
                    database.roomDao().upsertAll(rooms)
                }
                if (roomContentEntities.isNotEmpty()) {
                    database.roomContentDao().upsertAll(roomContentEntities)
                }
            }
        }

        clearLegacyRoomContent(appContext)
        statePrefs.edit().putBoolean(KEY_MIGRATION_COMPLETED, true).apply()
    }

    private fun hasExistingData(database: New1Database): Boolean = runBlocking {
        database.establishmentDao().count() > 0 ||
            database.roomDao().count() > 0 ||
            database.roomContentDao().count() > 0
    }

    private fun loadLegacyEstablishments(context: Context, state: MigrationState) {
        val preferences = context.getSharedPreferences(EstablishmentActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val storedValue = preferences.getString(EstablishmentActivity.KEY_ESTABLISHMENTS, null)
        if (storedValue.isNullOrBlank()) {
            return
        }
        try {
            val array = JSONArray(storedValue)
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val name = item.optString("name", "")
                val trimmedName = name.trim()
                val normalizedName = normalize(trimmedName)
                val comment = trimToNull(item.optString("comment", null))
                val photos = parseStringArray(item.optJSONArray("photos"))
                val existingId = state.establishmentIdsByName[normalizedName]
                val parsedId = trimToNull(item.optString("id", null))
                val id = existingId ?: parsedId ?: UUID.randomUUID().toString()
                val entity = EstablishmentEntity(id, trimmedName, comment, photos)
                state.establishmentIdsByName[normalizedName] = id
                state.establishments[id] = entity
            }
        } catch (_: JSONException) {
            // Données invalides : on ignore simplement l'entrée.
        }
    }

    private fun loadLegacyRooms(context: Context, state: MigrationState) {
        val preferences = context.getSharedPreferences(EstablishmentContentActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val prefix = EstablishmentContentActivity.KEY_ROOMS + "_"
        for ((key, value) in preferences.all) {
            val storedKey = key as? String ?: continue
            if (!storedKey.startsWith(prefix)) {
                continue
            }
            val storedValue = value as? String ?: continue
            val suffix = storedKey.substring(prefix.length)
            val establishmentName = if (suffix == "default") "" else suffix
            val trimmedEstablishment = establishmentName.trim()
            val normalizedEstablishment = normalize(trimmedEstablishment)
            val establishmentId = state.establishmentIdsByName[normalizedEstablishment]
                ?: UUID.randomUUID().toString().also { id ->
                    state.establishmentIdsByName[normalizedEstablishment] = id
                    val entity = EstablishmentEntity(id, trimmedEstablishment, null, emptyList())
                    state.establishments.putIfAbsent(id, entity)
                }
            val rooms = parseRoomsArray(storedValue, establishmentId)
            for (entity in rooms) {
                state.rooms[entity.id] = entity
                val normalizedRoom = normalize(entity.name)
                val keyRef = RoomKey(normalizedEstablishment, normalizedRoom)
                if (!state.roomNames.contains(keyRef)) {
                    state.roomNames[keyRef] = LegacyRoomNames(
                        trimToNull(trimmedEstablishment),
                        trimToNull(entity.name),
                    )
                }
            }
        }
    }

    private fun parseRoomsArray(value: String, establishmentId: String): List<RoomEntity> {
        if (value.isBlank()) {
            return emptyList()
        }
        val result = ArrayList<RoomEntity>()
        try {
            val array = JSONArray(value)
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val name = item.optString("name", "")
                val trimmedName = name.trim()
                val comment = trimToNull(item.optString("comment", null))
                val photos = parseStringArray(item.optJSONArray("photos"))
                val parsedId = trimToNull(item.optString("id", null))
                val id = parsedId ?: Room.generateStableId()
                val entity = RoomEntity(id, establishmentId, trimmedName, comment, photos)
                result.add(entity)
            }
        } catch (_: JSONException) {
            // Ignore les données corrompues pour cette entrée.
        }
        return result
    }

    private fun loadLegacyRoomContent(context: Context, state: MigrationState): List<RoomContentEntity> {
        val preferences = context.getSharedPreferences(RoomContentStorage.PREFS_NAME, Context.MODE_PRIVATE)
        if (preferences.all.isEmpty()) {
            return emptyList()
        }
        val result = ArrayList<RoomContentEntity>()
        val processedKeys = LinkedHashSet<String>()

        for ((_, names) in state.roomNames) {
            val establishmentName = names.establishment
            val roomName = names.room
            val resolvedKey = RoomContentStorage.resolveKey(preferences, establishmentName, roomName)
            val storedValue = preferences.getString(resolvedKey, null)
            if (storedValue.isNullOrBlank()) {
                continue
            }
            val canonicalKey = RoomContentStorage.buildKey(establishmentName, roomName)
            result.add(
                RoomContentEntity(
                    storageKey = canonicalKey,
                    establishment = establishmentName,
                    room = roomName,
                    payloadJson = storedValue,
                ),
            )
            processedKeys.add(resolvedKey)
            processedKeys.add(canonicalKey)
        }

        for ((key, value) in preferences.all) {
            val storedKey = key as? String ?: continue
            if (!storedKey.startsWith(ROOM_CONTENT_KEY_PREFIX)) {
                continue
            }
            if (!processedKeys.add(storedKey)) {
                continue
            }
            val storedValue = value as? String ?: continue
            if (storedValue.isBlank()) {
                continue
            }
            val decoded = decodeLegacyRoomContentKey(storedKey)
            val establishmentName = decoded?.first
            val roomName = decoded?.second
            val canonicalKey = RoomContentStorage.buildKey(establishmentName, roomName)
            if (!processedKeys.add(canonicalKey)) {
                continue
            }
            result.add(
                RoomContentEntity(
                    storageKey = canonicalKey,
                    establishment = establishmentName,
                    room = roomName,
                    payloadJson = storedValue,
                ),
            )
        }

        return result
    }

    private fun clearLegacyRoomContent(context: Context) {
        val preferences = context.getSharedPreferences(RoomContentStorage.PREFS_NAME, Context.MODE_PRIVATE)
        if (preferences.all.isEmpty()) {
            return
        }
        val editor = preferences.edit()
        var modified = false
        for (key in preferences.all.keys) {
            val storedKey = key as? String ?: continue
            if (!storedKey.startsWith(ROOM_CONTENT_KEY_PREFIX)) {
                continue
            }
            editor.remove(storedKey)
            modified = true
        }
        if (modified) {
            editor.apply()
        }
    }

    private fun decodeLegacyRoomContentKey(key: String): Pair<String?, String?>? {
        if (!key.startsWith(ROOM_CONTENT_KEY_PREFIX)) {
            return null
        }
        val suffix = key.substring(ROOM_CONTENT_KEY_PREFIX.length)
        if (suffix.isEmpty()) {
            return null
        }
        var fallback: Pair<String?, String?>? = null
        for (index in suffix.indices) {
            if (suffix[index] != '_') {
                continue
            }
            val first = suffix.substring(0, index)
            val second = suffix.substring(index + 1)
            if (second.isEmpty()) {
                continue
            }
            val establishment = decodeSegment(first) ?: continue
            val room = decodeSegment(second) ?: continue
            val pair = Pair(establishment.value, room.value)
            if (establishment.decoded && room.decoded) {
                return pair
            }
            if (fallback == null) {
                fallback = pair
            }
        }
        return fallback
    }

    private fun decodeSegment(value: String): DecodedSegment? {
        if (value.isEmpty()) {
            return DecodedSegment(null, false)
        }
        if (value == LEGACY_DEFAULT_TOKEN) {
            return DecodedSegment(null, true)
        }
        return try {
            val decodedBytes = Base64.decode(value, BASE64_FLAGS)
            val decoded = String(decodedBytes, StandardCharsets.UTF_8)
            DecodedSegment(trimToNull(decoded), true)
        } catch (_: IllegalArgumentException) {
            val sanitized = value.replace('_', ' ')
            DecodedSegment(trimToNull(sanitized), false)
        }
    }

    private fun parseStringArray(array: JSONArray?): List<String> {
        if (array == null || array.length() == 0) {
            return emptyList()
        }
        val result = ArrayList<String>(array.length())
        for (index in 0 until array.length()) {
            val value = array.optString(index, null)
            val trimmed = trimToNull(value)
            if (trimmed != null) {
                result.add(trimmed)
            }
        }
        return result
    }

    private fun normalize(value: String?): String {
        return value?.trim()?.lowercase(Locale.ROOT) ?: ""
    }

    private fun trimToNull(value: String?): String? {
        val trimmed = value?.trim()
        return if (trimmed.isNullOrEmpty()) null else trimmed
    }

    private data class DecodedSegment(val value: String?, val decoded: Boolean)

    private data class LegacyRoomNames(val establishment: String?, val room: String?)

    private data class RoomKey(val establishment: String, val room: String)

    private class MigrationState {
        val establishments: LinkedHashMap<String, EstablishmentEntity> = LinkedHashMap<String, EstablishmentEntity>()
        val establishmentIdsByName: MutableMap<String, String> = LinkedHashMap<String, String>()
        val rooms: LinkedHashMap<String, RoomEntity> = LinkedHashMap<String, RoomEntity>()
        val roomNames: LinkedHashMap<RoomKey, LegacyRoomNames> = LinkedHashMap<RoomKey, LegacyRoomNames>()
    }
}
