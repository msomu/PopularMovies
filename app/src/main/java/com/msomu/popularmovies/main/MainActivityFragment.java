package com.msomu.popularmovies.main;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.msomu.popularmovies.R;
import com.msomu.popularmovies.SettingsActivity;
import com.msomu.popularmovies.Utility;
import com.msomu.popularmovies.data.MoviesContract;
import com.msomu.popularmovies.model.MovieModel;
import com.msomu.popularmovies.sync.MoviesSyncAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements RecyclerViewAdapter.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int COL_ID = 0;
    public static final int COL_MOVIE_ID = 1;
    public static final int COL_MOVIE_NAME = 2;
    public static final int COL_MOVIE_IMAGE_URL = 3;
    public static final int COL_MOVIE_BG_IMAGE_URL = 4;
    public static final int COL_MOVIE_RELEASE_DATE = 5;
    public static final int COL_MOVIE_VOTE = 6;
    public static final int COL_MOVIE_DESCRIPTIO = 7;
    private static final String TAG = "MainActivityFragment";
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
    private static final int MOVIE_LOADER = 0;
    private static List<MovieModel> items = new ArrayList<>();
    private RecyclerViewAdapter adapter;
    private View rootView;
    private Cursor cursor;
    private String mSortPreference;

    public MainActivityFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.action_most_popular) {
//            Log.d("MainFragment", "Most Popular clicked");
//            getMoviesList(POPULAR_MOVIES_QUERY);
//            item.setVisible(false);
//
//            return true;
//        } else if (item.getItemId() == R.id.action_highest_rated) {
//            Log.d("MainFragment", "Highest Rated clicked");
//            getMoviesList(TOP_RATED_MOVIES_QUERY);
//            item.setVisible(false);
//            return true;
//        }
        if (item.getItemId() == R.id.action_settings) {
            Log.d(TAG, "Settings Selected");
            startActivity(new Intent(getContext(), SettingsActivity.class));
        }
        if (item.getItemId() == R.id.action_refresh) {
            Log.d(TAG, "Refresh Selected");
            MoviesSyncAdapter.syncImmediately(getContext());
        }
        return true;
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
        adapter = new RecyclerViewAdapter(getContext(), cursor);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(Uri contentUri) {
        ((Callback) getActivity())
                .onItemSelected(contentUri);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged() {
        mSortPreference = Utility.getSortValue(getContext());
        if (!mSortPreference.equals(getString(R.string.pref_sort_fav))) {
            MoviesSyncAdapter.syncImmediately(getActivity());
        }
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mSortPreference = Utility.getSortValue(getContext());
        Uri weatherForLocationUri = MoviesContract.MoviesEntry.CONTENT_URI;
        if (mSortPreference.equals(getString(R.string.pref_sort_fav))) {
            Log.d(TAG, "Favourite");
            return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, MoviesContract.MoviesEntry.COLUMN_MOVIE_FAV + " == ?", new String[]{"1"}, null);
        } else if (mSortPreference.equals(getString(R.string.pref_sort_high_rated))) {
            Log.d(TAG, "Rated");
            return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, MoviesContract.MoviesEntry.COLUMN_AVG + " == ?", new String[]{"1"}, null);
        } else {
            //popular
            Log.d(TAG, "Popular");
            return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, MoviesContract.MoviesEntry.COLUMN_POPULAR + " == ?", new String[]{"1"}, null);
        }
//        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
//                null, null, null, sortOrder);

    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        this.cursor = cursor;
        adapter.swapCursor(this.cursor);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.swapCursor(null);
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
    }

//    /**
//     * +     * A callback interface that all activities containing this fragment must
//     * +     * implement. This mechanism allows activities to be notified of item
//     * +     * selections.
//     * +
//     */
//    public interface Callback {
//        /**
//         * DetailFragmentCallback for when an item has been selected.
//         */
//        void onItemSelected(Uri dateUri);
//    }
}
