package com.example.cse476app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashSet;
/*
    * ExerciseActivity is an activity that allows the user to add a new exercise to the app. The user can
    * enter the exercise name, type, and duration in minutes and seconds. When the user clicks the "Create
    *  Exercise" button, the exercise is saved to SharedPreferences and the user is taken back to the main
    * activity.
 */
public class ExerciseActivity extends BaseActivity {

    private EditText editExerciseName;
    private Spinner spinnerExerciseType;
    private EditText editExerciseMinutes;
    private EditText editExerciseSeconds;
    private EditText editLatitude;
    private EditText editLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_exercise);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupBottomNavigation();
        Spinner spinnerExerciseType = findViewById(R.id.spinner_exercise);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.exercise_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExerciseType.setAdapter(adapter);

        editExerciseName = findViewById(R.id.edit_exercise_name);
        spinnerExerciseType = findViewById(R.id.spinner_exercise);
        editExerciseMinutes = findViewById(R.id.edit_exercise_minutes);
        editExerciseSeconds = findViewById(R.id.edit_exercise_seconds);
        editLatitude = findViewById(R.id.latitude);
        editLongitude = findViewById(R.id.longitude);

    }


    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_exercise;
    }

    public void onCreateExercise(View view) {
        Spinner spinnerExerciseType = findViewById(R.id.spinner_exercise);
       String exerciseName = editExerciseName.getText().toString();
        String exerciseType = spinnerExerciseType.getSelectedItem().toString();
        String exerciseMinutes = editExerciseMinutes.getText().toString();
        String exerciseSeconds = editExerciseSeconds.getText().toString();
        String exerciseLatitude = editLatitude.getText().toString();
        String exerciseLongitude = editLongitude.getText().toString();
        if (exerciseName.isEmpty() || exerciseMinutes.isEmpty() || exerciseSeconds.isEmpty() || exerciseLatitude.isEmpty() || exerciseLongitude.isEmpty()) {
            // Show a toast message if the username is empty.
            Toast.makeText(this, "All Values are required.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Add exercise to SharedPreferences.
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Create a set for the exercise, consisting of the exercise name, type, minutes, seconds, latitude, and longitude.
        HashSet<String> exerciseSet = new HashSet<>();
        exerciseSet.add(exerciseName);
        exerciseSet.add(exerciseType);
        exerciseSet.add("M" + exerciseMinutes);
        exerciseSet.add("S" + exerciseSeconds);
        exerciseSet.add(exerciseLatitude);
        exerciseSet.add(exerciseLongitude);
        editor.putStringSet(exerciseName, exerciseSet);
        editor.apply();
        // Go back to the main activity.
        // Intent intent = new Intent(this, MainActivity.class);
        // startActivity(intent);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}