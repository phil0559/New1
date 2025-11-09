package com.example.new1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Room {
    private final String id;
    private final String establishmentId;
    private final String name;
    private final String comment;
    private final List<String> photos;

    public Room(String establishmentId, String name, String comment, List<String> photos) {
        this(generateStableId(), establishmentId, name, comment, photos);
    }

    public Room(String id, String establishmentId, String name, String comment, List<String> photos) {
        this.id = (id == null || id.trim().isEmpty()) ? generateStableId() : id;
        this.establishmentId = establishmentId;
        this.name = name;
        this.comment = comment != null ? comment : "";
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

    public String getEstablishmentId() {
        return establishmentId;
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
