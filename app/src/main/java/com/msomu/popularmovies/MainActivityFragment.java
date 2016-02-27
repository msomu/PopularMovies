package com.msomu.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
public class MainActivityFragment extends Fragment implements RecyclerViewAdapter.OnItemClickListener {

    private static final String POPULAR_MOVIES_QUERY = "popularity.desc";
    private static final String TOP_RATED_MOVIES_QUERY = "vote_average.desc";
    private static List<ViewModel> items = new ArrayList<>();
    private RecyclerViewAdapter adapter;
    private Menu mainFragmentMenu;

    private View rootView;

    public MainActivityFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        getMoviesList(POPULAR_MOVIES_QUERY);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        mainFragmentMenu = menu;
        inflater.inflate(R.menu.fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_most_popular) {
            Log.d("MainFragment", "Most Popular clicked");
            getMoviesList(POPULAR_MOVIES_QUERY);
            item.setVisible(false);

            return true;
        } else if (item.getItemId() == R.id.action_highest_rated) {
            Log.d("MainFragment", "Highest Rated clicked");
            getMoviesList(TOP_RATED_MOVIES_QUERY);
            item.setVisible(false);
            return true;
        }
        return true;
    }

    private void getMoviesList(String query) {
        new FetchMoviesList().execute(query);
        if (query.equals(POPULAR_MOVIES_QUERY)) {
            if (mainFragmentMenu != null) {
                mainFragmentMenu.findItem(R.id.action_highest_rated).setVisible(true);
            }
        } else if (query.equals(TOP_RATED_MOVIES_QUERY)) {
            if (mainFragmentMenu != null) {
                mainFragmentMenu.findItem(R.id.action_most_popular).setVisible(true);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        initRecyclerView();
        return rootView;
    }

    private void initRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        adapter = new RecyclerViewAdapter(items);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, ViewModel viewModel) {
        startActivity(new Intent(getActivity(), DetailActivity.class));
    }

    public class FetchMoviesList extends AsyncTask<String, Void, List<ViewModel>> {
        private final String TAG = FetchMoviesList.class.getSimpleName();

        @Override
        protected void onPostExecute(List<ViewModel> result) {
            if (result != null) {
                items.clear();
                items.addAll(result);
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        protected List<ViewModel> doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            if (params == null) {
                return null;
            }
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
                        .appendQueryParameter(SORT_PARAM, params[0])
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
                movieListString = buffer.toString();
                Log.d("PlaceholderFragment", movieListString);
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
                return getMovies(movieListString);
            } catch (JSONException e) {
                Log.e("ForecastFragment", e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private List<ViewModel> getMovies(String movieListString) throws JSONException {
            Log.d(TAG, movieListString);
            final String TMDB_RESULTS = "results";
            final String TMDB_IMAGE = "poster_path";
            final String TMDB_TITLE = "title";

            JSONObject movieListJson = new JSONObject(movieListString);
            JSONArray resultsArray = movieListJson.getJSONArray(TMDB_RESULTS);
            List<ViewModel> movieList = new ArrayList<>();
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject singleMoview = resultsArray.getJSONObject(i);
                ViewModel viewModel = new ViewModel(singleMoview.getString(TMDB_TITLE), singleMoview.getString(TMDB_IMAGE));
                movieList.add(viewModel);
            }

            return movieList;
        }
    }
}
