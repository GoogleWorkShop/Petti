package com.firebase.petti.petti;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.firebase.petti.petti.utils.GridViewAdapter;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MatchesFragment extends Fragment {

    GridViewAdapter mMatchesAdapter;

    public MatchesFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMatchesAdapter = new GridViewAdapter(getActivity(), R.layout.grid_item_match);

        View rootView = inflater.inflate(R.layout.fragment_matches, container, false);

        GridView gridView = (GridView) rootView.findViewById(R.id.gridview_matches);
        gridView.setAdapter(mMatchesAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String id = mMatchesAdapter.getId(position);

                String dogName = mMatchesAdapter.getName(position);
                Toast.makeText(getActivity(), dogName, Toast.LENGTH_SHORT).show();

                String image = mMatchesAdapter.getImage(position);

                Intent intent = new Intent(getActivity(), MatchedDogActivity.class);
                intent.putExtra("id", id);
                intent.putExtra("dogName", dogName);
                intent.putExtra("image", image);

                startActivity(intent);
            }
        });

        return rootView;
    }

    private void updateMatches() {
        FetchMatchesTask matchesTask = new FetchMatchesTask();
        matchesTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMatches();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_neighbor_dogs, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Fragment myPrefrences = new MyPreferencesFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction().replace(((ViewGroup)getView().getParent()).getId(), myPrefrences)
                        .addToBackStack( "tag" ).commit();
                return true;
            case android.R.id.home:
                if(getActivity().getClass() == BarkActivity.class) {
                    getFragmentManager().popBackStack();
                    return true;
                }
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class FetchMatchesTask extends AsyncTask<Void, Void, ArrayList<String[]>> {

        @Override
        protected ArrayList<String[]> doInBackground(Void... voids) {
            ArrayList<String[]> mMatchesArray = new ArrayList();

            //TODO DELETE FROM HERE

            String[] a = {"a", "a",
                    "http://pngimg.com/upload/dog_PNG2416.png"};
            mMatchesArray.add(a);

            String[] b = {"b", "b",
                    "https://s-media-cache-ak0.pinimg.com/564x/fe/a6/dd/fea6dd493a862241952066fea699feaa.jpg"};
            mMatchesArray.add(b);

            String[] c = {"c", "c",
                    "https://s-media-cache-ak0.pinimg.com/564x/aa/21/8e/aa218e0d81d51178ab68f65ef759eb11.jpg"};
            mMatchesArray.add(c);

            String[] d = {"d", "d",
                    "https://s-media-cache-ak0.pinimg.com/originals/d9/1b/ee/d91bee03625f15e36020de6d9969a30b.png"};
            mMatchesArray.add(d);

            String[] e = {"e", "e",
                    "http://pngimg.com/upload/dog_PNG2422.png"};
            mMatchesArray.add(e);

            String[] f = {"f", "f",
                    "http://pngimg.com/upload/dog_PNG149.png"};
            mMatchesArray.add(f);

            String[] g = {"g", "g",
                    "http://pngimg.com/upload/dog_PNG192.png"};
            mMatchesArray.add(g);

            //TODO TO HERE

            return mMatchesArray;
        }


        @Override
        protected void onPostExecute(ArrayList<String[]> result) {
            if (result != null) {
                mMatchesAdapter.clear();
//                for(int i = 0; i < result.size(); i++) {
                    mMatchesAdapter.refresh(result);
//                }
                // New data is back from the server.  Hooray!
            }
        }
    }
}
