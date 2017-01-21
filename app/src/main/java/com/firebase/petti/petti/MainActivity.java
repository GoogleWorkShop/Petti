package com.firebase.petti.petti;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.petti.db.classes.User.Dog;
import com.firebase.petti.petti.utils.ImageLoaderUtils;
import com.firebase.petti.petti.utils.UtilsDBHelper;
import com.firebase.ui.auth.AuthUI;

import com.firebase.petti.db.API;


/**
 * This is the main activity where we can navigate between the main application features.
 * The navigation is possible through the navigation drawer.
 * The main features (bark and dog neighbours) AND the light weight features are set as fragments
 * on top of this activity {@link MatchesFragment}.
 * The heavy weight features as the {@link MapsActivity} map and the chat {@link MatchedFriendsActivity}
 * Are activities in their own right.
 */
public class MainActivity extends AppCompatActivity {

    final Context context = this;

    //DB
    public static UtilsDBHelper m_dbHelper;

    private DrawerLayout mDrawer;

    private Toolbar toolbar;
    private MenuItem prevMenuItem;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView nvDrawer;

    private TextView drawerDogNameTextView;
    private ImageView drawerProfilePicImageView;

    /* this are for the location permission request */
    private static final int INITIAL_REQUEST = 1337;
    private static final String[] INITIAL_PERMS={
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };


    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /* MUST BE FIRST! MUST HAPPEN FIRST! */
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        /* ---REST OF CREATION CODE BELLOW--- */
        super.onCreate(savedInstanceState);

        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        m_dbHelper =  new UtilsDBHelper(this);

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

        prevMenuItem = nvDrawer.getMenu().findItem(R.id.default_fragment);

        //get widgets from drawer( profile pic and name)
        final View drawerProfileHeader = nvDrawer.inflateHeaderView(R.layout.main_nav_header);
        drawerDogNameTextView = (TextView) drawerProfileHeader.findViewById(R.id.dog_name_header);
        drawerProfilePicImageView = (ImageView) drawerProfileHeader.findViewById(R.id.profile_pic);

        setDrawerProfileInfo();

        Menu menuNav = nvDrawer.getMenu();
        MenuItem defaultFragmentItem = menuNav.findItem(R.id.default_fragment);
        defaultFragmentItem.setChecked(true);
        // Set action bar title
        setTitle(defaultFragmentItem.getTitle());

