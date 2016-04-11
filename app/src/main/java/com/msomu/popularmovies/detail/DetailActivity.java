package com.msomu.popularmovies.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.msomu.popularmovies.R;
import com.msomu.popularmovies.model.MovieModel;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        Intent intent = getIntent();
        MovieModel movie = intent.getParcelableExtra(Intent.EXTRA_TEXT);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(movie.getText());

        if (savedInstanceState == null) {
            DetailActivityFragment fragment = DetailActivityFragment.getInstance(movie);
            getSupportFragmentManager().beginTransaction().add(R.id.main_container, fragment, null).commit();
        }

        loadBackdrop(movie);
    }

    private void loadBackdrop(MovieModel movie) {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        Picasso.with(this).load(movie.getImage()).into(imageView);
    }

}
