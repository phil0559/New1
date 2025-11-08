package com.example.new1.data

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import java.io.File
import java.io.IOException

/**
 * Gestionnaire de stockage chiffré pour les photos de contenu.
 * Les images sont conservées dans un dossier dédié et lues/écrites via {@link EncryptedFile}.
 */
object ContentPhotoStorage {
    private const val DIRECTORY_NAME = "content_photos"
    private const val FILE_EXTENSION = ".bin"

    fun generateFileName(photoId: String): String {
        return sanitize("${'$'}photoId${'$'}FILE_EXTENSION")
    }

    @Throws(IOException::class)
    fun write(context: Context, fileName: String, data: ByteArray) {
        val target = resolveFile(context, fileName)
        if (target.exists()) {
            target.delete()
        }
        target.parentFile?.let { parent ->
            if (!parent.exists() && !parent.mkdirs()) {
                throw IOException("Impossible de créer le répertoire pour les photos chiffrées.")
            }
        }
        val encryptedFile = buildEncryptedFile(context, target)
        encryptedFile.openFileOutput().use { output ->
            output.write(data)
            output.flush()
        }
    }

    fun read(context: Context, fileName: String): ByteArray? {
        val target = resolveFile(context, fileName)
        if (!target.exists()) {
            return null
        }
        val encryptedFile = buildEncryptedFile(context, target)
        return encryptedFile.openFileInput().use { input ->
            input.readBytes()
        }
    }

    fun delete(context: Context, fileName: String) {
        val target = resolveFile(context, fileName)
        if (target.exists()) {
            target.delete()
        }
    }

    private fun resolveFile(context: Context, fileName: String): File {
        val appContext = context.applicationContext
        val safeName = sanitize(fileName)
        val directory = File(appContext.filesDir, DIRECTORY_NAME)
        return File(directory, safeName)
    }

    private fun buildEncryptedFile(context: Context, file: File): EncryptedFile {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB,
        ).build()
    }

    private fun sanitize(value: String): String {
        val sanitized = value.replace(Regex("[^A-Za-z0-9_.-]"), "_")
        if (sanitized.isBlank()) {
            return "photo_${'$'}{System.currentTimeMillis()}${'$'}FILE_EXTENSION"
        }
        return sanitized
    }
}
