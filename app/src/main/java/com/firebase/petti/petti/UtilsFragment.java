package com.firebase.petti.petti;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A placeholder fragment containing a simple view.
 */
public class UtilsFragment extends Fragment {

    private ArrayAdapter<String> mUtilsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_utils, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * In here we updated the number and type of different utility buttons
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

//         Class<?>[] differentButtonsClasses = {}

         String[] differentButtons = {
                    "Food Notifications",
                    "Vaccinetion Card",
                    "Find Near Dog Parks",
                    "Find Near Veterinarians",
                    "Find Near Pet Stores",
                    "Such",
                    "And Such",
                    "Other Such",
                    "Not As Such"
        };

        ArrayList<String> utilsArrays = new ArrayList<>(Arrays.asList(differentButtons));

        mUtilsAdapter = new ArrayAdapter<>(this.getActivity(),
                R.layout.list_item_utils,
                R.id.list_item_utils_textview,
                utilsArrays);

        View rootView = inflater.inflate(R.layout.fragment_utils, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_utils);
        listView.setAdapter(mUtilsAdapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                String utility = mUtilsAdapter.getItem(position);
//                switch (utility){
//                    case Str:
//                }
//                Intent intent = new Intent(getActivity(), .class)
//                        .putExtra(Intent.EXTRA_TEXT, forecast);
//                startActivity(intent);
//            }
//        });

        return rootView;
    }
}
