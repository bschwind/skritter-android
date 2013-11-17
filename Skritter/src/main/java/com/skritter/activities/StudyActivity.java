package com.skritter.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.skritter.SkritterApplication;
import com.skritter.models.StudyItem;
import com.skritter.models.Vocab;
import com.skritter.persistence.SkritterDatabaseHelper;
import com.skritter.persistence.VocabTable;
import com.skritter.taskFragments.GetStudyItemsTaskFragment;
import com.skritter.views.PromptCanvas;
import com.skritter.R;

import java.util.List;

public class StudyActivity extends FragmentActivity implements GetStudyItemsTaskFragment.TaskCallbacks {
    private PromptCanvas promptCanvas;
    private GetStudyItemsTaskFragment getStudyItemsTaskFragment;
    private ProgressDialog progressDialog;
    private List<StudyItem> itemsToStudy;
    private int currentIndex = 0;
    private StudyItem currentItem;
    private SkritterDatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get rid of the app title element
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Set our layout
        setContentView(R.layout.activity_study);

        db = new SkritterDatabaseHelper(this);

        promptCanvas = (PromptCanvas) findViewById(R.id.canvas);
        promptCanvas.setBackgroundColor(Color.WHITE);
        promptCanvas.setPenColor(Color.BLACK);
        promptCanvas.setGridColor(Color.GRAY);

        // Restore saved state using the code pattern below
        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt("currentIndex");
        }

        FragmentManager fm = getSupportFragmentManager();
        getStudyItemsTaskFragment = (GetStudyItemsTaskFragment) fm.findFragmentByTag("getStudyItemsTask");

        // If the Fragment is non-null, then it is currently being
        // retained across a configuration change.
        if (getStudyItemsTaskFragment == null) {
            getStudyItemsTaskFragment = new GetStudyItemsTaskFragment();
            fm.beginTransaction().add(getStudyItemsTaskFragment, "getStudyItemsTask").commit();
        }

        if (getStudyItemsTaskFragment.isRunning()) {
            initializeProgressDialog();
        }

        SharedPreferences settings = getSharedPreferences(SkritterApplication.SKRITTER_SHARED_PREFERENCES, MODE_PRIVATE);
        String accessToken = settings.getString(SkritterApplication.PreferenceKeys.ACCESS_TOKEN, "");

        getStudyItemsTaskFragment.onAttach(this);
        getStudyItemsTaskFragment.start(accessToken);
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
        outState.putInt("currentIndex", currentIndex);
    }

    public void clearStrokes(View view) {
        promptCanvas.setShouldDrawStatusBorder(true);
        promptCanvas.setStatusBorderColor(Color.RED);
        promptCanvas.clearStrokes();
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

    public void onBack(View view) {
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = 0;
        }

        updateCurrentIndex();
    }

    public void onErase(View view) {
        promptCanvas.clearStrokes();
    }

    public void onShow(View view) {

    }

    public void onCorrect(View view) {

    }

    public void onNext(View view) {
        currentIndex++;
        if (currentIndex >= itemsToStudy.size()) {
            currentIndex = itemsToStudy.size() - 1;
        }

        updateCurrentIndex();
    }

    private void updateCurrentIndex() {
        currentItem = itemsToStudy.get(currentIndex);
        String[] vocabIDs = currentItem.getVocabIDs();

        Vocab vocab = VocabTable.getInstance().getByStringID(db, vocabIDs[0]);

        TextView text = (TextView) findViewById(R.id.itemDetails);
        text.setText(currentItem.getId());

        TextView timeText = (TextView) findViewById(R.id.itemTimes);
        timeText.setText("" + currentItem.getReviews());

        promptCanvas.setStudyItemAndVocab(currentItem, vocab);

        updateText();
    }

    private void updateText() {
        TextView text = (TextView) findViewById(R.id.itemDetails);
        text.setText(currentItem.getId() + ", " + currentItem.getPart());

        TextView timeText = (TextView) findViewById(R.id.itemTimes);
        timeText.setText("" + currentItem.getReviews());
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
    public void onPostExecute(List<StudyItem> result) {
        itemsToStudy = result;

        if (itemsToStudy.isEmpty()) {
            // We've got problems....try and re-fetch?
        }

        updateCurrentIndex();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
