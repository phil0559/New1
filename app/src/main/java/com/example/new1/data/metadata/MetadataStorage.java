package com.example.new1.data.metadata;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.new1.RoomContentStorage;
import com.example.new1.data.New1Database;
import com.example.new1.data.New1DatabaseFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class MetadataStorage {
    private static final String KEY_CUSTOM_CATEGORIES = "custom_categories";
    private static final String KEY_TYPE_FIELD_CONFIGS = "type_field_configs";
    private static final String KEY_TYPE_DATE_FORMATS = "type_date_formats";

    private static final String FIELD_NAME = "name";
    private static final String FIELD_COMMENT = "comment";
    private static final String FIELD_DATE = "date";
    private static final String FIELD_PHOTOS = "photos";
    private static final String FIELD_CUSTOM_1 = "custom_field_1";
    private static final String FIELD_CUSTOM_2 = "custom_field_2";

    private static final String DATE_FORMAT_EUROPEAN = "dd/MM/yyyy";
    private static final String DATE_FORMAT_ENGLISH = "MM/dd/yyyy";
    private static final String DATE_FORMAT_ISO = "yyyy-MM-dd";

    private final New1Database database;
    private final MetadataDao metadataDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final SharedPreferences legacyPreferences;

    public MetadataStorage(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        this.database = New1DatabaseFactory.create(appContext);
        this.metadataDao = database.metadataDao();
        this.legacyPreferences = appContext.getSharedPreferences(RoomContentStorage.PREFS_NAME, Context.MODE_PRIVATE);
        migrateLegacyDataIfNecessary();
    }

    public void close() {
        executor.shutdown();
    }

    @NonNull
    public List<String> loadCategories() {
        List<CategoryOptionEntity> entities = executeBlocking(metadataDao::listCategoryOptions);
        List<String> result = new ArrayList<>();
        for (CategoryOptionEntity entity : entities) {
            if (entity == null) {
                continue;
            }
            String label = trimToNull(entity.getLabel());
            if (label != null) {
                result.add(label);
            }
        }
        return result;
    }

    public void saveCategories(@NonNull List<String> labels) {
        List<CategoryOptionEntity> entities = buildCategoryEntities(labels);
        executeBlocking(() -> {
            database.runInTransaction(() -> {
                metadataDao.clearCategoryOptions();
                if (!entities.isEmpty()) {
                    metadataDao.insertCategoryOptions(entities);
                }
            });
            return null;
        });
    }

    @NonNull
    public Map<String, Set<String>> loadTypeFieldConfigurations() {
        List<TypeFieldConfigEntity> configs = executeBlocking(metadataDao::listTypeFieldConfigs);
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (TypeFieldConfigEntity entity : configs) {
            if (entity == null) {
                continue;
            }
            String typeLabel = trimToNull(entity.getTypeLabel());
            String fieldId = trimToNull(entity.getFieldId());
            if (typeLabel == null || fieldId == null) {
                continue;
            }
            Set<String> fields = result.get(typeLabel);
            if (fields == null) {
                fields = new LinkedHashSet<>();
                result.put(typeLabel, fields);
            }
            fields.add(fieldId);
        }
        return result;
    }

    public void saveTypeFieldConfigurations(@NonNull Map<String, Set<String>> configurations) {
        List<TypeFieldConfigEntity> entities = buildTypeFieldConfigEntities(configurations);
        executeBlocking(() -> {
            database.runInTransaction(() -> {
                metadataDao.clearTypeFieldConfigs();
                if (!entities.isEmpty()) {
                    metadataDao.insertTypeFieldConfigs(entities);
                }
            });
            return null;
        });
    }

    @NonNull
    public Map<String, String> loadTypeDateFormats() {
        List<TypeDateFormatEntity> formats = executeBlocking(metadataDao::listTypeDateFormats);
        Map<String, String> result = new LinkedHashMap<>();
        for (TypeDateFormatEntity entity : formats) {
            if (entity == null) {
                continue;
            }
            String typeLabel = trimToNull(entity.getTypeLabel());
            String format = sanitizeDateFormat(entity.getDateFormat());
            if (typeLabel != null && format != null) {
                result.put(typeLabel, format);
            }
        }
        return result;
    }

    public void saveTypeDateFormats(@NonNull Map<String, String> formats) {
        List<TypeDateFormatEntity> entities = buildTypeDateFormatEntities(formats);
        executeBlocking(() -> {
            database.runInTransaction(() -> {
                metadataDao.clearTypeDateFormats();
                if (!entities.isEmpty()) {
                    metadataDao.insertTypeDateFormats(entities);
                }
            });
            return null;
        });
    }

    private void migrateLegacyDataIfNecessary() {
        executeBlocking(() -> {
            migrateCategories();
            migrateTypeFieldConfigs();
            migrateTypeDateFormats();
            return null;
        });
    }

    private void migrateCategories() {
        if (metadataDao.countCategoryOptions() > 0) {
            clearLegacyKey(KEY_CUSTOM_CATEGORIES);
            return;
        }
        String stored = legacyPreferences.getString(KEY_CUSTOM_CATEGORIES, null);
        if (stored == null) {
            return;
        }
        List<String> labels = parseLegacyCategories(stored);
        database.runInTransaction(() -> {
            metadataDao.clearCategoryOptions();
            if (!labels.isEmpty()) {
                metadataDao.insertCategoryOptions(buildCategoryEntities(labels));
            }
        });
        clearLegacyKey(KEY_CUSTOM_CATEGORIES);
    }

    private void migrateTypeFieldConfigs() {
        if (metadataDao.countTypeFieldConfigs() > 0) {
            clearLegacyKey(KEY_TYPE_FIELD_CONFIGS);
            return;
        }
        String stored = legacyPreferences.getString(KEY_TYPE_FIELD_CONFIGS, null);
        if (stored == null) {
            return;
        }
        Map<String, Set<String>> configurations = parseLegacyTypeFieldConfigs(stored);
        database.runInTransaction(() -> {
            metadataDao.clearTypeFieldConfigs();
            if (!configurations.isEmpty()) {
                metadataDao.insertTypeFieldConfigs(buildTypeFieldConfigEntities(configurations));
            }
        });
        clearLegacyKey(KEY_TYPE_FIELD_CONFIGS);
    }

    private void migrateTypeDateFormats() {
        if (metadataDao.countTypeDateFormats() > 0) {
            clearLegacyKey(KEY_TYPE_DATE_FORMATS);
            return;
        }
        String stored = legacyPreferences.getString(KEY_TYPE_DATE_FORMATS, null);
        if (stored == null) {
            return;
        }
        Map<String, String> formats = parseLegacyTypeDateFormats(stored);
        database.runInTransaction(() -> {
            metadataDao.clearTypeDateFormats();
            if (!formats.isEmpty()) {
                metadataDao.insertTypeDateFormats(buildTypeDateFormatEntities(formats));
            }
        });
        clearLegacyKey(KEY_TYPE_DATE_FORMATS);
    }

    private void clearLegacyKey(@NonNull String key) {
        if (legacyPreferences.contains(key)) {
            legacyPreferences.edit().remove(key).apply();
        }
    }

    @NonNull
    private List<String> parseLegacyCategories(@NonNull String stored) {
        List<String> result = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(stored);
            LinkedHashSet<String> normalized = new LinkedHashSet<>();
            for (int i = 0; i < array.length(); i++) {
                String value = trimToNull(array.optString(i, null));
                if (value == null) {
                    continue;
                }
                String normalizedKey = value.toLowerCase(Locale.ROOT);
                if (normalized.add(normalizedKey)) {
                    result.add(value);
                }
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    @NonNull
    private Map<String, Set<String>> parseLegacyTypeFieldConfigs(@NonNull String stored) {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        try {
            JSONObject root = new JSONObject(stored);
            JSONArray names = root.names();
            if (names == null) {
                return result;
            }
            for (int i = 0; i < names.length(); i++) {
                String key = trimToNull(names.optString(i));
                if (key == null) {
                    continue;
                }
                JSONArray fieldsArray = root.optJSONArray(key);
                if (fieldsArray == null) {
                    continue;
                }
                LinkedHashSet<String> fields = new LinkedHashSet<>();
                for (int j = 0; j < fieldsArray.length(); j++) {
                    String value = trimToNull(fieldsArray.optString(j, null));
                    if (value != null) {
                        fields.add(value);
                    }
                }
                result.put(key, sanitizeFieldSelection(fields));
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    @NonNull
    private Map<String, String> parseLegacyTypeDateFormats(@NonNull String stored) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            JSONObject root = new JSONObject(stored);
            JSONArray names = root.names();
            if (names == null) {
                return result;
            }
            for (int i = 0; i < names.length(); i++) {
                String key = trimToNull(names.optString(i));
                if (key == null) {
                    continue;
                }
                String value = sanitizeDateFormat(root.optString(key, null));
                if (value != null) {
                    result.put(key, value);
                }
            }
        } catch (JSONException ignored) {
        }
        return result;
    }

    @NonNull
    private List<CategoryOptionEntity> buildCategoryEntities(@NonNull List<String> labels) {
        List<CategoryOptionEntity> entities = new ArrayList<>();
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        int position = 0;
        for (String label : labels) {
            String trimmed = trimToNull(label);
            if (trimmed == null) {
                continue;
            }
            String normalizedKey = trimmed.toLowerCase(Locale.ROOT);
            if (!normalized.add(normalizedKey)) {
                continue;
            }
            entities.add(new CategoryOptionEntity(0L, trimmed, normalizedKey, position));
            position++;
        }
        return entities;
    }

    @NonNull
    private List<TypeFieldConfigEntity> buildTypeFieldConfigEntities(@NonNull Map<String, Set<String>> configurations) {
        List<TypeFieldConfigEntity> entities = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entry : configurations.entrySet()) {
            String typeLabel = trimToNull(entry.getKey());
            if (typeLabel == null) {
                continue;
            }
            LinkedHashSet<String> sanitizedFields = sanitizeFieldSelection(entry.getValue());
            String normalizedLabel = typeLabel.toLowerCase(Locale.ROOT);
            int position = 0;
            for (String fieldId : sanitizedFields) {
                entities.add(new TypeFieldConfigEntity(typeLabel, normalizedLabel, fieldId, position));
                position++;
            }
        }
        return entities;
    }

    @NonNull
    private List<TypeDateFormatEntity> buildTypeDateFormatEntities(@NonNull Map<String, String> formats) {
        List<TypeDateFormatEntity> entities = new ArrayList<>();
        for (Map.Entry<String, String> entry : formats.entrySet()) {
            String typeLabel = trimToNull(entry.getKey());
            String format = sanitizeDateFormat(entry.getValue());
            if (typeLabel == null || format == null) {
                continue;
            }
            String normalizedLabel = typeLabel.toLowerCase(Locale.ROOT);
            entities.add(new TypeDateFormatEntity(typeLabel, normalizedLabel, format));
        }
        return entities;
    }

    @NonNull
    private LinkedHashSet<String> sanitizeFieldSelection(@Nullable Set<String> fields) {
        LinkedHashSet<String> sanitized = new LinkedHashSet<>();
        sanitized.add(FIELD_NAME);
        if (fields == null) {
            return sanitized;
        }
        for (String field : fields) {
            String trimmed = trimToNull(field);
            if (trimmed == null || FIELD_NAME.equals(trimmed)) {
                continue;
            }
            if (FIELD_COMMENT.equals(trimmed)
                    || FIELD_DATE.equals(trimmed)
                    || FIELD_PHOTOS.equals(trimmed)
                    || FIELD_CUSTOM_1.equals(trimmed)
                    || FIELD_CUSTOM_2.equals(trimmed)) {
                sanitized.add(trimmed);
            }
        }
        return sanitized;
    }

    @Nullable
    private String sanitizeDateFormat(@Nullable String value) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            return null;
        }
        if (DATE_FORMAT_EUROPEAN.equals(trimmed)
                || DATE_FORMAT_ENGLISH.equals(trimmed)
                || DATE_FORMAT_ISO.equals(trimmed)) {
            return trimmed;
        }
        return null;
    }

    @Nullable
    private String trimToNull(@Nullable String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private <T> T executeBlocking(@NonNull Callable<T> task) {
        Future<T> future = executor.submit(task);
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("La récupération des métadonnées a été interrompue", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Échec d'une opération sur les métadonnées", exception.getCause());
        }
    }
}
