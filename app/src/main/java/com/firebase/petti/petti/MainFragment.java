package com.firebase.petti.petti;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by barjon on 10-Dec-16.
 */
public class MainFragment extends Fragment {

    private static final String[] INITIAL_PERMS={
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int INITIAL_REQUEST = 1337;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (!canAccessLocation()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
        }

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
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final Animation animShiftUp = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_shift_up);
        final Animation animShiftDown = AnimationUtils.loadAnimation(getActivity(), R.anim.anim_shift_down);

        // Get a reference to the bark button and attach a listener.
        Button bark = (Button) rootView.findViewById(R.id.bark);
        bark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animShiftUp);
                if (canAccessLocation()) {
                    Toast.makeText(getActivity(), R.string.get_ready_for_a_walk,
                            Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getActivity(), NeighborDogsActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, "true");
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(),
                            "All locations and no permissions makes Johnny a dull boy",
                            Toast.LENGTH_SHORT).show();
                    requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
                }
            }
        });

        // Get a reference to the dog-neighbors button and attach a listener.
        Button find_partners = (Button) rootView.findViewById(R.id.find_walk_partners);
        find_partners.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(animShiftDown);
                if (canAccessLocation()) {
                    Intent intent = new Intent(getActivity(), NeighborDogsActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, "false");
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(),
                            "All locations and no permissions makes Johnny a dull boy",
                            Toast.LENGTH_SHORT).show();
                    requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
                }
            }
        });

        return rootView;
    }

    private boolean canAccessLocation() {
        return(hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED== ContextCompat.checkSelfPermission(getContext(),perm));
    }

    // Callback with the request from calling requestPermissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original GET_LOCATION request
        if (requestCode == INITIAL_REQUEST) {
            if (grantResults.length == 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(getActivity(),
                        "We can not play with you without your permission...",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
