package com.msomu.popularmovies.detail;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.msomu.popularmovies.R;
import com.msomu.popularmovies.Utility;
import com.msomu.popularmovies.data.MoviesContract;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final int COL_MOVIE_NMAE = 0;
    public static final int COL_MOVIE_IMG = 1;
    private static final String[] FORECAST_COLUMNS = {
            MoviesContract.MoviesEntry.COLUMN_MOVIE_NAME,
            MoviesContract.MoviesEntry.COLUMN_MOVIE_IMAGE_URL
    };
    private static final int DETAIL_LOADER = 0;
    private CollapsingToolbarLayout collapsingToolbar;
    private Uri mUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);


        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailActivityFragment.DETAIL_URI, getIntent().getData());
            mUri = getIntent().getData();
            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction().add(R.id.main_container, fragment, null).commit();
        }
        getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    private void loadBackdrop(String movie) {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        //Picasso.with(this).load(movie.getImage()).into(imageView);
        Utility.renderImage(this, movie, imageView);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            return new CursorLoader(
                    this,
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
        if (data != null & data.moveToFirst()) {
            collapsingToolbar.setTitle(data.getString(COL_MOVIE_NMAE));
            loadBackdrop(data.getString(COL_MOVIE_IMG));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
