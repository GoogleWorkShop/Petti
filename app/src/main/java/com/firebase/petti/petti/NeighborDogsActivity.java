package com.firebase.petti.petti;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.firebase.petti.petti.utils.GPSTracker;

public class NeighborDogsActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
