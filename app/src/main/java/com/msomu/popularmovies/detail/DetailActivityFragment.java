package com.msomu.popularmovies.detail;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.msomu.popularmovies.BuildConfig;
import com.msomu.popularmovies.R;
import com.msomu.popularmovies.model.MovieModel;
import com.msomu.popularmovies.model.ReviewsModel;
import com.squareup.picasso.Picasso;

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
public class DetailActivityFragment extends Fragment implements TrailerAdapter.OnItemClickListener {

    public final static String EXTRA_DATA = "extra_data";
    private static final String TAG = "DetailActivityFragment";
    private MovieModel mMovie;
    private ShareActionProvider mShareActionProvider;

    private List<String> trailersList;
    private TrailerAdapter trailerAdapter;

    private List<ReviewsModel> reviewsList;
    private ReviewsAdapter reviewsAdapter;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    public static DetailActivityFragment getInstance(MovieModel movieModel) {
        DetailActivityFragment detailActivityFragment = new DetailActivityFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_DATA, movieModel);
        detailActivityFragment.setArguments(bundle);
        return detailActivityFragment;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.movie_detail_fragment, menu);
        MenuItem shareTrailerMenuItem = menu.findItem(R.id.share_trailer);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareTrailerMenuItem);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mMovie = args.getParcelable(EXTRA_DATA);
            trailersList = new ArrayList<>();
            reviewsList = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ImageView thumb = (ImageView) rootView.findViewById(R.id.thumb);
        TextView title = (TextView) rootView.findViewById(R.id.title);
        TextView releaseDate = (TextView) rootView.findViewById(R.id.release_date);
        TextView avgRating = (TextView) rootView.findViewById(R.id.rating);
        TextView synopsis = (TextView) rootView.findViewById(R.id.overview);

        RecyclerView trailers = (RecyclerView) rootView.findViewById(R.id.trailerRecylerView);
        trailers.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        trailerAdapter = new TrailerAdapter(trailersList);
        trailerAdapter.setOnItemClickListener(this);
        trailers.setAdapter(trailerAdapter);

        RecyclerView reviews = (RecyclerView) rootView.findViewById(R.id.reviewsrRecylerView);
        //reviews.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        reviews.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        reviewsAdapter = new ReviewsAdapter(reviewsList);
        reviews.setAdapter(reviewsAdapter);

        if (mMovie != null) {
            if (!TextUtils.isEmpty(mMovie.getBgImage())) {
                Picasso.with(thumb.getContext()).load(mMovie.getBgImage()).placeholder(R.mipmap.place_hodler).into(thumb);
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
            if (!TextUtils.isEmpty(mMovie.getText())) {
                title.setText(mMovie.getText());
            }
        }
        new Fetchtrailers().execute("" + mMovie.getId());
        new Fetchreviews().execute("" + mMovie.getId());
        return rootView;
    }

    @Override
    public void onItemClick(View view, String movieModel) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(movieModel)));
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
                        "https://api.themoviedb.org/3/movie/" + movieId + "/videos";
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
            final String TMDB_RESULTS = "results";
            final String TMDB_KEY = "key";
            final String YOUTUBE_URL = "https://www.youtube.com/watch?v=";
            JSONObject trailerListJson = new JSONObject(trailersString);
            JSONArray trailersArray = trailerListJson.getJSONArray(TMDB_RESULTS);
            for (int i = 0; i < trailersArray.length(); i++) {
                JSONObject singleTrailer = trailersArray.getJSONObject(i);
                trailersList.add(YOUTUBE_URL + singleTrailer.getString(TMDB_KEY));
                Log.d(TAG, trailersList.get(i));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (trailersList.size() > 0) {
                updateShareActionProvider(trailersList.get(0));
            }
            trailerAdapter.notifyDataSetChanged();
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
            reviewsAdapter.notifyDataSetChanged();
        }
    }
}
