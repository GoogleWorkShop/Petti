package com.firebase.petti.petti;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.firebase.petti.petti.utils.Friend;
import com.firebase.petti.petti.utils.RVAdapter;

import com.firebase.petti.db.classes.User.Dog;

import java.util.ArrayList;
import java.util.List;

public class MatchedFriendsActivity extends Activity {

    private List<Dog> mFriends;
    private RVAdapter mAdapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_matched_friends);

        rv=(RecyclerView)findViewById(R.id.matched_friends);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        mFriends = new ArrayList<>();
        mAdapter = new RVAdapter(mFriends);
        rv.setAdapter(mAdapter);

//        initializeData();
//        initializeAdapter();
    }

//        adapter.persons.add(new Friend("yahav", "25 years old", R.drawable.lavery));
//        adapter.notifyItemInserted(persons.size() - 1);

}