package com.firebase.petti.petti.utils;

import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the user database.
 */

public class UserContract {

    // To make it easy to query for the exact date, we normalize all dates that go into
    // the database to the start of the the Julian day at UTC.
    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    /* Inner class that defines the table contents of the Food table */
    public static final class FoodEntry implements BaseColumns {

        public static final String TABLE_NAME = "food";

        // Column with the foreign key into the food table.
        public static final String COLUMN_FOOD_KEY = "food_id";
        // Date, stored as long in milliseconds since the epoch
        public static final String COLUMN_DATE = "date";

        public static final String COLUMN_AMOUNT = "amount";

        public static final String COLUMN_NAME = "name";
    }
}
