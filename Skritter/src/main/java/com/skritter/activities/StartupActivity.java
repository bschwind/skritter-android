package com.skritter.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.skritter.SkritterApplication;

public class StartupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent;
        if (userIsAuthenticated()) {
            intent = new Intent(this, HomeScreenActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }

    private boolean userIsAuthenticated() {
        SharedPreferences settings = getSharedPreferences(SkritterApplication.SKRITTER_SHARED_PREFERENCES, MODE_PRIVATE);

        String userID = settings.getString(SkritterApplication.PreferenceKeys.USER_ID, "");
        int secondsBeforeExpiring = settings.getInt(SkritterApplication.PreferenceKeys.SECONDS_BEFORE_EXPIRING, 0);
        String accessToken = settings.getString(SkritterApplication.PreferenceKeys.ACCESS_TOKEN, "");
        String refreshToken = settings.getString(SkritterApplication.PreferenceKeys.REFRESH_TOKEN, "");

        return !"".equals(accessToken);
    }
}
