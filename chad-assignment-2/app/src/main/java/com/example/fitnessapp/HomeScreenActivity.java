package com.example.fitnessapp;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.os.Bundle;
import android.widget.Button;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeScreenActivity extends AppCompatActivity {
    private Button createActivity;
    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get the intent that started this activity
        Intent intent = getIntent();
        String username = intent.getStringExtra("userName");
        String exercise = intent.getStringExtra("exercise");
        String time = intent.getStringExtra("time");
        String exerciseType = intent.getStringExtra("exerciseType");

        // Find TextView to display the username
        TextView usernameTextView = findViewById(R.id.textViewUsername);
        TextView exerciseTextView = findViewById(R.id.textViewExercise);
        if (username != null){
            // Display username
            String welcomeMessage = getString(R.string.welcome_message, username);
            usernameTextView.setText(welcomeMessage); // Display the username
        }

        if (exercise != null & time != null && exerciseType != null){
            // Display the exercise information
            String exerciseInfo = getString(R.string.exercise_info, exercise, time, exerciseType);
            exerciseTextView.setText(exerciseInfo);
        }

        // Initialize create activity/logout button
        createActivity = findViewById(R.id.newActivity);
        logout = findViewById(R.id.logout);

        // Set an OnClickListener on the create button
        createActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Start ExerciseActivity when create activity button is clicked
                Intent movetoExercise = new Intent(HomeScreenActivity.this, ExerciseActivity.class);
                movetoExercise.putExtra("userName", username);
                startActivity(movetoExercise); // Start the new activity
            }
        });

        // Set an OnClickListener on the create button
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Start ExerciseActivity when create activity button is clicked
                Intent movetoLogin = new Intent(HomeScreenActivity.this, MainActivity.class);
                startActivity(movetoLogin); // Start the new activity
            }
        });

    }
}