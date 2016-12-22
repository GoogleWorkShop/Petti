package com.firebase.petti.petti;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class AnotherDogActivity extends AppCompatActivity {

    private     String mDogId;
    protected   ImageView imageView;
    protected   String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_another_dog);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String dogName = getIntent().getStringExtra("dogName");
        imageUrl = getIntent().getStringExtra("image");

        TextView titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(dogName);

        imageView = (ImageView) findViewById(R.id.image);
    }

    private void updateDogDate() {
        FetchAnotherDogTask movieTask = new FetchAnotherDogTask(imageView);
        movieTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateDogDate();
    }


    public class FetchAnotherDogTask extends AsyncTask<String, Void, ArrayList<String>> {

        private ImageView imageView;

        public FetchAnotherDogTask(ImageView imageView){
            this.imageView = imageView;
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            //TODO get dog data from firebase by mDogId var

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            Picasso.with(getBaseContext()).load(imageUrl).into(imageView);
            // New data is back from the server.  Hooray!
        }

    }
}
