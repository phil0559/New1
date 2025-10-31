package com.example.new1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoomContentItem {

    public static final int FURNITURE_BOTTOM_LEVEL = 0;

    private static final String KEY_NAME = "name";
    private static final String KEY_COMMENT = "comment";
    private static final String KEY_TYPE = "type";
    private static final String KEY_CATEGORY = "category";
    private static final String KEY_CONTAINER = "container";
    private static final String KEY_BARCODE = "barcode";
    private static final String KEY_SERIES = "series";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_AUTHOR = "author";
    private static final String KEY_PUBLISHER = "publisher";
    private static final String KEY_EDITION = "edition";
    private static final String KEY_PUBLICATION_DATE = "publicationDate";
    private static final String KEY_SUMMARY = "summary";
    private static final String KEY_TRACKS = "tracks";
    private static final String KEY_PHOTOS = "photos";
    private static final String KEY_RANK = "rank";
    private static final String KEY_PARENT_RANK = "parentRank";
    private static final String KEY_FURNITURE = "furniture";
    private static final String KEY_FURNITURE_TYPE = "furnitureType";
    private static final String KEY_FURNITURE_CUSTOM_TYPE = "furnitureCustomType";
    private static final String KEY_FURNITURE_LEVELS = "furnitureLevels";
    private static final String KEY_FURNITURE_COLUMNS = "furnitureColumns";
    private static final String KEY_FURNITURE_HAS_TOP = "furnitureHasTop";
    private static final String KEY_FURNITURE_HAS_BOTTOM = "furnitureHasBottom";
    private static final String KEY_FURNITURE_STORAGE_TOWER = "furnitureStorageTower";
    private static final String KEY_ATTACHED_LEVEL = "attachedLevel";
    private static final String KEY_ATTACHED_COLUMN = "attachedColumn";

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
    private final List<String> tracks;
    @NonNull
    private final List<String> photos;
    @NonNull
    private final List<RoomContentItem> children;
    private final boolean container;
    private final boolean furniture;
    private final boolean storageTower;
    @Nullable
    private final String furnitureType;
    @Nullable
    private final String furnitureCustomType;
    @Nullable
    private final Integer furnitureLevels;
    @Nullable
    private final Integer furnitureColumns;
    private final boolean furnitureHasTop;
    private final boolean furnitureHasBottom;
    private boolean displayed;
    private long rank;
    @Nullable
    private Long parentRank;
    private int attachedItemCount;
    @Nullable
    private String displayRank;
    @Nullable
    private Integer containerLevel;
    @Nullable
    private Integer containerColumn;

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
                           @Nullable List<String> tracks,
                           @Nullable List<String> photos,
                           boolean isContainer) {
        this(name, comment, type, category, barcode, series, number, author, publisher,
                edition, publicationDate, summary, tracks, photos, isContainer, 0,
                false, false, null, null, null, null, false, false, null, null);
    }

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
                           @Nullable List<String> tracks,
                           @Nullable List<String> photos,
                           boolean isContainer,
                           int ignoredAttachedItemCount) {
        this(name, comment, type, category, barcode, series, number, author, publisher,
                edition, publicationDate, summary, tracks, photos, isContainer,
                ignoredAttachedItemCount, false, false, null, null, null, null, false,
                false, null, null);
    }

    private RoomContentItem(@NonNull String name,
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
                           @Nullable List<String> tracks,
                           @Nullable List<String> photos,
                           boolean isContainer,
                           int ignoredAttachedItemCount,
                           boolean isFurniture,
                           boolean isStorageTower,
                           @Nullable String furnitureType,
                           @Nullable String furnitureCustomType,
                           @Nullable Integer furnitureLevels,
                           @Nullable Integer furnitureColumns,
                           boolean hasTop,
                           boolean hasBottom,
                           @Nullable Integer attachedLevel,
                           @Nullable Integer attachedColumn) {
        // Le paramètre ignoredAttachedItemCount est conservé pour la compatibilité
        // binaire mais n'est plus utilisé : chaque élément est stocké sans lien.
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
        if (tracks == null) {
            this.tracks = new ArrayList<>();
        } else {
            this.tracks = new ArrayList<>();
            for (String track : tracks) {
                if (!isNullOrEmpty(track)) {
                    this.tracks.add(track);
                }
            }
        }
        if (photos == null) {
            this.photos = new ArrayList<>();
        } else {
            this.photos = new ArrayList<>(photos);
        }
        this.children = new ArrayList<>();
        this.container = isContainer;
        this.furniture = isFurniture;
        this.storageTower = isFurniture && isStorageTower;
        this.furnitureType = isNullOrEmpty(furnitureType) ? null : furnitureType;
        this.furnitureCustomType = isNullOrEmpty(furnitureCustomType)
                ? null
                : furnitureCustomType;
        this.furnitureLevels = furnitureLevels != null && furnitureLevels > 0
                ? furnitureLevels
                : null;
        this.furnitureColumns = furnitureColumns != null && furnitureColumns > 0
                ? furnitureColumns
                : null;
        this.furnitureHasTop = hasTop;
        this.furnitureHasBottom = hasBottom;
        this.displayed = true;
        this.rank = -1L;
        this.parentRank = null;
        this.attachedItemCount = Math.max(0, ignoredAttachedItemCount);
        this.displayRank = null;
        setContainerLevel(attachedLevel);
        setContainerColumn(attachedColumn);
    }

    public static RoomContentItem createFurniture(@NonNull String name,
                                                   @Nullable String comment,
                                                   @Nullable String type,
                                                   @Nullable String customType,
                                                   @Nullable List<String> photos,
                                                   @Nullable Integer levels,
                                                   @Nullable Integer columns,
                                                   boolean hasTop,
                                                   boolean hasBottom) {
        return new RoomContentItem(name,
                comment,
                type,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                photos,
                true,
                0,
                true,
                false,
                type,
                customType,
                levels,
                columns,
                hasTop,
                hasBottom,
                null,
                null);
    }

    public static RoomContentItem createStorageTower(@NonNull String name,
                                                     @Nullable String comment,
                                                     @Nullable String type,
                                                     @Nullable String customType,
                                                     @Nullable List<String> photos,
                                                     @Nullable Integer drawers,
                                                     @Nullable Integer columns,
                                                     boolean hasTop) {
        return new RoomContentItem(name,
                comment,
                type,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                photos,
                true,
                0,
                true,
                true,
                type,
                customType,
                drawers,
                columns,
                hasTop,
                false,
                null,
                null);
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
    public List<String> getTracks() {
        return Collections.unmodifiableList(tracks);
    }

    @NonNull
    public List<String> getPhotos() {
        return Collections.unmodifiableList(photos);
    }

    public boolean isContainer() {
        return container;
    }

    public boolean isFurniture() {
        return furniture;
    }

    public boolean isStorageTower() {
        return storageTower;
    }

    @Nullable
    public String getFurnitureType() {
        return furnitureType;
    }

    @Nullable
    public String getFurnitureCustomType() {
        return furnitureCustomType;
    }

    @Nullable
    public Integer getFurnitureLevels() {
        return furnitureLevels;
    }

    @Nullable
    public Integer getFurnitureColumns() {
        return furnitureColumns;
    }

    public boolean hasFurnitureTop() {
        return furnitureHasTop;
    }

    public boolean hasFurnitureBottom() {
        return furnitureHasBottom;
    }

    public int getAttachedItemCount() {
        return Math.max(0, attachedItemCount);
    }

    public boolean hasAttachedItems() {
        return getAttachedItemCount() > 0;
    }

    public void setAttachedItemCount(int count) {
        this.attachedItemCount = Math.max(0, count);
    }

    public void incrementAttachedItemCount() {
        if (attachedItemCount == Integer.MAX_VALUE) {
            return;
        }
        attachedItemCount++;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public void setDisplayed(boolean displayed) {
        this.displayed = displayed;
    }

    @Nullable
    public String getDisplayRank() {
        return displayRank;
    }

    public void setDisplayRank(@Nullable String displayRank) {
        this.displayRank = displayRank;
    }

    @Nullable
    public Integer getContainerLevel() {
        return containerLevel;
    }

    public void setContainerLevel(@Nullable Integer level) {
        if (level == null) {
            containerLevel = null;
        } else if (level < FURNITURE_BOTTOM_LEVEL) {
            containerLevel = null;
        } else {
            containerLevel = level;
        }
    }

    @Nullable
    public Integer getContainerColumn() {
        return containerColumn;
    }

    public void setContainerColumn(@Nullable Integer column) {
        if (column == null || column <= 0) {
            containerColumn = null;
        } else {
            containerColumn = column;
        }
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();
        try {
            object.put(KEY_NAME, name);
            object.put(KEY_COMMENT, comment);
            object.put(KEY_CONTAINER, container);
            object.put(KEY_RANK, rank);
            if (parentRank != null) {
                object.put(KEY_PARENT_RANK, parentRank.longValue());
            }
            if (furniture) {
                object.put(KEY_FURNITURE, true);
                if (furnitureType != null) {
                    object.put(KEY_FURNITURE_TYPE, furnitureType);
                }
                if (furnitureCustomType != null) {
                    object.put(KEY_FURNITURE_CUSTOM_TYPE, furnitureCustomType);
                }
                if (furnitureLevels != null) {
                    object.put(KEY_FURNITURE_LEVELS, furnitureLevels.intValue());
                }
                if (furnitureColumns != null) {
                    object.put(KEY_FURNITURE_COLUMNS, furnitureColumns.intValue());
                }
                if (furnitureHasTop) {
                    object.put(KEY_FURNITURE_HAS_TOP, true);
                }
                if (furnitureHasBottom) {
                    object.put(KEY_FURNITURE_HAS_BOTTOM, true);
                }
                if (storageTower) {
                    object.put(KEY_FURNITURE_STORAGE_TOWER, true);
                }
            }
            if (containerLevel != null) {
                object.put(KEY_ATTACHED_LEVEL, containerLevel.intValue());
            }
            if (containerColumn != null) {
                object.put(KEY_ATTACHED_COLUMN, containerColumn.intValue());
            }
            // Ne pas réécrire le nombre d'attachements : les éléments sont
            // sauvegardés individuellement désormais.
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
            if (!tracks.isEmpty()) {
                JSONArray tracksArray = new JSONArray();
                for (String track : tracks) {
                    tracksArray.put(track);
                }
                object.put(KEY_TRACKS, tracksArray);
            }
            if (!photos.isEmpty()) {
                JSONArray photosArray = new JSONArray();
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
        List<String> tracks = new ArrayList<>();
        if (object.has(KEY_TRACKS) && !object.isNull(KEY_TRACKS)) {
            JSONArray tracksArray = object.optJSONArray(KEY_TRACKS);
            if (tracksArray != null) {
                for (int i = 0; i < tracksArray.length(); i++) {
                    String value = tracksArray.optString(i, null);
                    if (value != null) {
                        String trimmed = value.trim();
                        if (!trimmed.isEmpty()) {
                            tracks.add(trimmed);
                        }
                    }
                }
            }
        }
        List<String> photos = new ArrayList<>();
        if (object.has(KEY_PHOTOS) && !object.isNull(KEY_PHOTOS)) {
            JSONArray photosArray = object.optJSONArray(KEY_PHOTOS);
            if (photosArray != null) {
                for (int i = 0; i < photosArray.length(); i++) {
                    String value = photosArray.optString(i, null);
                    if (value != null && !value.isEmpty()) {
                        photos.add(value);
                    }
                }
            }
        }
        boolean isContainer = parseBooleanCompat(object, KEY_CONTAINER, false);
        boolean isFurniture = parseBooleanCompat(object, KEY_FURNITURE, false);
        String parsedFurnitureType = null;
        String parsedFurnitureCustomType = null;
        Integer parsedFurnitureLevels = null;
        Integer parsedFurnitureColumns = null;
        boolean parsedHasTop = false;
        boolean parsedHasBottom = false;
        boolean parsedStorageTower = false;
        Integer parsedAttachedLevel = null;
        Integer parsedAttachedColumn = null;
        if (isFurniture) {
            if (object.has(KEY_FURNITURE_TYPE) && !object.isNull(KEY_FURNITURE_TYPE)) {
                parsedFurnitureType = object.optString(KEY_FURNITURE_TYPE, null);
            }
            if (object.has(KEY_FURNITURE_CUSTOM_TYPE)
                    && !object.isNull(KEY_FURNITURE_CUSTOM_TYPE)) {
                parsedFurnitureCustomType = object.optString(KEY_FURNITURE_CUSTOM_TYPE, null);
            }
            if (object.has(KEY_FURNITURE_LEVELS) && !object.isNull(KEY_FURNITURE_LEVELS)) {
                int value = object.optInt(KEY_FURNITURE_LEVELS, 0);
                if (value > 0) {
                    parsedFurnitureLevels = value;
                }
            }
            if (object.has(KEY_FURNITURE_COLUMNS) && !object.isNull(KEY_FURNITURE_COLUMNS)) {
                int value = object.optInt(KEY_FURNITURE_COLUMNS, 0);
                if (value > 0) {
                    parsedFurnitureColumns = value;
                }
            }
            parsedHasTop = parseBooleanCompat(object, KEY_FURNITURE_HAS_TOP, false);
            parsedHasBottom = parseBooleanCompat(object, KEY_FURNITURE_HAS_BOTTOM, false);
            parsedStorageTower = parseBooleanCompat(object, KEY_FURNITURE_STORAGE_TOWER, false);
        }
        if (object.has(KEY_ATTACHED_LEVEL) && !object.isNull(KEY_ATTACHED_LEVEL)) {
            int value = object.optInt(KEY_ATTACHED_LEVEL, 0);
            if (value >= FURNITURE_BOTTOM_LEVEL) {
                parsedAttachedLevel = value;
            }
        }
        if (object.has(KEY_ATTACHED_COLUMN) && !object.isNull(KEY_ATTACHED_COLUMN)) {
            int value = object.optInt(KEY_ATTACHED_COLUMN, 0);
            if (value > 0) {
                parsedAttachedColumn = value;
            }
        }
        long rank = object.has(KEY_RANK) ? object.optLong(KEY_RANK, -1L) : -1L;
        Long parentRank = null;
        if (object.has(KEY_PARENT_RANK) && !object.isNull(KEY_PARENT_RANK)) {
            parentRank = object.optLong(KEY_PARENT_RANK);
        }
        RoomContentItem item = new RoomContentItem(name,
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
                tracks,
                photos,
                isContainer,
                0,
                isFurniture,
                parsedStorageTower,
                parsedFurnitureType,
                parsedFurnitureCustomType,
                parsedFurnitureLevels,
                parsedFurnitureColumns,
                parsedHasTop,
                parsedHasBottom,
                parsedAttachedLevel,
                parsedAttachedColumn);
        item.setRank(rank);
        item.setParentRank(parentRank);
        item.setDisplayRank(null);
        return item;
    }

    @NonNull
    public List<RoomContentItem> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void setChildren(@Nullable List<RoomContentItem> children) {
        this.children.clear();
        if (children == null) {
            return;
        }
        for (RoomContentItem child : children) {
            if (child != null) {
                this.children.add(child);
            }
        }
    }

    public void clearChildren() {
        children.clear();
    }

    public void addChild(@NonNull RoomContentItem child) {
        children.add(child);
    }

    private static boolean isNullOrEmpty(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean parseBooleanCompat(@NonNull JSONObject object,
            @NonNull String key, boolean defaultValue) {
        if (!object.has(key) || object.isNull(key)) {
            return defaultValue;
        }
        Object rawValue = object.opt(key);
        if (rawValue instanceof Boolean) {
            return (Boolean) rawValue;
        }
        if (rawValue instanceof Number) {
            return ((Number) rawValue).intValue() != 0;
        }
        if (rawValue instanceof String) {
            String normalized = ((String) rawValue).trim();
            if (normalized.isEmpty()) {
                return defaultValue;
            }
            if ("true".equalsIgnoreCase(normalized)
                    || "yes".equalsIgnoreCase(normalized)
                    || "1".equals(normalized)) {
                return true;
            }
            if ("false".equalsIgnoreCase(normalized)
                    || "no".equalsIgnoreCase(normalized)
                    || "0".equals(normalized)) {
                return false;
            }
            try {
                return Double.parseDouble(normalized) != 0d;
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    @Nullable
    public Long getParentRank() {
        return parentRank;
    }

    public void setParentRank(@Nullable Long parentRank) {
        if (parentRank != null && parentRank < 0) {
            this.parentRank = null;
        } else {
            this.parentRank = parentRank;
        }
    }
}
