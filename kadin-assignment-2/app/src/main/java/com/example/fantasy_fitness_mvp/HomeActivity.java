package com.example.fantasy_fitness_mvp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class HomeActivity extends BaseActivity {
    public User user;
    private Bundle extras;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        extras = getIntent().getExtras();
        if(extras != null) {
            user = extras.getParcelable("user");
        } else {
            user = new User("User");
        }

        TextView greetUser = findViewById(R.id.textUser);
        String message = "Hello " + user.getUsername();
        greetUser.setText(message);
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_home;
    }

    /**
     * Handles the Logout button click. Creates an empty intent to switch to the login activity.
     *
     * @param view The view that was clicked (the Logout button).
     */
    public void onClickLogout(android.view.View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Handles the New Exercise button click. Creates an intent to switch to the
     * LogExercise activity, passing along the bundled extras.
     *
     * @param view The view that was clicked (the Logout button).
     */
    public void onClickNewExercise(android.view.View view) {
        Intent intent = new Intent(this, LogExerciseActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }
}
