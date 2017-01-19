package com.firebase.petti.petti;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.petti.db.API;


public class MyPreferencesFragment extends PreferenceFragmentCompat
        implements OnSharedPreferenceChangeListener {

    SharedPreferences sharedPreferences;

    public static final String DEFAULT_DISTANCE_S = "3";
    public static final int DEFAULT_DISTANCE_INT = 3;
    public static int last_distance_int = DEFAULT_DISTANCE_INT;

    public static String visible;
    public static String matchDistance;

    private android.support.v7.preference.CheckBoxPreference mGetNotifications;
    private android.support.v7.preference.EditTextPreference mMatchDistance;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setHasOptionsMenu(true);
        setPreferencesFromResource(R.xml.preferences, s);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        visible = "visible";
        matchDistance = "matchDistance";

        mGetNotifications = (android.support.v7.preference.CheckBoxPreference) findPreference(visible);
        mMatchDistance = (android.support.v7.preference.EditTextPreference) findPreference(matchDistance);

    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        // set the summaries from saved values
        onSharedPreferenceChanged(prefs, visible);
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

        if (visible.equals(key)) {
            boolean b = prefs.getBoolean(key, false);
            String summary = getString(R.string.visible_pref_sum) + (b ? "Enabled" : "Disabled");
            mGetNotifications.setSummary(summary);
            API.getCurrUserRef().child("enabled").setValue(b);
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
        return super.onOptionsItemSelected(item);
    }
}
