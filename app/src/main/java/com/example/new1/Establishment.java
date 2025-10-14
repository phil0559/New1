package com.example.new1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Establishment {
    private final String name;
    private final String comment;
    private final List<String> photos;

    public Establishment(String name, String comment) {
        this(name, comment, null);
    }

    public Establishment(String name, String comment, List<String> photos) {
        this.name = name;
        this.comment = comment;
        if (photos == null) {
            this.photos = new ArrayList<>();
        } else {
            this.photos = new ArrayList<>(photos);
        }
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
