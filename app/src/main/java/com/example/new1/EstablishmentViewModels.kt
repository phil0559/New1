package com.example.new1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.new1.data.EstablishmentEntity
import com.example.new1.data.RoomContentEntity
import com.example.new1.data.RoomContentRepository
import com.example.new1.data.RoomEntity
import com.example.new1.data.UnifiedInventoryRepository
import java.util.ArrayList
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONException

class EstablishmentListViewModel(
    application: Application,
    private val repository: UnifiedInventoryRepository,
) : AndroidViewModel(application) {

    private val establishmentsLiveData: LiveData<List<Establishment>> =
        repository.observeEstablishments()
            .map { entities -> entities.map { it.toDomain() } }
            .asLiveData()

    fun getEstablishments(): LiveData<List<Establishment>> = establishmentsLiveData

    fun upsertEstablishment(establishment: Establishment) {
        viewModelScope.launch {
            repository.upsertEstablishment(establishment.toEntity())
        }
    }

    fun deleteEstablishment(establishment: Establishment) {
        val identifier = establishment.id
        if (identifier.isNullOrBlank()) {
            return
        }
        viewModelScope.launch {
            repository.deleteEstablishment(identifier)
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EstablishmentListViewModel::class.java)) {
                val repository = ViewModelDependencies.inventoryRepository(application)
                @Suppress("UNCHECKED_CAST")
                return EstablishmentListViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @JvmStatic
        fun provideFactory(application: Application): ViewModelProvider.Factory = Factory(application)
    }
}

class RoomListViewModel(
    application: Application,
    private val repository: UnifiedInventoryRepository,
    private val establishmentId: String?,
) : AndroidViewModel(application) {

    private val roomsLiveData: LiveData<List<Room>> = when {
        establishmentId.isNullOrBlank() -> MutableLiveData(emptyList())
        else -> repository.observeRooms(establishmentId)
            .map { entities -> entities.map { it.toDomain() } }
            .asLiveData()
    }

    fun getRooms(): LiveData<List<Room>> = roomsLiveData

    fun upsertRoom(room: Room) {
        viewModelScope.launch {
            repository.upsertRoom(room.toEntity())
        }
    }

    fun deleteRoom(room: Room) {
        val identifier = room.id
        if (identifier.isNullOrBlank()) {
            return
        }
        viewModelScope.launch {
            repository.deleteRoom(identifier)
        }
    }

    class Factory(
        private val application: Application,
        private val establishmentId: String?,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoomListViewModel::class.java)) {
                val repository = ViewModelDependencies.inventoryRepository(application)
                @Suppress("UNCHECKED_CAST")
                return RoomListViewModel(application, repository, establishmentId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        @JvmStatic
        fun provideFactory(
            application: Application,
            establishmentId: String?,
        ): ViewModelProvider.Factory = Factory(application, establishmentId)
    }
}

class EstablishmentSearchViewModel(
    application: Application,
    private val inventoryRepository: UnifiedInventoryRepository,
    private val roomContentRepository: RoomContentRepository,
) : AndroidViewModel(application) {

    fun loadEstablishmentsSnapshot(): List<Establishment> = runBlocking {
        inventoryRepository.listEstablishments().map { it.toDomain() }
    }

    fun loadRoomsSnapshot(establishmentId: String): List<Room> = runBlocking {
        inventoryRepository.listRooms(establishmentId).map { it.toDomain() }
    }

    fun loadRoomContentSnapshot(
        establishmentName: String?,
        roomName: String?,
    ): List<RoomContentItem> = runBlocking {
        val canonicalKey = RoomContentStorage.buildKey(establishmentName, roomName)
        val entity = roomContentRepository.getByStorageKey(canonicalKey)
            ?: loadLegacyRoomContent(canonicalKey, establishmentName, roomName)
            ?: return@runBlocking emptyList()
        parseRoomContent(entity.payloadJson)
    }

    private suspend fun loadLegacyRoomContent(
        canonicalKey: String,
        establishmentName: String?,
        roomName: String?,
    ): RoomContentEntity? {
        val legacyKey = buildLegacyKey(establishmentName, roomName)
        if (legacyKey == canonicalKey) {
            return null
        }
        val legacy = roomContentRepository.getByStorageKey(legacyKey)
        if (legacy != null) {
            roomContentRepository.upsert(
                legacy.copy(
                    storageKey = canonicalKey,
                    establishment = legacy.establishment,
                    room = legacy.room,
                ),
            )
            roomContentRepository.deleteByStorageKey(legacyKey)
        }
        return legacy
    }

    private fun parseRoomContent(payload: String): List<RoomContentItem> {
        if (payload.isBlank()) {
            return emptyList()
        }
        val items = ArrayList<RoomContentItem>()
        try {
            val array = JSONArray(payload)
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                val parsed = RoomContentItem.fromJson(item)
                items.add(parsed)
            }
            RoomContentHierarchyHelper.normalizeHierarchy(items)
        } catch (_: JSONException) {
            return emptyList()
        }
        return items
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
        val sanitized = trimmed.replace("[^A-Za-z0-9]".toRegex(), "_")
        return if (sanitized.isEmpty()) LEGACY_DEFAULT_TOKEN else sanitized
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EstablishmentSearchViewModel::class.java)) {
                val inventory = ViewModelDependencies.inventoryRepository(application)
                val roomContent = ViewModelDependencies.roomContentRepository(application)
                @Suppress("UNCHECKED_CAST")
                return EstablishmentSearchViewModel(application, inventory, roomContent) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        private const val LEGACY_PREFIX = "room_content_"
        private const val LEGACY_DEFAULT_TOKEN = "default"

        @JvmStatic
        fun provideFactory(application: Application): ViewModelProvider.Factory = Factory(application)
    }
}

private fun EstablishmentEntity.toDomain(): Establishment =
    Establishment(id, name, comment ?: "", photos)

private fun RoomEntity.toDomain(): Room =
    Room(id, establishmentId, name, comment ?: "", photos)

private fun Establishment.toEntity(): EstablishmentEntity =
    EstablishmentEntity(
        id = id ?: Establishment.generateStableId(),
        name = (name ?: "").trim(),
        comment = comment?.takeIf { it.isNotBlank() },
        photos = ArrayList(photos),
    )

private fun Room.toEntity(): RoomEntity =
    RoomEntity(
        id = id ?: Room.generateStableId(),
        establishmentId = establishmentId ?: "",
        name = (name ?: "").trim(),
        comment = comment?.takeIf { it.isNotBlank() },
        photos = ArrayList(photos),
    )
