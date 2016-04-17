package com.msomu.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.msomu.popularmovies.BuildConfig;
import com.msomu.popularmovies.R;
import com.msomu.popularmovies.Utility;
import com.msomu.popularmovies.data.MoviesContract;
import com.msomu.popularmovies.model.MovieModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter {
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    // these indices must match the projection
    public static final int INDEX_MOVIE_NAME = 0;
    public static final int INDEX_MOVIE_IMAGE_URL = 1;
    public static final int INDEX_MOVIE_IMAGE = 2;
    public static final int INDEX_MOVIE_BG_IMAGE = 3;
    public static final int INDEX_MOVIE_BG_IMAGE_URL = 4;
    public static final int INDEX_MOVIE_RELEASE_DATE = 5;
    public static final int INDEX_MOVIE_VOTE = 6;
    public static final int INDEX_MOVIE_DESCRIPTION = 7;
    private static final String TAG = "MoviesSyncAdapter";
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    private static final String[] NOTIFY_MOVIE_PROJECTION = new String[]{
            MoviesContract.MoviesEntry.COLUMN_MOVIE_NAME,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_IMAGE_URL,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_IMAGE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_BG_IMAGE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_BG_IMAGE_URL,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_RELEASE_DATE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_VOTE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_DESCRIPTION
    };
    public final String LOG_TAG = MoviesSyncAdapter.class.getSimpleName();

    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * +     * Helper method to schedule the sync adapter periodic execution
     * +
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        MoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting Sync");
// These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieListString = null;
        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            //String baseUrl = "http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7";
            //String apiKey = "&APPID=" + BuildConfig.OPEN_WEATHER_MAP_API_KEY;
            final String FORECAST_BASE_URL =
                    "https://api.themoviedb.org/3/discover/movie?";
            final String APPID_PARAM = "api_key";
            final String SORT_PARAM = "sort_by";
            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, Utility.getSortPreference(getContext()))
                    .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                    .build();
            URL url = new URL(builtUri.toString());
            Log.v("Forecast Fragment", "Built URI " + builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            movieListString = buffer.toString();
            Log.d("PlaceholderFragment", movieListString);
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            //TODO Internet check
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        try {
            getMovies(movieListString);
        } catch (JSONException e) {
            Log.e("ForecastFragment", e.getMessage(), e);
            e.printStackTrace();
        }
        return;
        // This will only happen if there was an error getting or parsing the forecast.
    }

    private void getMovies(String movieListString) throws JSONException {
        Log.d(TAG, movieListString);
        final String TMDB_RESULTS = "results";
        final String TMDB_IMAGE = "poster_path";
        final String TMDB_TITLE = "title";
        final String TMDB_ID = "id";
        final String TMDB_BG_IMAGE = "backdrop_path";
        final String TMDB_RELEASE_DATE = "release_date";
        final String TMDB_VOTE_AVERAGE = "vote_average";
        final String TMDB_PLOT_SYNOPSIS = "overview";

        JSONObject movieListJson = new JSONObject(movieListString);
        JSONArray resultsArray = movieListJson.getJSONArray(TMDB_RESULTS);
        Vector<ContentValues> cVVector = new Vector<ContentValues>(resultsArray.length());
        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject singleMoview = resultsArray.getJSONObject(i);
            MovieModel movieModel = new MovieModel(singleMoview.getInt(TMDB_ID), singleMoview.getString(TMDB_TITLE), singleMoview.getString(TMDB_IMAGE), singleMoview.getString(TMDB_BG_IMAGE), singleMoview.getString(TMDB_RELEASE_DATE), singleMoview.getString(TMDB_VOTE_AVERAGE), singleMoview.getString(TMDB_PLOT_SYNOPSIS));
            ContentValues movieValues = new ContentValues();
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_NAME, movieModel.getText());
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, movieModel.getId());
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_IMAGE_URL, movieModel.getImage());
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_IMAGE, "");
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_BG_IMAGE, "");
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_BG_IMAGE_URL, movieModel.getBgImage());
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_RELEASE_DATE, movieModel.getReleaseDate());
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_VOTE, movieModel.getVoteAverage());
            movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_DESCRIPTION, movieModel.getPlotSynopsis());
            Cursor query = getContext().getContentResolver().query(MoviesContract.MoviesEntry.CONTENT_URI, new String[]{MoviesContract.MoviesEntry.COLUMN_MOVIE_FAV}, MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " == ?", new String[]{"" + movieModel.getId()}, null);
            int fav;
            if (query.moveToFirst()) {
                fav = query.getInt(query.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_FAV));
                if (fav != 0) {
                    movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_FAV, 1);
                } else {
                    movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_FAV, 0);
                }
            } else {
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_FAV, 0);
            }

            String sort = Utility.getSortValue(getContext());
            if (sort.equals(getContext().getString(R.string.pref_sort_high_rated))) {
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_AVG, 1);
            } else {
                movieValues.put(MoviesContract.MoviesEntry.COLUMN_POPULAR, 1);
            }

            cVVector.add(movieValues);
        }
        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            for (int i = 0; i < cVVector.size(); i++) {
                getContext().getContentResolver().delete(MoviesContract.MoviesEntry.CONTENT_URI,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_NAME + "== ?",
                        new String[]{cVVector.get(i).getAsString(MoviesContract.MoviesEntry.COLUMN_MOVIE_NAME)});
            }

            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = getContext().getContentResolver().bulkInsert(MoviesContract.MoviesEntry.CONTENT_URI, cvArray);
        }
        Log.d(LOG_TAG, " Complete. " + inserted + " Inserted");
        return;
    }
}