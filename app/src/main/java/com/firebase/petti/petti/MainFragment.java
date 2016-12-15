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
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by barjon on 10-Dec-16.
 */
public class MainFragment extends Fragment {

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
//        int id = item.getItemId();
//        if (id == R.id.action_refresh) {
//            updateWeather();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the bark button and attach a listener.
        Button bark = (Button) rootView.findViewById(R.id.bark);
        bark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), R.string.get_ready_for_a_walk,
                        Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), BarkActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, "Get This Bizzzz");
                startActivity(intent);
            }
        });

        // Get a reference to the dog-neighbors button and attach a listener.
        Button find_partners = (Button) rootView.findViewById(R.id.find_walk_partners);
        find_partners.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NeighborDogsActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, "Get This Bizzzz");
                startActivity(intent);
            }
        });

        // Get a reference to the utils button and attach a listener.
        Button utils = (Button) rootView.findViewById(R.id.utils);
        utils.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UtilsActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, "Get This Bizzzz");
                startActivity(intent);
            }
        });

        return rootView;
    }
}
