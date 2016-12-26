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

import com.firebase.petti.petti.utils.GPSTracker;
import com.firebase.petti.petti.utils.GridViewAdapter;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MatchesFragment extends Fragment {

    View rootView;

    GridViewAdapter mMatchesAdapter;
    boolean bark;
    private final static String DEFAULT_PREFERENCE_STRING = "com.firebase.petti.petti_preferences";

    // GPSTracker class
    GPSTracker gps;
    Location location; // location
    private static final String[] INITIAL_PERMS={
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST = 1337;


    public MatchesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (!canAccessLocation()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
        }
        // create class object
        gps = new GPSTracker(getActivity());

        // check if GPS enabled
        if (gps.canGetLocation()) {

            location = gps.getLocation();
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            // \n is for new line
            Toast.makeText(getActivity(),
                    "Your Location is - \nLat: " + latitude + "\nLong: " + longitude,
                    Toast.LENGTH_LONG).show();
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMatchesAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_match);
        bark = getArguments().getBoolean("bark");
        Toast.makeText(getActivity(),
                "Bark is: " + bark,
                Toast.LENGTH_LONG).show();

        rootView = inflater.inflate(R.layout.fragment_matches, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_matches);
        gridView.setAdapter(mMatchesAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String id = mMatchesAdapter.getId(position);

                String dogName = mMatchesAdapter.getName(position);
                Toast.makeText(getActivity(), dogName, Toast.LENGTH_SHORT).show();

                String image = mMatchesAdapter.getImage(position);

                Intent intent = new Intent(getActivity(), MatchedDogActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("dogName", dogName);
                intent.putExtra("image", image);

                startActivity(intent);
            }
        });

        return rootView;
    }

    private void updateMatches() {
        FetchMatchesTask matchesTask = new FetchMatchesTask();
        matchesTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMatches();
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
            case android.R.id.home:
                if(getActivity().getClass() == BarkActivity.class) {
                    getFragmentManager().popBackStack();
                    return true;
                }
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean canAccessLocation() {
        return(hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED==getActivity().checkSelfPermission(perm));
    }

    private class FetchMatchesTask extends AsyncTask<Void, Void, ArrayList<String[]>> {

        @Override
        protected ArrayList<String[]> doInBackground(Void... voids) {
            ArrayList<String[]> mMatchesArray = new ArrayList();
            SharedPreferences pref = getActivity().getSharedPreferences(DEFAULT_PREFERENCE_STRING, 0);
            String s = pref.getString("matchDistance", "0");
            int radius = Integer.parseInt(s);
            if(radius < 0 || radius > 20){

            }



            //TODO DELETE FROM HERE

//            String[] a = {"a", "a",
//                    "http://pngimg.com/upload/dog_PNG2416.png"};
//            mMatchesArray.add(a);
//
//            String[] b = {"b", "b",
//                    "https://s-media-cache-ak0.pinimg.com/564x/fe/a6/dd/fea6dd493a862241952066fea699feaa.jpg"};
//            mMatchesArray.add(b);
//
//            String[] c = {"c", "c",
//                    "https://s-media-cache-ak0.pinimg.com/564x/aa/21/8e/aa218e0d81d51178ab68f65ef759eb11.jpg"};
//            mMatchesArray.add(c);
//
//            String[] d = {"d", "d",
//                    "https://s-media-cache-ak0.pinimg.com/originals/d9/1b/ee/d91bee03625f15e36020de6d9969a30b.png"};
//            mMatchesArray.add(d);
//
//            String[] e = {"e", "e",
//                    "http://pngimg.com/upload/dog_PNG2422.png"};
//            mMatchesArray.add(e);
//
//            String[] f = {"f", "f",
//                    "http://pngimg.com/upload/dog_PNG149.png"};
//            mMatchesArray.add(f);
//
//            String[] g = {"g", "g",
//                    "http://pngimg.com/upload/dog_PNG192.png"};
//            mMatchesArray.add(g);

            //TODO TO HERE

            return mMatchesArray;
        }


        @Override
        protected void onPostExecute(ArrayList<String[]> result) {

            GridView gridView = (GridView) rootView.findViewById(R.id.gridview_matches);
            TextView textView = (TextView) rootView.findViewById(R.id.no_matches_str);

            if (result != null) {
                mMatchesAdapter.clear();
                mMatchesAdapter.refresh(result);
                // New data is back from the server.  Hooray!
            }
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
