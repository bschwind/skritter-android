package com.skritter.taskFragments;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Base64;

import com.skritter.SkritterAPI;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * This Fragment manages a single background task and retains itself across
 * configuration changes.
 */
public class LoginTaskFragment extends Fragment {

    /**
     * Callback interface through which the fragment can report the task's
     * progress and results back to the Activity.
     */
    public static interface TaskCallbacks {
        public void onPreExecute();
        public void onCancelled();
        public void onPostExecute(Boolean loggedIn);
    }

    private TaskCallbacks mCallbacks;
    private LoginTask task;
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
    public void start(String username, String password) {
        if (!running) {
            task = new LoginTask();
            task.execute(username, password);
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
    private class LoginTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mCallbacks.onPreExecute();
            running = true;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (params == null || params.length != 2) {
                return false;
            }

            String username = params[0];
            String password = params[1];

            return SkritterAPI.login(username, password);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            // Proxy the call to the Activity
            mCallbacks.onCancelled();
            running = false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // Proxy the call to the Activity
            mCallbacks.onPostExecute(result);
            running = false;
        }
    }
}