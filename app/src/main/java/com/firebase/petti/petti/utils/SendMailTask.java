package com.firebase.petti.petti.utils;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * In this task we are sending a mail with text from the user either from
 * pettiReview@gmail.com for review OR pettiComplaints@gmail.com for complaints about other users.
 * both e-mails should have the same password and be sent to the same recipient
 */

public class SendMailTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = SendMailTask.class.getSimpleName();
    private String from;
    private String subject;
    private String text;

    public SendMailTask(@NonNull String from, @NonNull String subject, @Nullable String text){
        this.from = from;
        this.subject = subject;
        if (text != null){
            this.text = text;
        } else {
            this.text = "Empty Text :/";
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            GMailSender sender = new GMailSender(from, "Welcome1!");
            sender.sendMail(subject,
                    text,
                    from,
                    "allbusinessgws@gmail.com");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return false;
        }

        return true;
    }
}
