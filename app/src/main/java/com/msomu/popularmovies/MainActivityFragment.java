package com.msomu.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements RecyclerViewAdapter.OnItemClickListener {

    private static List<ViewModel> items = new ArrayList<>();

    static {
        for (int i = 1; i <= 10; i++) {
            items.add(new ViewModel("Item " + i, "http://lorempixel.com/500/500/animals/" + i));
        }
    }

    private View rootView;
    public MainActivityFragment() {
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
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(items);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(View view, ViewModel viewModel) {

    }
}
