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
    private static final String KEY_SERIES = "series";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_PUBLISHER = "publisher";
    private static final String KEY_EDITION = "edition";
    private static final String KEY_PUBLICATION_DATE = "publicationDate";
    private static final String KEY_SUMMARY = "summary";
    private static final String KEY_PHOTOS = "photos";

    private final String name;
    private final String comment;
    @Nullable
    private final String type;
    @Nullable
    private final String category;
    @Nullable
    private final String barcode;
    @Nullable
    private final String series;
    @Nullable
    private final String number;
    @Nullable
    private final String author;
    @Nullable
    private final String publisher;
    @Nullable
    private final String edition;
    @Nullable
    private final String publicationDate;
    @Nullable
    private final String summary;
    @NonNull
    private final List<String> photos;

    public RoomContentItem(@NonNull String name,
                           @Nullable String comment,
                           @Nullable String type,
                           @Nullable String category,
                           @Nullable String barcode,
                           @Nullable String series,
                           @Nullable String number,
                           @Nullable String author,
                           @Nullable String publisher,
                           @Nullable String edition,
                           @Nullable String publicationDate,
                           @Nullable String summary,
                           @Nullable List<String> photos) {
        this.name = name;
        this.comment = comment != null ? comment : "";
        this.type = isNullOrEmpty(type) ? null : type;
        this.category = isNullOrEmpty(category) ? null : category;
        this.barcode = isNullOrEmpty(barcode) ? null : barcode;
        this.series = isNullOrEmpty(series) ? null : series;
        this.number = isNullOrEmpty(number) ? null : number;
        this.author = isNullOrEmpty(author) ? null : author;
        this.publisher = isNullOrEmpty(publisher) ? null : publisher;
        this.edition = isNullOrEmpty(edition) ? null : edition;
        this.publicationDate = isNullOrEmpty(publicationDate) ? null : publicationDate;
        this.summary = isNullOrEmpty(summary) ? null : summary;
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

    @Nullable
    public String getSeries() {
        return series;
    }

    @Nullable
    public String getNumber() {
        return number;
    }

    @Nullable
    public String getAuthor() {
        return author;
    }

    @Nullable
    public String getPublisher() {
        return publisher;
    }

    @Nullable
    public String getEdition() {
        return edition;
    }

    @Nullable
    public String getPublicationDate() {
        return publicationDate;
    }

    @Nullable
    public String getSummary() {
        return summary;
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
            if (series != null) {
                object.put(KEY_SERIES, series);
            }
            if (number != null) {
                object.put(KEY_NUMBER, number);
            }
            if (author != null) {
                object.put(KEY_AUTHOR, author);
            }
            if (publisher != null) {
                object.put(KEY_PUBLISHER, publisher);
            }
            if (edition != null) {
                object.put(KEY_EDITION, edition);
            }
            if (publicationDate != null) {
                object.put(KEY_PUBLICATION_DATE, publicationDate);
            }
            if (summary != null) {
                object.put(KEY_SUMMARY, summary);
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
        String series = object.has(KEY_SERIES) && !object.isNull(KEY_SERIES)
                ? object.optString(KEY_SERIES)
                : null;
        String number = object.has(KEY_NUMBER) && !object.isNull(KEY_NUMBER)
                ? object.optString(KEY_NUMBER)
                : null;
        String author = object.has(KEY_AUTHOR) && !object.isNull(KEY_AUTHOR)
                ? object.optString(KEY_AUTHOR)
                : null;
        String publisher = object.has(KEY_PUBLISHER) && !object.isNull(KEY_PUBLISHER)
                ? object.optString(KEY_PUBLISHER)
                : null;
        String edition = object.has(KEY_EDITION) && !object.isNull(KEY_EDITION)
                ? object.optString(KEY_EDITION)
                : null;
        String publicationDate = object.has(KEY_PUBLICATION_DATE)
                && !object.isNull(KEY_PUBLICATION_DATE)
                ? object.optString(KEY_PUBLICATION_DATE)
                : null;
        String summary = object.has(KEY_SUMMARY) && !object.isNull(KEY_SUMMARY)
                ? object.optString(KEY_SUMMARY)
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
        return new RoomContentItem(name,
                comment,
                type,
                category,
                barcode,
                series,
                number,
                author,
                publisher,
                edition,
                publicationDate,
                summary,
                photos);
    }

    private static boolean isNullOrEmpty(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }
}
