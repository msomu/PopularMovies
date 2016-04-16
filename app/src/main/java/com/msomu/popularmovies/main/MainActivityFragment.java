package com.msomu.popularmovies.main;

import android.content.Intent;
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

import com.msomu.popularmovies.R;
import com.msomu.popularmovies.SettingsActivity;
import com.msomu.popularmovies.detail.DetailActivity;
import com.msomu.popularmovies.model.MovieModel;
import com.msomu.popularmovies.sync.MoviesSyncAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements RecyclerViewAdapter.OnItemClickListener {

    private static final String TAG = "MainActivityFragment";
    private static List<MovieModel> items = new ArrayList<>();
    private RecyclerViewAdapter adapter;
    private Menu mainFragmentMenu;

    private View rootView;

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
        mainFragmentMenu = menu;
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
        adapter = new RecyclerViewAdapter(items);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, MovieModel movieModel) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, movieModel);
        startActivity(intent);
    }
}
