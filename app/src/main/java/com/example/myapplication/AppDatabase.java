package com.example.myapplication;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.ArrayList;

@Database(
        entities = {PlaceEntity.class, FavoriteEntity.class}, // ✅ ADDED FavoriteEntity
        version = 3,                                          // ✅ BUMP VERSION
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // ✅ DAOs
    public abstract PlaceDao placeDao();
    public abstract FavoriteDao favoriteDao(); // ✅ ADDED

    // ❌ OLD MIGRATION IS NO LONGER NEEDED
    // Favorites are now in a separate table
    // private static final Migration MIGRATION_1_2 = ...

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "places_db"
                            )
                            // ✅ SIMPLIFY: recreate DB if schema changed
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // ✅ KEEP YOUR SEED LOGIC EXACTLY AS BEFORE
    public static void seedIfEmpty(AppDatabase db) {
        PlaceDao dao = db.placeDao();
        if (dao.count() > 0) return;

        ArrayList<PlaceEntity> seed = new ArrayList<>();

        seed.add(new PlaceEntity("Library FEIT", "Academic", "Главна библиотека на ФЕИТ",
                "Ruger Boskovic b.b.", "02/30 99 188", "ljilja@feit.ukim.edu.mk",
                42.0042, 21.4096, false));

        seed.add(new PlaceEntity("Amphitheater B", "Amphitheater", "Амфитеатар за предавања",
                "FEIT campus", "", "",
                42.0048509, 21.4083305, false));

        seed.add(new PlaceEntity("Robotics Lab", "Lab", "Лабораторија за роботика",
                "FEIT campus", "", "",
                42.0045, 21.4101, false));

        seed.add(new PlaceEntity("Bife", "Kantina", "Food & Drinks",
                "FEIT campus", "078270140", "",
                42.0050676, 21.4085491, false));

        seed.add(new PlaceEntity("Student Services", "Sluzba", "Студентски служби",
                "FEIT campus", "", "",
                42.0043, 21.4099, false));

        seed.add(new PlaceEntity("Amphitheater", "Amphitheater", "Амфитеатар за предавања",
                "FEIT campus", "", "",
                42.0041509, 21.4083305, false));

        seed.add(new PlaceEntity("FABLAB", "Lab", "Амфитеатар за предавања",
                "FEIT campus", "", "",
                42.0031509, 21.4083305, false));

        seed.add(new PlaceEntity("INNOFEIT", "Lab", "Амфитеатар за предавања",
                "FEIT campus", "", "",
                42.0031109, 21.4083305, false));

        dao.insertAll(seed);
    }
}
