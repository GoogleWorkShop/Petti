package com.firebase.petti.db;

import com.firebase.petti.db.classes.ChatMessage;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This the chat API class- used as a placeholder for some static methods to access and alter the
 * the chat section of the database. These methods are used in the chat activity and includes
 * send message, get message etc.
 */

public class ChatApi {

    private static Map<String, String> chatUids;

    private static DatabaseReference mMessagesDatabaseReference;

    protected static void initChatDb(){
        if (API.mFirebaseDatabase == null){
            // ERROR NOT INITIALIZED DB YET
        }
        chatUids = new HashMap<>();
        mMessagesDatabaseReference = API.mFirebaseDatabase.getReference().child("messages");
    }

    public static void sendChatMessage(String toUid, String text){
        long timestamp = System.currentTimeMillis();
        ChatMessage msg = new ChatMessage(API.currUserUid, timestamp, text);
        getMsgRefById(toUid).push().setValue(msg);
        API.getUserRef(toUid).child("msgTracker").child(API.currUserUid).setValue(false);
//        mCurrUserMessagesDatabaseReference.child(toUid).push().setValue(msg);
//        getUserMsgRefById(toUid).child(API.currUserUid).push().setValue(msg);
    }


    public static DatabaseReference getMsgRefById(String otherUserUid){
        String chatUid = getCurrChatUid(otherUserUid);
        return mMessagesDatabaseReference.child(chatUid);
    }

    public static String getCurrChatUid(String otherUserUid){
        if (chatUids.containsKey(otherUserUid)){
            return chatUids.get(otherUserUid);
        }
        String firstId;
        String secondId;
        if (API.currUserUid.compareTo(otherUserUid)>0){
            firstId = API.currUserUid;
            secondId = otherUserUid;
        } else {
            firstId = otherUserUid;
            secondId = API.currUserUid;
        }
        String chatId = firstId + secondId;
        chatUids.put(otherUserUid, chatId);
        return chatId;
    }
}
