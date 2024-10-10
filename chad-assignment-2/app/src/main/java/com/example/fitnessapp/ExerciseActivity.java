package com.example.fitnessapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class ExerciseActivity extends AppCompatActivity {
    private EditText editTextExercise;
    private EditText editTextTime;
    private Spinner exerciseTypes;
    private Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_exercise);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.exercise), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        editTextExercise = findViewById(R.id.editTextExerciseName);
        editTextTime = findViewById(R.id.editTextTime);
        createButton = findViewById(R.id.createButton);
        Spinner exerciseTypes = findViewById(R.id.exercise_types);

        // Create ArrayAdapter
        ArrayAdapter<CharSequence>adapter=ArrayAdapter.createFromResource(this, R.array.exercise_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        // Attach ArrayAdapter to spinner dropdown menu
        exerciseTypes.setAdapter(adapter);

        // Get the username passed from HomeScreenActivity
        Intent intent = getIntent();
        String username = intent.getStringExtra("userName");

        // Set an OnClickListener on the login button
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String exercise = editTextExercise.getText().toString(); // Get exercise text
                String time = editTextTime.getText().toString(); // Get time text
                String selectedExercise = exerciseTypes.getSelectedItem().toString(); // Get selected spinner value

                if (!exercise.isEmpty() && !time.isEmpty() && selectedExercise != null) {

                    // Start HomeScreenActivity when create exercise button is clicked
                    Intent goHome = new Intent(ExerciseActivity.this, HomeScreenActivity.class);
                    goHome.putExtra("exercise", exercise); // Pass the exercise to the next activity
                    goHome.putExtra("time", time); // Pass the time to the next activity
                    goHome.putExtra("exerciseType", selectedExercise); // Pass the exercise to the next activity
                    goHome.putExtra("userName", username); // Pass the username
                    startActivity(goHome); // Start the new activity
                }
                else {
                    Toast.makeText(ExerciseActivity.this, "Please fill in all fields",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}