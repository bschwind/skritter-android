package com.skritter.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.skritter.SkritterAPI;
import com.skritter.SkritterApplication;
import com.skritter.taskFragments.LoginTaskFragment;
import com.skritter.R;

public class LoginActivity extends FragmentActivity implements LoginTaskFragment.TaskCallbacks {

    private LoginTaskFragment taskFragment;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get rid of the app title element
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set our layout
        setContentView(R.layout.activity_login);

        // This bit of code sets the hint font for the password field to be the default font
        // instead of a fixed-width courier font
        EditText password = (EditText) findViewById(R.id.password);
        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());

        // Android Activity lifecycle notes, because I'm a noob
        //onCreate();
        //onStart();
        //onResume(); - Now Running
        //onPause(); -> onResume();
        //onStop();  -> onRestart(); -> onStart() -> onRestoreInstanceState();
        //onDestroy();


        // Restore saved state using the code pattern below
        if (savedInstanceState != null) {
            //savedInstanceState.get("someKey");
        }

        FragmentManager fm = getSupportFragmentManager();
        taskFragment = (LoginTaskFragment) fm.findFragmentByTag("task");

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (taskFragment == null) {
            taskFragment = new LoginTaskFragment();
            fm.beginTransaction().add(taskFragment, "task").commit();
        }

        if (taskFragment.isRunning()) {
            initializeProgressDialog();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Store shit about this particular activity instance here
        //outState.putString("key", "value");
    }

    public void login(View view) throws Exception {
        // Hide the software keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        String username = ((EditText) findViewById(R.id.username)).getText().toString();
        String password = ((EditText) findViewById(R.id.password)).getText().toString();

        taskFragment.start(username, password);
    }

    private void initializeProgressDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            progressDialog = new ProgressDialog(this);
        } else {
            progressDialog = new ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT);
        }
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Logging in...");
        progressDialog.show();
    }

    @Override
    public void onPreExecute() {
        initializeProgressDialog();
    }

    @Override
    public void onCancelled() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onPostExecute(SkritterAPI.LoginStatus loginStatus) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (loginStatus.isLoggedIn()) {
            storeLoginInfo(loginStatus);

            Intent intent = new Intent(this, DrawingActivity.class);
            startActivity(intent);
        } else {
            // Invalid username toast
        }
    }

    private void storeLoginInfo(SkritterAPI.LoginStatus loginStatus) {
        SharedPreferences settings = getSharedPreferences(SkritterApplication.SKRITTER_SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(SkritterApplication.PreferenceKeys.USER_ID, loginStatus.getUserID());
        editor.putInt(SkritterApplication.PreferenceKeys.SECONDS_BEFORE_EXPIRING, loginStatus.getSecondsBeforeExpiring());
        editor.putString(SkritterApplication.PreferenceKeys.REFRESH_TOKEN, loginStatus.getRefreshToken());
        editor.putString(SkritterApplication.PreferenceKeys.ACCESS_TOKEN, loginStatus.getAccessToken());

        editor.commit();
    }
}
