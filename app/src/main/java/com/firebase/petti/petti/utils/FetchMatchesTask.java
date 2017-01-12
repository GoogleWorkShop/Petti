package com.firebase.petti.petti.utils;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

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

    boolean bark;
    Location location;

    @Override
    protected ArrayList<User> doInBackground(TaskParams... params) {

        bark = params[0].bark;
        location = params[0].location;


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
