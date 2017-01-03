package com.firebase.petti.petti;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import com.firebase.petti.db.API;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private MenuItem mainMenuItem;
    private TextView dogNameHederText;

    // Make sure to be using android.support.v7.app.ActionBarDrawerToggle version.
    // The android.support.v4.app.ActionBarDrawerToggle has been deprecated.
    private ActionBarDrawerToggle drawerToggle;

    public static final int RC_SIGN_IN = 1;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private boolean editUserProfile;
    private boolean editDogProfile;


    private final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        mFirebaseAuth = FirebaseAuth.getInstance();
        API.initDatabaseApi();
        editUserProfile = false;
        editDogProfile = false;

        dogNameHederText = (TextView) findViewById(R.id.dog_name_header); 

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.main_drawer_layout);

        drawerToggle = setupDrawerToggle();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.addDrawerListener(drawerToggle);

        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);

        setupDrawerContent(nvDrawer);

        mainMenuItem = nvDrawer.getMenu().findItem(R.id.default_fragment);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container, new MainFragment())
                    .commit();
        }

        initAuthStateListener();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass;
        switch (menuItem.getItemId()) {
            case R.id.default_fragment:
                fragmentClass = MainFragment.class;
                break;
            case R.id.food_notifications:
                fragmentClass = FoodNotificationsFragment.class;
                break;
            case R.id.vaccinetion_card:
                fragmentClass = VaccinationCardFragment.class;
                break;
            case R.id.find_near_dog_parks:
                fragmentClass = FindNearDogParksFragment.class;
                break;
            case R.id.find_near_veterinarians:
                fragmentClass = FindNearVeterinariansFragment.class;
                break;
            case R.id.find_near_pet_stores:
                fragmentClass = FindNearPetStoresFragment.class;
                break;
            case R.id.my_preferences:
                fragmentClass = MyPreferencesFragment.class;
                break;
            case R.id.edit_user_profile:
                Intent userIntent = new Intent(this,UserRegistrationActivitey.class);
                userIntent.putExtra("edit",true);
                startActivity(userIntent);
                return;
            case R.id.edit_dog_profile:
                Intent dogIntent = new Intent(this,DogRegistrationActivity.class);
                dogIntent.putExtra("edit",true);
                startActivityForResult(dogIntent,0);
                return;


            default:
                fragmentClass = MainFragment.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE 1: Make sure to override the method with only a single `Bundle` argument
    // Note 2: Make sure you implement the correct `onPostCreate(Bundle savedInstanceState)` method.
    // There are 2 signatures and only `onPostCreate(Bundle state)` shows the hamburger icon.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.main_container);
        if(mDrawer.isDrawerOpen(GravityCompat.START)) {
            // Close the navigation drawer
            mDrawer.closeDrawers();
            return true;
        } else if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5
                && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0
                && fragment.getClass() != MainFragment.class) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.main_container);
        if(fragment.getClass() != MainFragment.class) {
            fragment = new MainFragment();
            fragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit();
            // Highlight the selected item has been done by NavigationView
            mainMenuItem.setChecked(true);
            // Set action bar title
            setTitle(mainMenuItem.getTitle());
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }


    private void initAuthStateListener() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
//                    String username = user.getDisplayName();
//                    if (username == null) {
//                        username = user.getEmail();
//                    }
                    onSignedInInitialize(firebaseAuth.getCurrentUser());

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

        ValueEventListener mNewUserListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                editUserProfile = !dataSnapshot.exists();
                if (editUserProfile) {
                    // 1st registration
                    API.createUser(display_name, email);
                }
                editDogProfile = !dataSnapshot.child("dog").hasChild("name")
                        || dataSnapshot.child("dog").child("name").getValue().equals("");
                if (editDogProfile || editUserProfile){
                    startEditProfileActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        API.mDatabaseUsersRef.child(user_id).addListenerForSingleValueEvent(mNewUserListener);
        API.attachCurrUserDataReadListener();
    }

    private void onSignedOutCleanup() {
        // clear adapters if any populated
        // currently none is populated
        API.detachCurrUserDataReadListener();
        API.currUserUid = null;
        API.currUserData = null;
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
//                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void startEditProfileActivity() {
        Toast.makeText(this, "Need to add dog data", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, DogRegistrationActivity.class);
        startActivity(intent);

    }
}
