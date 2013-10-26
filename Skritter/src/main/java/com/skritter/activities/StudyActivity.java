package com.skritter.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;

import com.skritter.SkritterAPI;
import com.skritter.SkritterApplication;
import com.skritter.taskFragments.GetStudyItemsTaskFragment;
import com.skritter.taskFragments.LoginTaskFragment;
import com.skritter.views.PromptCanvas;
import com.skritter.R;

public class StudyActivity extends FragmentActivity implements GetStudyItemsTaskFragment.TaskCallbacks {
    private PromptCanvas canvas;
    private GetStudyItemsTaskFragment taskFragment;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get rid of the app title element
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set our layout
        setContentView(R.layout.activity_study);

        canvas = (PromptCanvas) findViewById(R.id.canvas);
        canvas.setBackgroundColor(Color.LTGRAY);
        canvas.setPenColor(Color.BLACK);
        canvas.setGridColor(Color.GRAY);

        // Restore saved state using the code pattern below
        if (savedInstanceState != null) {
            //savedInstanceState.get("someKey");
        }

        FragmentManager fm = getSupportFragmentManager();
        taskFragment = (GetStudyItemsTaskFragment) fm.findFragmentByTag("getStudyItemsTask");

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (taskFragment == null) {
            taskFragment = new GetStudyItemsTaskFragment();
            fm.beginTransaction().add(taskFragment, "getStudyItemsTask").commit();
        }

        if (taskFragment.isRunning()) {
            initializeProgressDialog();
        }

        SharedPreferences settings = getSharedPreferences(SkritterApplication.SKRITTER_SHARED_PREFERENCES, MODE_PRIVATE);
        String accessToken = settings.getString(SkritterApplication.PreferenceKeys.ACCESS_TOKEN, "");

        taskFragment.onAttach(this);
        taskFragment.start(accessToken);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    public void clearStrokes(View view) {
        canvas.setShouldDrawStatusBorder(true);
        canvas.setStatusBorderColor(Color.RED);
        canvas.clearStrokes();
    }

    private void initializeProgressDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            progressDialog = new ProgressDialog(this);
        } else {
            progressDialog = new ProgressDialog(this, AlertDialog.THEME_HOLO_LIGHT);
        }
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Fetching items...");
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
    public void onPostExecute(Void result) {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

//        if (loginStatus.isLoggedIn()) {
//            storeLoginInfo(loginStatus);
//
//            Intent intent = new Intent(this, HomeScreenActivity.class);
//            startActivity(intent);
//        } else {
//            // Invalid username toast
//        }
    }

}
