package com.skritter.taskFragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.skritter.SkritterAPI;

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
        public void onPostExecute(Void result);
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
    private class GetStudyItemsTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCallbacks.onPreExecute();
            running = true;
        }

        @Override
        protected Void doInBackground(String... params) {
            if (params == null || params.length != 1) {
                return null;
            }

            String accessToken = params[0];

            SkritterAPI.fetchItems(accessToken);

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            // Proxy the call to the Activity
            mCallbacks.onCancelled();
            running = false;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Proxy the call to the Activity
            mCallbacks.onPostExecute(result);
            running = false;
        }
    }
}