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
import com.skritter.math.BoundingBox;
import com.skritter.models.StrokeData;
import com.skritter.models.StudyItem;
import com.skritter.math.Vector2;
import com.skritter.models.Vocab;
import com.skritter.persistence.SkritterDatabaseHelper;
import com.skritter.persistence.StrokeDataTable;
import com.skritter.persistence.VocabTable;
import com.skritter.taskFragments.GetStudyItemsTaskFragment;
import com.skritter.utils.ShortStraw;
import com.skritter.utils.StringUtil;
import com.skritter.views.PromptCanvas;
import com.skritter.R;

import java.util.Iterator;
import java.util.List;

public class StudyActivity extends FragmentActivity implements GetStudyItemsTaskFragment.TaskCallbacks {
    private PromptCanvas promptCanvas;
    private GetStudyItemsTaskFragment getStudyItemsTaskFragment;
    private ProgressDialog progressDialog;
    private List<StudyItem> itemsToStudy;
    private int currentIndex = 0;
    private int currentRuneIndex = 0;
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
        promptCanvas.setEventListener(new PromptCanvas.IGradingButtonListener() {
            @Override
            public void onGradingButtonPressed(int gradingButton) {
                onGrade(gradingButton);
            }
        });

        promptCanvas.setEventListener(new PromptCanvas.IStrokeListener() {
            @Override
            public void onNewStroke(Vector2[] strokePoints, int numPoints) {
                onStroke(strokePoints, numPoints);
            }
        });

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

    private void onGrade(int gradingButton) {
        promptCanvas.setShouldDrawStatusBorder(false);
        switch (gradingButton) {
            case 0:
                promptCanvas.setStatusBorderColor(Color.RED);
                break;
            case 1:
                promptCanvas.setStatusBorderColor(Color.YELLOW);
                break;
            case 2:
                promptCanvas.setStatusBorderColor(Color.GREEN);
                break;
            case 3:
                promptCanvas.setStatusBorderColor(Color.BLUE);
                break;
            default:
                break;
        }

        // Populate review item and store in database
        // Update relevant study items

        onNext(null);
    }

    private void onStroke(Vector2[] points, int numPoints) {
        Vector2[] corners = ShortStraw.runShortStraw(points, numPoints);
        Vector2 startPoint = new Vector2(0, 0);
        float startAngle = 0.0f;
        
        if (corners.length > 1) {
            Vector2 start = corners[0];
            Vector2 end = corners[corners.length-1];
            
            Vector2 dir = new Vector2(end.x - start.x, end.y - start.y);
            startAngle = Vector2.angleBetweenVectors(new Vector2(1.0f, 0.0f), dir);
        }
        
        BoundingBox box = BoundingBox.getBounds(corners, corners.length);
        
        startPoint.x = box.x;
        startPoint.y = box.y;
        
        promptCanvas.drawNextStroke(startPoint, startAngle);
    }

    public void onBack(View view) {
        updateCurrentIndex(false);
    }

    public void onErase(View view) {
        promptCanvas.clearStrokes();
    }

    public void onShow(View view) {

    }

    public void onCorrect(View view) {

    }

    public void onNext(View view) {
        updateCurrentIndex(true);
    }

    private void updateCurrentIndex(boolean forward) {
        currentItem = itemsToStudy.get(currentIndex);

        if (currentItem.isRune()) {
            // Check which character we're on, and move to the next character
            // if there are any left. Otherwise, go to the next StudyItem
            String[] vocabIDs = currentItem.getVocabIDs();

            Vocab vocab = null;
            if (vocabIDs != null && vocabIDs.length > 0) {
                vocab = VocabTable.getInstance().getByStringID(db, vocabIDs[0]);
            }

            boolean shouldMoveToNextItem = false;
            if (vocab != null) {
                int writingLength = StringUtil.filterOutNonKanji(vocab.getWriting()).length();
                if (forward && currentRuneIndex < writingLength - 1) {
                    currentRuneIndex++;
                } else if (!forward && currentRuneIndex > 0) {
                    currentRuneIndex--;
                } else {
                    shouldMoveToNextItem = true;
                    currentRuneIndex = 0;
                }
            } else {
                shouldMoveToNextItem = true;
            }

            if (forward && shouldMoveToNextItem) {
                currentIndex++;
                if (currentIndex >= itemsToStudy.size()) {
                    currentIndex = itemsToStudy.size() - 1;
                }
            } else if (shouldMoveToNextItem) {
                currentIndex--;
                if (currentIndex < 0) {
                    currentIndex = 0;
                }
            }
        } else {
            if (forward) {
                currentIndex++;
                if (currentIndex >= itemsToStudy.size()) {
                    currentIndex = itemsToStudy.size() - 1;
                }
            } else {
                currentIndex--;
                if (currentIndex < 0) {
                    currentIndex = 0;
                }
            }
        }

        updateCurrentItem();
    }

    private void updateCurrentItem() {
        currentItem = itemsToStudy.get(currentIndex);

        String[] vocabIDs = currentItem.getVocabIDs();

        Vocab vocab = null;
        if (vocabIDs != null && vocabIDs.length > 0) {
            vocab = VocabTable.getInstance().getByStringID(db, vocabIDs[0]);
        }

        TextView text = (TextView) findViewById(R.id.itemDetails);
        text.setText(currentItem.getId());

        TextView timeText = (TextView) findViewById(R.id.itemTimes);
        timeText.setText("" + currentItem.getReviews());

        StrokeData strokeData = null;
        if (currentItem.isRune() && vocab != null) {
            String kanjiOnly = StringUtil.filterOutNonKanji(vocab.getWriting());
            strokeData = StrokeDataTable.getInstance().getByRuneAndLanguage(db, kanjiOnly.substring(currentRuneIndex, currentRuneIndex + 1), vocab.getLanguage());
        }

        promptCanvas.setStudyItemAndVocab(currentItem, vocab, strokeData);

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

        // Get rid of items that don't have an associated vocab, for now
        Iterator<StudyItem> iterator = itemsToStudy.iterator();

        while (iterator.hasNext()) {
            StudyItem item = iterator.next();

            String[] vocabIDs = item.getVocabIDs();

            Vocab vocab = null;
            if (vocabIDs == null || vocabIDs.length == 0) {
                iterator.remove();
            }
        }

        updateCurrentItem();

        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
