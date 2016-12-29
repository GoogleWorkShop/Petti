package com.firebase.petti.petti;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import com.firebase.petti.db.API;
import com.firebase.petti.db.classes.User.Owner;


public class UserRegistrationActivitey extends AppCompatActivity {


    //YAHAV: Fields to upload

    Owner currOwnerData = new Owner();

    String userName;
    String userAge;
    boolean user_is_female;
    String cityStr;
    List<String> lookingForList = new ArrayList<String>();
    String UserDescreption;
    String userNickname;

    //picture....


    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "UserRegActivity";
    EditText nameView;
    EditText ageView;
    Button uploadButton;
    ImageView userImage;
    final String[] city = new String[1];
    final String[] looking4 = new String[1];


    enum Gender {Male, Female}

    ;
    UserRegistrationActivitey.Gender gender;
    TextInputEditText userDescreptionView;
    TextInputEditText nicknameView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        currOwnerData = API.getCurrOwnerData();

        nameView = (EditText) findViewById(R.id.user_name);
        ageView = (EditText) findViewById(R.id.user_age);
        uploadButton = (Button) findViewById(R.id.user_uploadButton);
        userImage = (ImageView) findViewById(R.id.user_image);
        userDescreptionView = (TextInputEditText) findViewById(R.id.user_descreption);
        nicknameView = (TextInputEditText) findViewById(R.id.user_nickname);

        //city spinner
        Spinner city_spinner = (Spinner) findViewById(R.id.user_city_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> cityAdapter = ArrayAdapter.createFromResource(this,
                R.array.city_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        cityAdapter.setDropDownViewResource(R.layout.dog_ype_spinner_item);
        // Apply the adapter to the spinner
        city_spinner.setAdapter(cityAdapter);
        city_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                city[0] = adapterView.getItemAtPosition(pos).toString();
                Toast.makeText(adapterView.getContext(), "city :" + city[0], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //Lokking for spinner
        Spinner looking_4_spinner = (Spinner) findViewById(R.id.user_looking4_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> looking4Adapter = ArrayAdapter.createFromResource(this,
                R.array.looking4_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        looking4Adapter.setDropDownViewResource(R.layout.dog_ype_spinner_item);
        // Apply the adapter to the spinner
        looking_4_spinner.setAdapter(looking4Adapter);
        looking_4_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {
                looking4[0] = adapterView.getItemAtPosition(pos).toString();
                Toast.makeText(adapterView.getContext(), "looking 4 :" + looking4[0], Toast.LENGTH_SHORT).show();
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
                    userImage.setImageURI(selectedImageUri);

                }
            }
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


    //YAHAV : this is the button listener that will move the user to edit his profile, now here you can upload all the pet details the you have as fields under tha comment "Fields to upload"
    public void MoveToMainAndUploodUserToDB(View view) {

        //fill fields to pass to db
        userName = nameView.getText().toString();
        userAge = ageView.getText().toString();
        user_is_female = (gender == UserRegistrationActivitey.Gender.Female);
        cityStr = city[0];
        lookingForList.add(looking4[0]);
        UserDescreption = userDescreptionView.getText().toString();
        userNickname = nicknameView.getText().toString();

        currOwnerData.setName(userName);
        currOwnerData.setAge(userAge);
        currOwnerData.setFemale(user_is_female);
        currOwnerData.setCity(cityStr);
        currOwnerData.setLookingForList(lookingForList);
        currOwnerData.setDescription(UserDescreption);
        currOwnerData.setNickname(userNickname);

        API.setOwner(currOwnerData);

        //move to Main activitey
        startMainActivity(view);

    }

    public void startMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

}
