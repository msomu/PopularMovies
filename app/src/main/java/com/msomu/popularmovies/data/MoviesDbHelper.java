/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.msomu.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.msomu.popularmovies.data.MoviesContract.ImageEntry;
import com.msomu.popularmovies.data.MoviesContract.MoviesEntry;

/**
 * Manages a local database for weather data.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "movies.db";
    private static final String TAG = "MoviesDbHelper";
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME + " (" +
                MoviesEntry._ID + " INTEGER PRIMARY KEY," +
                MoviesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL," +
                MoviesEntry.COLUMN_MOVIE_NAME + " INTEGER NOT NULL," +
                MoviesEntry.COLUMN_MOVIE_IMAGE_URL + " TEXT NULL," +
                MoviesEntry.COLUMN_MOVIE_IMAGE + " TEXT NULL," +
                MoviesEntry.COLUMN_MOVIE_BG_IMAGE + " TEXT NULL," +
                MoviesEntry.COLUMN_MOVIE_BG_IMAGE_URL + " TEXT NULL," +
                MoviesEntry.COLUMN_MOVIE_RELEASE_DATE + " TEXT NOT NULL," +
                MoviesEntry.COLUMN_MOVIE_VOTE + " TEXT NULL," +
                MoviesEntry.COLUMN_MOVIE_DESCRIPTION + " TEXT NULL" +
                " );";

        final String SQL_IMAGE_TABLE = "CREATE TABLE " + ImageEntry.TABLE_NAME + " (" +
                ImageEntry._ID + " INTEGER PRIMARY KEY," +
                ImageEntry.COLUMN_URL + " TEXT NOT NULL," +
                ImageEntry.COLUMN_IMAGE + " BLOB  NULL" +
                " );";

        Log.d(TAG, SQL_CREATE_MOVIE_TABLE);
        Log.d(TAG, SQL_IMAGE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);
        sqLiteDatabase.execSQL(SQL_IMAGE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ImageEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
