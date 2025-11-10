package com.example.new1.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.security.KeyStore

internal object DatabaseEncryptionKeyManager {
    private const val PREFS_NAME = "room_content_db_encryption"
    private const val PREFS_FALLBACK_NAME = "room_content_db_encryption_plain"
    private const val PREF_KEY_PASSPHRASE = "passphrase"
    private const val PASSPHRASE_LENGTH_BYTES = 32
    private const val TAG = "DbEncryptionKeyMgr"
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
        return tryCreateEncryptedPreferences(context, allowReset = true)
            ?: buildUnencryptedPreferences(context)
    }

    private fun tryCreateEncryptedPreferences(
        context: Context,
        allowReset: Boolean,
    ): SharedPreferences? {
        return try {
            buildEncryptedPreferences(context)
        } catch (exception: GeneralSecurityException) {
            handleEncryptedPreferencesFailure(context, exception, allowReset)
        } catch (exception: IOException) {
            handleEncryptedPreferencesFailure(context, exception, allowReset)
        }
    }

    private fun buildEncryptedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private fun handleEncryptedPreferencesFailure(
        context: Context,
        exception: Exception,
        allowReset: Boolean,
    ): SharedPreferences? {
        if (allowReset) {
            Log.w(
                TAG,
                "Échec de l'accès aux préférences chiffrées, suppression de la clé et nouvelle tentative.",
                exception,
            )
            resetSecureStorage(context)
            return tryCreateEncryptedPreferences(context, allowReset = false)
        }
        Log.e(
            TAG,
            "Impossible d'initialiser les préférences chiffrées de manière sécurisée, recours à un stockage non chiffré.",
            exception,
        )
        return null
    }

    private fun buildUnencryptedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_FALLBACK_NAME, Context.MODE_PRIVATE)
    }

    private fun resetSecureStorage(context: Context) {
        context.deleteSharedPreferences(PREFS_NAME)
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            val alias = MasterKey.DEFAULT_MASTER_KEY_ALIAS
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
            }
        } catch (exception: GeneralSecurityException) {
            Log.w(TAG, "Impossible de réinitialiser la clé maître AndroidKeyStore.", exception)
        } catch (exception: IOException) {
            Log.w(TAG, "Impossible de réinitialiser la clé maître AndroidKeyStore.", exception)
        }
    }
}
