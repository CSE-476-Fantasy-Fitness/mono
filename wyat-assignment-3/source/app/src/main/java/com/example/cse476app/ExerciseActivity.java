package com.example.cse476app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ExerciseActivity extends AppCompatActivity {

    private EditText editExerciseName;
    private Spinner spinnerExerciseType;
    private EditText editExerciseMinutes;
    private EditText editExerciseSeconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exercise);

        spinnerExerciseType = findViewById(R.id.spinner_exercise);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.exercise_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExerciseType.setAdapter(adapter);

        editExerciseName = findViewById(R.id.edit_exercise_name);
        editExerciseMinutes = findViewById(R.id.edit_exercise_minutes);
        editExerciseSeconds = findViewById(R.id.edit_exercise_seconds);
    }

    public void submitExercise(View view) {
        String exerciseName = editExerciseName.getText().toString();
        String exerciseType = spinnerExerciseType.getSelectedItem().toString();
        String exerciseMinutes = editExerciseMinutes.getText().toString();
        String exerciseSeconds = editExerciseSeconds.getText().toString();

        if (exerciseName.isEmpty() || exerciseMinutes.isEmpty() || exerciseSeconds.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("exercise_name", exerciseName);
        resultIntent.putExtra("exercise_type", exerciseType);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
