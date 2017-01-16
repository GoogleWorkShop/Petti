package com.firebase.petti.db;

import android.location.Location;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.geofire.LocationCallback;
import com.firebase.petti.db.classes.User;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import static com.firebase.petti.db.API.mFirebaseDatabase;

/**
 * Created by yahav on 1/16/2017.
 */

public class LocationsApi {

    private static final String tag = "***LOCATIONS API***";

    public static GeoFire geoFire;
    public static GeoFire geoFireStatic;

    public static DatabaseReference mDatabaseLocationsRef;
    public static DatabaseReference mDatabaseStaticLocationsRef;

    private static GeoQueryEventListener mLocationsListener;
    private static GeoQueryEventListener mStaticLocationsListener;

    private static GeoQuery geoQuery;
    private static GeoQuery geoStaticQuery;

    public static Map<String, User> nearbyUsers;
    public static boolean queryReady;

    protected static void initLocationsApi() {
        mDatabaseLocationsRef = mFirebaseDatabase.getReference().child("locations");
        mDatabaseStaticLocationsRef = mFirebaseDatabase.getReference().child("static_locations");

        geoFireStatic = new GeoFire(mDatabaseStaticLocationsRef);
        geoFire = new GeoFire(mDatabaseLocationsRef);
        geoQuery = null;
        geoStaticQuery = null;
        queryReady = false;
        nearbyUsers = new HashMap<>();
    }

    public static void addStaticLocation(Place place) {
        if (place != null) {
            addStaticLocation(place.getLatLng());
        }
    }

    public static void addStaticLocation(LatLng ltlng) {
        GeoLocation geoLoc = new GeoLocation(ltlng.latitude, ltlng.longitude);
        geoFireStatic.setLocation(API.currUserUid, geoLoc);
    }

    private static void addLocation(GeoLocation geoLoc, long timestamp) {
        API.getCurrUserRef().child("lastLocationTime").setValue(timestamp);
        geoFire.setLocation(API.currUserUid, geoLoc);
    }

    public static boolean attachNearbyUsersListener(Location location, int radius, boolean bark) {
        if (bark) {
            return attachDynamicNearbyUsersListener(location, radius);
        } else {
            return attachStaticNearbyUsersListener(radius);
        }
    }

    private static boolean attachDynamicNearbyUsersListener(Location location, int radius) {

        nearbyUsers.clear();

        //first set the location for the user
        if (location == null) {
            Log.d(tag, "Got a null value in location parameter");
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
                    if (!userId.equals(API.currUserUid)) {
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
                                } catch (NullPointerException e) {
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
                        API.getUserRef(key).addListenerForSingleValueEvent(userLocationListener);

                        Log.d("Number of users", String.valueOf(nearbyUsers.size()));
                        Log.d("KEY", String.valueOf(key));
                    }
                }

                @Override
                public void onKeyExited(String key) {
                    queryReady = false;
                    if (!key.equals(API.currUserUid)) {
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


    private static boolean attachStaticNearbyUsersListener(final int radius) {

        nearbyUsers.clear();

        if (API.currUserData.getOwner().getCity() == null || API.currUserData.getOwner().getCity().isEmpty()){
            return false;
        }

        geoFireStatic.getLocation(API.currUserUid, new LocationCallback() {
            @Override
            public void onLocationResult(String key, final GeoLocation geoLoc) {
                if (geoLoc == null){
                    Log.d(tag, "There is no location for key...");
                }
                else if (LocationsApi.geoStaticQuery == null) {
                    LocationsApi.geoStaticQuery = geoFireStatic.queryAtLocation(geoLoc, radius);
                    LocationsApi.mStaticLocationsListener = new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, final GeoLocation location) {

                            final String userId = key;
                            final double userLongtitude = location.longitude;
                            final double userLatitude = location.latitude;

                            LocationsApi.queryReady = false;
                            if (!userId.equals(API.currUserUid)) {
                                ValueEventListener userLocationListener = new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        // Get Post object and use the values to update the UI
                                        User user = dataSnapshot.getValue(User.class);
                                        Float distanceFromMe = calcDistanceTo(geoLoc, userLatitude, userLongtitude);
                                        user.setTempDistanceFromMe(distanceFromMe);
                                        LocationsApi.nearbyUsers.put(userId, user);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        // Getting Post failed, log a message
                                    }
                                };
                                API.getUserRef(key).addListenerForSingleValueEvent(userLocationListener);

                                Log.d("Number of users", String.valueOf(LocationsApi.nearbyUsers.size()));
                                Log.d("KEY", String.valueOf(key));
                            }
                        }

                        @Override
                        public void onKeyExited(String key) {
                            LocationsApi.queryReady = false;
                            if (!key.equals(API.currUserUid)) {
                                LocationsApi.nearbyUsers.remove(key);
                            }
                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {
                            LocationsApi.queryReady = true;
                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {

                        }
                    };
                    LocationsApi.geoStaticQuery.addGeoQueryEventListener(mStaticLocationsListener);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(tag, "*** Failed to get my static location ***");
            }
        });
        return true;
    }

    public static void detachNearbyUsersListener(boolean bark) {
        if (bark){
            detachDynamicNearbyUsersListener();
        } else {
            detachStaticNearbyUsersListener();
        }
    }

    private static void detachDynamicNearbyUsersListener() {
        if (geoQuery != null) {
            geoQuery.removeGeoQueryEventListener(mLocationsListener);
            geoQuery = null;
            nearbyUsers.clear();
        }
    }

    private static void detachStaticNearbyUsersListener() {
        if (geoStaticQuery != null) {
            geoStaticQuery.removeGeoQueryEventListener(mStaticLocationsListener);
            geoStaticQuery = null;
            nearbyUsers.clear();
        }
    }

    private static float calcDistanceTo(Location myLoc, double otherLat, double otherLon) {
        Location otherLoacation = new Location("");
        otherLoacation.setLatitude(otherLat);
        otherLoacation.setLongitude(otherLon);
        return myLoc.distanceTo(otherLoacation);
    }

    private static float calcDistanceTo(GeoLocation myLoc, double otherLat, double otherLon) {
        double myLat = myLoc.latitude;
        double myLon = myLoc.longitude;
        float[] result = new float[1];
        Location.distanceBetween(myLat, myLon, otherLat, otherLon, result);
        return result[0];
    }

}
