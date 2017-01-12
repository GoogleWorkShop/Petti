package com.firebase.petti.petti;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.petti.petti.utils.NotificationPublisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class VaccinationCardFragment extends Fragment {

    private static final long MILLIES_IN_SECOND = 1000;
    private static final long SECONDS_IN_MINUTE = 60;
    private static final long MINUTES_IN_HOUR = 60;
    private static final long HOURS_IN_DAY = 24;
    private static final long DAYS_IN_MONTH = 30;
    private static final long DAY_IN_MILLIES = MILLIES_IN_SECOND * SECONDS_IN_MINUTE * MINUTES_IN_HOUR * HOURS_IN_DAY;
    private static final String[] VACCINETIONS = {"rabies", "Distemper", "Spirocerca lupi", "Deworming", "Check"};

    private static final Map<String, Long> myMap;

    /* A map that matches between the treatment and the interval in months */
    static {
        myMap = new HashMap<>();
        myMap.put(VACCINETIONS[0], 12L);
        myMap.put(VACCINETIONS[1], 12L);
        myMap.put(VACCINETIONS[2], 2L);
        myMap.put(VACCINETIONS[3], 6L);
        myMap.put(VACCINETIONS[4], 1L);
    }

    private ArrayAdapter<String> mVaccinationAdapter;

    public VaccinationCardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mVaccinationAdapter =
                new ArrayAdapter<>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_vaccinetion_textview, // The name of the layout ID.
                        R.id.list_item_vaccinetion_textview, // The ID of the textview to populate.
                        new ArrayList<>(Arrays.asList(VACCINETIONS)));

        View rootView = inflater.inflate(R.layout.fragment_vaccinetion_card, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_vaccinetions);
        listView.setAdapter(mVaccinationAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int position, long l) {
                final String VAC = mVaccinationAdapter.getItem(position);
                final int POSITION = position;

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.app_name);
                builder.setMessage("Has " + VAC + " had been done?");
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        long daysUntilNotif = myMap.get(VACCINETIONS[POSITION]) * DAY_IN_MILLIES * DAYS_IN_MONTH - DAY_IN_MILLIES * 7;
                        if (VAC.equals("Check")){
                            daysUntilNotif = 5000;
                        }
                        NotificationPublisher.scheduleNotification(
                                "It's time for " + VAC + " again next week",
                                daysUntilNotif,
                                getActivity());
                        Toast.makeText(getActivity(),
                                "A notification has been set for the a week before next: " + VAC,
                                Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });


        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Fragment myPrefrences = new MyPreferencesFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_container, myPrefrences)
                    .addToBackStack("tag").commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
