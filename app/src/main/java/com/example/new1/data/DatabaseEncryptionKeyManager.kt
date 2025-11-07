package com.example.new1.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.SecureRandom

internal object DatabaseEncryptionKeyManager {
    private const val PREFS_NAME = "room_content_db_encryption"
    private const val PREF_KEY_PASSPHRASE = "passphrase"
    private const val PASSPHRASE_LENGTH_BYTES = 32
    private val secureRandom = SecureRandom()

    fun getOrCreatePassphrase(context: Context): ByteArray {
        val preferences = getSecurePreferences(context.applicationContext)
        val encodedPassphrase = preferences.getString(PREF_KEY_PASSPHRASE, null)
        if (encodedPassphrase != null) {
            val decoded = Base64.decode(encodedPassphrase, Base64.NO_WRAP)
            if (decoded.size == PASSPHRASE_LENGTH_BYTES) {
                return decoded
            }
        }
        val passphrase = ByteArray(PASSPHRASE_LENGTH_BYTES)
        secureRandom.nextBytes(passphrase)
        val encoded = Base64.encodeToString(passphrase, Base64.NO_WRAP)
        preferences.edit().putString(PREF_KEY_PASSPHRASE, encoded).apply()
        return passphrase
    }

    private fun getSecurePreferences(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (exception: GeneralSecurityException) {
            throw IllegalStateException(
                "Impossible de créer les préférences chiffrées pour la clé SQLCipher.",
                exception,
            )
        } catch (exception: IOException) {
            throw IllegalStateException(
                "Impossible de créer les préférences chiffrées pour la clé SQLCipher.",
                exception,
            )
        }
    }
}
