package com.firebase.petti.petti.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.firebase.petti.petti.R;

/**
 * This utility is responsible for creating and scheduling a notification to the user's device.
 * Used both by the foodNotification fragment and the vaccination card fragment.
 */
public class NotificationPublisher extends BroadcastReceiver {
    public NotificationPublisher() {
    }

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";

    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManager.notify(id, notification);

    }

    /**
     * Sechdule a notification to future time
     * @param context: SHOULD ALWAYS BE AN ACTIVITY CLASS (use getActivity from Fragment or this
     *               from Activity)
     */
    public static void scheduleNotification(String content, long delay, Context context) {

        if(!Activity.class.isInstance(context)){
            Log.d("Notifs-schedular", "Sent wrong context: " + context.toString());
            return;
        }

        Notification notification = NotificationPublisher.getNotification(content, context);

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private static Notification getNotification(String content, Context context) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.pet_pic_white_burned);
        return builder.build();
    }
}
