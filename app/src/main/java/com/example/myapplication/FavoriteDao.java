package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void add(FavoriteEntity fav);

    @Query("DELETE FROM favorites WHERE username = :username AND placeId = :placeId")
    void remove(String username, int placeId);

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE username = :username AND placeId = :placeId)")
    int isFavorite(String username, int placeId);

    // ✅ Java 8 / Android compatible
    @Query(
            "SELECT p.* FROM places p " +
                    "INNER JOIN favorites f ON f.placeId = p.id " +
                    "WHERE f.username = :username"
    )
    List<PlaceEntity> getFavoritesForUser(String username);
}
