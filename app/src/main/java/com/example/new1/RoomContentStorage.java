package com.example.new1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.SharedPreferences;

final class RoomContentStorage {
    static final String PREFS_NAME = "room_content_prefs";
    private static final String KEY_PREFIX = "room_content_";

    private RoomContentStorage() {
    }

    @NonNull
    static String buildKey(@Nullable String establishment, @Nullable String room) {
        return KEY_PREFIX + sanitize(establishment) + "_" + sanitize(room);
    }

    @NonNull
    static String buildLegacyKey(@Nullable String establishment, @Nullable String room) {
        return KEY_PREFIX + legacySanitize(establishment) + "_" + legacySanitize(room);
    }

    @Nullable
    static String readValue(@NonNull SharedPreferences preferences,
            @Nullable String establishment,
            @Nullable String room) {
        String primaryKey = buildKey(establishment, room);
        String storedValue = preferences.getString(primaryKey, null);
        if (storedValue != null) {
            return storedValue;
        }
        String legacyKey = buildLegacyKey(establishment, room);
        if (legacyKey.equals(primaryKey)) {
            return null;
        }
        String legacyValue = preferences.getString(legacyKey, null);
        if (legacyValue == null) {
            return null;
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(primaryKey, legacyValue);
        editor.remove(legacyKey);
        editor.apply();
        return legacyValue;
    }

    static void writeValue(@NonNull SharedPreferences preferences,
            @Nullable String establishment,
            @Nullable String room,
            @NonNull String value) {
        String primaryKey = buildKey(establishment, room);
        String legacyKey = buildLegacyKey(establishment, room);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(primaryKey, value);
        if (!legacyKey.equals(primaryKey)) {
            editor.remove(legacyKey);
        }
        editor.apply();
    }

    static void clearValue(@NonNull SharedPreferences preferences,
            @Nullable String establishment,
            @Nullable String room) {
        String primaryKey = buildKey(establishment, room);
        String legacyKey = buildLegacyKey(establishment, room);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(primaryKey);
        if (!legacyKey.equals(primaryKey)) {
            editor.remove(legacyKey);
        }
        editor.apply();
    }

    @NonNull
    private static String sanitize(@Nullable String value) {
        if (value == null) {
            return "default";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "default";
        }
        return trimmed.replaceAll("[^A-Za-z0-9]", "_");
    }

    @NonNull
    private static String legacySanitize(@Nullable String value) {
        if (value == null) {
            return "default";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "default" : trimmed;
    }
}
