package com.skritter.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Window;

import com.skritter.R;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Get rid of the app title element
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        super.onCreate(savedInstanceState);
        
        addPreferencesFromResource(R.xml.preferences);
    }
}