package com.example.new1.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.util.Arrays

object New1DatabaseFactory {
    @JvmStatic
    fun create(context: Context): New1Database {
        val appContext = context.applicationContext
        SQLiteDatabase.loadLibs(appContext)
        val passphrase = DatabaseEncryptionKeyManager.getOrCreatePassphrase(appContext)
        val factory = SupportFactory(passphrase.copyOf())
        return try {
            Room.databaseBuilder(
                appContext,
                New1Database::class.java,
                New1Database.DATABASE_NAME,
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration(false)
                .build()
        } finally {
            Arrays.fill(passphrase, 0.toByte())
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
