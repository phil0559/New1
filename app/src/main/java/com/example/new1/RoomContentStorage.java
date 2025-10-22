package com.example.new1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
}
