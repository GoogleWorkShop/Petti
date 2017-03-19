package com.firebase.petti.petti.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.petti.db.API;
import com.firebase.petti.db.classes.User;
import com.firebase.petti.db.classes.User.Dog;
import com.firebase.petti.petti.MatchedDogActivity;
import com.firebase.petti.petti.R;
import com.firebase.petti.petti.UserChatActivity;

import java.util.List;

import static java.lang.Thread.sleep;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder> {

    Context context;
    public List<User> mFriends;

    public class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cv;
        TextView personName;
        ImageView personPhoto;
        public Button delete_bt;
         public Button start_chat_bt;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            personName = (TextView) itemView.findViewById(R.id.matched_user_dogs_name);
            personPhoto = (ImageView) itemView.findViewById(R.id.person_photo);
            delete_bt = (Button) itemView.findViewById(R.id.delete_bt);
            start_chat_bt = (Button) itemView.findViewById(R.id.chat_bt);
            cv.setOnClickListener(this);
            start_chat_bt.setOnClickListener(this);
            personPhoto.setOnClickListener(this);
            delete_bt.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            //needs to be final for the animation
            final int position = getAdapterPosition();
            final Button b = (Button)v;
            final String message = RVAdapter.this.mFriends.get(position).getTempUid();
            final Animation myAnim = AnimationUtils.loadAnimation(context, R.anim.bounce);
            MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
            myAnim.setInterpolator(interpolator);
            myAnim.setAnimationListener(new Animation.AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    int viewId = v.getId();
                    if (viewId == delete_bt.getId()) {
                        // delete the user from the friends list
                        API.getCurrUserRef().child("msgTracker").child(message).removeValue();
                        mFriends.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, mFriends.size());
                    } else if(viewId == personPhoto.getId()){
                        // enter the user card
                        User otherUser =  mFriends.get(position);
                        Intent intent = new Intent(context, MatchedDogActivity.class);
                        intent.putExtra("user", otherUser);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }  else if (viewId == start_chat_bt.getId() || viewId == cv.getId()) {
                        // go into chat activity with this user
                        String currUser = mFriends.get(position).getDog().getName();
                        Intent myIntent = new Intent(context, UserChatActivity.class);
                        myIntent.putExtra("USER_NAME", currUser);
                        myIntent.putExtra("USER_ID", message);
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(myIntent);

                    }

                }
            });
            b.startAnimation(myAnim);


        }

    }

    public RVAdapter(List<User> mFriends, Context con) {
        context = con;
        this.mFriends = mFriends;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.friend_card, viewGroup, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        User currUser = mFriends.get(i);
        Dog currDogData = currUser.getDog();
        personViewHolder.personName.setText(currDogData.getName());
        ImageView petImage = personViewHolder.personPhoto;
        if (!API.getCurrMsgTracker().get(currUser.getTempUid())) {

            Animation pulse = AnimationUtils.loadAnimation(context, R.anim.pulse);
            personViewHolder.start_chat_bt.setAnimation(pulse);
        }
        String dogPhotoUrl = currDogData.getPhotoUrl();
        ImageLoaderUtils.setImage(dogPhotoUrl, petImage, R.drawable.anonymous_grn);
    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }
}
