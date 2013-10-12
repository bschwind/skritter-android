package com.skritter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class LoginActivity extends Activity implements LoginFrament.Callbacks {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        // This bit of code sets the hint font for the password field to be the default font
        // instead of a fixed-width courier font
        EditText password = (EditText) findViewById(R.id.password);
        password.setTypeface(Typeface.DEFAULT);
        password.setTransformationMethod(new PasswordTransformationMethod());
    }

    public void login(View view) throws Exception {
        // Hide the software keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        new LoginUser().execute("lol", "lol");
    }

    @Override
    public void onTaskFinished() {

    }

    private class LoginUser extends AsyncTask<String, Void, Boolean> {

        private ProgressDialog progressDialog;
        @Override
        protected Boolean doInBackground(String... params) {

            if(params != null && params.length >= 1){
                String username = params[0];
                String password = params[1];

                try {
                    Thread.sleep(2000);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // if everything is ok return true
                return true;

            }

            return false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                progressDialog = new ProgressDialog(LoginActivity.this);
            } else {
                progressDialog = new ProgressDialog(LoginActivity.this, AlertDialog.THEME_HOLO_LIGHT);
            }
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Logging in...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            if (result) {
                // open new activity
            } else {
                // show your error dialog.
            }

        }

    }
    
}
