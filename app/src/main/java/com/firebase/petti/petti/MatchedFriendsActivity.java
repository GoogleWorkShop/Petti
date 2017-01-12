package com.firebase.petti.petti;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.firebase.petti.db.API;
import com.firebase.petti.db.classes.User;
import com.firebase.petti.petti.utils.RVAdapter;

import com.firebase.petti.db.classes.User.Dog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MatchedFriendsActivity extends AppCompatActivity {

    private List<User> mFriends;
    private RVAdapter mAdapter;
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_matched_friends);

        rv=(RecyclerView)findViewById(R.id.matched_friends);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        mFriends = new ArrayList<>();
        mAdapter = new RVAdapter(mFriends, this.getApplicationContext());
        rv.setAdapter(mAdapter);

        Map<String, Boolean> msgTracker = API.getCurrMsgTracker();
        if (msgTracker != null && msgTracker.size() > 0){
            Set<String> friendsSet = msgTracker.keySet();
            API.mDatabaseUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (String friendUid: API.currUserData.getMsgTracker().keySet()){
                        DataSnapshot currFriend = dataSnapshot.child(friendUid);
                        if (currFriend.exists() && currFriend.hasChild("dog")){
                            User currUser = currFriend.getValue(User.class);
                            currUser.setTempUid(friendUid);
                            mAdapter.mFriends.add(currUser);
                            mAdapter.notifyItemInserted(mFriends.size() - 1);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

//        initializeData();
//        initializeAdapter();
    }

//        adapter.persons.add(new Friend("yahav", "25 years old", R.drawable.lavery));
//        adapter.notifyItemInserted(persons.size() - 1);

}