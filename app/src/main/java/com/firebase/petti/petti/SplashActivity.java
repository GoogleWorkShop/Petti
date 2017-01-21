package com.firebase.petti.petti;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.petti.db.API;
import com.firebase.petti.db.NewMessagesHandler;
import com.firebase.petti.petti.utils.ImageLoaderUtils;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

/**
 * This class is the activity for the splash screen
 * The splash screen is need to let the fire base action to get resolved before jumping into action.
 */
public class SplashActivity extends AppCompatActivity {

    private int SLEEP_TIME = 4;
    private static final String TAG = SplashActivity.class.getSimpleName();

    private boolean editUserProfile;
    private boolean editDogProfile;
    public static final int RC_SIGN_IN = 1;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    ImageView splashHeader;
    ImageView splashImage;
    TextView splashText;

    Intent nextActivityIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);    // Removes title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);    // Removes notification bar

        setContentView(R.layout.activity_splash);


        splashText = (TextView) findViewById(R.id.splash_text);

        /* initiate the sign in/up process */
        API.initDatabaseApi();
        mFirebaseAuth = FirebaseAuth.getInstance();
        editUserProfile = false;
        editDogProfile = false;
        ImageLoaderUtils.initImageLoader(this.getApplicationContext());
        initAuthStateListener();
    }

    private void initAuthStateListener(){
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                /* User is signed in */
                if (user != null) {

                    onSignedInInitialize(firebaseAuth.getCurrentUser());

                    Animation fadeInImage = new AlphaAnimation(0, 1);
                    fadeInImage.setInterpolator(new DecelerateInterpolator()); //add this
                    fadeInImage.setDuration(1000);
                    fadeInImage.setStartOffset(500);

                    Animation fadeInText = new AlphaAnimation(0, 1);
                    fadeInText.setInterpolator(new DecelerateInterpolator()); //add this
                    fadeInText.setStartOffset(1000);
                    fadeInText.setDuration(2000);

                    Animation fadeOut = new AlphaAnimation(1, 0);
                    fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
                    fadeOut.setStartOffset(3000);
                    fadeOut.setDuration(1000);

                    AnimationSet imageAnim = new AnimationSet(false); //change to false
                    imageAnim.addAnimation(fadeInImage);
                    imageAnim.addAnimation(fadeOut);

                    AnimationSet textAnim = new AnimationSet(false); //change to false
                    textAnim.addAnimation(fadeInImage);
                    textAnim.addAnimation(fadeOut);

                    splashText.startAnimation(textAnim);

                    // Start timer and launch main activity
                    IntentLauncher launcher = new IntentLauncher();
                    launcher.start();


                } else {
                    // User is signed out
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setLogo(R.drawable.pet_pic)
                                    .setProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER).build(),
                                            new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    private void onSignedInInitialize(FirebaseUser user) {
        // clear adapters if any populated
        // currently none is populated

        // creating db user
        final String user_id = user.getUid();
        final String display_name = user.getDisplayName();
        final String email = user.getEmail();
        API.currUserUid = user_id;
        Log.d(TAG, user_id);

//        NewMessagesHandler.initNewMessagesHandler();

//        startProgressBar();
        ValueEventListener mNewUserListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Inside first listener");
                editUserProfile = !dataSnapshot.exists();
                if (editUserProfile) {
                    // 1st registration
                    API.createUser(display_name, email);
                }
                editDogProfile = !dataSnapshot.child("dog").hasChild("name")
                        || dataSnapshot.child("dog").child("name").getValue().equals("");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        API.mDatabaseUsersRef.child(user_id).addListenerForSingleValueEvent(mNewUserListener);
        API.attachCurrUserDataReadListener();
        NewMessagesHandler.trackNewMessages(getApplicationContext());
    }

    private void onSignedOutCleanup() {
        // clear adapters if any populated
        // currently none is populated
        API.detachCurrUserDataReadListener();
        NewMessagesHandler.untrackNewMessages();
        API.currUserUid = null;
        API.currUserData = null;
//        setDrawerProfileInfo("", "");
    }


    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        // clear adapters if any populated
        // currently none is populated
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
//                if (editUserProfile) {
//                    Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(this, UserRegistrationActivitey.class);
//                    startActivity(intent);
//                } else if (editDogProfile) {
//                    Toast.makeText(this, "Need to add dog data", Toast.LENGTH_SHORT).show();
//                    Intent intent = new Intent(this, DogRegistrationActivity.class);
//                    startActivity(intent);
//                }

            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                finish();
            }
        }
    }

    private void startEditProfileActivity() {
        Toast.makeText(this, "Need to add dog data", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, DogRegistrationActivity.class);
        SplashActivity.this.startActivity(intent);
    }

    private class IntentLauncher extends Thread {
        @Override
        /**
         * Sleep for some time and than start new activity.
         */
        public void run() {
            try {
                // Sleeping
                Thread.sleep(SLEEP_TIME*1000);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }

            if (editDogProfile || editUserProfile){
                nextActivityIntent = new Intent(SplashActivity.this, DogRegistrationActivity.class);
            } else {
                // Start main activity
                nextActivityIntent = new Intent(SplashActivity.this, MainActivity.class);
            }
            SplashActivity.this.startActivity(nextActivityIntent);
            SplashActivity.this.finish();
        }
    }
}
