package com.firebase.petti.petti;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DogRegistrationActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "DogRegistrationActivity";

    //TODO YAHAV: Fields to upload

    String dogName;
    String dogAge;
    boolean dog_is_female;
    String dogType;
    List<String> dogCharacters = new ArrayList<String>();
    String dogDescreption;
    String preferedPartners;
    String commonWalkPlaces;
    //TODO picture....






    /*views*/
    EditText nameView;
    EditText ageView ;
    TextInputEditText petDescreptionText;
    TextInputEditText preferdPartnersText;
    TextInputEditText commonWalkPlacesText;
    Button uploadButton;
    ImageView petImage;
    enum Gender{Male,Female};
    Gender gender;
    final String[] dog_type = new String[1];
    final String[] dog_charater = new String[1];





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dog_registration);


        nameView = (EditText) findViewById(R.id.pet_name);
        ageView = (EditText) findViewById(R.id.pet_age);
        uploadButton = (Button) findViewById(R.id.uploadButton);
        petImage = (ImageView) findViewById(R.id.pet_image);
        petDescreptionText = (TextInputEditText) findViewById(R.id.pet_descreption_text);
        preferdPartnersText = (TextInputEditText) findViewById(R.id.preferd_walk_partners_text);
        commonWalkPlacesText = (TextInputEditText) findViewById(R.id.common_walk_places_text);

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
                Toast.makeText(adapterView.getContext(), "type :" + dog_type[0], Toast.LENGTH_SHORT).show();
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
                Toast.makeText(adapterView.getContext(), "character :" + dog_charater[0], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });





    }


    public void uploadImageMethod(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                    String path = getPathFromURI(selectedImageUri);
                    Log.i(TAG, "Image Path : " + path);
                    // Set the image in ImageView
                    petImage.setImageURI(selectedImageUri);

                }
            }
        }
    }

    public void genderSelection(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
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
         dogAge = ageView.getText().toString();
         dog_is_female = (gender == Gender.Female);
         dogType = dog_type[0];
         dogCharacters.add(dog_charater[0]);
         dogDescreption = petDescreptionText.getText().toString();
         preferedPartners = preferdPartnersText.getText().toString();
         commonWalkPlaces = commonWalkPlacesText.getText().toString();

        //move to edit profile
        startUserRegistrationActivity(view);

    }


    public void startUserRegistrationActivity(View view) {
        Intent intent = new Intent(this, UserRegistrationActivitey.class);
        startActivity(intent);
    }
}
