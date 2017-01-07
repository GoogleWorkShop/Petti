package com.firebase.petti.petti.utils;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.petti.db.classes.User;
import com.firebase.petti.db.classes.User.Dog;
import com.firebase.petti.petti.R;
import com.firebase.petti.petti.UserChatActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.PersonViewHolder> {
    Context context;
    public  class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cv;
        TextView personName;
        TextView personAge;
        ImageView personPhoto;
        public Button delete_bt;
        public Button start_chat_bt;

        PersonViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            personName = (TextView)itemView.findViewById(R.id.person_name);
            personAge = (TextView)itemView.findViewById(R.id.person_age);
            personPhoto = (ImageView)itemView.findViewById(R.id.person_photo);
            delete_bt = (Button) itemView.findViewById(R.id.delete_bt);
            start_chat_bt = (Button) itemView.findViewById(R.id.chat_bt);
            delete_bt.setOnClickListener(this);
            start_chat_bt.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {

            if (v.getId() == delete_bt.getId()){

                Toast.makeText(v.getContext(), "ITEM PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            } else {
                Intent myIntent = new Intent(context, UserChatActivity.class);
                String message = RVAdapter.this.mFriends.get(getAdapterPosition()).getTempUid();
                myIntent.putExtra("USER_ID", message);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(myIntent);
                Toast.makeText(v.getContext(), "ROW PRESSED = " + String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
            }
        }

    }

    public List<User> mFriends;

    public RVAdapter(List<User> mFriends, Context con){
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
        Dog currDogData = mFriends.get(i).getDog();
        personViewHolder.personName.setText(currDogData.getName());
        personViewHolder.personAge.setText(currDogData.getAge());
        ImageView petImage = personViewHolder.personPhoto;
        Picasso.with(petImage.getContext()).load(currDogData.getPhotoUrl()).into(petImage);

    }

    @Override
    public int getItemCount() {
        return mFriends.size();
    }
}
