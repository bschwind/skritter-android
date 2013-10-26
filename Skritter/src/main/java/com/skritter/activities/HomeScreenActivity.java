package com.skritter.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.skritter.R;
import com.skritter.SkritterAPI;
import com.skritter.SkritterApplication;

public class HomeScreenActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get rid of the app title element
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set our layout
        setContentView(R.layout.activity_home);

        // Get the user ID from shared preferences
        SharedPreferences settings = getSharedPreferences(SkritterApplication.SKRITTER_SHARED_PREFERENCES, MODE_PRIVATE);
        String userID = settings.getString(SkritterApplication.PreferenceKeys.USER_ID, "");

        // Set the user ID on the welcome message
        Resources resources = getResources();
        String welcomeText = String.format(resources.getString(R.string.home_welcome_message, userID));
        TextView welcomeEditText = (TextView) findViewById(R.id.welcomeMessage);
        welcomeEditText.setText(welcomeText);
    }

    public void onClickStudy(View view) throws Exception {
        Intent intent = new Intent(this, StudyActivity.class);
        startActivity(intent);
    }

    public void onClickAdvancedStudy(View view) throws Exception {
        Intent intent = new Intent(this, StudyActivity.class);
        startActivity(intent);
    }

    public void onClickProgress(View view) throws Exception {

    }

    public void onClickLists(View view) throws Exception {

    }

    public void onClickSettings(View view) throws Exception {

    }

    public void onClickHelp(View view) throws Exception {

    }
}
