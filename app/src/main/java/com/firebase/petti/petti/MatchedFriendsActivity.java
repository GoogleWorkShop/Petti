package com.firebase.petti.petti;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
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
import java.util.LinkedList;
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

        mFriends = new LinkedList<>();
        mAdapter = new RVAdapter(mFriends, this.getApplicationContext());
        rv.setAdapter(mAdapter);

        Map<String, Boolean> msgTracker = API.getCurrMsgTracker();
        if (msgTracker != null && msgTracker.size() > 0){
            Set<String> friendsSet = msgTracker.keySet();
            API.mDatabaseUsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (Map.Entry<String, Boolean> entry: API.currUserData.getMsgTracker().entrySet()){
                        String friendUid = entry.getKey();
                        DataSnapshot currFriend = dataSnapshot.child(friendUid);
                        if (currFriend.exists() && currFriend.hasChild("dog") &&
                                !API.isBlockedByMe(friendUid)){
                            User currUser = currFriend.getValue(User.class);
                            currUser.setTempUid(friendUid);
                            if (entry.getValue()) {
                                mAdapter.mFriends.add(currUser);
                                mAdapter.notifyItemInserted(mFriends.size() - 1);
                            } else {
                                mAdapter.mFriends.add(0, currUser);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

}