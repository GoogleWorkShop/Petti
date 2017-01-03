package com.firebase.petti.petti;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;

public class NeighborDogsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_neighbor_dogs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {

            String s = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            boolean bark = Boolean.parseBoolean(s);

            Bundle bundle = new Bundle();
            bundle.putBoolean("bark", bark);

            MatchesFragment matchesFragment = new MatchesFragment();
            matchesFragment.setArguments(bundle);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.neighbor_container, matchesFragment)
                    .commit();
        }
    }
}