        // We want this to be the last so we will initiate every thing and then set the fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container, new MatchesFragment())
                    .commit();
        }
    }

    /* Drawer handling function */

    private void setDrawerProfileInfo(String dogName, String dogPhotoUrl){
        if (drawerDogNameTextView != null && drawerProfilePicImageView != null) {
            String defaultDog = getString(R.string.default_dog_name);
            if (dogPhotoUrl != null && !dogPhotoUrl.isEmpty()) {
                ImageLoaderUtils.setImage(dogPhotoUrl, drawerProfilePicImageView, R.drawable.anonymous_grn);
            } else {
                drawerProfilePicImageView.setImageResource(R.drawable.anonymous_prpl);
            }
            if (dogName != null && dogName.length() > 1) {
                drawerDogNameTextView.setText(dogName);
            } else {
                drawerDogNameTextView.setText(defaultDog);
            }
        }
    }

    public void setDrawerProfileInfo(){
        Dog myDog = API.getCurrDogData();
        if (myDog != null){
            String dogName = myDog.getName();
            String dogPhoto = myDog.getPhotoUrl();
            setDrawerProfileInfo(dogName, dogPhoto);
        } else {
            setDrawerProfileInfo("", "");
        }
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
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
        Bundle bundle = null;

        switch (menuItem.getItemId()) {
            case R.id.default_fragment:
                fragmentClass = MatchesFragment.class;
                bundle = new Bundle();
                bundle.putBoolean("bark", false);
                prevMenuItem = menuItem;
                break;
            case R.id.bark_fragment:
                if (!canAccessLocation()) {
                    Toast.makeText(context,
                            "All locations and no permissions makes Johnny a dull boy",
                            Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
                    }
                    setTitle(prevMenuItem.getTitle());
                    prevMenuItem.setChecked(true);
                    prevMenuItem = menuItem;
                    return;
                }
                Toast.makeText(context, R.string.get_ready_for_a_walk,
                        Toast.LENGTH_SHORT).show();
                fragmentClass = MatchesFragment.class;
                bundle = new Bundle();
                bundle.putBoolean("bark", true);
                prevMenuItem = menuItem;
                break;
            case R.id.food_notifications:
                fragmentClass = FoodNotificationsFragment.class;
                prevMenuItem = menuItem;
                break;
            case R.id.vaccination_card:
                fragmentClass = VaccinationCardFragment.class;
                prevMenuItem = menuItem;
                break;
            case R.id.find_near_map:
                prevMenuItem.setChecked(true);
                mDrawer.closeDrawers();
                Intent mapsIntent = new Intent(this, MapsActivity.class);
                mapsIntent.putExtra("neighbours", false);
                startActivity(mapsIntent);
                return;
            case R.id.sign_out:
                mDrawer.closeDrawers();
                AuthUI.getInstance().signOut(this);
                Intent signOutIntent = new Intent(this,SplashActivity.class);
                startActivityForResult(signOutIntent, 888);
                return;
            case R.id.edit_user_profile:
                prevMenuItem.setChecked(true);
                mDrawer.closeDrawers();
                Intent userIntent = new Intent(this,UserRegistrationActivitey.class);
                userIntent.putExtra("edit",true);
                startActivityForResult(userIntent, 0);
                return;
            case R.id.edit_dog_profile:
                prevMenuItem.setChecked(true);
                mDrawer.closeDrawers();
                Intent dogIntent = new Intent(this,DogRegistrationActivity.class);
                dogIntent.putExtra("edit",true);
                startActivityForResult(dogIntent, 0);
                return;
            case R.id.friends:
                prevMenuItem.setChecked(true);
                mDrawer.closeDrawers();
                Intent chatFriendsIntent = new Intent(this, MatchedFriendsActivity.class);
                startActivity(chatFriendsIntent);
                return;
            default:
                fragmentClass = MatchesFragment.class;
                prevMenuItem = nvDrawer.getMenu().findItem(R.id.default_fragment);
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* if this is a matches fragment we need to pass the bark variable */
        if (menuItem.getItemId() == R.id.default_fragment ||
                menuItem.getItemId() == R.id.bark_fragment){
            fragment.setArguments(bundle);
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

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // There are 2 signatures and only `onPostCreate(Bundle state)` shows the hamburger icon.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    /* End drawer Handling */


    /* Option pressed function */

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
            case R.id.settings_menu:
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.main_container,
                                new MyPreferencesFragment())
                        .commit();
                return true;
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* End option pressed Handling */

    /**
     *  Handle the logic of pressing the back button:
     *      if in neighbour dogs or bark - exit the app
     *      else - get back to neighbor dogs
     */
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
                && fragment.getClass() != MatchesFragment.class) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.main_container);
        if(fragment.getClass() != MatchesFragment.class) {
            fragment = new MatchesFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("bark", false);
            fragment.setArguments(bundle);
            fragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit();
            // Highlight the selected item has been done by NavigationView
            Menu menuNav = nvDrawer.getMenu();
            prevMenuItem = menuNav.findItem(R.id.default_fragment);
            prevMenuItem.setChecked(true);
            // Set action bar title
            setTitle(prevMenuItem.getTitle());
            return;
        }
        super.onBackPressed();
    }

    /* End back handling */

    /* Location permissions Function */

    private boolean canAccessLocation() {
        return(hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED== ContextCompat.checkSelfPermission(this, perm));
    }

    // Callback with the request from calling requestPermissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original GET_LOCATION request
        if (requestCode == INITIAL_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // pass
            } else {
                Toast.makeText(this,
                        "We can not play with you without your permission...",
                        Toast.LENGTH_SHORT).show();
            }
            setTitle(prevMenuItem.getTitle());
            prevMenuItem.setChecked(true);
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /* End location permissions Function */

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void startEditProfileActivity() {
        Toast.makeText(this, "Need to add dog information", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, DogRegistrationActivity.class);
        startActivity(intent);
    }

    // used in main_nav_header as on click
    public void EditDogProfile(View view){
        Intent intent = new Intent(this, DogRegistrationActivity.class);
        intent.putExtra("edit", true);
        startActivity(intent);
    }
}
