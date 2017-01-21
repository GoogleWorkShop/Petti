package com.firebase.petti.petti.utils;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.firebase.petti.db.API;
import com.firebase.petti.db.LocationsApi;
import com.firebase.petti.db.classes.User;
import com.firebase.petti.petti.MatchesFragment;
import com.firebase.petti.petti.MatchesFragment.TaskParams;
import com.firebase.petti.petti.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

/**
 * Created by barjon on 09-Jan-17.
 */

public class FetchMatchesTask extends AsyncTask<Location, Void, ArrayList<User>> {

    private static final String tag = "FETCH-MATCHES-TASK";
    private static final long HALF_HOUR_MILLSEC = 30*60*1000;

    private GridViewAdapter mMatchesAdapter;

    private GridView gridView;
    private TextView notFoundView;
    private PulsatorLayout searchingView;
    private Button mapBtn;

    private boolean bark;

    public FetchMatchesTask(boolean bark, View rootView, GridViewAdapter mMatchesAdapter) {
        this.mMatchesAdapter = mMatchesAdapter;

        gridView = (GridView) rootView.findViewById(R.id.gridview_matches);
        notFoundView = (TextView) rootView.findViewById(R.id.no_matches_str);
        searchingView = (PulsatorLayout) rootView.findViewById(R.id.searching_matches_view);
        mapBtn = (Button)rootView.findViewById(R.id.go_to_map_btn);

        this.bark = bark;
    }

    @Override
    protected ArrayList<User> doInBackground(Location... params) {

        Location location = params[0];

        /* 3 seconds of placebo searching for feel good effect */
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            return null;
        }

        int timeout = 10; // ten seconds of timeout until we decide there are no matches
        do {
            try {
                if(isCancelled()) {
                    return new ArrayList<>();
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex){
                return null;
            }
        }
        while (timeout-- != 0 && !LocationsApi.queryReady);

        /* ensuring we have any results before iterating over them */
        if(LocationsApi.nearbyUsers == null){
            return null;
        }

        ArrayList<User> mMatchesArray = new ArrayList<>();
        for (Map.Entry<String, User> item : LocationsApi.nearbyUsers.entrySet()){
            User userCandidate = item.getValue();
            userCandidate.setTempUid(item.getKey());
            mMatchesArray.add(userCandidate);
            if(isCancelled()){
                return new ArrayList<>();
            }
        }

        /**
         * this is a check and logging of reciving an empty matches because of  API.ready
         * before timeout - this sometimes happen although there ARE matches
         * TODO remove after bug is fixed
          */
        if (timeout !=-1 && mMatchesArray.isEmpty()){
            Log.d(tag, "in 'timeout !=-1 && mMatchesArray.isEmpty()' - timeout: " + timeout);
        }

        // sort list by distance to current user
        Collections.sort(mMatchesArray, new MatchedUserComparator());
        return mMatchesArray;
    }

    @Override
    protected void onPostExecute(ArrayList<User> users) {

        if (users != null) {
            mMatchesAdapter.clear();
            mMatchesAdapter.refresh(users);
            // New data is back from the server.  Hooray!
        }

        searchingView.setVisibility(View.GONE);
        searchingView.stop();
        if(mMatchesAdapter.isEmpty()){
            gridView.setVisibility(View.GONE);
            notFoundView.setVisibility(View.VISIBLE);

        } else {
            gridView.setVisibility(View.VISIBLE);
            notFoundView.setVisibility(View.GONE);
        }
        if (bark){
            if(users.isEmpty()) {
                mapBtn.setEnabled(false);
            } else {
                mapBtn.setEnabled(true);
            }
        }
        super.onPostExecute(users);
    }

    /**
     * returning a user nullified with
     */
    private User createFalseUser(){
        User falseUser = new User();
        return falseUser;
    }

    private static class MatchedUserComparator implements Comparator<User> {
        @Override
        public int compare(User a, User b) {

            boolean aFriend = API.isMatchedWith(a.getTempUid());
            boolean bFriend = API.isMatchedWith(b.getTempUid());

            if (aFriend ^ bFriend){
                return aFriend ? -1: 1;
            }

            float aDist = a.getTempDistanceFromMe();
            float bDist = b.getTempDistanceFromMe();

            if (aDist > bDist){
                return 1;
            } else if (aDist == bDist){
                return 0;
            } else {
                return -1;
            }

        }
    }
}
