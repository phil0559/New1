package com.example.new1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomContentItem {

    private static final String KEY_NAME = "name";
    private static final String KEY_COMMENT = "comment";
    private static final String KEY_TYPE = "type";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_BARCODE = "barcode";
    private static final String KEY_PHOTOS = "photos";

    private final String name;
    private final String comment;
    @Nullable
    private final String type;
    @Nullable
    private final String category;
    @Nullable
    private final String barcode;
    @NonNull
    private final List<String> photos;

    public RoomContentItem(@NonNull String name,
                           @Nullable String comment,
                           @Nullable String type,
                           @Nullable String category,
                           @Nullable String barcode,
                           @Nullable List<String> photos) {
        this.name = name;
        this.comment = comment != null ? comment : "";
        this.type = isNullOrEmpty(type) ? null : type;
        this.category = isNullOrEmpty(category) ? null : category;
        this.barcode = isNullOrEmpty(barcode) ? null : barcode;
        if (photos == null) {
            this.photos = new ArrayList<>();
        } else {
            this.photos = new ArrayList<>(photos);
        }
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getComment() {
        return comment;
    }

    @Nullable
    public String getType() {
        return type;
    }

    @Nullable
    public String getCategory() {
        return category;
    }

    @Nullable
    public String getBarcode() {
        return barcode;
    }

    @NonNull
    public List<String> getPhotos() {
        return Collections.unmodifiableList(photos);
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put(KEY_NAME, name);
            object.put(KEY_COMMENT, comment);
            if (type != null) {
                object.put(KEY_TYPE, type);
            }
            if (category != null) {
                object.put(KEY_CATEGORY, category);
            }
            if (barcode != null) {
                object.put(KEY_BARCODE, barcode);
            }
            if (!photos.isEmpty()) {
                org.json.JSONArray photosArray = new org.json.JSONArray();
                for (String photo : photos) {
                    photosArray.put(photo);
                }
                object.put(KEY_PHOTOS, photosArray);
            }
        } catch (JSONException ignored) {
        }
        return object;
    }

    public static RoomContentItem fromJson(@NonNull JSONObject object) throws JSONException {
        String name = object.optString(KEY_NAME, "");
        String comment = object.optString(KEY_COMMENT, "");
        String type = object.has(KEY_TYPE) && !object.isNull(KEY_TYPE)
                ? object.optString(KEY_TYPE)
                : null;
        String category = object.has(KEY_CATEGORY) && !object.isNull(KEY_CATEGORY)
                ? object.optString(KEY_CATEGORY)
                : null;
        String barcode = object.has(KEY_BARCODE) && !object.isNull(KEY_BARCODE)
                ? object.optString(KEY_BARCODE)
                : null;
        List<String> photos = new ArrayList<>();
        if (object.has(KEY_PHOTOS) && !object.isNull(KEY_PHOTOS)) {
            org.json.JSONArray photosArray = object.optJSONArray(KEY_PHOTOS);
            if (photosArray != null) {
                for (int i = 0; i < photosArray.length(); i++) {
                    String value = photosArray.optString(i, null);
                    if (value != null && !value.isEmpty()) {
                        photos.add(value);
                    }
                }
            }
        }
        return new RoomContentItem(name, comment, type, category, barcode, photos);
    }

    private static boolean isNullOrEmpty(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }
}
