package com.firebase.petti.db.classes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yahav on 12/30/2016.
 */

public class ChatMessage {

    private String fromUid;
//    private String toUid;
    private String text;
    private Long timestamp;

//    public ChatMessage(String fromUid, String toUid, String text){
//        this.fromUid = fromUid;
////        this.toUid = toUid;
//        this.text = text;
//    }

    public ChatMessage(String fromUid, Long timestamp, String text){
        this.fromUid = fromUid;
        this.timestamp = timestamp;
        this.text = text;
    }


    public ChatMessage(){}

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

//    public String getToUid() {
//        return toUid;
//    }
//
//    public void setToUid(String toUid) {
//        this.toUid = toUid;
//    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String formatTimestamp(){
        try {
        Date d = new Date(timestamp);
        DateFormat dateFormat = new SimpleDateFormat("dd MMM HH:mm:ss");
            return dateFormat.format(d);
        } catch (Exception ex){
            return "";
        }
    }

//    public boolean isViewed() {
//        return viewed;
//    }
//
//    public void setViewed(boolean viewed) {
//        this.viewed = viewed;
//    }
//
//    public String getFromName() {
//        return fromName;
//    }
//
//    public void setFromName(String fromName) {
//        this.fromName = fromName;
//    }
}
