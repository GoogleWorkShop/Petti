package com.firebase.petti.petti;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.petti.db.API;
import com.firebase.petti.db.classes.User;
import com.firebase.petti.petti.utils.ImageLoaderUtils;

import java.util.ArrayList;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class MatchedDogFragment extends Fragment {

    private User user;
    private ImageView imageView;

    ArrayAdapter<String> mDogDetailsAdapter;
    ArrayAdapter<String> mDogOwnerDetailsAdapter;


    public MatchedDogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        MatchedDogActivity parentActivity = (MatchedDogActivity) getActivity();

        user = parentActivity.getUser();

        final View rootView = inflater.inflate(R.layout.fragment_matched_dog, container, false);

        TextView titleTextView = (TextView) rootView.findViewById(R.id.matched_dog_name);
        titleTextView.setText(user.getDog().getName());

        imageView = (ImageView) rootView.findViewById(R.id.matched_dog_image);

        // Get a reference to the bark button and attach a listener.
        Button startChatBtn = (Button) rootView.findViewById(R.id.start_chat_btn);
        startChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getActivity(), UserChatActivity.class);
                user.getTempUid();
                String message =    user.getTempUid();
                myIntent.putExtra("USER_ID", message);
                getActivity().startActivity(myIntent);
            }
        });

        mDogDetailsAdapter =
                new ArrayAdapter<>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_matched_dog_info, // The name of the layout ID.
                        R.id.list_item_matched_dog_textview, // The ID of the textview to populate.
                        user.getDog().retrieveDetailList()
                );

        mDogOwnerDetailsAdapter =
                new ArrayAdapter<>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_matched_dog_owner_info, // The name of the layout ID.
                        R.id.list_item_matched_dog_owner_textview, // The ID of the textview to populate.
                        user.getOwner().retrieveDetailList()
                );

        final Button showOwnerBtn = (Button) rootView.findViewById(R.id.show_owner_btn);
        final Button showDogBtn = (Button) rootView.findViewById(R.id.show_dog_btn);
        final Button addAsFriendBtn = (Button) rootView.findViewById(R.id.add_as_friend_btn);
        final ListView dogDetail = (ListView) rootView.findViewById(R.id.matched_dog_detail);
        dogDetail.setAdapter(mDogDetailsAdapter);
        final ListView ownerDetail = (ListView) rootView.findViewById(R.id.matched_dog_owner_detail);
        ownerDetail.setAdapter(mDogOwnerDetailsAdapter);


        showOwnerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOwnerBtn.setVisibility(View.GONE);
                ownerDetail.setVisibility(View.VISIBLE);
                showDogBtn.setVisibility(View.VISIBLE);
                dogDetail.setVisibility(View.GONE);
                ImageLoaderUtils.setImage(user.getOwner().getPhotoUrl(), imageView, R.drawable.dog_person_burned);
            }
        });
        showDogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOwnerBtn.setVisibility(View.VISIBLE);
                ownerDetail.setVisibility(View.GONE);
                showDogBtn.setVisibility(View.GONE);
                dogDetail.setVisibility(View.VISIBLE);
                ImageLoaderUtils.setImage(user.getDog().getPhotoUrl(), imageView, R.drawable.anonymous_prpl);
            }
        });
        addAsFriendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Boolean> msgsData = API.getCurrMsgTracker();
                String friendUid = user.getTempUid();
                if (msgsData != null && msgsData.containsKey(friendUid)){
                    friendToast(false);
                } else {
                    API.getCurrUserRef().child("msgTracker").child(user.getTempUid()).setValue(true);
                    friendToast(true);
                }
            }
        });

        return rootView;
    }

    private void friendToast(boolean isAdded){
        String toastText;
        String dogName = user.getDog().getName();
        if (isAdded){
            toastText = String.format("Added %s as friend successfully", dogName);
        } else {
            toastText = String.format("You and %s are already friends", dogName);
        }
        Toast.makeText(getActivity(), toastText, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        ImageLoaderUtils.setImage(user.getDog().getPhotoUrl(), imageView, R.drawable.anonymous_prpl);
    }

}
