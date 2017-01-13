package com.firebase.petti.petti;


import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.petti.db.API;
import com.firebase.petti.db.classes.User;
import com.firebase.petti.petti.utils.GPSTracker;
import com.firebase.petti.petti.utils.GridViewAdapter;
import com.google.android.gms.plus.model.people.Person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class MatchesFragment extends Fragment {

    View rootView;

    private static final String tag = "***MATCHES-FRAGMENT***";

    GridViewAdapter mMatchesAdapter;
    FetchMatchesTask matchesTask;
    boolean bark;
    int mRadius;

    // GPSTracker class
    GPSTracker gps;
    Location location; // location


    public MatchesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // create class object
        gps = new GPSTracker(getActivity());

        // check if GPS enabled
        if (gps.canGetLocation()) {

            location = gps.getLocation();
            if (location == null){
                Toast.makeText(getActivity(),
                        "All locations and no permissions makes Johnny a dull boy",
                        Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mRadius = Integer.parseInt(pref.getString("matchDistance", "1"));

        API.attachNearbyUsersListener(location, mRadius);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_matches, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_matches);
        TextView notFoundView = (TextView) rootView.findViewById(R.id.no_matches_str);
        TextView searchingView = (TextView) rootView.findViewById(R.id.searching_matches_str);

        gridView.setVisibility(View.GONE);
        notFoundView.setVisibility(View.GONE);
        searchingView.setVisibility(View.VISIBLE);

        mMatchesAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_match);

        //TODO: Yahav: this returns null after we go to settings and back
        if (getArguments() != null) {
            if (getArguments().isEmpty()) {
                bark = false;
            } else {
                bark = getArguments().getBoolean("bark");
            }
        }
        gridView.setAdapter(mMatchesAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                User selected = mMatchesAdapter.getItem(position);

                Intent intent = new Intent(getActivity(), MatchedDogActivity.class);
                intent.putExtra("user", selected);

                startActivity(intent);
            }
        });

        return rootView;
    }

    private void updateMatches() {
        matchesTask = new FetchMatchesTask();
        matchesTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMatches();
    }

    @Override
    public void onDestroy() {
        matchesTask.cancel(true);
        API.detachNearbyUsersListener();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            SharedPreferences pref = getActivity().getSharedPreferences(DEFAULT_PREFERENCE_STRING, 0);
        String s = pref.getString("matchDistance", "1");
        int radius = Integer.parseInt(s);
        if (radius != mRadius) {
            mRadius = radius;
            API.detachNearbyUsersListener();
            API.attachNearbyUsersListener(location, mRadius);
            updateMatches();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_neighbor_dogs, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Fragment myPrefrences = new MyPreferencesFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(((ViewGroup)getView().getParent()).getId(), myPrefrences)
                        .addToBackStack( "tag" ).commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private boolean canAccessLocation() {
        return(hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED== ContextCompat.checkSelfPermission(getContext(),perm));
    }


    private class FetchMatchesTask extends AsyncTask<Void, Void, ArrayList<User>> {

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
                long minBarkTimeLimit = (location.getTime() - API.HALF_HOUR_MILLSEC);
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
}
