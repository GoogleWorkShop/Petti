package com.firebase.petti.petti;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.petti.db.ChatApi;
import com.firebase.petti.db.NewMessagesHandler;
import com.firebase.petti.db.classes.ChatMessage;
import com.firebase.petti.petti.utils.SendMailTask;
import com.firebase.petti.petti.utils.chatAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;


/**
 * This activity is responsible for a single conversation chat between 2 Petti users.
 * Each message is saved in the firebase database so each conversation is both real-time
 * (the messages are automatically shown on both ends without the need of refresh) as well as
 * history-preserved: the full conversation is shown every time.
 */
public class UserChatActivity extends AppCompatActivity {
    private ListView mainListView;
    private String otherUserId;
    private ArrayAdapter<String> listAdapter;

    public static final int DEFAULT_MSG_LENGTH_LIMIT = 256;
    private static final String PETTI_COMPLAINTS_MAIL = "petticomplaints@gmail.com";

    private chatAdapter mMessageAdapter;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mMessagesDatabaseReference;
    private DatabaseReference mMessageViewedTracker;
    private EditText mMessageEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_chat_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        otherUserId = getIntent().getStringExtra("USER_ID");
        mainListView = (ListView) findViewById(R.id.mainListView);

        setTitle(getIntent().getStringExtra("USER_NAME"));

        mMessageViewedTracker = NewMessagesHandler.getCurrMsgTracker().child(otherUserId);
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
        mMessageAdapter = new chatAdapter(this, massageList);
        mainListView.setAdapter(mMessageAdapter);
        mMessageAdapter.notifyDataSetChanged();
        attachDatabaseReadListener();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detachDatabaseReadListener();
    }

    private void attachDatabaseReadListener() {
        NewMessagesHandler.setCurrentlyChatting(otherUserId);
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
                    mMessageViewedTracker.setValue(true);
                    mMessageAdapter.add(chatMessage);
                    mMessageAdapter.notifyDataSetChanged();
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
        NewMessagesHandler.unsetCurrentlyChatting();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.report_user:
                getReportAndSend();

        }

        return super.onOptionsItemSelected(item);
    }

    private void getReportAndSend() {
        /* First get the report */
        // Add text to dialog
        final EditText text = new EditText(this);
        final Context context = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setView(text);
        builder.setMessage("Enter Report Reason");
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String enteredReport = text.getText().toString();
                Toast.makeText(context,
                        "You sent this: " + enteredReport,
                        Toast.LENGTH_SHORT).show();
                SendMailTask sendMailTask = new SendMailTask(PETTI_COMPLAINTS_MAIL,
                        "Complaint On: " + otherUserId,
                        enteredReport);
                /* Now send it by mail to company mail */
                sendMailTask.execute();
                return;
            }
        });
        builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
