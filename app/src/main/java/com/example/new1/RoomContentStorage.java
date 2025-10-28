package com.example.new1;

import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;

final class RoomContentStorage {
    static final String PREFS_NAME = "room_content_prefs";
    private static final String KEY_PREFIX = "room_content_";
    private static final String LEGACY_DEFAULT_TOKEN = "default";
    private static final int ENCODING_FLAGS = Base64.NO_WRAP | Base64.URL_SAFE;

    private RoomContentStorage() {
    }

    @NonNull
    static String buildKey(@Nullable String establishment, @Nullable String room) {
        return KEY_PREFIX + encode(establishment) + "_" + encode(room);
    }

    @NonNull
    static String resolveKey(@NonNull SharedPreferences preferences,
            @Nullable String establishment,
            @Nullable String room) {
        String canonicalKey = buildKey(establishment, room);
        if (preferences.contains(canonicalKey)) {
            return canonicalKey;
        }
        String legacyKey = buildLegacyKey(establishment, room);
        if (preferences.contains(legacyKey)) {
            return legacyKey;
        }
        return canonicalKey;
    }

    static void ensureCanonicalKey(@NonNull SharedPreferences preferences,
            @Nullable String establishment,
            @Nullable String room,
            @NonNull String resolvedKey) {
        String canonicalKey = buildKey(establishment, room);
        if (canonicalKey.equals(resolvedKey)) {
            return;
        }
        String storedValue = preferences.getString(resolvedKey, null);
        if (storedValue == null) {
            return;
        }
        preferences.edit()
                .putString(canonicalKey, storedValue)
                .remove(resolvedKey)
                .apply();
    }

    @NonNull
    private static String encode(@Nullable String value) {
        if (value == null) {
            return LEGACY_DEFAULT_TOKEN;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return LEGACY_DEFAULT_TOKEN;
        }
        String encoded = Base64.encodeToString(trimmed.getBytes(StandardCharsets.UTF_8),
                ENCODING_FLAGS);
        if (encoded == null || encoded.trim().isEmpty()) {
            return LEGACY_DEFAULT_TOKEN;
        }
        return encoded;
    }

    @NonNull
    private static String buildLegacyKey(@Nullable String establishment, @Nullable String room) {
        return KEY_PREFIX + legacySanitize(establishment) + "_" + legacySanitize(room);
    }

    @NonNull
    private static String legacySanitize(@Nullable String value) {
        if (value == null) {
            return LEGACY_DEFAULT_TOKEN;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return LEGACY_DEFAULT_TOKEN;
        }
        return trimmed.replaceAll("[^A-Za-z0-9]", "_");
    }
}
