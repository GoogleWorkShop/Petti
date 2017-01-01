package com.firebase.petti.petti;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ImageButton;

import com.firebase.petti.db.classes.ChatMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class user_chat extends AppCompatActivity {
    private ListView mainListView ;
    private String otherUserId;
    private ArrayAdapter<String> listAdapter ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        otherUserId = getIntent().getStringExtra("USER_ID");
        setContentView(R.layout.activity_chat_activity);
        mainListView = (ListView) findViewById( R.id.mainListView );

        final ImageButton button = (ImageButton) findViewById(R.id.sendButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText inputText = (EditText) findViewById(R.id.messageInput);
                String input = inputText.getText().toString();
                sendMassage(input);
            }
        });
        List<ChatMessage> massageList = getMassages();
        chatAdapter customAdapter = new chatAdapter(this, R.layout.chatrow, massageList);

        mainListView.setAdapter( customAdapter );
    }
    public int sendMassage (String imput)
    {
      return 1;
    };
    public List<ChatMessage> getMassages()
    {
        List<ChatMessage> toret  = new ArrayList<ChatMessage>();
        toret.add(new ChatMessage("amir","raz","hi hi mother fucker"));
        toret.add(new ChatMessage("amir","raz","hi u"));
        toret.add(new ChatMessage("raz","amir","like"));
        toret.add(new ChatMessage("raz","amir","unlike"));
        toret.add(new ChatMessage("amir","raz","1"));
        toret.add(new ChatMessage("amir","raz","2"));
        return toret;

    }
}
