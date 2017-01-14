package com.firebase.petti.petti;


import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.firebase.petti.petti.utils.FetchMatchesTask;
import com.firebase.petti.petti.utils.UpdateableFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class MatchesFragment extends Fragment {

    View rootView;

    private static final String LOG_TAG = MatchesFragment.class.getSimpleName();

    GridViewAdapter mMatchesAdapter;
    FetchMatchesTask matchesTask;
    boolean bark;
    int mRadius;

    // GPSTracker class
    GPSTracker gps;
    // location
    Location location = null;

    GridView gridView;
    TextView notFoundView;
    TextView searchingView;

    private boolean attachedNearbyList;


    public MatchesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            if (getArguments().isEmpty()) {
                bark = false;
            } else {
                bark = getArguments().getBoolean("bark");
            }
        } else {
            bark = false;
        }

        Log.d(LOG_TAG, "****** bark is: " + bark + " ********");

        if (!bark){
            //TODO change to location from address
            location = null;
        } else if(canAccessLocation()){
            setUpGPS();
        } else {
            Log.d(LOG_TAG, "neither bark nor bark with params");
        }


        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mRadius = Integer.parseInt(pref.getString("matchDistance", "1"));

//        API.attachNearbyUsersListener(location, mRadius);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_matches, container, false);

        gridView = (GridView) rootView.findViewById(R.id.gridview_matches);
        notFoundView = (TextView) rootView.findViewById(R.id.no_matches_str);
        searchingView = (TextView) rootView.findViewById(R.id.searching_matches_str);

        gridView.setVisibility(View.GONE);
        notFoundView.setVisibility(View.GONE);
        searchingView.setVisibility(View.VISIBLE);

        mMatchesAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_match);

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
        if ((canAccessLocation() && bark) || !bark) {
            Log.d(LOG_TAG, "****** bark is: " + bark + " AND " + canAccessLocation() + " ********");
            gridView.setVisibility(View.GONE);
            notFoundView.setVisibility(View.GONE);
            searchingView.setVisibility(View.VISIBLE);
            TaskParams taskParams = new TaskParams(bark, location);
            matchesTask = new FetchMatchesTask(mMatchesAdapter, gridView, notFoundView, searchingView);
            matchesTask.execute(taskParams);
        } else {
            Log.d(LOG_TAG, "****** bark is: " + bark + " IN HAS NNNNNNOOOOOO PERMISSION ********");
            searchingView.setVisibility(View.GONE);
        }
        // TODO deal with cancellations
        /*if (isCancelled()){
            return;
        }*/
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
        checkAndUpdate();
    }

    /**
     *  We want to check if we have been given a permission to search location for the first time.
     * If so, we need to attach the location listener run the match task
     * for bark it will be while having location permission and for neighbour it will be only
     * having no listener before.
     *
     * Another event that we want to reattach the listener ad update is when radius preference is changed.
     */
    public void checkAndUpdate(){
        boolean needUpdate = false;
        int radius;
        String stringRadius;
        //TODO restore after there is address
//        if (!bark || canAccessLocation() && !attachedNearbyList) {
//            API.attachNearbyUsersListener(location, mRadius);
//            needUpdate = true;
//        }
        //TODO delete this if after upper if is restored
        if (bark && canAccessLocation() && !attachedNearbyList){
            attachedNearbyList = API.attachNearbyUsersListener(location, mRadius);
            needUpdate = true;
        }
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        stringRadius = pref.getString("matchDistance", "1");
        radius = Integer.parseInt(stringRadius);
        if (radius != mRadius) {
            mRadius = radius;
            API.detachNearbyUsersListener();
            attachedNearbyList = API.attachNearbyUsersListener(location, mRadius);
            needUpdate = true;
        }

        /* if one of the above is true - call upfate function */
        if (needUpdate){
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
            case R.id.settings_menu:
                Fragment myPrefrences = new MyPreferencesFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(((ViewGroup)getView().getParent()).getId(), myPrefrences)
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

    private void setUpGPS() {
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
    }

    public static class TaskParams{
        public boolean bark;
        public Location location;

        TaskParams(boolean bark, Location location) {
            this.bark = bark;
            this.location = location;
        }
    }

}
