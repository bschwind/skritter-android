package com.skritter;

import android.os.AsyncTask;
import android.os.SystemClock;

// This is a fairly standard AsyncTask that does some dummy work.
public class MyTask extends AsyncTask<Void, Void, Void>
{
    TaskFragment mFragment;
    int mProgress = 0;

    void setFragment(TaskFragment fragment)
    {
        mFragment = fragment;
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        // Do some longish task. This should be a task that we don't really
        // care about continuing
        // if the user exits the app.
        // Examples of these things:
        // * Logging in to an app.
        // * Downloading something for the user to view.
        // * Calculating something for the user to view.
        // Examples of where you should probably use a service instead:
        // * Downloading files for the user to save (like the browser does).
        // * Sending messages to people.
        // * Uploading data to a server.
        for (int i = 0; i < 10; i++)
        {
            // Check if this has been cancelled, e.g. when the dialog is dismissed.
            if (isCancelled())
                return null;

            SystemClock.sleep(500);
            mProgress = i * 10;
            publishProgress();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... unused)
    {
        if (mFragment == null)
            return;
        mFragment.updateProgress(mProgress);
    }

    @Override
    protected void onPostExecute(Void unused)
    {
        if (mFragment == null)
            return;
        mFragment.taskFinished();
    }
}
