package com.firebase.petti.db;


import android.location.Location;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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

public class API {

    private static FirebaseDatabase mFirebaseDatabase;
    private static FirebaseStorage mFirebaseStorage;
    public static GeoFire geoFire;

    public static DatabaseReference mDatabaseUsersRef;
    public static StorageReference mPetPhotos;
    public static DatabaseReference mDatabaseLocationsRef;

    private static ValueEventListener mUserEventListener;
    private static GeoQueryEventListener mLocationsListener;

    // This variables will set on sign in
    public static String currUserUid;
    public static User currUserData;
//    public static Map<String,String[]> nearbyUsers;
    public static Map<String,User> nearbyUsers;
    private static GeoQuery geoQuery;
    public static boolean queryReady;

    public static void initDatabaseApi() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mDatabaseUsersRef = mFirebaseDatabase.getReference().child("users");
        mPetPhotos = mFirebaseStorage.getReference().child("pet_photos");

        mDatabaseLocationsRef = mFirebaseDatabase.getReference().child("locations");
        geoFire = new GeoFire(mDatabaseLocationsRef);
        geoQuery = null;
        queryReady = false;

        mUserEventListener = null;

        currUserUid = null;
        currUserData = null;

    }

    private static void addLocation(GeoLocation geoLoc, long timestamp) {
        getCurrUserRef().child("lastLocationTime").setValue(timestamp);
        geoFire.setLocation(currUserUid, geoLoc);
    }

    public static void createUser(String name, String mail) {
//        Owner new_user = new Owner(name, mail);
        Owner new_user = new Owner(name, mail);
        setOwner(new_user);
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
        if (currUserData == null) {
            return null;
        }
        Owner tempOwner = currUserData.getOwner();
        return tempOwner == null ? new Owner() : tempOwner;
    }

    public static Dog getCurrDogData() {
        if (currUserData == null) {
            return null;
        }
        Dog tempDog = currUserData.getDog();
        return tempDog == null ? new Dog() : tempDog;
    }

    public static void attachNearbyUsersListener(Location location, int radius) {

        nearbyUsers = new HashMap<>();

        //first set the location for the user

        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        GeoLocation geoLoc = new GeoLocation(latitude, longitude);

        addLocation(geoLoc, location.getTime());

        if (geoQuery == null) {
            geoQuery = geoFire.queryAtLocation(geoLoc, radius);

            mLocationsListener = new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    final String userId = key;
                    queryReady = false;
                    if (!userId.equals(currUserUid)) {
                        ValueEventListener userLocationListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Get Post object and use the values to update the UI
//                                Dog dog = dataSnapshot.getValue(Dog.class);
                                User user = dataSnapshot.getValue(User.class);
//                                String[] ownerDetails = new String[]{userId, dog.getName(), dog.getPhotoUrl()};
                                nearbyUsers.put(userId, user);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Getting Post failed, log a message
                            }
                        };
//                        getUserRef(key).child("dog").addListenerForSingleValueEvent(userLocationListener);
                        getUserRef(key).addListenerForSingleValueEvent(userLocationListener);

                        Log.d("Number of users", String.valueOf(nearbyUsers.size()));
                        Log.d("KEY", String.valueOf(key));
                    }
                }

                @Override
                public void onKeyExited(String key) {
                    queryReady = false;
                    if (!key.equals(currUserUid)) {
                        nearbyUsers.remove(key);
                    }
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {

                }

                @Override
                public void onGeoQueryReady() {
                    queryReady = true;
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {

                }
            };
            geoQuery.addGeoQueryEventListener(mLocationsListener);
        }
    }


    public static void detachNearbyUsersListener(){
        if (geoQuery != null) {
            geoQuery.removeGeoQueryEventListener(mLocationsListener);
            geoQuery = null;
            nearbyUsers = null;
        }
    }

    public static boolean isMyUid(String uid){
        return currUserUid.equals(uid);
    }
}
