package com.firebase.petti.petti;


import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
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
import android.provider.CalendarContract.Reminders;

import com.firebase.petti.petti.utils.NotificationPublisher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import static android.Manifest.permission.READ_CALENDAR;
import static android.Manifest.permission.WRITE_CALENDAR;

/**
 * A placeholder fragment containing a simple view.
 */
public class VaccinationCardFragment extends Fragment {

    private static final long MILLIE_IN_SECOND = 1000;
    private static final long SECONDS_IN_MINUTE = 60;
    private static final long MINUTES_IN_HOUR = 60;
    private static final long HOURS_IN_DAY = 24;
    private static final long DAYS_IN_MONTH = 30;
    private static final long DAY_IN_MILLIE = MILLIE_IN_SECOND * SECONDS_IN_MINUTE * MINUTES_IN_HOUR * HOURS_IN_DAY;
    private static final String[] VACCINATIONS = {"Rabies Vaccination", "Distemper Vaccination",
                                                    "Spirocerca Lupi Vaccination", "Deworming Treatment"};

    private static final Map<String, Integer> myMap;
    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;

    /* A map that matches between the treatment and the interval in months */
    static {
        myMap = new HashMap<>();
        myMap.put(VACCINATIONS[0], 12);
        myMap.put(VACCINATIONS[1], 12);
        myMap.put(VACCINATIONS[2], 2);
        myMap.put(VACCINATIONS[3], 6);
    }

    /* this are for the calendar permission request */
    private static final String[] INITIAL_PERMS={
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR
    };
    private static final int INITIAL_REQUEST = 1337;

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
                        new ArrayList<>(Arrays.asList(VACCINATIONS)));

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

                        if (!checkPermission()) {
                            createVacNotif(POSITION, VAC);
                            return;
                        }
                        Uri calendars = Uri.parse(("content://com.android.calendar/calendars"));
                        Cursor managedCursor = getContext().getContentResolver().query(calendars, EVENT_PROJECTION, null, null, null);
                        if (managedCursor.getCount() > 0) {
                            setCalendarEvent(managedCursor, VACCINATIONS[POSITION]);
                        } else {
                            createVacNotif(POSITION, VAC);
                        }


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

    private void setCalendarEvent(Cursor managedCursor, String vaccinetion) {
        if(!checkPermission()){
            return;
        }
        Calendar calendar = Calendar.getInstance();
        managedCursor.moveToNext();
        String displayName = managedCursor.getString(PROJECTION_DISPLAY_NAME_INDEX);
        ContentResolver cr = getActivity().getContentResolver();


        calendar.add(Calendar.MONTH, myMap.get(vaccinetion));
        int idColumn = managedCursor.getColumnIndex("_id");
        String calId = managedCursor.getString(idColumn);
        ContentValues event = new ContentValues();
        event.put("calendar_id", calId);
        event.put("title", vaccinetion);
        event.put("description", "it has been " + myMap.get(vaccinetion) +
                                    " months since " + vaccinetion + " has been done!");
        event.put("dtstart", calendar.getTimeInMillis());
        event.put("dtend", calendar.getTimeInMillis() + 1000*60*60);
        event.put("allDay", 0);
        event.put("hasAlarm", 1);
        event.put("eventTimezone", TimeZone.getDefault().getID());

        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, event);

        int id = Integer.parseInt(uri.getLastPathSegment());

        ContentValues reminders = new ContentValues();
        reminders.put(Reminders.EVENT_ID, id);
        reminders.put(Reminders.METHOD, Reminders.METHOD_ALERT);
        // we want to notify the user a week before the event
        reminders.put(Reminders.MINUTES, 7*24*60);

        cr.insert(Reminders.CONTENT_URI, reminders);

        Toast.makeText(getActivity(),
                "An event has beed adding to your " + displayName + " calendar",
                Toast.LENGTH_LONG).show();
    }

    private boolean checkPermission() {
        return PermissionChecker.checkSelfPermission(getContext(), READ_CALENDAR) == PermissionChecker.PERMISSION_GRANTED
                && PermissionChecker.checkSelfPermission(getContext(), WRITE_CALENDAR) == PermissionChecker.PERMISSION_GRANTED;
    }

    private void createVacNotif(int POSITION, String VAC) {
        long daysUntilNotif = myMap.get(VACCINATIONS[POSITION]) * DAY_IN_MILLIE * DAYS_IN_MONTH - DAY_IN_MILLIE * 7;
        if (VAC.equals("Check")) {
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
        if(!checkPermission() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity().requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.settings_menu) {
            Fragment myPrefrences = new MyPreferencesFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_container, myPrefrences)
                    .addToBackStack("pref").commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
