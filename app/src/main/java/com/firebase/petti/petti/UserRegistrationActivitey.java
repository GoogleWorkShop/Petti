package com.firebase.petti.petti;

import android.app.DatePickerDialog;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.firebase.petti.db.API;
import com.firebase.petti.db.LocationsApi;
import com.firebase.petti.db.classes.User.Owner;
import com.firebase.petti.petti.utils.ImageLoaderUtils;
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


public class UserRegistrationActivitey extends AppCompatActivity {



    Owner currOwnerData = new Owner();
    boolean finishedSignUp = false;
    String userName;
    String userBD;
    Boolean user_is_female;
    String cityStr;
    //List<String> lookingForList = new ArrayList<String>();
    String userDescreption;
    //String userNickname;

    Place newAddressPlace;

    private static final int SELECT_PICTURE = 100;
    private static final int SELECT_LOCATION = 200;
    private static final String TAG = UserRegistrationActivitey.class.getSimpleName();
    EditText nameView;
    TextView BDView;
    Button uploadButton;
    ImageView userImage;
    TextView addressText;
    final String[] looking4 = new String[1];
    RadioButton maleButton;
    RadioButton femaleButton;

    public void ShowDatePicker(View view) {
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                userBD = Integer.toString(dayOfMonth)+"/" + Integer.toString(month+1) + "/" + Integer.toString(year);
                BDView.setText(userBD);
            }
        };
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(UserRegistrationActivitey.this,dateListener, now
                .get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)).show();
    }

    enum Gender {Male, Female}

    UserRegistrationActivitey.Gender gender;
    TextInputEditText userDescreptionView;
    TextInputEditText nicknameView;

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
        userDescreptionView = (TextInputEditText) findViewById(R.id.user_descreption);
       // nicknameView = (TextInputEditText) findViewById(R.id.user_nickname);
        maleButton = (RadioButton) findViewById(R.id.user_gender_male_radio);
        femaleButton = (RadioButton) findViewById(R.id.user_gender_female_radio);

        addressText = (TextView) findViewById(R.id.address_str);

//        userEmailView = (TextView) findViewById(R.id.user_email_view);

        newAddressPlace = null;


        //change button text acoording to ui flow, it its from initail registration: move to user reg,
        //if it is from editing profile, go back to main
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();


        //fill views with data
        currOwnerData = API.getCurrOwnerData();
        if(currOwnerData != null) {
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
            if (cityStr != null){
                addressText.setText(cityStr);

            }
//            //email
//            userEmail = currOwnerData.getMail();
//            if (userEmail != null) {
//                userEmailView.setText(userEmail);
//            }
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

            //descreption
            userDescreption = currOwnerData.getDescription();
            if (userDescreption != null) {
                userDescreptionView.setText(userDescreption);
            }
//            //nickname
//            userNickname = currOwnerData.getNickname();
//            if (userNickname != null) {
//                nicknameView.setText(userNickname);
//            }
        }

//        //Lokking for spinner
//        Spinner looking_4_spinner = (Spinner) findViewById(R.id.user_looking4_spinner);
//        // Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> looking4Adapter = ArrayAdapter.createFromResource(this,
//                R.array.looking4_array, android.R.layout.simple_spinner_item);
//        // Specify the layout to use when the list of choices appears
//        looking4Adapter.setDropDownViewResource(R.layout.dog_ype_spinner_item);
//        // Apply the adapter to the spinner
//        looking_4_spinner.setAdapter(looking4Adapter);
//        looking_4_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
//                looking4[0] = adapterView.getItemAtPosition(pos).toString();
//                Toast.makeText(adapterView.getContext(), "looking 4 :" + looking4[0], Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
//
//        // set looking 4
//        if(currOwnerData != null) {
//            //set looking 4 list
//            lookingForList = currOwnerData.getLookingForList();
//            if(lookingForList != null && lookingForList.size() > 0){
//                looking_4_spinner.setSelection(looking4Adapter.getPosition(lookingForList.get(0)));
//                looking4[0] = lookingForList.get(0);
//            } else {
//                lookingForList = new ArrayList<>();
//            }

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
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
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
            // Get the url from data
            Uri selectedImageUri = data.getData();
            userImage.setImageURI(selectedImageUri);
            StorageReference photoRef = API.mOwnerPhotos
                    .child(API.currUserUid)
                    .child(selectedImageUri.getLastPathSegment());
            // Upload file to Firebase Storage
            UploadTask uploadImageTask = photoRef.putFile(selectedImageUri);
            uploadImageTask.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // When the image has successfully uploaded, we get its download URL
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    currOwnerData.setPhotoUrl(downloadUrl.toString());
//                    setDogImage();
                }
            })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            uploadImageFailedToast();
                            setOwnerImage();
                        }
                    });
        }
        else if (requestCode == SELECT_LOCATION) {
            if (resultCode == RESULT_OK) {
                newAddressPlace = PlaceAutocomplete.getPlace(this, data);
                cityStr = (String) newAddressPlace.getName();
                addressText.setText(cityStr);
//                API.addStaticLocation(place.getLatLng());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    private void uploadImageFailedToast(){
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


//    public String getPathFromURI(Uri contentUri) {
//        String res = null;
//        String[] proj = {MediaStore.Images.Media.DATA};
//        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
//        if (cursor.moveToFirst()) {
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            res = cursor.getString(column_index);
//        }
//        cursor.close();
//        return res;
//    }


    @Override
    public void onBackPressed() {

        if(!finishedSignUp) {
            Toast.makeText(this, "Please Finish Your Registration", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            super.onBackPressed();
        }
    }

    public void MoveToMainAndUploodUserToDB(View view) {

        //fill fields to pass to db
        userName = nameView.getText().toString();
        if(userName.length() < 2){
            nameView.requestFocus();
            nameView.setError("Your Name Is A Must");
            return;
        }
        cityStr = addressText.getText().toString();
        if(cityStr == null || cityStr.isEmpty() || cityStr.startsWith("Tap here")){
            addressText.requestFocus();
            addressText.setError("Must Select Your Home Address");
            return;
        }

        userBD = BDView.getText().toString();
        user_is_female = (gender == UserRegistrationActivitey.Gender.Female);
//        cityStr = city[0];
      //  lookingForList.clear();
       // lookingForList.add(looking4[0]);
        userDescreption = userDescreptionView.getText().toString();
       // userNickname = nicknameView.getText().toString();

        currOwnerData.setName(userName);
        currOwnerData.setAge(userBD);
        currOwnerData.setFemale(user_is_female);
        currOwnerData.setCity(cityStr);
    //    currOwnerData.setLookingForList(lookingForList);
        currOwnerData.setDescription(userDescreption);
     //   currOwnerData.setNickname(userNickname);

        LocationsApi.addStaticLocation(newAddressPlace);

        API.setOwner(currOwnerData);


        //move to Main activitey
        startMainActivity(view);

    }

    public void startMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}