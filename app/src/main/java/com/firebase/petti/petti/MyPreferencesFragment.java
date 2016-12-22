package com.firebase.petti.petti;

/**
 * Created by barjon on 22-Dec-16.
 */
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;
import android.widget.Toast;


public class MyPreferencesFragment extends PreferenceFragmentCompat
        implements OnSharedPreferenceChangeListener {

    public static final String DEFAULT_DISTANCE_S = "3";
    public static final int DEFAULT_DISTANCE_INT = 3;
    public static int last_distance_int = DEFAULT_DISTANCE_INT;

    public static String getNotifications;
    public static String matchDistance;

    private android.support.v7.preference.CheckBoxPreference mGetNotifications;
    private android.support.v7.preference.EditTextPreference mMatchDistance;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setHasOptionsMenu(true);
        setPreferencesFromResource(R.xml.preferences, s);

        getNotifications = "getNotifications";
        matchDistance = "matchDistance";

        mGetNotifications = (android.support.v7.preference.CheckBoxPreference) findPreference(getNotifications);
        mMatchDistance = (android.support.v7.preference.EditTextPreference) findPreference(matchDistance);

    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        // set the summaries from saved values
        onSharedPreferenceChanged(prefs, getNotifications);
        onSharedPreferenceChanged(prefs, matchDistance);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

        if (getNotifications.equals(key)) {
            boolean b = prefs.getBoolean(key, false);
            mGetNotifications.setSummary(b ? "Enabled" : "Disabled");
        } else if (matchDistance.equals(key)) {
            String i = prefs.getString(key, DEFAULT_DISTANCE_S);
            try {
                int ii = Integer.parseInt(i);
                if (ii < 0 || ii > 20){
                    Toast.makeText(getActivity(), R.string.invalid_distance,
                            Toast.LENGTH_SHORT).show();
                    ii = last_distance_int;
                    i = Integer.toString(ii);
                    prefs.edit().putString(key, i).apply();
                } else {
                    last_distance_int = ii;
                }
                mMatchDistance.setSummary("Match finding distance is: " + i);
            } catch (NumberFormatException e){
                Toast.makeText(getActivity(), R.string.invalid_distance,
                        Toast.LENGTH_SHORT).show();
                prefs.edit().putString(key, Integer.toString(last_distance_int)).apply();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Fragment fragment = null;
            Class fragmentClass;
            int container;
            if(getActivity().getClass() == BarkActivity.class) {
                fragmentClass = BarkFragment.class;
                container = R.id.bark_container;
            } else if(getActivity().getClass() == NeighborDogsActivity.class) {
                fragmentClass = MatchesFragment.class;
                container = R.id.neighbor_container;
            } else {
                return super.onOptionsItemSelected(item);
            }

            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(container, fragment).commit();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
