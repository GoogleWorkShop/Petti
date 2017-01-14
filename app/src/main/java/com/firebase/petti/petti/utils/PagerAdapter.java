package com.firebase.petti.petti.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.firebase.petti.petti.MatchesFragment;


/**
 * Created by barjon on 09-Jan-17.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    MatchesFragment neighbourDogsFragment;
    MatchesFragment barkFragment;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        Bundle bundle = new Bundle();
        switch (position) {
            case 0:
                neighbourDogsFragment = new MatchesFragment();
                bundle.putBoolean("bark", false);
                neighbourDogsFragment.setArguments(bundle);
                return neighbourDogsFragment;
            case 1:
                barkFragment = new MatchesFragment();
                bundle.putBoolean("bark", true);
                barkFragment.setArguments(bundle);
                return barkFragment;
            default:
                return null;
        }
    }

    public int getItemPosition(Object item) {
        ((MatchesFragment) item).update();
        return super.getItemPosition(item);
    }

    public void update(int position) {
        switch (position) {
            case 0:
                neighbourDogsFragment.update();
                break;
            case 1:
                barkFragment.update();
                break;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
