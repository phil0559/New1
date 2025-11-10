package com.example.new1.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.util.Arrays

object New1DatabaseFactory {
    private const val TAG = "New1DatabaseFactory"

    @JvmStatic
    fun create(context: Context): New1Database {
        val appContext = context.applicationContext
        return try {
            createEncryptedDatabase(appContext)
        } catch (exception: Throwable) {
            Log.w(
                TAG,
                "Échec du chargement de la base de données sécurisée, utilisation d'une base non chiffrée.",
                exception,
            )
            createUnencryptedDatabase(appContext)
        }
    }

    private fun createEncryptedDatabase(context: Context): New1Database {
        SQLiteDatabase.loadLibs(context)
        val passphrase = DatabaseEncryptionKeyManager.getOrCreatePassphrase(context)
        val factory = SupportFactory(passphrase.copyOf())
        try {
            return Room.databaseBuilder(
                context,
                New1Database::class.java,
                New1Database.DATABASE_NAME,
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration(false)
                .build()
                .also { database ->
                    DatabaseInitializer.initialize(context, database)
                }
        } finally {
            Arrays.fill(passphrase, 0.toByte())
        }
    }

    private fun createUnencryptedDatabase(context: Context): New1Database {
        return Room.databaseBuilder(
            context,
            New1Database::class.java,
            New1Database.DATABASE_NAME,
        )
            .fallbackToDestructiveMigration(false)
            .build()
            .also { database ->
                DatabaseInitializer.initialize(context, database)
            }
    }

    private fun <T : RoomDatabase> RoomDatabase.Builder<T>.fallbackToDestructiveMigration(
        enabled: Boolean,
    ): RoomDatabase.Builder<T> {
        return if (enabled) {
            fallbackToDestructiveMigration()
        } else {
            this
        }
    }
}
