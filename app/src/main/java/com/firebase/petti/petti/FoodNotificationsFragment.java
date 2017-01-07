package com.firebase.petti.petti;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.petti.petti.utils.NotificationPublisher;
import com.firebase.petti.petti.utils.UtilsContract;
import com.firebase.petti.petti.utils.UtilsDBHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.support.customtabs.CustomTabsIntent.KEY_ID;


/**
 * A simple {@link Fragment} subclass.
 */
public class FoodNotificationsFragment extends Fragment {

    View rootView;
    private static final long MILLIES_IN_SECOND = 1000;
    private static final long SECONDS_IN_MINUTE = 60;
    private static final long MINUTES_IN_HOUR = 60;
    private static final long HOURS_IN_DAY = 24;
    private static final long DAY_IN_MILLIES = MILLIES_IN_SECOND*SECONDS_IN_MINUTE*MINUTES_IN_HOUR*HOURS_IN_DAY;

    SQLiteDatabase db;


    public FoodNotificationsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_food_notifications, container, false);


        Button button = (Button) rootView.findViewById(R.id.bought_it_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View vv) {

                EditText food_name = (EditText) rootView.findViewById(R.id.food_name_input);
                String food_name_str = food_name.getText().toString();
                if (food_name_str.isEmpty()) {
                    Toast.makeText(getActivity(),
                            "Give the food a name!",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                EditText amount = (EditText) rootView.findViewById(R.id.input_amount);
                String amount_str = amount.getText().toString();
                if (amount_str.isEmpty()) {
                    Toast.makeText(getActivity(),
                            "How much have you bought???",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                EditText per_meal = (EditText) rootView.findViewById(R.id.input_nutritions);
                String per_meal_str = per_meal.getText().toString();
                if (per_meal_str.isEmpty()) {
                    Toast.makeText(getActivity(),
                            "How much does your dog need per meal?\nHINT: It is written on the back",
                            Toast.LENGTH_LONG).show();
                    return;
                }


                int amount_int = Integer.parseInt(amount_str);
                int per_day_int = Integer.parseInt(per_meal_str);
                long daysUntilNotif = Math.round(( amount_int - ((amount_int*20)/100) ) / per_day_int);

                scheduleNotification(getNotification("Buy FOOOOOOOOOD"), DAY_IN_MILLIES * daysUntilNotif);

                Toast.makeText(getActivity(),
                        "A notification has been set to when food is at 30% captain!",
                        Toast.LENGTH_LONG).show();

                //save to DB
                if(db != null){
                    db.close();
                }
                db = MainActivity.m_dbHelper.getWritableDatabase();

                ContentValues values = new ContentValues();
                values.put(UtilsContract.FoodEntry.COLUMN_NAME,food_name_str);
                values.put(UtilsContract.FoodEntry.COLUMN_AMOUNT,amount_str);


                // Insert the new row, returning the primary key value of the new row
                long newRowId = db.insert(UtilsContract.FoodEntry.TABLE_NAME, null, values);
                Cursor cursor = db.query(UtilsContract.FoodEntry.TABLE_NAME, null, null, null, null, null, null);
                if(cursor.getCount() >= 4) {
                    //delete the first row, bc valuses are readed backwards so now the first row is outdated bc we only save 3 last entrys
                        cursor.moveToNext();

                        String rowId = cursor.getString(cursor.getColumnIndex(UtilsContract.FoodEntry._ID));

                        db.delete(UtilsContract.FoodEntry.TABLE_NAME, UtilsContract.FoodEntry._ID + "=?", new String[]{rowId});

                }
                updateFoodTable(food_name_str,amount_str);

            }

        });

//        //delete all table
//        while(true){
//            Cursor cursor = db.query(UtilsContract.FoodEntry.TABLE_NAME, null, null, null, null, null, null);
//            if(cursor.moveToNext()) {
//                String rowId = cursor.getString(cursor.getColumnIndex(UtilsContract.FoodEntry._ID));
//
//                db.delete(UtilsContract.FoodEntry.TABLE_NAME, UtilsContract.FoodEntry._ID + "=?", new String[]{rowId});
//            }
//        }

//            //read DB data to put in table
        Cursor cursor = db.rawQuery("SELECT * from "+ UtilsContract.FoodEntry.TABLE_NAME,null);
        cursor.moveToNext();

        while(cursor.moveToNext()) {
            updateFoodTable(cursor.getString(1), cursor.getString(2)); //name, amount
        }


//
           return rootView;
        }







    private void scheduleNotification(Notification notification, long delay) {

        Intent notificationIntent = new Intent(getActivity(), NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(getActivity());
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        return builder.build();
    }

    /*
    * This function steps down the rows in the table to allow a new line in the top for
    * a new line of bought food*/
    private void updateFoodTable(String food_name_str, String amount_str) {

        TextView first_purchase_date = (TextView) rootView.findViewById(R.id.first_row_purchase_date);
        TextView first_food_name  = (TextView) rootView.findViewById(R.id.first_row_food_name);
        TextView first_amount = (TextView) rootView.findViewById(R.id.first_row_amount);

        TextView second_purchase_date = (TextView) rootView.findViewById(R.id.second_row_purchase_date);
        TextView second_food_name  = (TextView) rootView.findViewById(R.id.second_row_food_name);
        TextView second_amount = (TextView) rootView.findViewById(R.id.second_row_amount);

        TextView third_purchase_date = (TextView) rootView.findViewById(R.id.third_row_purchase_date);
        TextView third_food_name  = (TextView) rootView.findViewById(R.id.third_row_food_name);
        TextView third_amount = (TextView) rootView.findViewById(R.id.third_row_amount);

        //switch the third row with the second
        String second_purchase_date_str = (second_purchase_date.getText() == null) ? "" : second_purchase_date.getText().toString();
        third_purchase_date.setText(second_purchase_date_str);
        String second_food_name_str = (second_food_name.getText() == null) ? "" : second_food_name.getText().toString();
        third_food_name.setText(second_food_name_str);
        String second_amount_str = (second_amount.getText() == null) ? "" :second_amount.getText().toString();
        third_amount.setText(second_amount_str);

        //switch the first row with the second
        String first_purchase_date_str = (first_purchase_date.getText() == null) ? "" : first_purchase_date.getText().toString();
        second_purchase_date.setText(first_purchase_date_str);
        String first_food_name_str = (first_food_name.getText() == null) ? "" : first_food_name.getText().toString();
        second_food_name.setText(first_food_name_str);
        String first_amount_str = (first_amount.getText() == null) ? "" :first_amount.getText().toString();
        second_amount.setText(first_amount_str);

        //change the first row
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        first_purchase_date.setText(df.format(c.getTime()));
        first_amount.setText(amount_str);
        first_food_name.setText(food_name_str);

        //put data into DB

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);

        db=MainActivity.m_dbHelper.getReadableDatabase();

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

    @Override
    public void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
