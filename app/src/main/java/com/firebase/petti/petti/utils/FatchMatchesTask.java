package com.firebase.petti.petti.utils;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;

import com.firebase.petti.db.API;
import com.firebase.petti.db.classes.User;
import com.firebase.petti.petti.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * Created by barjon on 09-Jan-17.
 */

class FetchMatchesTask extends AsyncTask<Void, Void, ArrayList<User>> {

    private static final String tag = "FETCH-MATCHES-TASK";

    @Override

    protected ArrayList<User> doInBackground(Void... voids) {
        int timeout = 10; // five seconds of timeout until we decide there are no matches
        do {
            try {
                if(isCancelled()) {
                    return new ArrayList<>();
                } else {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException ex){
                return new ArrayList<>();
            }
        }
        while (timeout-- != 0 && !API.queryReady);

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

        if (timeout !=0 && mMatchesArray.isEmpty()){
            Log.d(tag, "in 'timeout !=0 && mMatchesArray.isEmpty()' - timeout: " + timeout);
        }

        // sort list by distance to current user
        Collections.sort(mMatchesArray, new MatchedUserComparator());

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

    class MatchedUserComparator implements Comparator<User> {
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

    @Override
    protected void onPostExecute(ArrayList<User> result) {

        if (isCancelled()){
            return;
        }

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_matches);
        TextView textView = (TextView) rootView.findViewById(R.id.no_matches_str);
        TextView searchingView = (TextView) rootView.findViewById(R.id.searching_matches_str);

        if (result != null) {
            mMatchesAdapter.clear();
            mMatchesAdapter.refresh(result);
            // New data is back from the server.  Hooray!
        }

        searchingView.setVisibility(View.GONE);
        if(mMatchesAdapter.isEmpty()){
            gridView.setVisibility(View.GONE);
            textView.setVisibility(View.VISIBLE);

        } else {
            gridView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.GONE);
        }
    }
}
