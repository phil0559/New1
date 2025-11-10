package com.example.new1

import android.app.Application
import androidx.room.Room
import com.example.new1.data.CoroutineDispatcherProvider
import com.example.new1.data.EstablishmentEntity
import com.example.new1.data.New1Database
import com.example.new1.data.RoomContentRepository
import com.example.new1.data.RoomEntity
import com.example.new1.data.UnifiedInventoryRepository
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class RoomContentViewModelTest {

    private lateinit var application: Application
    private lateinit var database: New1Database
    private lateinit var viewModel: RoomContentViewModel
    private lateinit var previousLocale: Locale

    private val dispatcherProvider = object : CoroutineDispatcherProvider {
        override val io = Dispatchers.Unconfined
        override val main = Dispatchers.Unconfined
    }

    @Before
    fun setUp() {
        application = RuntimeEnvironment.getApplication()
        previousLocale = Locale.getDefault()
        Locale.setDefault(Locale.FRANCE)
        database = Room.inMemoryDatabaseBuilder(application, New1Database::class.java)
            .allowMainThreadQueries()
            .build()
        val roomContentRepository = RoomContentRepository(
            database.roomContentDao(),
            dispatcherProvider,
        )
        val inventoryRepository = UnifiedInventoryRepository(
            database,
            dispatcherProvider = dispatcherProvider,
        )
        viewModel = RoomContentViewModel(application, roomContentRepository, inventoryRepository)
    }

    @After
    fun tearDown() {
        database.close()
        Locale.setDefault(previousLocale)
    }

    @Test
    fun loadEstablishmentNamesUsesDatabaseAndAddsCurrent() {
        runBlocking {
            database.establishmentDao().upsert(
                EstablishmentEntity("est-2", " Biblio ", "", emptyList()),
            )
            database.establishmentDao().upsert(
                EstablishmentEntity("est-1", "Atelier", "", emptyList()),
            )
        }

        val names = viewModel.loadEstablishmentNames("Salle Annexe")

        assertEquals(
            "Les établissements doivent provenir de la base et être triés.",
            listOf("Atelier", "Biblio", "Salle Annexe"),
            names,
        )
    }

    @Test
    fun loadRoomNamesUsesDatabaseAndReinsertsCurrentRoom() {
        runBlocking {
            database.establishmentDao().upsert(
                EstablishmentEntity("est-1", "Atelier", "", emptyList()),
            )
            database.roomDao().upsert(
                RoomEntity("room-1", "est-1", "Workshop", null, emptyList()),
            )
            database.roomDao().upsert(
                RoomEntity("room-2", "est-1", "Studio", null, emptyList()),
            )
        }

        // Précharger les établissements afin d'initialiser la correspondance nom -> identifiant.
        viewModel.loadEstablishmentNames(null)

        val rooms = viewModel.loadRoomNames("Atelier", "Atelier", "Warehouse")

        assertEquals(
            "Les pièces doivent refléter la base et contenir la pièce courante.",
            listOf("Studio", "Warehouse", "Workshop"),
            rooms,
        )
    }
}
