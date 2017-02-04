package com.firebase.petti.db;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.firebase.petti.petti.R;
import com.firebase.petti.petti.UserChatActivity;
import com.firebase.petti.petti.utils.NotificationPublisher;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by yahav on 1/13/2017.
 */

public class NewMessagesHandler {

    public static DatabaseReference mMessagesTracker;
    private static ChildEventListener mNewMsgsListener;

    // Used to avoid spamming for every new message
    private static long lastNotificationTime = 0;
    // Used to avoid notifacation from current chat activity
    private static String currentlyChatting;
    private static int newMessagesId = 9;

    public static void initNewMessagesHandler(){
        mNewMsgsListener = null;
        mMessagesTracker = getCurrMsgTracker();
//        resetLastNotificationTime();
        unsetCurrentlyChatting();
    }

    public static DatabaseReference getMsgTrackerById(String uid){
        return API.getUserRef(uid).child("msgTracker");
    }

    public static DatabaseReference getCurrMsgTracker(){
        return API.getCurrUserRef().child("msgTracker");
    }

    public static void setCurrentlyChatting(String uid){
        currentlyChatting = uid;
    }

    public static void unsetCurrentlyChatting(){
        setCurrentlyChatting(null);
    }

    public static void resetLastNotificationTime(){
        lastNotificationTime = 0;
    }

    private static void notifyNewMessage(final Context appContext, @NonNull String fromUid){
        long now = System.currentTimeMillis();
        long minLimit = now - API.HALF_HOUR_MILLSEC;
        if (!fromUid.equals(currentlyChatting) && !API.isBlockedByMe(fromUid)) {
//            NotificationPublisher.scheduleNotification("You have unread messages!", 0, appContext);
            lastNotificationTime = now;

            Intent startChatIntent = new Intent(appContext, UserChatActivity.class);
            startChatIntent.putExtra("USER_ID", fromUid);
            startChatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    appContext, 0, startChatIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationManager notificationManager = (NotificationManager)
                    appContext.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification.Builder builder = new Notification.Builder(appContext);
            builder.setContentIntent(pendingIntent);
            builder.setContentTitle("You Have Unread Messages!");
            builder.setContentText("Go to \"My Friends\" or tap to view");
            builder.setSmallIcon(R.drawable.pet_pic_white_burned);
            builder.setVibrate(new long[] { 500, 500 });
            builder.setAutoCancel(true);
            Notification notification = builder.build();

            notificationManager.notify(newMessagesId, notification);
        }
    }

    public static void trackNewMessages(final Context appContext){
        if (mNewMsgsListener == null){
            initNewMessagesHandler();
            mNewMsgsListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (!(Boolean) dataSnapshot.getValue()){
                        notifyNewMessage(appContext, dataSnapshot.getKey());
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    if (!(Boolean) dataSnapshot.getValue()){
                        notifyNewMessage(appContext, dataSnapshot.getKey());
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            mMessagesTracker.addChildEventListener(mNewMsgsListener);
        }
    }

    public static void untrackNewMessages(){
        if (mNewMsgsListener != null){
            mMessagesTracker.removeEventListener(mNewMsgsListener);
        }
        unsetCurrentlyChatting();
//        resetLastNotificationTime();
    }
}
