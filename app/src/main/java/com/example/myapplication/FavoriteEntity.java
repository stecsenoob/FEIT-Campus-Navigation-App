package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "favorites",
        primaryKeys = {"username", "placeId"},
        indices = {@Index("username"), @Index("placeId")}
)
public class FavoriteEntity {

    @NonNull
    public String username;

    public int placeId;

    public FavoriteEntity(@NonNull String username, int placeId) {
        this.username = username;
        this.placeId = placeId;
    }
}
