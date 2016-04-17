package com.msomu.popularmovies.detail;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.msomu.popularmovies.BuildConfig;
import com.msomu.popularmovies.R;
import com.msomu.popularmovies.Utility;
import com.msomu.popularmovies.ViewUtil;
import com.msomu.popularmovies.data.MoviesContract;
import com.msomu.popularmovies.model.MovieModel;
import com.msomu.popularmovies.model.ReviewsModel;
import com.msomu.popularmovies.model.TrailerModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public final static String EXTRA_DATA = "extra_data";
    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_MOVIE_NAME = 2;
    public static final int COL_MOVIE_IMAGE_URL = 3;
    public static final int COL_MOVIE_BG_IMAGE_URL = 4;
    public static final int COL_MOVIE_RELEASE_DATE = 5;
    public static final int COL_MOVIE_VOTE = 6;
    public static final int COL_MOVIE_DESCRIPTIO = 7;
    public static final String DETAIL_URI = "URI";
    private static final String TAG = "DetailActivityFragment";
    private static final int DETAIL_LOADER = 0;
    private static final String[] FORECAST_COLUMNS = {
            MoviesContract.MoviesEntry._ID,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_NAME,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_IMAGE_URL,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_BG_IMAGE_URL,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_RELEASE_DATE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_VOTE,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_DESCRIPTION
    };
    int fav;
    private MovieModel mMovie;
    private TextView releaseDate;
    private TextView synopsis;
    private ImageView thumb;
    private TextView avgRating;
    private ShareActionProvider mShareActionProvider;
    private List<TrailerModel> trailersList;
    private List<ReviewsModel> reviewsList;
    private LinearLayout trailersLayout, reviewsLayout;
    private Uri mUri;
    private Button favourite;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trailersList = new ArrayList<>();
        reviewsList = new ArrayList<>();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_detail_fragment, menu);
        MenuItem shareTrailerMenuItem = menu.findItem(R.id.share_trailer);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareTrailerMenuItem);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
        }

        thumb = (ImageView) rootView.findViewById(R.id.thumb);
        releaseDate = (TextView) rootView.findViewById(R.id.release_date);
        avgRating = (TextView) rootView.findViewById(R.id.rating);
        synopsis = (TextView) rootView.findViewById(R.id.overview);
        trailersLayout = (LinearLayout) rootView.findViewById(R.id.layoutTrailers);
        reviewsLayout = (LinearLayout) rootView.findViewById(R.id.layoutReviews);
        favourite = (Button) rootView.findViewById(R.id.fav);
        return rootView;
    }

    private void updateShareActionProvider(String trailer) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mMovie.getText());
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Trailer : " + trailer);
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(sharingIntent);
        }
    }

    public void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (null != uri) {
            mUri = uri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "On Finsihed called");
        if (data != null && data.moveToFirst()) {
            mMovie = new MovieModel();
            mMovie.setBgImage(data.getString(COL_MOVIE_BG_IMAGE_URL));
            mMovie.setId(data.getInt(COL_MOVIE_ID));
            mMovie.setImage(data.getString(COL_MOVIE_IMAGE_URL));
            mMovie.setPlotSynopsis(data.getString(COL_MOVIE_DESCRIPTIO));
            mMovie.setReleaseDate(data.getString(COL_MOVIE_RELEASE_DATE));
            mMovie.setText(data.getString(COL_MOVIE_NAME));
            mMovie.setVoteAverage(data.getString(COL_MOVIE_VOTE));
            Cursor query = getContext().getContentResolver().query(MoviesContract.MoviesEntry.CONTENT_URI, new String[]{MoviesContract.MoviesEntry.COLUMN_MOVIE_FAV}, MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " == ?", new String[]{"" + mMovie.getId()}, null);
            if (query.moveToFirst()) {
                fav = query.getInt(query.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_FAV));
            }
            if (fav != 0) {
                favourite.setText("Un Favourite");
            } else {
                favourite.setText("Favourite");
            }
            favourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentValues contentValues = new ContentValues();
                    if (fav != 0) {
                        fav = 0;
                        contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_FAV, 0);
                        favourite.setText("Favourite");
                    } else {
                        fav = 1;
                        contentValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_FAV, 1);
                        favourite.setText("Un Favourite");
                    }
                    getContext().getContentResolver().update(MoviesContract.MoviesEntry.CONTENT_URI, contentValues, MoviesContract.MoviesEntry.COLUMN_MOVIE_ID + " == ?", new String[]{"" + mMovie.getId()});
                }
            });
            if (mMovie != null) {
                if (!TextUtils.isEmpty(mMovie.getBgImage())) {
                    // Picasso.with(thumb.getContext()).load(mMovie.getBgImage()).placeholder(R.mipmap.place_hodler).into(thumb);
                    Utility.renderImage(getContext(), mMovie.getBgImage(), thumb);
                }
                if (!TextUtils.isEmpty(mMovie.getReleaseDate())) {
                    releaseDate.setText(mMovie.getReleaseDate());
                }
                if (!TextUtils.isEmpty(mMovie.getVoteAverage())) {
                    avgRating.setText(mMovie.getVoteAverage());
                }
                if (!TextUtils.isEmpty(mMovie.getPlotSynopsis())) {
                    synopsis.setText(mMovie.getPlotSynopsis());
                }
            }
            new Fetchtrailers().execute("" + mMovie.getId());
            new Fetchreviews().execute("" + mMovie.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class Fetchtrailers extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            if (params == null) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailersString = null;
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                // String BASE_URL = "https://api.themoviedb.org/3/movie/209112/videos?api_key=5078dce8ec19ba4682df32ed1f7c2726";
                String movieId = params[0];
                final String FORECAST_BASE_URL =
                        "https://api.themoviedb.org/3/movie/" + movieId + "/trailers";
                final String APPID_PARAM = "api_key";
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();
                URL url = new URL(builtUri.toString());
                Log.v("Detail Fragment", "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
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
                    return null;
                }
                trailersString = buffer.toString();
                Log.d("PlaceholderFragment", trailersString);
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
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
                return getTrailers(trailersString);
            } catch (JSONException e) {
                Log.e("ForecastFragment", e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private Void getTrailers(String trailersString) throws JSONException {
            Log.d(TAG, trailersString);
            final String TMDB_RESULTS = "youtube";
            final String TMDB_KEY = "source";
            final String TMDB_NAME = "name";
            JSONObject trailerListJson = new JSONObject(trailersString);
            JSONArray trailersArray = trailerListJson.getJSONArray(TMDB_RESULTS);
            for (int i = 0; i < trailersArray.length(); i++) {
                JSONObject singleTrailer = trailersArray.getJSONObject(i);
                TrailerModel trailerModel = new TrailerModel();
                trailerModel.setName(singleTrailer.getString(TMDB_NAME));
                trailerModel.setSource(singleTrailer.getString(TMDB_KEY));
                trailersList.add(trailerModel);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (trailersList.size() > 0) {
                updateShareActionProvider(trailersList.get(0).getSource());
            }
            for (TrailerModel trailer : trailersList) {
                trailersLayout.addView(ViewUtil.createTrailerLayout(getContext(), trailer.getName(), trailer.getSource()));
            }
        }
    }

    public class Fetchreviews extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            if (params == null) {
                return null;
            }
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailersString = null;
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                // String BASE_URL = "https://api.themoviedb.org/3/movie/209112/videos?api_key=5078dce8ec19ba4682df32ed1f7c2726";
                String movieId = params[0];
                final String FORECAST_BASE_URL =
                        "https://api.themoviedb.org/3/movie/" + movieId + "/reviews";
                final String APPID_PARAM = "api_key";
                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(APPID_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                        .build();
                URL url = new URL(builtUri.toString());
                Log.v("Detail Fragment", "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
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
                    return null;
                }
                trailersString = buffer.toString();
                Log.d("PlaceholderFragment", trailersString);
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
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
                return getReviews(trailersString);
            } catch (JSONException e) {
                Log.e("ForecastFragment", e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private Void getReviews(String trailersString) throws JSONException {
            Log.d(TAG, trailersString);
            final String TMDB_RESULTS = "results";
            final String TMDB_AUTHOR = "author";
            final String TMDB_CONTENT = "content";
            JSONObject trailerListJson = new JSONObject(trailersString);
            JSONArray trailersArray = trailerListJson.getJSONArray(TMDB_RESULTS);
            for (int i = 0; i < trailersArray.length(); i++) {
                ReviewsModel reviewsModel = new ReviewsModel();
                JSONObject singleTrailer = trailersArray.getJSONObject(i);
                reviewsModel.setName(singleTrailer.getString(TMDB_AUTHOR));
                reviewsModel.setContent(singleTrailer.getString(TMDB_CONTENT));
                reviewsList.add(reviewsModel);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            for (ReviewsModel reviewsModel :
                    reviewsList) {
                reviewsLayout.addView(ViewUtil.createReviewLayout(getContext(), reviewsModel.getName(), reviewsModel.getContent()));
                reviewsLayout.addView(ViewUtil.getLineView(getContext()));
            }
        }
    }
}
