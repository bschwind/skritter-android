package com.skritter;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LoginFragment extends Fragment implements View.OnClickListener
{
    // This code up to onDetach() is all to get easy callbacks to the Activity. 
    private Callbacks mCallbacks = sDummyCallbacks;

    public interface Callbacks
    {
        public void onTaskFinished();
    }
    private static Callbacks sDummyCallbacks = new Callbacks()
    {
        public void onTaskFinished() { }
    };

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks))
        {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    // Save a reference to the fragment manager. This is initialised in onCreate().
    private FragmentManager mFM;

    // Code to identify the fragment that is calling onActivityResult(). We don't really need
    // this since we only have one fragment to deal with.
    static final int TASK_FRAGMENT = 0;

    // Tag so we can find the task fragment again, in another instance of this fragment after rotation.
    static final String TASK_FRAGMENT_TAG = "task";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // At this point the fragment may have been recreated due to a rotation,
        // and there may be a TaskFragment lying around. So see if we can find it.
        mFM = getFragmentManager();
        // Check to see if we have retained the worker fragment.
        TaskFragment taskFragment = (TaskFragment)mFM.findFragmentByTag(TASK_FRAGMENT_TAG);

        if (taskFragment != null)
        {
            // Update the target fragment so it goes to this fragment instead of the old one.
            // This will also allow the GC to reclaim the old LoginFragment, which the TaskFragment
            // keeps a reference to. Note that I looked in the code and setTargetFragment() doesn't
            // use weak references. To be sure you aren't leaking, you may wish to make your own
            // setTargetFragment() which does.
            taskFragment.setTargetFragment(this, TASK_FRAGMENT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // Callback for the "start task" button. I originally used the XML onClick()
        // but it goes to the Activity instead.
        view.findViewById(R.id.loginButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        // We only have one click listener so we know it is the "Start Task" button.

        // We will create a new TaskFragment.
        TaskFragment taskFragment = new TaskFragment();
        // And create a task for it to monitor. In this implementation the taskFragment
        // executes the task, but you could change it so that it is started here.
        taskFragment.setTask(new MyTask());
        // And tell it to call onActivityResult() on this fragment.
        taskFragment.setTargetFragment(this, TASK_FRAGMENT);

        // Show the fragment.
        // I'm not sure which of the following two lines is best to use but this one works well.
        taskFragment.show(mFM, TASK_FRAGMENT_TAG);
//      mFM.beginTransaction().add(taskFragment, TASK_FRAGMENT_TAG).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == TASK_FRAGMENT && resultCode == Activity.RESULT_OK)
        {
            // Inform the activity. 
            mCallbacks.onTaskFinished();
        }
    }