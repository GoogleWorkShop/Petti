package com.firebase.petti.petti;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class matched_friends extends Activity {

    private List<Friend> persons;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_matched_friends);

        rv=(RecyclerView)findViewById(R.id.matched_friends);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeData();
        initializeAdapter();
    }

    private void initializeData(){
        persons = new ArrayList<>();
        persons.add(new Friend("roee", "23 years old", R.drawable.emma));
        persons.add(new Friend("yahav", "25 years old", R.drawable.lavery));
        persons.add(new Friend("nir", "35 years old", R.drawable.lillie));
        persons.add(new Friend("amir", "23 years old", R.drawable.emma));
        persons.add(new Friend("amir 2", "25 years old", R.drawable.lavery));
        persons.add(new Friend("Lillie Watts", "35 years old", R.drawable.lillie));
    }

    private void initializeAdapter(){
        RVAdapter adapter = new RVAdapter(persons);
        rv.setAdapter(adapter);
    }
}