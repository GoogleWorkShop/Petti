package com.firebase.petti.petti;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MatchedDogFragment extends Fragment {

    private   String mDogId;
    private   String dogName;
    private   ImageView imageView;
    private   String imageUrl;


    public MatchedDogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        MatchedDogActivity parentActivity = (MatchedDogActivity) getActivity();

        dogName = parentActivity.getDogName();
        imageUrl = parentActivity.getImageUrl();

        View rootView = inflater.inflate(R.layout.fragment_matched_dog, container, false);

        TextView titleTextView = (TextView) rootView.findViewById(R.id.matched_dog_detail);
        titleTextView.setText(dogName);

        imageView = (ImageView) rootView.findViewById(R.id.matched_dog_image);

        // Get a reference to the bark button and attach a listener.
        Button startChatBtn = (Button) rootView.findViewById(R.id.start_chat_btn);
//        startChatBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Fragment matchesFragment = new ();
//
//                FragmentManager fragmentManager = getFragmentManager();
//
//                fragmentManager.beginTransaction().replace(R.id.bark_container, matchesFragment)
//                        .addToBackStack( "tag" ).commit();
//            }
//        });

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    private void updateDogDate() {
        FetchAnotherDogTask movieTask = new FetchAnotherDogTask(imageView);
        movieTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateDogDate();
    }


    public class FetchAnotherDogTask extends AsyncTask<String, Void, ArrayList<String>> {

        private ImageView imageView;

        public FetchAnotherDogTask(ImageView imageView){
            this.imageView = imageView;
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {

            //TODO get dog data from firebase by mDogId var

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            Picasso.with(getActivity()).load(imageUrl).into(imageView);
            // New data is back from the server.  Hooray!
        }

    }

}
