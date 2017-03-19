package com.firebase.petti.petti;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import com.firebase.petti.db.classes.User.Dog;
import com.firebase.petti.petti.utils.ImageLoaderUtils;
import com.firebase.petti.petti.utils.MyBounceInterpolator;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DogRegistrationActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "DogRegistrationActivity";

    //TODO YAHAV: Fields to upload

    Dog currDogData = new Dog();

    String dogName;
    String dogBD;
    Boolean dog_is_female;
    String dogType;
    List<String> dogCharacters;

    boolean finishedSignUp = false;

    String dogDescription;

    boolean isEditState;

    EditText nameView;
    TextView BDView;
    TextInputEditText petDescriptionText;
    Button uploadButton;
    ImageView petImage;
    RadioButton dogMaleButton;
    RadioButton dogFemaleButton;

    enum Gender {Male, Female}
    Gender gender;
    final String[] dog_type = new String[1];
    final String[] dog_character = new String[1];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_registration);

        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // add back arrow to toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        nameView = (EditText) findViewById(R.id.pet_name);
        BDView = (TextView) findViewById(R.id.pet_BD);
        uploadButton = (Button) findViewById(R.id.uploadButton);
        petImage = (ImageView) findViewById(R.id.pet_image);
        petDescriptionText = (TextInputEditText) findViewById(R.id.pet_description_text);
        dogMaleButton = (RadioButton) findViewById(R.id.pet_gender_male_radio);
        dogFemaleButton = (RadioButton) findViewById(R.id.pet_gender_female_radio);

        //change button text acoording to ui flow, it its
        //from initail registration: move to user reg,
        //if it is from editing profile, go back to main
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            try {
                isEditState = (Boolean) bundle.get("edit");
            } catch (NullPointerException e){
                isEditState = false;
            }
        }

        //fill dog data if exists from DB
        currDogData = API.getCurrDogData();

        if (currDogData != null) {

            //name
            dogName = currDogData.getName();
            if (dogName != null) {
                nameView.setText(dogName);
            }
            //age
            dogBD = currDogData.getAge();
            if (dogBD != null) {
                BDView.setText(dogBD);
            }
            //gender
            dog_is_female = currDogData.getFemale();
            if (dog_is_female != null) {
                if (dog_is_female) {
                    dogFemaleButton.toggle();
                }
                if (!dog_is_female) {
                    dogMaleButton.toggle();
                }
            }

            //description
            dogDescription = currDogData.getDescription();
            if (dogDescription != null) {
                petDescriptionText.setText(dogDescription);
            }

            //if in that point the dog have a name that means the user is allready sign in
            finishedSignUp = dogName.length() > 2;

            setDogImage();

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    //when upload button is clicked upload picture chooser dialog
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


    //after the user choose picture, upadte the db and and activity
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            // Get the url from data
            Uri selectedImageUri = data.getData();
            petImage.setImageURI(selectedImageUri);
            StorageReference photoRef = API.mPetPhotos
                    .child(API.currUserUid)
                    .child(selectedImageUri.getLastPathSegment());
            // Upload file to Firebase Storage
            UploadTask uploadImageTask = photoRef.putFile(selectedImageUri);
            uploadImageTask.addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // When the image has successfully uploaded, we get its download URL
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    currDogData.setPhotoUrl(downloadUrl.toString());
                }
            })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            uploadImageFailedToast();
                            setDogImage();
                        }
                    });
        }
    }
    // notify that the upload has faild
    private void uploadImageFailedToast() {
        Toast.makeText(this, "Failed to upload image..", Toast.LENGTH_SHORT).show();
    }
    //update the activity with the new image
    private void setDogImage() {
        if (currDogData != null) {
            ImageLoaderUtils.setImage(currDogData.getPhotoUrl(), petImage);
        }
    }

    public void genderSelection(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.pet_gender_male_radio:
                if (checked)
                    gender = Gender.Male;
                break;
            case R.id.pet_gender_female_radio:
                if (checked)
                    gender = Gender.Female;
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

    public void MoveToEditProfileAndUploadPet(View view) {
        if (!updateFields(true)) {
            return;
        }

        //move to edit profile
        if (isEditState) {
            startMainActivity(view);
        } else {
            startUserRegistrationActivity(view);
        }
    }

    private boolean updateFields(boolean stubborn) {
        //fill fields to pass to db
        dogName = nameView.getText().toString();

        if (dogName.length() < 2 && stubborn) {
            nameView.setError("A Dog's Name Is A must");
            return false;
        }

        dogBD = BDView.getText().toString();
        dog_is_female = (gender == Gender.Female);
        dogType = dog_type[0];
        if (dogCharacters == null) {
            dogCharacters = new ArrayList<>();
        }
        dogCharacters.clear();
        dogCharacters.add(dog_character[0]);
        dogDescription = petDescriptionText.getText().toString();

        currDogData.setName(dogName);
        currDogData.setAge(dogBD);
        currDogData.setFemale(dog_is_female);
        currDogData.setType(dogType);
        currDogData.setPersonalityAttributes(dogCharacters);
        currDogData.setDescription(dogDescription);

        API.setDog(currDogData);
        return true;
    }

    public void startMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void startUserRegistrationActivity(View view) {
        Intent intent = new Intent(this, UserRegistrationActivitey.class);
        startActivity(intent);
    }
  // date picker to choose the dog age
    public void ShowDatePicker(View view) {
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                dogBD = Integer.toString(dayOfMonth) + "/" + Integer.toString(month + 1) + "/" + Integer.toString(year);
                BDView.setText(dogBD);
            }
        };
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(DogRegistrationActivity.this, dateListener, now
                .get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)).show();
    }
}
