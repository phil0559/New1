package com.example.new1.storage.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {RoomContentEntity.class}, version = 1, exportSchema = false)
public abstract class RoomContentDatabase extends RoomDatabase {
    static final String DATABASE_NAME = "room_content.db";

    public abstract RoomContentDao roomContentDao();
}
