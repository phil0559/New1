package com.example.new1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Room {
    private final String name;
    private final List<String> photos;

    public Room(String name, List<String> photos) {
        this.name = name;
        if (photos == null) {
            this.photos = new ArrayList<>();
        } else {
            this.photos = new ArrayList<>(photos);
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getPhotos() {
        return Collections.unmodifiableList(photos);
    }
}
