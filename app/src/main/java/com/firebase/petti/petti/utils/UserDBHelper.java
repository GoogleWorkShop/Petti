package com.firebase.petti.petti.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.firebase.petti.petti.utils.UserContract.FoodEntry;

/**
 * Created by barjon on 30-Dec-16.
 */

public class UserDBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "dog.db";

    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude

        final String SQL_CREATE_FOOD_TABLE = "CREATE TABLE " + FoodEntry.TABLE_NAME + " (" +
                FoodEntry._ID + " INTEGER PRIMARY KEY," +
                FoodEntry.COLUMN_FOOD_KEY + " TEXT UNIQUE NOT NULL, " +
                FoodEntry.COLUMN_DATE + " TEXT NOT NULL, " +
                FoodEntry.COLUMN_AMOUNT + " REAL NOT NULL, " +
                FoodEntry.COLUMN_NAME + " REAL NOT NULL " +

                // To assure the application have just one weather entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + FoodEntry.COLUMN_FOOD_KEY + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_FOOD_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        db.execSQL("DROP TABLE IF EXISTS " + FoodEntry.TABLE_NAME);
        onCreate(db);

    }
}
