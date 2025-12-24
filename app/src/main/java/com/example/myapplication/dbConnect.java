package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class dbConnect extends SQLiteOpenHelper {

    private static String dbName = "findFriendsManager";
    private static String dbTable = "users";
    private static final int dbVersion = 1;

    private static String ID = "id";
    private static String username = "username";
    private static String password = "password";

    public dbConnect(@Nullable Context context) {
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + dbTable + " (" +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                username + " TEXT UNIQUE, " +
                password + " TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + dbTable);
        onCreate(db);
    }

    public void addUser(Users user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(username, user.getUsername());
        values.put(password, user.getPassword());
        db.insert(dbTable, null, values);
    }

    // 🔥 Login function
    public boolean checkLogin(String user, String pass) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + dbTable + " WHERE username=? AND password=?",
                new String[]{user, pass}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }


    public boolean userExists(String user) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + dbTable + " WHERE username=?",
                new String[]{user}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
