package com.firebase.petti.db;


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

public class API {

    private static FirebaseDatabase mFirebaseDatabase;
    private static FirebaseStorage mFirebaseStorage;

    public static DatabaseReference mDatabaseUsersRef;
    public static StorageReference mPetPhotos;

    private static ValueEventListener mUserEventListener;

    // This variables will set on sign in
    public static String currUserUid;
    public static User currUserData;

    public static void initDatabaseApi() {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        mDatabaseUsersRef = mFirebaseDatabase.getReference().child("users");
        mPetPhotos = mFirebaseStorage.getReference().child("pet_photos");

        mUserEventListener = null;

        currUserUid = null;
        currUserData = null;
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

    public static Owner getCurrOwnerData(){
        if (currUserData == null){
            return null;
        }
        Owner tempOwner = currUserData.getOwner();
        return tempOwner == null ? new Owner():tempOwner;
    }

    public static Dog getCurrDogData(){
        if (currUserData == null){
            return null;
        }
        Dog tempDog = currUserData.getDog();
        return tempDog == null ? new Dog():tempDog;
    }
}
