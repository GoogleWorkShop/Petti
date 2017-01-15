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

public class API {

    private static final String tag = "***FIREBASE API***";

    protected static FirebaseDatabase mFirebaseDatabase;
    private static FirebaseStorage mFirebaseStorage;
    public static GeoFire geoFire;
    public static GeoFire geoFireStatic;

    public static DatabaseReference mDatabaseUsersRef;
    public static StorageReference mPetPhotos;
    public static StorageReference mOwnerPhotos;
    public static DatabaseReference mDatabaseLocationsRef;
    public static DatabaseReference mDatabaseStaticLocationsRef;

    private static ValueEventListener mUserEventListener;
    private static GeoQueryEventListener mLocationsListener;

    // This variables will set on sign in
    public static String currUserUid;
    public static User currUserData;
//    public static Map<String,String[]> nearbyUsers;
    public static Map<String,User> nearbyUsers;
    private static GeoQuery geoQuery;
    public static boolean queryReady;

    public static final long HALF_HOUR_MILLSEC = 30*60*1000;

    public static void initDatabaseApi() {
        if (mFirebaseDatabase == null) {
            mFirebaseDatabase = FirebaseDatabase.getInstance();
            mFirebaseStorage = FirebaseStorage.getInstance();


            mDatabaseUsersRef = mFirebaseDatabase.getReference().child("users");
            mPetPhotos = mFirebaseStorage.getReference().child("pet_photos");
            mOwnerPhotos = mFirebaseStorage.getReference().child("owner_photos");

            mDatabaseLocationsRef = mFirebaseDatabase.getReference().child("locations");
            mDatabaseStaticLocationsRef = mFirebaseDatabase.getReference().child("static_locations");

            geoFireStatic = new GeoFire(mDatabaseStaticLocationsRef);
            geoFire = new GeoFire(mDatabaseLocationsRef);
            geoQuery = null;
            queryReady = false;

            mUserEventListener = null;

            ChatApi.initChatDb();
//        NewMessagesHandler.initNewMessagesHandler();

            currUserUid = null;
            currUserData = null;
        }

    }

    public static void addStaticLocation(Place place){
        if (place != null) {
            addStaticLocation(place.getLatLng());
        }
    }

    public static void addStaticLocation(LatLng ltlng){
        GeoLocation geoLoc = new GeoLocation(ltlng.latitude, ltlng.longitude);
        geoFireStatic.setLocation(currUserUid, geoLoc);
    }

    private static void addLocation(GeoLocation geoLoc, long timestamp) {
        getCurrUserRef().child("lastLocationTime").setValue(timestamp);
        geoFire.setLocation(currUserUid, geoLoc);
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

    public static boolean attachNearbyUsersListener(Location location, int radius) {

        nearbyUsers = new HashMap<>();

        //first set the location for the user
        if(location == null){
            Log.d("**** PETTI API ****", "Got a null value in location parameter");
            return false;
        }

        double longitude = location.getLongitude();
        double latitude = location.getLatitude();
        GeoLocation geoLoc = new GeoLocation(latitude, longitude);

        addLocation(geoLoc, location.getTime());

        final Location myLocation = location;

        if (geoQuery == null) {
            geoQuery = geoFire.queryAtLocation(geoLoc, radius);

            mLocationsListener = new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, final GeoLocation location) {

                    final String userId = key;
                    final double userLongtitude = location.longitude;
                    final double userLatitude = location.latitude;

                    queryReady = false;
                    if (!userId.equals(currUserUid)) {
                        ValueEventListener userLocationListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                // Get Post object and use the values to update the UI
//                                Dog dog = dataSnapshot.getValue(Dog.class);
                                User user = dataSnapshot.getValue(User.class);
                                Float distanceFromMe = calcDistanceTo(myLocation, userLatitude, userLongtitude);
                                user.setTempDistanceFromMe(distanceFromMe);
//                                user.setTempLatitude(userLatitude);
//                                user.setTempLongtitude(userLongtitude);
//                                String[] ownerDetails = new String[]{userId, dog.getName(), dog.getPhotoUrl()};
                                try {
                                    nearbyUsers.put(userId, user);
                                } catch (NullPointerException e){
                                    Log.d(tag, "Tried to put in -nerbayUsers- after detach (nulified)");
                                    return;
                                }
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
        return true;
    }


    public static void detachNearbyUsersListener(){
        if (geoQuery != null) {
            geoQuery.removeGeoQueryEventListener(mLocationsListener);
            geoQuery = null;
            nearbyUsers.clear();
        }
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
        return currUserData.getMsgTracker();
    }

    private static float calcDistanceTo(Location myLoc, double otherLat, double otherLon){
        Location otherLoacation = new Location("");
        otherLoacation.setLatitude(otherLat);
        otherLoacation.setLongitude(otherLon);
        return myLoc.distanceTo(otherLoacation);
    }
}
