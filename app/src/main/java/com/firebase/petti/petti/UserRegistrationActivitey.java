package com.firebase.petti.petti;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.firebase.petti.db.API;
import com.firebase.petti.db.classes.User.Owner;
import com.firebase.petti.petti.utils.ImageLoaderUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


public class UserRegistrationActivitey extends AppCompatActivity {


    //TODO YAHAV: Fields to upload

    Owner currOwnerData = new Owner();

    String userName;
    String userAge;
    Boolean user_is_female;
    String cityStr;
    List<String> lookingForList = new ArrayList<String>();
    String userDescreption;
    String userNickname;
    String userEmail;

    //TODO picture....


    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "UserRegActivity";
    EditText nameView;
    EditText ageView;
    Button uploadButton;
    ImageView userImage;
    final String[] city = new String[1];
    final String[] looking4 = new String[1];
    RadioButton maleButton;
    RadioButton femaleButton;
    ImageButton moveToMainButton;

    boolean isEditState;


    enum Gender {Male, Female}

    ;
    UserRegistrationActivitey.Gender gender;
    TextInputEditText userDescreptionView;
    TextInputEditText nicknameView;
    TextView userEmailView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);


        //init views
        nameView = (EditText) findViewById(R.id.user_name);
        ageView = (EditText) findViewById(R.id.user_age);
        uploadButton = (Button) findViewById(R.id.user_uploadButton);
        userImage = (ImageView) findViewById(R.id.user_image);
        userDescreptionView = (TextInputEditText) findViewById(R.id.user_descreption);
        nicknameView = (TextInputEditText) findViewById(R.id.user_nickname);
        maleButton = (RadioButton) findViewById(R.id.user_gender_male_radio);
        femaleButton = (RadioButton) findViewById(R.id.user_gender_female_radio);
        userEmailView = (TextView) findViewById(R.id.user_email_view);

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
            userAge = currOwnerData.getAge();
            if (userAge != null) {
                ageView.setText(userAge);
            }
            //email
            userEmail = currOwnerData.getMail();
            if (userEmail != null) {
                userEmailView.setText(userEmail);
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

            //descreption
            userDescreption = currOwnerData.getDescription();
            if (userDescreption != null) {
                userDescreptionView.setText(userDescreption);
            }
            //nickname
            userNickname = currOwnerData.getNickname();
            if (userNickname != null) {
                nicknameView.setText(userNickname);
            }
        }



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

        // set city selection
        if(currOwnerData != null) {
            cityStr = currOwnerData.getCity();
            if (cityStr != null) {
                city_spinner.setSelection(cityAdapter.getPosition(cityStr));
                city[0] = cityStr;
            }

            //set looking 4 list
            lookingForList = currOwnerData.getLookingForList();
            if(lookingForList != null && lookingForList.size() > 0){
                looking_4_spinner.setSelection(looking4Adapter.getPosition(lookingForList.get(0)));
                looking4[0] = lookingForList.get(0);
            } else {
                lookingForList = new ArrayList<>();
            }

            setOwnerImage();
        }

    }

    public void uploadImageMethod(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
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

    public void MoveToMainAndUploodUserToDB(View view) {

        //fill fields to pass to db
        userName = nameView.getText().toString();
        userAge = ageView.getText().toString();
        user_is_female = (gender == UserRegistrationActivitey.Gender.Female);
        cityStr = city[0];
        lookingForList.add(looking4[0]);
        userDescreption = userDescreptionView.getText().toString();
        userNickname = nicknameView.getText().toString();

        currOwnerData.setName(userName);
        currOwnerData.setAge(userAge);
        currOwnerData.setFemale(user_is_female);
        currOwnerData.setCity(cityStr);
        currOwnerData.setLookingForList(lookingForList);
        currOwnerData.setDescription(userDescreption);
        currOwnerData.setNickname(userNickname);

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
