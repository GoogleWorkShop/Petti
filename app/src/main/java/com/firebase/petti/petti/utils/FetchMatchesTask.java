package com.firebase.petti.petti.utils;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.firebase.petti.db.API;
import com.firebase.petti.db.classes.User;
import com.firebase.petti.petti.MatchesFragment;
import com.firebase.petti.petti.MatchesFragment.TaskParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by barjon on 09-Jan-17.
 */

public class FetchMatchesTask extends AsyncTask<MatchesFragment.TaskParams, Void, ArrayList<User>> {

    private static final String tag = "FETCH-MATCHES-TASK";
    private static final long HALF_HOUR_MILLSEC = 30*60*1000;

    private GridViewAdapter mMatchesAdapter;

    private GridView gridView;
    private TextView notFoundView;
    private TextView searchingView;

    public FetchMatchesTask(GridViewAdapter mMatchesAdapter, GridView gridView,
                            TextView notFoundView, TextView searchingView) {
        this.mMatchesAdapter = mMatchesAdapter;
        this.gridView = gridView;
        this.notFoundView = notFoundView;
        this.searchingView = searchingView;
    }

    @Override
    protected ArrayList<User> doInBackground(TaskParams... params) {

        boolean bark = params[0].bark;
        Location location = params[0].location;


        int timeout = 10; // five seconds of timeout until we decide there are no matches
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
        while (timeout-- != 0 && !API.queryReady);

        /* ensuring we have any results before iterating over them */
        if(API.nearbyUsers == null){
            return null;
        }

        ArrayList<User> mMatchesArray = new ArrayList<>();
        for (Map.Entry<String, User> item : API.nearbyUsers.entrySet()){
            User userCandidate = item.getValue();
            Long userLastWalkTimestamp = userCandidate.getLastLocationTime();
            long minBarkTimeLimit = (location.getTime() - HALF_HOUR_MILLSEC);
            if (bark && (userLastWalkTimestamp == null ||
                    userLastWalkTimestamp < minBarkTimeLimit)){
                continue;
            }
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

        // TODO YAHAV - can we delete this?
//            //put friends before non-friends
//            ArrayList<User> tmpFriendsListByLocation = new ArrayList<>();
//            ArrayList<User> tmpNotFriendsListByLocation = new ArrayList<>();
//            for (User user : mMatchesArray){
//                if (API.isMatchedWith(user.getTempUid())){
//                    tmpFriendsListByLocation.add(user);
//                }else{
//                    tmpNotFriendsListByLocation.add(user);
//                }
//            }
//            mMatchesArray = new ArrayList<>(tmpFriendsListByLocation);
//            mMatchesArray.addAll(tmpNotFriendsListByLocation);
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
        if(mMatchesAdapter.isEmpty()){
            gridView.setVisibility(View.GONE);
            notFoundView.setVisibility(View.VISIBLE);

        } else {
            gridView.setVisibility(View.VISIBLE);
            notFoundView.setVisibility(View.GONE);
        }
        super.onPostExecute(users);
    }

    /**
     * returning a user with
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
