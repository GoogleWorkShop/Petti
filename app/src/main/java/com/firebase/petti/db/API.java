package com.firebase.petti.db;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.petti.db.classes.*;
import com.google.firebase.database.ValueEventListener;

public class API {

    private static FirebaseDatabase mFirebaseDatabase;
    private static DatabaseReference mDatabaseUsersRef;

    private static boolean exists;

    public static void initDatabaseApi(){
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseUsersRef = mFirebaseDatabase.getReference().child("users");
    }

    public static boolean isUserExists(String user_id){
        return pathExists(mDatabaseUsersRef.child(user_id));
//        return key != null;
    }

    public static void createUser(String uid, String name, String mail){
        Owner new_user = new Owner(name, mail);
        mDatabaseUsersRef.child(uid).child("owner").setValue(new_user);
    }

    protected static boolean pathExists(DatabaseReference ref){
        return pathExists(ref.toString());
    }

    public static boolean pathExists(String path){
        exists = false;
        ValueEventListener mValueExistsListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                exists = dataSnapshot.exists();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        DatabaseReference ref = mFirebaseDatabase.getReferenceFromUrl(path);
        ref.addValueEventListener(mValueExistsListener);
        return exists;
    }
}
