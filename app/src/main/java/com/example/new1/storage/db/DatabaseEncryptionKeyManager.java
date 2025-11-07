package com.example.new1.storage.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

final class DatabaseEncryptionKeyManager {
    private static final String PREFS_NAME = "room_content_db_encryption";
    private static final String PREF_KEY_PASSPHRASE = "passphrase";
    private static final int PASSPHRASE_LENGTH_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private DatabaseEncryptionKeyManager() {
    }

    @NonNull
    static byte[] getOrCreatePassphrase(@NonNull Context context) {
        SharedPreferences preferences = getSecurePreferences(context.getApplicationContext());
        String encodedPassphrase = preferences.getString(PREF_KEY_PASSPHRASE, null);
        if (encodedPassphrase != null) {
            byte[] decoded = Base64.decode(encodedPassphrase, Base64.NO_WRAP);
            if (decoded.length == PASSPHRASE_LENGTH_BYTES) {
                return decoded;
            }
        }
        byte[] passphrase = new byte[PASSPHRASE_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(passphrase);
        String encoded = Base64.encodeToString(passphrase, Base64.NO_WRAP);
        preferences.edit().putString(PREF_KEY_PASSPHRASE, encoded).apply();
        return passphrase;
    }

    @NonNull
    private static SharedPreferences getSecurePreferences(@NonNull Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            return EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException exception) {
            throw new IllegalStateException(
                    "Impossible de créer les préférences chiffrées pour la clé SQLCipher.",
                    exception);
        }
    }
}
