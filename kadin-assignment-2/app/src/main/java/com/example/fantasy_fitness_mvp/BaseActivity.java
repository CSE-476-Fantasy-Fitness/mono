package com.example.fantasy_fitness_mvp;

import android.app.Activity;
import android.os.Bundle;

/**
 * An abstract BaseActivity class that inherits from the Activity class and
 * provides some common methods to be implemented and used by activity subclasses.
 */
public abstract class BaseActivity extends Activity {
    /**
     * Basic OnCreate method for any activity. Creates the activity based
     * a saved instance state, if there is any, then sets content view.
     *
     * @param savedInstanceState The saved instance state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
    }
    protected abstract int getLayoutResourceId();
}