package com.msomu.popularmovies.main;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.msomu.popularmovies.R;
import com.msomu.popularmovies.SettingsActivity;
import com.msomu.popularmovies.data.MoviesContract;
import com.msomu.popularmovies.detail.DetailActivity;
import com.msomu.popularmovies.model.MovieModel;
import com.msomu.popularmovies.sync.MoviesSyncAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements RecyclerViewAdapter.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int COL_ID = 0;
    public static final int COL_MOVIE_NAME = 1;
    public static final int COL_MOVIE_IMAGE_URL = 2;
    public static final int COL_MOVIE_BG_IMAGE_URL = 3;
    public static final int COL_MOVIE_RELEASE_DATE = 4;
    public static final int COL_MOVIE_VOTE = 5;
    public static final int COL_MOVIE_DESCRIPTIO = 6;
    private static final String TAG = "MainActivityFragment";
    private static final String[] FORECAST_COLUMNS = {
            MoviesContract.MoviesEntry._ID,
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

    public MainActivityFragment() {
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        getMoviesList();
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
        if(item.getItemId() == R.id.action_settings){
            Log.d(TAG,"Settings Selected");
            startActivity(new Intent(getContext(),SettingsActivity.class));
        }
        if (item.getItemId() == R.id.action_refresh) {
            Log.d(TAG, "Refresh Selected");
            MoviesSyncAdapter.syncImmediately(getContext());
        }
        return true;
    }

    private void getMoviesList() {
        //TODO
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
    public void onItemClick(View view, MovieModel movieModel) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, movieModel);
        startActivity(intent);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri weatherForLocationUri = MoviesContract.MoviesEntry.CONTENT_URI;
        String sortOrder = MoviesContract.MoviesEntry.COLUMN_MOVIE_RELEASE_DATE + " ASC";
//        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
//                null, null, null, sortOrder);
        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
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
