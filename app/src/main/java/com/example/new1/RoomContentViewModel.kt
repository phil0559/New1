package com.example.new1

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.new1.data.New1DatabaseFactory
import com.example.new1.data.RoomContentEntity
import com.example.new1.data.RoomContentRepository
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.ArrayList
import java.util.LinkedHashSet
import java.util.List
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class RoomContentViewModel(
    application: Application,
    private val repository: RoomContentRepository,
) : AndroidViewModel(application) {

    private val establishmentPrefs: SharedPreferences =
        application.getSharedPreferences(ESTABLISHMENTS_PREFS, Context.MODE_PRIVATE)
    private val roomsPrefs: SharedPreferences =
        application.getSharedPreferences(ROOMS_PREFS, Context.MODE_PRIVATE)
    private val roomContentPrefs: SharedPreferences =
        application.getSharedPreferences(RoomContentStorage.PREFS_NAME, Context.MODE_PRIVATE)

    fun loadEstablishmentNames(currentEstablishment: String?): List<String> {
        val storedValue = establishmentPrefs.getString(KEY_ESTABLISHMENTS, null)
        val result = ArrayList<String>()
        if (!storedValue.isNullOrBlank()) {
            try {
                val array = JSONArray(storedValue)
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val name = item.optString("name", "").trim()
                    if (name.isEmpty()) {
                        continue
                    }
                    if (findIndexIgnoreCase(result, name) < 0) {
                        result.add(name)
                    }
                }
            } catch (_: JSONException) {
            }
        }
        val current = normalizeName(currentEstablishment)
        if (current.isNotEmpty() && findIndexIgnoreCase(result, current) < 0) {
            result.add(0, current)
        }
        return result
    }

    fun loadRoomNames(
        selectedEstablishment: String?,
        currentEstablishment: String?,
        currentRoom: String?,
    ): List<String> {
        val key = buildRoomsKeyForEstablishment(selectedEstablishment)
        val storedValue = roomsPrefs.getString(key, null)
        val result = ArrayList<String>()
        if (!storedValue.isNullOrBlank()) {
            try {
                val array = JSONArray(storedValue)
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    val name = item.optString("name", "").trim()
                    if (name.isEmpty()) {
                        continue
                    }
                    if (findIndexIgnoreCase(result, name) < 0) {
                        result.add(name)
                    }
                }
            } catch (_: JSONException) {
            }
        }
        val normalizedSelected = normalizeName(selectedEstablishment)
        val normalizedCurrent = normalizeName(currentEstablishment)
        if (normalizedSelected.isNotEmpty() &&
            normalizedSelected.equals(normalizedCurrent, ignoreCase = true)
        ) {
            val currentRoomName = normalizeName(currentRoom)
            if (currentRoomName.isNotEmpty() && findIndexIgnoreCase(result, currentRoomName) < 0) {
                result.add(0, currentRoomName)
            }
        }
        return result
    }

    fun loadRoomContentFor(establishment: String?, room: String?): List<RoomContentItem> {
        val canonicalKey = RoomContentStorage.buildKey(establishment, room)
        val resolvedKey = RoomContentStorage.resolveKey(roomContentPrefs, establishment, room)
        val payload = loadPayloadFromRepository(canonicalKey, resolvedKey)
            ?: loadPayloadFromPreferences(resolvedKey)
        if (payload.isNullOrBlank()) {
            if (resolvedKey != canonicalKey) {
                RoomContentStorage.ensureCanonicalKey(roomContentPrefs, establishment, room, resolvedKey)
            }
            return emptyList()
        }
        val result = ArrayList<RoomContentItem>()
        try {
            val array = JSONArray(payload)
            for (index in 0 until array.length()) {
                val itemObject = array.optJSONObject(index) ?: continue
                val parsed = RoomContentItem.fromJson(itemObject)
                result.add(parsed)
            }
            RoomContentHierarchyHelper.normalizeHierarchy(result)
        } catch (exception: JSONException) {
            roomContentPrefs.edit().remove(resolvedKey).apply()
            runBlocking {
                repository.deleteByStorageKey(canonicalKey)
                if (resolvedKey != canonicalKey) {
                    repository.deleteByStorageKey(resolvedKey)
                }
            }
            return emptyList()
        }
        if (resolvedKey != canonicalKey) {
            RoomContentStorage.ensureCanonicalKey(roomContentPrefs, establishment, room, resolvedKey)
        }
        runBlocking {
            repository.upsert(
                RoomContentEntity(
                    storageKey = canonicalKey,
                    establishment = trimmedOrNull(establishment),
                    room = trimmedOrNull(room),
                    payloadJson = payload,
                ),
            )
            if (resolvedKey != canonicalKey) {
                repository.deleteByStorageKey(resolvedKey)
            }
        }
        return result
    }

    fun saveRoomContentFor(
        establishment: String?,
        room: String?,
        items: List<RoomContentItem>,
    ) {
        val normalizedItems = ArrayList(items)
        RoomContentHierarchyHelper.normalizeHierarchy(normalizedItems)
        val array = JSONArray()
        for (item in normalizedItems) {
            array.put(item.toJson())
        }
        val payload = array.toString()
        val canonicalKey = RoomContentStorage.buildKey(establishment, room)
        roomContentPrefs.edit()
            .putString(canonicalKey, payload)
            .apply()
        runBlocking {
            repository.upsert(
                RoomContentEntity(
                    storageKey = canonicalKey,
                    establishment = trimmedOrNull(establishment),
                    room = trimmedOrNull(room),
                    payloadJson = payload,
                ),
            )
            val resolvedKey = RoomContentStorage.resolveKey(roomContentPrefs, establishment, room)
            if (resolvedKey != canonicalKey) {
                RoomContentStorage.ensureCanonicalKey(roomContentPrefs, establishment, room, resolvedKey)
                repository.deleteByStorageKey(resolvedKey)
            }
        }
    }

    fun performLookup(barcode: String, maxPhotos: Int): BarcodeLookupResult {
        val bookResult = lookupBook(barcode, maxPhotos)
        if (bookResult?.found == true) {
            return bookResult
        }
        val musicResult = lookupMusic(barcode, maxPhotos)
        if (musicResult?.found == true) {
            return musicResult
        }
        if (bookResult != null) {
            if (bookResult.errorMessage != null || bookResult.infoMessage != null) {
                return bookResult
            }
        }
        if (musicResult != null) {
            if (musicResult.errorMessage != null || musicResult.infoMessage != null) {
                return musicResult
            }
        }
        val fallback = BarcodeLookupResult()
        fallback.infoMessage =
            getApplication<Application>().getString(R.string.dialog_barcode_lookup_not_found, barcode)
        return fallback
    }

    private fun lookupBook(barcode: String, maxPhotos: Int): BarcodeLookupResult? {
        if (!isIsbnCandidate(barcode)) {
            return null
        }
        var connection: HttpURLConnection? = null
        return try {
            val urlValue = "https://www.googleapis.com/books/v1/volumes?q=isbn:" +
                URLEncoder.encode(barcode, StandardCharsets.UTF_8.name())
            val url = URL(urlValue)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "New1App/1.0 (Barcode lookup)")
            val responseCode = connection.responseCode
            val rawStream: InputStream? = if (responseCode >= 400) {
                connection.errorStream
            } else {
                connection.inputStream
            }
            if (rawStream == null) {
                val error = BarcodeLookupResult()
                error.errorMessage = getApplication<Application>().getString(R.string.dialog_barcode_lookup_error)
                error.networkError = true
                return error
            }
            val response = rawStream.use { readStream(it) }
            if (responseCode >= 400) {
                val error = BarcodeLookupResult()
                error.errorMessage = getApplication<Application>().getString(R.string.dialog_barcode_lookup_error)
                error.networkError = true
                return error
            }
            val root = JSONObject(response)
            val items = root.optJSONArray("items")
            if (items == null || items.length() == 0) {
                val fallback = BarcodeLookupResult()
                enrichBookWithOpenLibrary(barcode, fallback, maxPhotos)
                return fallback
            }
            val firstItem = items.getJSONObject(0)
            val volumeInfo = firstItem.optJSONObject("volumeInfo")
                ?: return BarcodeLookupResult().also { enrichBookWithOpenLibrary(barcode, it, maxPhotos) }
            val result = BarcodeLookupResult()
            result.found = true
            val title = volumeInfo.optString("title", null)
            if (!title.isNullOrBlank()) {
                result.title = title
            }
            val authorsArray = volumeInfo.optJSONArray("authors")
            if (authorsArray != null && authorsArray.length() > 0) {
                val authors = ArrayList<String>()
                for (i in 0 until authorsArray.length()) {
                    val value = authorsArray.optString(i, null)
                    if (value != null) {
                        val trimmed = value.trim()
                        if (trimmed.isNotEmpty()) {
                            authors.add(trimmed)
                        }
                    }
                }
                if (authors.isNotEmpty()) {
                    result.author = android.text.TextUtils.join(", ", authors)
                }
            }
            val publisher = volumeInfo.optString("publisher", null)
            if (!publisher.isNullOrBlank()) {
                result.publisher = publisher
            }
            val publishedDate = volumeInfo.optString("publishedDate", null)
            if (!publishedDate.isNullOrBlank()) {
                result.publishDate = publishedDate
            }
            val description = resolveDescription(volumeInfo.opt("description"))
            if (!description.isNullOrBlank()) {
                result.summary = description
            }
            val subtitle = volumeInfo.optString("subtitle", null)
            if (!subtitle.isNullOrBlank()) {
                val trimmedSubtitle = subtitle.trim()
                if (result.summary.isNullOrBlank()) {
                    result.summary = trimmedSubtitle
                }
                val loweredSubtitle = trimmedSubtitle.lowercase(Locale.getDefault())
                if (loweredSubtitle.contains("édition") ||
                    loweredSubtitle.contains("edition") ||
                    loweredSubtitle.contains("éd.")
                ) {
                    result.edition = trimmedSubtitle
                }
            }
            val seriesInfo = volumeInfo.optJSONObject("seriesInfo")
            if (seriesInfo != null) {
                val series = seriesInfo.optString("series", null)
                if (!series.isNullOrBlank()) {
                    result.series = series
                }
                val number = seriesInfo.optString("bookDisplayNumber", null)
                if (!number.isNullOrBlank()) {
                    result.number = number
                }
            }
            val categories = volumeInfo.optJSONArray("categories")
            var isComic = false
            if (categories != null) {
                for (i in 0 until categories.length()) {
                    val category = categories.optString(i, "")
                    val lowered = category.lowercase(Locale.getDefault())
                    if (lowered.contains("comic") ||
                        lowered.contains("bande dessin") ||
                        lowered.contains("manga")
                    ) {
                        isComic = true
                        break
                    }
                }
            }
            result.typeLabel = getApplication<Application>().getString(
                if (isComic) R.string.dialog_type_comic else R.string.dialog_type_book,
            )
            val imageLinks = volumeInfo.optJSONObject("imageLinks")
            if (imageLinks != null) {
                val keys = arrayOf("extraLarge", "large", "medium", "thumbnail", "smallThumbnail")
                for (key in keys) {
                    val urlCandidate = imageLinks.optString(key, null)
                    if (urlCandidate.isNullOrBlank()) {
                        continue
                    }
                    val sanitizedUrl = urlCandidate.replace("http://", "https://")
                    val photo = downloadImageAsBase64(sanitizedUrl)
                    if (photo != null && !result.photos.contains(photo)) {
                        result.photos.add(photo)
                        if (result.photos.size >= maxPhotos) {
                            break
                        }
                    }
                }
            }
            enrichBookWithOpenLibrary(barcode, result, maxPhotos)
            result
        } catch (e: IOException) {
            val error = BarcodeLookupResult()
            error.errorMessage = getApplication<Application>().getString(R.string.dialog_barcode_lookup_error)
            error.networkError = true
            error
        } catch (e: JSONException) {
            val error = BarcodeLookupResult()
            error.errorMessage = getApplication<Application>().getString(R.string.dialog_barcode_lookup_error)
            error
        } finally {
            connection?.disconnect()
        }
    }

    private fun enrichBookWithOpenLibrary(
        isbn: String,
        result: BarcodeLookupResult,
        maxPhotos: Int,
    ) {
        var connection: HttpURLConnection? = null
        var hasData = result.found
        try {
            val urlValue = "https://openlibrary.org/isbn/" +
                URLEncoder.encode(isbn, StandardCharsets.UTF_8.name()) + ".json"
            val url = URL(urlValue)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "New1App/1.0 (Barcode lookup)")
            val responseCode = connection.responseCode
            val rawStream: InputStream? = if (responseCode >= 400) {
                connection.errorStream
            } else {
                connection.inputStream
            }
            if (rawStream == null || responseCode >= 400) {
                return
            }
            val response = rawStream.use { readStream(it) }
            val root = JSONObject(response)

            if (result.title.isNullOrBlank()) {
                val title = root.optString("title", null)
                if (!title.isNullOrBlank()) {
                    result.title = title
                    hasData = true
                }
            }

            if (result.publisher.isNullOrBlank()) {
                val publishers = root.optJSONArray("publishers")
                if (publishers != null && publishers.length() > 0) {
                    val publisherSet = LinkedHashSet<String>()
                    for (i in 0 until publishers.length()) {
                        val value = publishers.optString(i, null)
                        if (value != null) {
                            val trimmed = value.trim()
                            if (trimmed.isNotEmpty()) {
                                publisherSet.add(trimmed)
                            }
                        }
                    }
                    if (publisherSet.isNotEmpty()) {
                        result.publisher = android.text.TextUtils.join(", ", ArrayList(publisherSet))
                        hasData = true
                    }
                }
            }

            if (result.series.isNullOrBlank()) {
                val seriesArray = root.optJSONArray("series")
                if (seriesArray != null && seriesArray.length() > 0) {
                    val seriesSet = LinkedHashSet<String>()
                    for (i in 0 until seriesArray.length()) {
                        val value = seriesArray.optString(i, null)
                        if (value != null) {
                            val trimmed = value.trim()
                            if (trimmed.isNotEmpty()) {
                                seriesSet.add(trimmed)
                            }
                        }
                    }
                    if (seriesSet.isNotEmpty()) {
                        result.series = android.text.TextUtils.join(", ", ArrayList(seriesSet))
                        hasData = true
                    }
                }
            }

            if (result.number.isNullOrBlank()) {
                var volume: String? = root.optString("series_number", null)
                if (volume.isNullOrBlank()) {
                    volume = root.optString("volume", null)
                }
                if (volume.isNullOrBlank()) {
                    volume = root.optString("number", null)
                }
                if (!volume.isNullOrBlank()) {
                    result.number = volume
                    hasData = true
                }
            }

            if (result.edition.isNullOrBlank()) {
                val edition = root.optString("edition_name", null)
                if (!edition.isNullOrBlank()) {
                    result.edition = edition
                    hasData = true
                }
            }

            if (result.publishDate.isNullOrBlank()) {
                val publishDate = root.optString("publish_date", null)
                if (!publishDate.isNullOrBlank()) {
                    result.publishDate = publishDate
                    hasData = true
                }
            }

            if (result.summary.isNullOrBlank()) {
                var description = resolveDescription(root.opt("description"))
                if (description.isNullOrBlank()) {
                    description = resolveDescription(root.opt("notes"))
                }
                if (!description.isNullOrBlank()) {
                    result.summary = description
                    hasData = true
                }
            }

            if (result.author.isNullOrBlank()) {
                val authors = root.optJSONArray("authors")
                if (authors != null && authors.length() > 0) {
                    val authorNames = LinkedHashSet<String>()
                    for (i in 0 until authors.length()) {
                        val authorObject = authors.optJSONObject(i) ?: continue
                        val key = authorObject.optString("key", null)
                        val authorName = fetchOpenLibraryAuthorName(key)
                        if (!authorName.isNullOrBlank()) {
                            authorNames.add(authorName)
                        }
                    }
                    if (authorNames.isNotEmpty()) {
                        result.author = android.text.TextUtils.join(", ", ArrayList(authorNames))
                        hasData = true
                    }
                }
            }

            val covers = root.optJSONArray("covers")
            if (covers != null) {
                for (i in 0 until covers.length()) {
                    if (result.photos.size >= maxPhotos) {
                        break
                    }
                    val coverId = covers.optInt(i, -1)
                    if (coverId <= 0) {
                        continue
                    }
                    if (addOpenLibraryCoverPhoto(coverId, result.photos, maxPhotos)) {
                        hasData = true
                    }
                }
            }

            if (hasData) {
                result.found = true
            }
        } catch (_: IOException) {
        } catch (_: JSONException) {
        } finally {
            connection?.disconnect()
        }
    }

    private fun fetchOpenLibraryAuthorName(authorKey: String?): String? {
        if (authorKey == null) {
            return null
        }
        val normalizedKey = authorKey.trim()
        if (normalizedKey.isEmpty()) {
            return null
        }
        var connection: HttpURLConnection? = null
        return try {
            val urlValue = "https://openlibrary.org" + normalizedKey + ".json"
            val url = URL(urlValue)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "New1App/1.0 (Barcode lookup)")
            val responseCode = connection.responseCode
            if (responseCode >= 400) {
                return null
            }
            val response = connection.inputStream.use { readStream(it) }
            val root = JSONObject(response)
            val name = root.optString("personal_name", null)
                ?: root.optString("name", null)
            name?.trim()?.takeIf { it.isNotEmpty() }
        } catch (_: IOException) {
            null
        } catch (_: JSONException) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun addOpenLibraryCoverPhoto(
        coverId: Int,
        destination: MutableList<String>,
        maxPhotos: Int,
    ): Boolean {
        if (coverId <= 0 || destination.size >= maxPhotos) {
            return false
        }
        val url = "https://covers.openlibrary.org/b/id/" + coverId + "-L.jpg"
        val photo = downloadImageAsBase64(url)
        if (photo == null || destination.contains(photo)) {
            return false
        }
        destination.add(photo)
        return true
    }

    private fun resolveDescription(descriptionValue: Any?): String? {
        return when (descriptionValue) {
            is JSONObject -> sanitizeDescription(descriptionValue.optString("value", null))
            is String -> sanitizeDescription(descriptionValue)
            else -> null
        }
    }

    private fun sanitizeDescription(value: String?): String? {
        if (value == null) {
            return null
        }
        val trimmed = value.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        val withoutHtml = trimmed.replace("<[^>]+>".toRegex(), "").replace("\r", "").trim()
        return if (withoutHtml.isEmpty()) null else withoutHtml
    }

    private fun isIsbnCandidate(barcode: String): Boolean {
        val trimmed = barcode.replace("\\s".toRegex(), "")
        return when (trimmed.length) {
            10 -> true
            13 -> trimmed.startsWith("978") || trimmed.startsWith("979")
            else -> false
        }
    }

    private fun lookupMusic(barcode: String, maxPhotos: Int): BarcodeLookupResult? {
        var connection: HttpURLConnection? = null
        return try {
            val urlValue = "https://musicbrainz.org/ws/2/release/?query=barcode:" +
                URLEncoder.encode(barcode, StandardCharsets.UTF_8.name()) +
                "&fmt=json&limit=1"
            val url = URL(urlValue)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "New1App/1.0 (Barcode lookup)")
            val responseCode = connection.responseCode
            val rawStream: InputStream? = if (responseCode >= 400) {
                connection.errorStream
            } else {
                connection.inputStream
            }
            if (rawStream == null) {
                val error = BarcodeLookupResult()
                error.errorMessage = getApplication<Application>().getString(R.string.dialog_barcode_lookup_error)
                error.networkError = true
                return error
            }
            val response = rawStream.use { readStream(it) }
            if (responseCode >= 400) {
                val error = BarcodeLookupResult()
                error.errorMessage = getApplication<Application>().getString(R.string.dialog_barcode_lookup_error)
                error.networkError = true
                return error
            }
            val root = JSONObject(response)
            val releases = root.optJSONArray("releases")
            if (releases == null || releases.length() == 0) {
                val fallback = BarcodeLookupResult()
                fallback.infoMessage =
                    getApplication<Application>().getString(R.string.dialog_barcode_lookup_not_found, barcode)
                return fallback
            }
            val release = releases.getJSONObject(0)
            val result = BarcodeLookupResult()
            result.found = true
            val title = release.optString("title", null)
            if (!title.isNullOrBlank()) {
                result.title = title
            }
            val artistCredit = release.optJSONArray("artist-credit")
            if (artistCredit != null) {
                val artistNames = LinkedHashSet<String>()
                for (i in 0 until artistCredit.length()) {
                    val credit = artistCredit.optJSONObject(i) ?: continue
                    val artistObject = credit.optJSONObject("artist") ?: continue
                    val name = artistObject.optString("name", null)
                    if (!name.isNullOrBlank()) {
                        artistNames.add(name.trim())
                    }
                }
                if (artistNames.isNotEmpty()) {
                    result.author = android.text.TextUtils.join(", ", ArrayList(artistNames))
                }
            }
            val mediaArray = release.optJSONArray("media")
            if (mediaArray != null) {
                val trackDetails = mutableListOf<String>()
                for (i in 0 until mediaArray.length()) {
                    val media = mediaArray.optJSONObject(i) ?: continue
                    val format = media.optString("format", null)
                    val tracks = media.optJSONArray("tracks")
                    if (tracks != null && tracks.length() > 0) {
                        val titles = ArrayList<String>()
                        for (trackIndex in 0 until tracks.length()) {
                            val track = tracks.optJSONObject(trackIndex) ?: continue
                            val trackTitle = track.optString("title", null)
                            if (!trackTitle.isNullOrBlank()) {
                                titles.add(trackTitle)
                            }
                        }
                        if (titles.isNotEmpty()) {
                            val label = if (format.isNullOrBlank()) {
                                getApplication<Application>().getString(R.string.dialog_track_list_generic)
                            } else {
                                getApplication<Application>().getString(R.string.dialog_track_list_with_format, format)
                            }
                            trackDetails.add(label + "\n" + android.text.TextUtils.join("\n", titles))
                        }
                    }
                }
                if (trackDetails.isNotEmpty()) {
                    result.summary = android.text.TextUtils.join("\n\n", trackDetails)
                }
            }
            val date = release.optString("date", null)
            if (!date.isNullOrBlank()) {
                result.publishDate = date
            }
            val labelInfo = release.optJSONArray("label-info")
            if (labelInfo != null) {
                for (i in 0 until labelInfo.length()) {
                    val info = labelInfo.optJSONObject(i) ?: continue
                    val label = info.optJSONObject("label") ?: continue
                    val labelName = label.optString("name", null)
                    if (!labelName.isNullOrBlank()) {
                        result.publisher = labelName
                        break
                    }
                }
            }
            val releaseId = release.optString("id", null)
            if (!releaseId.isNullOrBlank()) {
                addCoverArtFromMusicBrainz(releaseId, result.photos, maxPhotos)
            }
            result
        } catch (e: IOException) {
            val error = BarcodeLookupResult()
            error.errorMessage = getApplication<Application>().getString(R.string.dialog_barcode_lookup_error)
            error.networkError = true
            error
        } catch (e: JSONException) {
            val error = BarcodeLookupResult()
            error.errorMessage = getApplication<Application>().getString(R.string.dialog_barcode_lookup_error)
            error
        } finally {
            connection?.disconnect()
        }
    }

    private fun addCoverArtFromMusicBrainz(
        releaseId: String,
        destination: MutableList<String>,
        maxPhotos: Int,
    ) {
        if (destination.size >= maxPhotos) {
            return
        }
        val base = "https://coverartarchive.org/release/" + releaseId + "/"
        val paths = arrayOf("front-500", "front")
        for (path in paths) {
            if (destination.size >= maxPhotos) {
                break
            }
            val photo = downloadImageAsBase64(base + path)
            if (photo != null && !destination.contains(photo)) {
                destination.add(photo)
            }
        }
    }

    private fun downloadImageAsBase64(urlString: String): String? {
        val bitmap = downloadBitmap(urlString) ?: return null
        return encodePhoto(bitmap)
    }

    private fun downloadBitmap(urlString: String): Bitmap? {
        var connection: HttpURLConnection? = null
        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 15_000
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "New1App/1.0 (Barcode lookup)")
            val responseCode = connection.responseCode
            if (responseCode >= 400) {
                null
            } else {
                BufferedInputStream(connection.inputStream).use { BitmapFactory.decodeStream(it) }
            }
        } catch (exception: IOException) {
            Log.w(TAG, "Failed to download bitmap", exception)
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun encodePhoto(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun readStream(stream: InputStream): String {
        val builder = StringBuilder()
        BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
            var line = reader.readLine()
            while (line != null) {
                builder.append(line)
                line = reader.readLine()
            }
        }
        return builder.toString()
    }

    private fun loadPayloadFromPreferences(key: String): String? {
        return roomContentPrefs.getString(key, null)
    }

    private fun loadPayloadFromRepository(canonicalKey: String, resolvedKey: String): String? {
        val fromCanonical = runBlocking { repository.getByStorageKey(canonicalKey) }
        if (fromCanonical != null) {
            return fromCanonical.payloadJson
        }
        if (resolvedKey != canonicalKey) {
            val legacy = runBlocking { repository.getByStorageKey(resolvedKey) }
            if (legacy != null) {
                runBlocking {
                    repository.upsert(
                        legacy.copy(
                            storageKey = canonicalKey,
                            establishment = legacy.establishment,
                            room = legacy.room,
                        ),
                    )
                    repository.deleteByStorageKey(resolvedKey)
                }
                return legacy.payloadJson
            }
        }
        return null
    }

    private fun buildRoomsKeyForEstablishment(establishment: String?): String {
        val trimmed = establishment?.trim().orEmpty()
        return if (trimmed.isEmpty()) {
            KEY_ROOMS + "_default"
        } else {
            KEY_ROOMS + "_" + trimmed
        }
    }

    private fun normalizeName(value: String?): String {
        return value?.trim().orEmpty()
    }

    private fun findIndexIgnoreCase(values: List<String>, target: String?): Int {
        if (target == null) {
            return -1
        }
        val normalizedTarget = target.trim()
        for (index in values.indices) {
            val value = values[index]
            if (value.trim().equals(normalizedTarget, ignoreCase = true)) {
                return index
            }
        }
        return -1
    }

    private fun trimmedOrNull(value: String?): String? {
        val trimmed = value?.trim()
        return if (trimmed.isNullOrEmpty()) null else trimmed
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RoomContentViewModel::class.java)) {
                val database = New1DatabaseFactory.create(application)
                val repository = RoomContentRepository(database.roomContentDao())
                @Suppress("UNCHECKED_CAST")
                return RoomContentViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
        }
    }

    companion object {
        private const val TAG = "RoomContentViewModel"

        @JvmStatic
        fun provideFactory(application: Application): ViewModelProvider.Factory = Factory(application)
    }
}

class BarcodeLookupResult {
    var found: Boolean = false
    var networkError: Boolean = false
    var typeLabel: String? = null
    var title: String? = null
    var author: String? = null
    var publisher: String? = null
    var series: String? = null
    var number: String? = null
    var edition: String? = null
    var publishDate: String? = null
    var summary: String? = null
    val photos: MutableList<String> = ArrayList()
    var infoMessage: String? = null
    var errorMessage: String? = null
}

private const val ESTABLISHMENTS_PREFS = "establishments_prefs"
private const val KEY_ESTABLISHMENTS = "establishments"
private const val ROOMS_PREFS = "rooms_prefs"
private const val KEY_ROOMS = "rooms"
