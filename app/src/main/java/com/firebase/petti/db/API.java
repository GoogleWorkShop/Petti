package com.firebase.petti.db;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.firebase.petti.db.classes.*;
import com.google.firebase.database.ValueEventListener;

public class API {

    public static FirebaseDatabase mFirebaseDatabase;
    public static DatabaseReference mDatabaseUsersRef;


    public static void initDatabaseApi(){
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseUsersRef = mFirebaseDatabase.getReference().child("users");
    }

    public static void createUser(String uid, String name, String mail){
        Owner new_user = new Owner(name, mail);
        mDatabaseUsersRef.child(uid).child("owner").setValue(new_user);
    }
}
