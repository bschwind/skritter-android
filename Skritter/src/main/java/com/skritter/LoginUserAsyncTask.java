package com.skritter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;

import java.util.logging.Logger;

public class LoginUserAsyncTask extends AsyncTask<String, Void, Boolean> {

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

            System.out.println("FUCK YOU");
            // if everything is ok return true
            return true;
        }

        return false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
//            progressDialog = new ProgressDialog(activity);
//        } else {
//            progressDialog = new ProgressDialog(activity, AlertDialog.THEME_HOLO_LIGHT);
//        }
//        progressDialog.setIndeterminate(true);
//        progressDialog.setMessage("Logging in...");
//        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        if (result) {
            // open new activity
        } else {
            // show your error dialog.
        }
    }
}