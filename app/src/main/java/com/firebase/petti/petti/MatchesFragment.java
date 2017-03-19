package com.firebase.petti.petti;


import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.petti.db.LocationsApi;
import com.firebase.petti.db.classes.User;
import com.firebase.petti.petti.utils.FetchMatchesTask;
import com.firebase.petti.petti.utils.GPSTracker;
import com.firebase.petti.petti.utils.GridViewAdapter;
import com.firebase.petti.petti.utils.MyBounceInterpolator;

import pl.bclogic.pulsator4droid.library.PulsatorLayout;

import static com.facebook.FacebookSdk.getApplicationContext;


/**
 * The main matches screen. This fragment is launched upon both "Neighbour Dogs" and "Who wants to
 * walk now" sidebar buttons. It will display a grid of all the user that match the current user
 * preferences (i.e radius and location). Click on a matched item will lead to the corresponding
 * user details and elaborated in matchedDogFragment doc.
 * if the fragment was launched via the "who wants to walk now" option, a flag is checked and
 * and an option to see the matches in a map is available.
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
    Button goToMapBtn;

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
        goToMapBtn = (Button)rootView.findViewById(R.id.go_to_map_btn);

        if(bark){
            goToMapBtn.setVisibility(View.VISIBLE);
        } else {
            goToMapBtn.setVisibility(View.GONE);
        }
        MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.sound);
        mp.start();
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


        goToMapBtn.setEnabled(true);
        goToMapBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        return true;
                    case MotionEvent.ACTION_UP:
                        final Animation myAnim = AnimationUtils.loadAnimation(getContext(), R.anim.bounce);
                        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
                        myAnim.setInterpolator(interpolator);
                        myAnim.setAnimationListener(new Animation.AnimationListener(){

                            @Override
                            public void onAnimationStart(Animation animation){}

                            @Override
                            public void onAnimationRepeat(Animation animation){}

                            @Override
                            public void onAnimationEnd(Animation animation){
                                Intent mapsIntent = new Intent(getActivity(), MapsActivity.class);
                                mapsIntent.putExtra("neighbours", true);
                                startActivity(mapsIntent);
                            }
                        });
                        goToMapBtn.startAnimation(myAnim);

                        return true;
                }
                return false;
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
            matchesTask = new FetchMatchesTask(bark, rootView, mMatchesAdapter);
            matchesTask.execute(location);
        } else {
            searchingView.setVisibility(View.GONE);
            searchingView.stop();
            visibleView.setVisibility(View.GONE);
            notFoundView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        if(visible) {
            // cancel the async
            matchesTask.cancel(true);
            detachLocationsListener();
        }
        goToMapBtn.setEnabled(false);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkVisible()) {
            checkAndUpdate();
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

        TaskParams(boolean bark, @Nullable Location location) {
            this.bark = bark;
            this.location = location;
        }
    }
}
