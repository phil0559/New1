package com.example.new1

import android.content.Context
import com.example.new1.data.New1Database
import com.example.new1.data.New1DatabaseFactory
import com.example.new1.data.RoomContentRepository
import com.example.new1.data.UnifiedInventoryRepository

internal object ViewModelDependencies {
    @Volatile
    private var database: New1Database? = null

    @Volatile
    private var inventoryRepository: UnifiedInventoryRepository? = null

    @Volatile
    private var roomContentRepository: RoomContentRepository? = null

    private fun database(context: Context): New1Database {
        val appContext = context.applicationContext
        val existing = database
        if (existing != null) {
            return existing
        }
        return synchronized(this) {
            val synchronizedExisting = database
            if (synchronizedExisting != null) {
                synchronizedExisting
            } else {
                New1DatabaseFactory.create(appContext).also { created ->
                    database = created
                }
            }
        }
    }

    fun inventoryRepository(context: Context): UnifiedInventoryRepository {
        val existing = inventoryRepository
        if (existing != null) {
            return existing
        }
        return synchronized(this) {
            val synchronizedExisting = inventoryRepository
            if (synchronizedExisting != null) {
                synchronizedExisting
            } else {
                val repository = UnifiedInventoryRepository(database(context))
                inventoryRepository = repository
                repository
            }
        }
    }

    fun roomContentRepository(context: Context): RoomContentRepository {
        val existing = roomContentRepository
        if (existing != null) {
            return existing
        }
        return synchronized(this) {
            val synchronizedExisting = roomContentRepository
            if (synchronizedExisting != null) {
                synchronizedExisting
            } else {
                val repository = RoomContentRepository(database(context).roomContentDao())
                roomContentRepository = repository
                repository
            }
        }
    }
}
