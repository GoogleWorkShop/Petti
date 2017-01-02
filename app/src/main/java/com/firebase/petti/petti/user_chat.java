package com.firebase.petti.petti;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ImageButton;

import com.firebase.petti.db.API;
import com.firebase.petti.db.ChatApi;
import com.firebase.petti.db.classes.ChatMessage;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class user_chat extends AppCompatActivity {
    private ListView mainListView ;
    private String otherUserId;
    private ArrayAdapter<String> listAdapter ;

    // YAHAV:

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 256;

    private chatAdapter mMessageAdapter;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mMessagesDatabaseReference;
    private DatabaseReference mMessageViewedTracker;
    private EditText mMessageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        otherUserId = getIntent().getStringExtra("USER_ID");
        setContentView(R.layout.activity_chat_activity);
        mainListView = (ListView) findViewById( R.id.mainListView );

        mMessageViewedTracker = API.getCurrUserRef().child("msgTracker").child(otherUserId);
        mMessagesDatabaseReference = ChatApi.getMsgRefById(otherUserId);
        mMessageEditText = (EditText) findViewById(R.id.messageInput);
        final ImageButton mSendButton = (ImageButton) findViewById(R.id.sendButton);

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String input = mMessageEditText.getText().toString();
                ChatApi.sendChatMessage(otherUserId, input);
                mMessageEditText.setText("");
            }
        });
        List<ChatMessage> massageList = new ArrayList<>();
//        chatAdapter customAdapter = new chatAdapter(this, R.layout.chatrow, massageList);
        mMessageAdapter = new chatAdapter(this, R.layout.chatrow, massageList);

        mainListView.setAdapter(mMessageAdapter);
        attachDatabaseReadListener();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        detachDatabaseReadListener();
    }

//    public int sendMassage (String imput)
//    {
//      return 1;
//    };


        private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    mMessageViewedTracker.setValue(true);
                    mMessageAdapter.add(chatMessage);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }
}
