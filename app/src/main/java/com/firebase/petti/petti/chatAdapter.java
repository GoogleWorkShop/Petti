package com.firebase.petti.petti;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.firebase.petti.db.classes.ChatMessage;

import java.util.List;

/**
 * Created by USER on 31/12/2016.
 */

public class chatAdapter extends ArrayAdapter<ChatMessage> {

    public chatAdapter (Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public chatAdapter (Context context, int resource, List<ChatMessage> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.chatrow, null);
        }

        ChatMessage p = getItem(position);

            TextView chatView = (TextView) v.findViewById(R.id.rowTextView);
            chatView.setText(p.getText());
            if (p.getFromUid() == getMyUid())
                chatView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            else
                chatView.setBackgroundColor(Color.parseColor("#009900"));





        return v;
    }
     public String getMyUid()
     {
         return "amir";
     }
}