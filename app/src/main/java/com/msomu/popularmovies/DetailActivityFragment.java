package com.msomu.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {


    public final static String EXTRA_DATA = "extra_data";
    private ViewModel mMovie;

    public DetailActivityFragment() {
    }

    public static DetailActivityFragment getInstance(ViewModel viewModel) {
        DetailActivityFragment detailActivityFragment = new DetailActivityFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_DATA, viewModel);
        detailActivityFragment.setArguments(bundle);
        return detailActivityFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            mMovie = args.getParcelable(EXTRA_DATA);
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
        return rootView;
    }
}
