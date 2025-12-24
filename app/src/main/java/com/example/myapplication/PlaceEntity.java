package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "places")
public class PlaceEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String category;
    public String description;
    public String location;
    public String phone;
    public String email;
    public double lat;
    public double lng;

    // ✅ NEW
    public boolean isFavorite;

    public PlaceEntity(String title, String category, String description,
                       String location, String phone, String email,
                       double lat, double lng,
                       boolean isFavorite) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.location = location;
        this.phone = phone;
        this.email = email;
        this.lat = lat;
        this.lng = lng;
        this.isFavorite = isFavorite;
    }
}
