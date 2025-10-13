package com.example.new1;

public class Establishment {
    private final String name;
    private final String comment;

    public Establishment(String name, String comment) {
        this.name = name;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }
}
