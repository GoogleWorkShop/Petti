package com.firebase.petti.db;

import com.firebase.petti.db.classes.ChatMessage;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by yahav on 12/31/2016.
 */

public class ChatApi {

    private static DatabaseReference mCurrUserMessagesDatabaseReference;

    protected static void initChatDb(){
        if (API.mDatabaseUsersRef == null){
            // ERROR NOT INITIALIZED DB YET
        }
        mCurrUserMessagesDatabaseReference = API.getCurrUserRef().child("messages");
    }

    public static void sendChatMessage(String toUid, String text){
        ChatMessage msg = new ChatMessage(API.currUserUid, toUid, text);
        mCurrUserMessagesDatabaseReference.child(toUid).push().setValue(msg);
        getUserMsgRefById(toUid).child(API.currUserUid).push().setValue(msg);
    }


    private static DatabaseReference getUserMsgRefById(String uid){
        return API.getUserRef(uid).child("messages");
    }



//    private void attachDatabaseReadListener() {
//        if (mChildEventListener == null) {
//            mChildEventListener = new ChildEventListener() {
//                @Override
//                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                    ChatMessage chatMessage = dataSnapshot.getValue(ChatMessage.class);
//                    mMessageAdapter.add(chatMessage);
//                }
//
//                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                }
//
//                public void onChildRemoved(DataSnapshot dataSnapshot) {
//                }
//
//                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//                }
//
//                public void onCancelled(DatabaseError databaseError) {
//                }
//            };
//            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
//        }
//    }
//
//    private void detachDatabaseReadListener() {
//        if (mChildEventListener != null) {
//            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
//            mChildEventListener = null;
//        }
//    }
}
