package com.firebase.petti.petti;

import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.petti.db.API;
import com.firebase.petti.db.classes.User.Dog;
import com.firebase.petti.petti.utils.ImageLoaderUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

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

    String dogDescreption;
    String preferedPartners;
    String commonWalkPlaces;
    //TODO picture....

    boolean isEditState;

    EditText nameView;
    TextView BDView;
    TextInputEditText petDescreptionText;
    TextInputEditText preferdPartnersText;
    TextInputEditText commonWalkPlacesText;
    Button uploadButton;
    ImageView petImage;
    RadioButton dogMaleButton;
    RadioButton dogFemaleButton;
    Button moveToEditButton;



    enum Gender {Male, Female}

    ;
    Gender gender;
    final String[] dog_type = new String[1];
    final String[] dog_charater = new String[1];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_registration);


        nameView = (EditText) findViewById(R.id.pet_name);
        BDView = (TextView) findViewById(R.id.pet_BD);
        uploadButton = (Button) findViewById(R.id.uploadButton);
        petImage = (ImageView) findViewById(R.id.pet_image);
        petDescreptionText = (TextInputEditText) findViewById(R.id.pet_descreption_text);
        preferdPartnersText = (TextInputEditText) findViewById(R.id.preferd_walk_partners_text);
        commonWalkPlacesText = (TextInputEditText) findViewById(R.id.common_walk_places_text);
        dogMaleButton = (RadioButton) findViewById(R.id.pet_gender_male_radio);
        dogFemaleButton = (RadioButton) findViewById(R.id.pet_gender_female_radio);

        //change button text acoording to ui flow, it its from initail registration: move to user reg,
        //if it is from editing profile, go back to main
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();


        //dog type spinner
        Spinner dog_type_spinner = (Spinner) findViewById(R.id.pet_type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.dog_types_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        typeAdapter.setDropDownViewResource(R.layout.dog_ype_spinner_item);
        // Apply the adapter to the spinner
        dog_type_spinner.setAdapter(typeAdapter);
        dog_type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                dog_type[0] = adapterView.getItemAtPosition(pos).toString();
//                Toast.makeText(adapterView.getContext(), "type :" + dog_type[0], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        //character spinner
        Spinner dog_character_spinner = (Spinner) findViewById(R.id.pet_character_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> characterAdapter = ArrayAdapter.createFromResource(this,
                R.array.dog_character_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        characterAdapter.setDropDownViewResource(R.layout.dog_ype_spinner_item);
        // Apply the adapter to the spinner
        dog_character_spinner.setAdapter(characterAdapter);
        dog_character_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                dog_charater[0] = adapterView.getItemAtPosition(pos).toString();
//                Toast.makeText(adapterView.getContext(), "character :" + dog_charater[0], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

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
            //walk places
            commonWalkPlaces = currDogData.getWalkWhere();
            if (commonWalkPlaces != null) {
                commonWalkPlacesText.setText(commonWalkPlaces);
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

            //descreption
            dogDescreption = currDogData.getDescription();
            if (dogDescreption != null) {
                petDescreptionText.setText(dogDescreption);
            }
            //partners
            preferedPartners = currDogData.getWalkWith();
            if (preferedPartners != null) {
                preferdPartnersText.setText(preferedPartners);
            }

            dogType = currDogData.getType();
            if (dogType != null) {
                dog_type_spinner.setSelection(typeAdapter.getPosition(dogType));
                dog_type[0] = dogType;
            }

            //set looking 4 list
            dogCharacters = currDogData.getPersonalityAttributes();
            if (dogCharacters != null && dogCharacters.size() > 0) {
                dog_character_spinner.setSelection(characterAdapter.getPosition(dogCharacters.get(0)));
                dog_charater[0] = dogCharacters.get(0);
            }

            setDogImage();

        }


    }


    public void uploadImageMethod(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            // Get the url from data
//            Uri selectedImageUri = data.getData();
//            if (null != selectedImageUri) {
//                // Get the path from the Uri
//                String path = getPathFromURI(selectedImageUri);
//                Log.i(TAG, "Image Path : " + path);
//                // Set the image in ImageView
//                petImage.setImageURI(selectedImageUri);
//            }
            final Uri selectedImageUri = data.getData();
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
//                    setDogImage();
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

    private void uploadImageFailedToast(){
        Toast.makeText(this, "Failed to upload image..", Toast.LENGTH_SHORT).show();
    }

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


    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    //TODO YAHAV : this is the button listener that will move the user to edit his profile, now here you can upload all the pet details the you have as fields under tha comment "Fields to upload"
    public void MoveToEditProfileAndUploadPet(View view) {
        //fill fields to pass to db
        dogName = nameView.getText().toString();

        if (dogName.length() < 2) {
            Toast.makeText(this, "Must add at least name", Toast.LENGTH_SHORT).show();
            return;
        }

        dogBD = BDView.getText().toString();
        dog_is_female = (gender == Gender.Female);
        dogType = dog_type[0];
        if(dogCharacters == null){
            dogCharacters = new ArrayList<String>();
        }
        dogCharacters.add(dog_charater[0]);
        dogDescreption = petDescreptionText.getText().toString();
        preferedPartners = preferdPartnersText.getText().toString();
        commonWalkPlaces = commonWalkPlacesText.getText().toString();

        currDogData.setName(dogName);
        currDogData.setAge(dogBD);
        currDogData.setFemale(dog_is_female);
        currDogData.setType(dogType);
        currDogData.setPersonalityAttributes(dogCharacters);
        currDogData.setDescription(dogDescreption);
        currDogData.setWalkWith(preferedPartners);
        currDogData.setWalkWhere(commonWalkPlaces);

        API.setDog(currDogData);


        //move to edit profile
        if(isEditState){
            startMainActivity(view);
        }
        else{
            startUserRegistrationActivity(view);
        }


    }



    public void startMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void startUserRegistrationActivity(View view) {
        Intent intent = new Intent(this, UserRegistrationActivitey.class);
        startActivity(intent);
    }

    public void ShowDatePicker(View view) {
        DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
               dogBD = Integer.toString(dayOfMonth)+"/" + Integer.toString(month+1) + "/" + Integer.toString(year);
                BDView.setText(dogBD);
            }
        };
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(DogRegistrationActivity.this,dateListener, now
                .get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)).show();
    }
}
