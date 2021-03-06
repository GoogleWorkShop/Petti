package com.firebase.petti.petti;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.petti.db.API;
import com.firebase.petti.db.LocationsApi;
import com.firebase.petti.db.classes.User.Owner;
import com.firebase.petti.petti.utils.ImageLoaderUtils;
import com.firebase.petti.petti.utils.MyBounceInterpolator;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * This activity is responsible for registering the a new user - getting and populating
 * his details. These details are assigned to the "Owner" object in the database, as well as to
 * the storage section of the database in which the uploaded image will be stored. Additionally,
 * the static location of the user will also be stored in the location section of the database. t
 * This static location will then be used for the "neighbouring dogs" feature which list users with
 * static locations near you.
 */
public class UserRegistrationActivitey extends AppCompatActivity {


    Owner currOwnerData = new Owner();
    boolean finishedSignUp = false;
    String userName;
    String userBD;
    Boolean user_is_female;
    String cityStr;
    String userDescreption;

    Place newAddressPlace;
    boolean isEditState;

    private static final int SELECT_PICTURE = 100;
    private static final int SELECT_LOCATION = 200;
    private static final String TAG = UserRegistrationActivitey.class.getSimpleName();
    EditText nameView;
    TextView BDView;
    Button uploadButton;
    ImageView userImage;
    TextView addressText;
    RadioButton maleButton;
    RadioButton femaleButton;

    public void ShowDatePicker(View view) {
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                userBD = Integer.toString(dayOfMonth) + "/" + Integer.toString(month + 1) + "/" + Integer.toString(year);
                BDView.setText(userBD);
            }
        };
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(UserRegistrationActivitey.this, dateListener, now
                .get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)).show();
    }

    enum Gender {Male, Female}

    UserRegistrationActivitey.Gender gender;
    TextInputEditText userDescreptionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //init views
        nameView = (EditText) findViewById(R.id.user_name);
        BDView = (TextView) findViewById(R.id.user_BD);
        uploadButton = (Button) findViewById(R.id.user_uploadButton);
        userImage = (ImageView) findViewById(R.id.user_image);
        userDescreptionView = (TextInputEditText) findViewById(R.id.user_description);
        maleButton = (RadioButton) findViewById(R.id.user_gender_male_radio);
        femaleButton = (RadioButton) findViewById(R.id.user_gender_female_radio);

        addressText = (TextView) findViewById(R.id.address_str);

        newAddressPlace = null;

        //change button text according to ui flow, it its from initial
        //registration: move to user reg, if it is from editing profile,
        //go back to main
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            try {
                isEditState = (Boolean) bundle.get("edit");
            } catch (NullPointerException e){
                isEditState = false;
            }
        }

        //fill views with data
        currOwnerData = API.getCurrOwnerData();
        if (currOwnerData != null) {
            //name
            userName = currOwnerData.getName();
            if (userName != null) {
                nameView.setText(userName);
            }
            //age
            userBD = currOwnerData.getAge();
            if (userBD != null) {
                BDView.setText(userBD);
            }

            //address
            cityStr = currOwnerData.getCity();
            if (cityStr != null) {
                addressText.setText(cityStr);

            }

            //gender
            user_is_female = currOwnerData.getFemale();
            if (user_is_female != null) {
                if (user_is_female) {
                    femaleButton.toggle();
                }
                if (!user_is_female) {
                    maleButton.toggle();
                }
            }

            //description
            userDescreption = currOwnerData.getDescription();
            if (userDescreption != null) {
                userDescreptionView.setText(userDescreption);
            }
        }

        // there is a city and a name, therefore the user is allready signed in
        finishedSignUp = (!(cityStr == null || cityStr.isEmpty() || cityStr.startsWith("Tap here")) && (userName.length() < 2));

        setOwnerImage();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void uploadImageMethod(View view) {
        final Animation myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce);
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);
        myAnim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

            }
        });
        uploadButton.startAnimation(myAnim);

    }

    public void ChooseStaticLocationMethod(View view) {
        try {

            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                    .build();
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                            .setFilter(typeFilter)
                            .build(this);

            startActivityForResult(intent, SELECT_LOCATION);
        } catch (GooglePlayServicesRepairableException e) {
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            InputStream imageStream = null;
            try {
                imageStream = getContentResolver().openInputStream(imageUri);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
            selectedImage = getResizedBitmap(selectedImage,400);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            userImage.setImageBitmap(selectedImage);
            StorageReference photoRef = API.mOwnerPhotos
                    .child(API.currUserUid)
                    .child(imageUri.getLastPathSegment());
            // Upload file to Firebase Storage
            UploadTask uploadImageTask = photoRef.putBytes(byteArray);
            uploadImageTask.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // When the image has successfully uploaded, we get its download URL
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    currOwnerData.setPhotoUrl(downloadUrl.toString());
                }
            })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            uploadImageFailedToast();
                            setOwnerImage();
                        }
                    });
        } else if (requestCode == SELECT_LOCATION) {
            if (resultCode == RESULT_OK) {
                newAddressPlace = PlaceAutocomplete.getPlace(this, data);
                cityStr = (String) newAddressPlace.getName();
                addressText.setText(cityStr);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void uploadImageFailedToast() {
        Toast.makeText(this, "Failed to upload image..", Toast.LENGTH_SHORT).show();
    }

    private void setOwnerImage() {
        if (currOwnerData != null) {
            ImageLoaderUtils.setImage(currOwnerData.getPhotoUrl(), userImage);
        }
    }

    public void genderSelection(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.user_gender_male_radio:
                if (checked)
                    gender = UserRegistrationActivitey.Gender.Male;
                break;
            case R.id.user_gender_female_radio:
                if (checked)
                    gender = UserRegistrationActivitey.Gender.Female;
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (!finishedSignUp) {
            Toast.makeText(this, "Please Finish Your Registration", Toast.LENGTH_SHORT).show();
            return;
        } else {
            super.onBackPressed();
        }
    }

    public void MoveToMainAndUploadUserToDB(View view) {
        if (!updateFields(true)) {
            return;
        }

        //move to Main activitey
        startMainActivity(view);

    }
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        Bitmap toRet = Bitmap.createScaledBitmap(image, width, height, true);

        return toRet;
    }
    private boolean updateFields(boolean stubborn) {
        //fill fields to pass to db
        userName = nameView.getText().toString();
        if (userName.length() < 2 && stubborn) {
            nameView.requestFocus();
            nameView.setError("Your Name Is A Must");
            return false;
        }
        cityStr = addressText.getText().toString();
        if ((cityStr == null ||
                cityStr.isEmpty() ||
                cityStr.startsWith("Tap here"))
                && stubborn) {
            addressText.requestFocus();
            addressText.setError("Must Select Your Home Address");
            return false;
        }

        userBD = BDView.getText().toString();
        user_is_female = (gender == Gender.Female);
        userDescreption = userDescreptionView.getText().toString();

        currOwnerData.setName(userName);
        currOwnerData.setAge(userBD);
        currOwnerData.setFemale(user_is_female);
        currOwnerData.setCity(cityStr);
        currOwnerData.setDescription(userDescreption);

        LocationsApi.addStaticLocation(newAddressPlace);

        API.setOwner(currOwnerData);
        return true;
    }

    public void startMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

}