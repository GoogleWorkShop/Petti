package com.firebase.petti.petti.utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.petti.db.API;
import com.firebase.petti.db.classes.User;
import com.firebase.petti.petti.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by barjon on 18-Dec-16.
 */

public class GridViewAdapter extends ArrayAdapter {
    private Context             mContext;
    private ArrayList<User>     mMatchesArray;
    private int                 layoutResourceId;

    public GridViewAdapter(Context context, int layoutResourceId) {
        super(context, layoutResourceId);
        this.layoutResourceId = layoutResourceId;
        mContext = context;
        mMatchesArray = new ArrayList<>();
    }

    public int getCount() {
        return mMatchesArray.size();
    }

    public String getId(int position){
        return mMatchesArray.get(position).getDog().getName();
    }

    public String getName(int position){
        return mMatchesArray.get(position).getDog().getName();
    }

    public String getDistanceTo(int position){
        /* meters / 500m/minute */
        int toM = Math.round(mMatchesArray.get(position).getTempDistanceFromMe() / 83);
        //check if more minutes then an hour - change to hour string
        if(toM >= 60){
            int minutes = toM % 60;
            toM /= 60;
            return toM + " Hours and " + minutes + " minutes";
        }
        return toM + " Minutes";
    }

    public String getImage(int position){

        String photo = mMatchesArray.get(position).getDog().getPhotoUrl();
        if (photo == null)
        {
            photo = "@drawable/anonymous_prpl";
        }
        return photo;
    }

    public boolean isFriend(int position){
        return API.isMatchedWith(mMatchesArray.get(position).getTempUid());
    }

    public User getItem(int position) {
        return mMatchesArray.get(position);
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.distance = (TextView) row.findViewById(R.id.distance);
            holder.image = (ImageView) row.findViewById(R.id.image);
            holder.friendIndicator = (ImageView) row.findViewById(R.id.friend_indicator);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        String url  = getImage(position);
        String name = getName(position);
        String distanceTo = getDistanceTo(position);

        holder.imageTitle.setText(name);
        holder.distance.setText(distanceTo);
//        Picasso.with(mContext).load(url).into(holder.image);
        ImageLoaderUtils.setImage(url, holder.image);
        if (isFriend(position)) {
            holder.friendIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.friendIndicator.setVisibility(View.GONE);
        }

        return row;
    }

    public ArrayList<User> getmMatchesArray() {
        return mMatchesArray;
    }

    public void clear() {
        if (mMatchesArray != null) {
            mMatchesArray.clear();
        }
    }

    public void refresh(ArrayList<User> result) {

        if (mMatchesArray == null) {
            mMatchesArray = new ArrayList<>();
        }
        mMatchesArray.addAll(result);
        notifyDataSetChanged();

    }

    static class ViewHolder {
        TextView imageTitle;
        TextView distance;
        ImageView image;
        ImageView friendIndicator;
    }
}
