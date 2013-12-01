package com.skritter.taskFragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.skritter.SkritterAPI;
import com.skritter.models.StrokeData;
import com.skritter.models.StudyItem;
import com.skritter.models.Vocab;
import com.skritter.persistence.SkritterDatabaseHelper;
import com.skritter.persistence.StrokeDataTable;
import com.skritter.persistence.StudyItemTable;
import com.skritter.persistence.VocabTable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This Fragment manages a single background task and retains itself across
 * configuration changes.
 */
public class GetStudyItemsTaskFragment extends Fragment {

    /**
     * Callback interface through which the fragment can report the task's
     * progress and results back to the Activity.
     */
    public static interface TaskCallbacks {
        public void onPreExecute();
        public void onCancelled();
        public void onPostExecute(List<StudyItem> result);
    }

    private TaskCallbacks mCallbacks;
    private GetStudyItemsTask task;
    private boolean running;

    /**
     * Android passes us a reference to the newly created Activity by calling this
     * method after each configuration change.
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof TaskCallbacks)) {
            throw new IllegalStateException("Activity must implement the TaskCallbacks interface.");
        }

        // Hold a reference to the parent Activity so we can report back the task's
        // current progress and results.
        mCallbacks = (TaskCallbacks) activity;
    }

    /**
     * This method is called only once when the Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * This method is not called when the Fragment is being retained
     * across Activity instances.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        cancel();
    }

    /**
     * Start the background task.
     */
    public void start(String accessToken) {
        if (!running) {
            task = new GetStudyItemsTask();
            task.execute(accessToken);
            running = true;
        }
    }

    /**
     * Cancel the background task.
     */
    public void cancel() {
        if (running) {
            task.cancel(false);
            task = null;
            running = false;
        }
    }

    /**
     * Returns the current state of the background task.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * The task to log in the user
     */
    private class GetStudyItemsTask extends AsyncTask<String, Void, List<StudyItem>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCallbacks.onPreExecute();
            running = true;
        }

        @Override
        protected List<StudyItem> doInBackground(String... params) {
            if (params == null || params.length != 1) {
                return null;
            }

            // Use the database as a cache to the API results. If there are relatively recent
            // items in the table, use them instead of fetching from the API
            // This will have to be revisited
            SkritterDatabaseHelper db = new SkritterDatabaseHelper((Activity)mCallbacks);
            List<StudyItem> studyItems = StudyItemTable.getInstance().getAllItems(db);

            if (studyItems.size() > 0) {
                return studyItems;
            }

            String accessToken = params[0];

            JSONObject recentItemsJSON = SkritterAPI.batchGetStudyItems(accessToken);

            if (recentItemsJSON == null) {
                return studyItems;
            }

            populateDBWithJSONItems(db, recentItemsJSON);

            return StudyItemTable.getInstance().getAllItems(db);
        }

        private void populateDBWithJSONItems(SkritterDatabaseHelper db, JSONObject response) {
            // Populate study items from the response
            JSONArray studyItemJSONArray = response.optJSONArray("Items");

            for (int i = 0; i < studyItemJSONArray.length(); i++) {
                JSONObject studyItemJSONObject = studyItemJSONArray.optJSONObject(i);
                StudyItem item = new StudyItem(studyItemJSONObject);

                StudyItemTable.getInstance().create(db, item);
            }

            // Populate Vocabs which were included in the study item response
            JSONArray vocabJSONArray = response.optJSONArray("Vocabs");

            for (int i = 0; i < vocabJSONArray.length(); i++) {
                JSONObject vocabJSONObject = vocabJSONArray.optJSONObject(i);
                Vocab vocab = new Vocab(vocabJSONObject);

                VocabTable.getInstance().create(db, vocab);
            }

            // Populate Strokes which were included in the study item response
            JSONArray strokeJSONArray = response.optJSONArray("Strokes");

            for (int i = 0; i < strokeJSONArray.length(); i++) {
                JSONObject strokeJSONObject = strokeJSONArray.optJSONObject(i);
                StrokeData strokeData = new StrokeData(strokeJSONObject);

                StrokeDataTable.getInstance().create(db, strokeData);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            // Proxy the call to the Activity
            mCallbacks.onCancelled();
            running = false;
        }

        @Override
        protected void onPostExecute(List<StudyItem> result) {
            super.onPostExecute(result);
            // Proxy the call to the Activity
            mCallbacks.onPostExecute(result);
            running = false;
        }
    }
}