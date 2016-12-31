package com.firebase.petti.petti;

/**
 * Created by yahav on 12/30/2016.
 */

public class ChatMessage {

    private String fromUid;
//    private String fromName;
    private String toUid;
    private String text;
//    private boolean viewed;

    public ChatMessage(String fromUid, String toUid, String text){
        this.fromUid = fromUid;
        this.toUid = toUid;
        this.text = text;
    }


    public ChatMessage(){}

    public String getFromUid() {
        return fromUid;
    }

    public void setFromUid(String fromUid) {
        this.fromUid = fromUid;
    }

    public String getToUid() {
        return toUid;
    }

    public void setToUid(String toUid) {
        this.toUid = toUid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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
