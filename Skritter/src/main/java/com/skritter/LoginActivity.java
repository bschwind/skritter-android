package com.skritter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class LoginActivity extends Activity {

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

        //onCreate();
        //onStart();
        //onResume(); - Now Running
        //onPause(); -> onResume();
        //onStop();  -> onRestart(); -> onStart() -> onRestoreInstanceState();
        //onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Store shit about this particular activity instance here
    }

    public void login(View view) throws Exception {
        // Hide the software keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        // Start the async task
//        new LoginUserAsyncTask().execute("Username", "Password");

        Intent intent = new Intent(this, DrawingActivity.class);
        startActivity(intent);

    }
}
