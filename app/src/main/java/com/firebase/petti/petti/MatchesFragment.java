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
import com.firebase.petti.db.LocationsApi;
import com.firebase.petti.db.classes.User;
import com.firebase.petti.petti.utils.GPSTracker;
import com.firebase.petti.petti.utils.GridViewAdapter;
import com.firebase.petti.petti.utils.FetchMatchesTask;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class MatchesFragment extends Fragment {

    View rootView;

    private static final String LOG_TAG = MatchesFragment.class.getSimpleName();

    GridViewAdapter mMatchesAdapter;
    FetchMatchesTask matchesTask;
    boolean bark;
    boolean visible;
    int mRadius;

    // GPSTracker class
    GPSTracker gps;
    // location
    Location location = null;

    GridView gridView;
    TextView notFoundView;
    PulsatorLayout searchingView;
    TextView visibleView;

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

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mRadius = Integer.parseInt(pref.getString("matchDistance", "1"));

        if(checkVisible()) {
            setUpLocation();

            if (location == null && bark) {
                Toast.makeText(getActivity(),
                        "Could not get your location. Does your GPS on?",
                        Toast.LENGTH_LONG).show();
            } else {
                LocationsApi.attachNearbyUsersListener(location, mRadius, bark);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_matches, container, false);

        gridView = (GridView) rootView.findViewById(R.id.gridview_matches);
        notFoundView = (TextView) rootView.findViewById(R.id.no_matches_str);
        searchingView = (PulsatorLayout) rootView.findViewById(R.id.searching_matches_view);
        visibleView = (TextView) rootView.findViewById(R.id.non_visible_state_str);

        gridView.setVisibility(View.GONE);
        notFoundView.setVisibility(View.GONE);
        if (checkVisible()){
            searchingView.setVisibility(View.VISIBLE);
            searchingView.start();
            visibleView.setVisibility(View.GONE);
        } else {
            searchingView.setVisibility(View.GONE);

            searchingView.stop();
            visibleView.setVisibility(View.VISIBLE);
        }

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
            setUpLocation();
            if(location == null && bark){
                Toast.makeText(getActivity(),
                        "Could not get your location. Does your GPS on or address set?",
                        Toast.LENGTH_LONG).show();
                return;
            }
            detachLocationsListener();
            attachedNearbyList = LocationsApi.attachNearbyUsersListener(location, mRadius, bark);
            gridView.setVisibility(View.GONE);
            notFoundView.setVisibility(View.GONE);
            visibleView.setVisibility(View.GONE);
            searchingView.setVisibility(View.VISIBLE);
            searchingView.start();
            TaskParams taskParams = new TaskParams(bark, location);
            matchesTask = new FetchMatchesTask(mMatchesAdapter, gridView, notFoundView, searchingView);
            matchesTask.execute(taskParams);
        } else {
            searchingView.setVisibility(View.GONE);
            searchingView.stop();
            visibleView.setVisibility(View.GONE);
            notFoundView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        // TODO deal with cancellations
        if(visible) {
            matchesTask.cancel(true);
            detachLocationsListener();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkVisible()) {
            checkAndUpdate();
        } else if (!bark){
            TaskParams taskParams = new TaskParams(bark, location);
            matchesTask = new FetchMatchesTask(mMatchesAdapter, gridView, notFoundView, searchingView);
            matchesTask.execute(taskParams);
        } else {
            gridView.setVisibility(View.GONE);
            notFoundView.setVisibility(View.GONE);
            searchingView.setVisibility(View.GONE);
            searchingView.stop();
            visibleView.setVisibility(View.VISIBLE);
            visible = false;
        }
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

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int radius = Integer.parseInt(pref.getString("matchDistance", "1"));

        /* if one of the above is true - call update function */
        if ((bark && canAccessLocation()) ||
                !attachedNearbyList ||
                radius != mRadius ||
                (!visible && checkVisible())) {
            mRadius = radius;
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
                Fragment myPreferences = new MyPreferencesFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(((ViewGroup)getView().getParent()).getId(), myPreferences)
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

    private void setUpLocation() {
        if (bark) {
            gps = new GPSTracker(getActivity());

            // check if GPS enabled
            if (gps.canGetLocation()) {
                location = gps.getLocation();
            } else {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gps.showSettingsAlert();
            }
        } else {
            // static location doesn't need a Location object
            location = null;
        }
    }

    private boolean checkVisible(){
        return PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("visible", true);
    }

    private void detachLocationsListener(){
        LocationsApi.detachNearbyUsersListener(bark);
        attachedNearbyList = false;
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
