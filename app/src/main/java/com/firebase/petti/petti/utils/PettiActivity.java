package com.firebase.petti.petti.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.petti.db.API;
import com.firebase.petti.petti.SplashActivity;

/**
 * Created by yahav on 1/29/2017.
 */

public abstract class PettiActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!API.verifyMandatoryData()){
            Intent splashIntent = new Intent(this, SplashActivity.class);
            this.startActivity(splashIntent);
            finish();
        }
    }
}
