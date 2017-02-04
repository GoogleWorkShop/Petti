package com.firebase.petti.db.classes;

import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by yahav on 12/26/2016.
 */

public class User implements Serializable{

    private Dog dog;
    private Owner owner;

    // map of format: <FriendUid, isViewed>
    // isViewed: This will be true if we have seen all messages from that friend
    private Map<String, Boolean> msgTracker;

    private Map<String, Boolean> blockedUsers;

    private Long lastLocationTime;
    private Boolean enabled;

    // These members are set only in the java realm. they are always null in the db
    private String tempUid;
    private Float tempDistanceFromMe;

    private Double tempLongtitude;
    private Double tempLatitude;

    public User(){
    }

    public User(String name, String mail){
        this.owner = new Owner();
        this.owner.setName(name);
        this.owner.mail = mail;
    }

    public Float getTempDistanceFromMe() {
        return tempDistanceFromMe;
    }

    public void setTempDistanceFromMe(Float tempDistanceFromMe) {
        this.tempDistanceFromMe = tempDistanceFromMe;
    }

    public String getTempUid() {
        return tempUid;
    }

    public void setTempUid(String tempUid) {
        this.tempUid = tempUid;
    }


    public LatLng retLatLng(){
        return new LatLng(tempLatitude, tempLongtitude);
    }


    public Double getTempLatitude() { return tempLatitude; }

    public void setTempLatitude(Double tempLatitude) { this.tempLatitude = tempLatitude; }

    public Double getTempLongtitude() { return tempLongtitude; }

    public void setTempLongtitude(Double tempLongtitude) { this.tempLongtitude = tempLongtitude; }

    public Map<String, Boolean> getMsgTracker() {
        return msgTracker;
    }

    public void setMsgTracker(Map<String, Boolean> msgTracker) {
        this.msgTracker = msgTracker;
    }

    public Map<String, Boolean> getBlockedUsers() {
        return blockedUsers;
    }

    public void setBlockedUsers(Map<String, Boolean> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public Long getLastLocationTime() {
        return lastLocationTime;
    }

    public void setLastLocationTime(Long lastLocationTime) {
        this.lastLocationTime = lastLocationTime;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Dog getDog(){
        return this.dog;
    }

    public Owner getOwner(){
        return this.owner;
    }

    public void setDog(Dog dog){
        this.dog = dog;
    }

    public void setOwner(Owner owner){
        this.owner = owner;
    }

    private static abstract class ABCNamedEntity implements Serializable{

        private String name;
        private String description;
        private String age;
        private String photoUrl;
        private Boolean female;

        public ABCNamedEntity(){
        }

        public void setName(String name){
            this.name = name;
        }

        public String getName(){
            return this.name;
        }

        public void setDescription(String description){
            this.description = description;
        }

        public String getDescription(){
            return this.description;
        }

        public void setAge(String age){
            this.age = age;
        }

        public String getAge(){
            return this.age;
        }

//    public void setAge(int age){
//        this.setAge((long)age);
//    }

        public void setPhotoUrl(String photo_url){
            this.photoUrl = photo_url;
        }

        public String getPhotoUrl(){
            return this.photoUrl;
        }

        public void setFemale(Boolean is_female){
            this.female = is_female;
        }

        public Boolean getFemale(){
            return this.female;
        }

        public ArrayList<String> retrieveDetailList () {
            ArrayList<String> output = new ArrayList<>();
            if (getName() != null && !getName().isEmpty()){
                output.add("Name: " + getName());
            }
            String myAge = getAge();
            if (myAge != null && !myAge.isEmpty()) {
                if (!TextUtils.isDigitsOnly(myAge)) {
                    String[] ageParams = getAge().split("/");
                    myAge = retrieveAgeFromDate(Integer.parseInt(ageParams[0]),
                            Integer.parseInt(ageParams[1]),
                            Integer.parseInt(ageParams[2]));
                }
                output.add("Age: " + myAge);
            }
            if (getFemale() != null) {
                output.add("Gender: " + (getFemale() ? "Female" : "Male"));
            }
            return output;
        }
    }

    public static class Owner extends ABCNamedEntity {

        private String mail;
        private String city;
        private String nickname;
        private List<String> lookingForList;

        public Owner(){
        }

        public Owner(String name, String mail){
            this.setName(name);
            this.mail = mail;
        }

        public void setMail(String mail){
            this.mail = mail;
        }

        public String getMail(){
            return this.mail;
        }

        public void setCity(String city){
            this.city = city;
        }

        public String getCity(){
            return this.city;
        }

        public void setNickname(String nickname){
            this.nickname = nickname;
        }

        public String getNickname(){
            return this.nickname;
        }

        public void setLookingForList(List<String> lookingForList){
            this.lookingForList = lookingForList;
        }

        public List<String> getLookingForList(){
            return this.lookingForList;
        }

        public ArrayList<String> retrieveDetailList (){
            ArrayList<String> output = super.retrieveDetailList();
            // TODO: Uncomment when fixed
            output.add("Description:\n" + getDescription());
            return output;
        }
    }

    public static class Dog extends ABCNamedEntity {

        private String type;
        private List<String> personalityAttributes;
        private String walkWith;
        private String walkWhere;

        public Dog(){
        }


        public void setType(String type){
            this.type = type;
        }

        public String getType(){
            return this.type;
        }

        public void setPersonalityAttributes(List<String> personalityAttributes){
            this.personalityAttributes = personalityAttributes;
        }

        public List<String> getPersonalityAttributes(){
            return this.personalityAttributes;
        }

        public void setWalkWith(String walkWith){
            this.walkWith = walkWith;
        }

        public String getWalkWith(){
            return this.walkWith;
        }

        public void setWalkWhere(String walkWhere){
            this.walkWhere = walkWhere;
        }

        public String getWalkWhere(){
            return this.walkWhere;
        }

        public ArrayList<String> retrieveDetailList (){
            ArrayList<String> output = super.retrieveDetailList();
            output.add("Description:\n" + getDescription());
            return output;
        }
    }


    private static String retrieveAgeFromDate(int day, int month, int year){

        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        boolean inMonths = false;
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if (age == 0){
            age = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
            inMonths = true;
        }

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = String.format("%d %s old", ageInt, inMonths ? "Months": "Years");

        return ageS;
    }
}
