package com.msomu.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        ViewModel movie = intent.getParcelableExtra(Intent.EXTRA_TEXT);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            DetailActivityFragment fragment = DetailActivityFragment.getInstance(movie);
            getSupportFragmentManager().beginTransaction().add(R.id.main_container, fragment, null).commit();
        }
    }

}
