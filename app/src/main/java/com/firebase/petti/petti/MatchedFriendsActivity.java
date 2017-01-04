package com.firebase.petti.petti;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.firebase.petti.db.API;
import com.firebase.petti.petti.utils.RVAdapter;

import com.firebase.petti.db.classes.User.Dog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        Map<String, Boolean> msgTracker = API.currUserData.getMsgTracker();
        if (msgTracker != null && msgTracker.size() > 0){
            Set<String> friendsSet = msgTracker.keySet();
            API.mDatabaseUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (String friendUid: API.currUserData.getMsgTracker().keySet()){
                        DataSnapshot currFriend = dataSnapshot.child(friendUid);
                        if (currFriend.exists() && currFriend.hasChild("dog")){
                            Dog currDogData = currFriend.child("dog").getValue(Dog.class);
                            mAdapter.mFriends.add(currDogData);
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