package com.example.myapplication;

import java.io.Serializable;

public class Place implements Serializable {

    public final String title;
    public final String category;
    public final String description;
    public final String location;
    public final String phone;
    public final String email;
    public final double lat;
    public final double lng;

    public boolean isFavorite; // ⭐ KEY

    public Place(String title, String category, String description,
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
