package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlaceDao {

    @Query("SELECT * FROM places ORDER BY title ASC")
    List<PlaceEntity> getAll();

    @Query("SELECT * FROM places WHERE title = :title LIMIT 1")
    PlaceEntity getByTitle(String title);

    // ✅ NEW: favorites list
    @Query("SELECT * FROM places WHERE isFavorite = 1 ORDER BY title ASC")
    List<PlaceEntity> getFavorites();

    // ✅ NEW: toggle favorite
    @Query("UPDATE places SET isFavorite = :fav WHERE title = :title")
    void setFavorite(String title, boolean fav);

    @Insert
    void insertAll(List<PlaceEntity> places);

    @Query("SELECT COUNT(*) FROM places")
    int count();
}
