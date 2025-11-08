package com.example.new1.data

import android.content.Context
import androidx.room.Room
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.util.Arrays

object RoomContentDatabaseFactory {
    @JvmStatic
    fun create(context: Context): RoomContentDatabase {
        val appContext = context.applicationContext
        SQLiteDatabase.loadLibs(appContext)
        val passphrase = DatabaseEncryptionKeyManager.getOrCreatePassphrase(appContext)
        val factory = SupportFactory(passphrase.copyOf())
        return try {
            Room.databaseBuilder(
                appContext,
                RoomContentDatabase::class.java,
                RoomContentDatabase.DATABASE_NAME,
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        } finally {
            Arrays.fill(passphrase, 0.toByte())
        }
    }
}
