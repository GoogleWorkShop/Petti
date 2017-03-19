package com.firebase.petti.db;


import android.location.Location;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.petti.petti.R;
import com.firebase.petti.petti.utils.ImageLoaderUtils;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.petti.db.classes.User;
import com.firebase.petti.db.classes.User.Dog;
import com.firebase.petti.db.classes.User.Owner;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This API class is functioning practically as placeholder for general-purpose static methods to be
 * used in several activities and fragments through the entire app. For example some of these
 * methods are used to alter the firebase DB, such as user creation and modfication; get information
 * of the current user; and get friends statuses between this user and other.
 */
public class API {

    private static final String tag = "***FIREBASE API***";

    protected static FirebaseDatabase mFirebaseDatabase;
    private static FirebaseStorage mFirebaseStorage;

    public static DatabaseReference mDatabaseUsersRef;
    public static StorageReference mPetPhotos;
    public static StorageReference mOwnerPhotos;

    private static ValueEventListener mUserEventListener;

    // This variables will set on sign in
    public static String currUserUid;
    public static User currUserData;

    public static final long HALF_HOUR_MILLSEC = 30*60*1000;

    public static void initDatabaseApi() {
        if (mFirebaseDatabase == null) {
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mFirebaseStorage = FirebaseStorage.getInstance();


            mDatabaseUsersRef = mFirebaseDatabase.getReference().child("users");
            mPetPhotos = mFirebaseStorage.getReference().child("pet_photos");
            mOwnerPhotos = mFirebaseStorage.getReference().child("owner_photos");

            mUserEventListener = null;

            ChatApi.initChatDb();
            LocationsApi.initLocationsApi();

            currUserUid = null;
            currUserData = null;
        }

    }

    public static void createUser(String name, String mail) {
//        Owner new_user = new Owner(name, mail);
        Owner new_user = new Owner(name, mail);
        setOwner(new_user);
        Dog default_dog = new Dog();
        //default_dog.setName("John Doggo");
        default_dog.setName("");
        setDog(default_dog);
    }

    public static void setOwner(Owner owner) {
        getUserRef(currUserUid).child("owner").setValue(owner);
    }

    public static void setDog(User.Dog dog) {
        getUserRef(currUserUid).child("dog").setValue(dog);
    }

    public static DatabaseReference getUserRef(String uid) {
        return mDatabaseUsersRef.child(uid);
    }

    public static DatabaseReference getCurrUserRef() {
        return mDatabaseUsersRef.child(currUserUid);
    }

    public static void attachCurrUserDataReadListener() {
        if (mUserEventListener == null) {
            mUserEventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    currUserData = dataSnapshot.getValue(User.class);
                }


                public void onCancelled(DatabaseError databaseError) {
                }
            };

            getCurrUserRef().addValueEventListener(mUserEventListener);
        }
    }

    public static void detachCurrUserDataReadListener() {
        if (mUserEventListener != null) {
            getCurrUserRef().removeEventListener(mUserEventListener);
            mUserEventListener = null;
        }
    }

    public static Owner getCurrOwnerData() {
        return currUserData == null || currUserData.getOwner() == null ?
                new Owner() : currUserData.getOwner();
    }

    public static Dog getCurrDogData() {
        return currUserData == null || currUserData.getDog() == null ?
                new Dog() : currUserData.getDog();
    }

    public static boolean isMyUid(String uid){
        return currUserUid.equals(uid);
    }

    public static boolean isMatchedWith(String uid){
        return (getCurrMsgTracker() != null &&
                uid != null &&
                getCurrMsgTracker().containsKey(uid));
    }

    public static Map<String, Boolean> getCurrMsgTracker(){
        return currUserData != null && currUserData.getMsgTracker() != null ?
                currUserData.getMsgTracker() : new HashMap<String, Boolean>();
    }

    public static boolean verifyMandatoryData(){
        return currUserUid != null && currUserData != null;
    }

    public static void blockUser(String uid){
        getCurrUserRef().child("blockedUsers").child(uid).setValue(true);
    }

    public static void unBlockUser(String uid){
        getCurrUserRef().child("blockedUsers").child(uid).setValue(null);
    }

    public static boolean isBlockedByMe(String uid){
        return currUserData != null && currUserData.getBlockedUsers() != null
                && currUserData.getBlockedUsers().containsKey(uid);
    }

    public static boolean isUserBlockingMe(User user){
        return currUserUid != null && user.getBlockedUsers() != null
                && user.getBlockedUsers().containsKey(currUserUid);
    }
}
