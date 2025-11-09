package com.example.new1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Establishment {
    private final String id;
    private final String name;
    private final String comment;
    private final List<String> photos;

    public Establishment(String name, String comment) {
        this(generateStableId(), name, comment, null);
    }

    public Establishment(String name, String comment, List<String> photos) {
        this(generateStableId(), name, comment, photos);
    }

    public Establishment(String id, String name, String comment, List<String> photos) {
        this.id = (id == null || id.trim().isEmpty()) ? generateStableId() : id;
        this.name = name;
        this.comment = comment;
        if (photos == null) {
            this.photos = new ArrayList<>();
        } else {
            this.photos = new ArrayList<>(photos);
        }
    }

    public static String generateStableId() {
        return UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public List<String> getPhotos() {
        return Collections.unmodifiableList(photos);
    }
}
