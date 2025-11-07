package com.example.new1.storage.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SupportFactory;

import java.util.Arrays;

public final class RoomContentDatabaseFactory {
    private RoomContentDatabaseFactory() {
    }

    @NonNull
    public static RoomContentDatabase create(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        SQLiteDatabase.loadLibs(appContext);
        byte[] passphrase = DatabaseEncryptionKeyManager.getOrCreatePassphrase(appContext);
        SupportFactory factory = new SupportFactory(Arrays.copyOf(passphrase, passphrase.length));
        try {
            return Room.databaseBuilder(appContext, RoomContentDatabase.class,
                            RoomContentDatabase.DATABASE_NAME)
                    .openHelperFactory(factory)
                    .fallbackToDestructiveMigration()
                    .build();
        } finally {
            Arrays.fill(passphrase, (byte) 0);
        }
    }
}
