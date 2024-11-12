package com.example.cse476app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AddExerciseLocation extends AppCompatActivity {

    /**
     * Create the Add exercise activity.
     * @param savedInstanceState The instance to restore, if created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load mMap from the saved instance state
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exercise);
    }

    /**
     * Submission for the new exercise. This method will ensure that the entries are valid.
     * If they have issues, a toast message will provided to help assist the user.
     * If they are valid, then the new exercise will be added to the map.
     */
    public void submitExercise(View view) {
        // Get the entered data from the EditText fields
        EditText latitude = (EditText)findViewById(R.id.latitude);
        String check_latitude = latitude.getText().toString();
        EditText longitude = (EditText)findViewById(R.id.longitude);
        String check_longitude = longitude.getText().toString();
        EditText name = (EditText)findViewById(R.id.new_exercise_name);
        String entered_name = name.getText().toString();
        if (check_latitude.isEmpty() ||
                check_longitude.isEmpty() ||
                !(check_latitude.matches("-?\\d{1,3}\\.\\d{6}")) ||
                !(check_longitude.matches("-?\\d{1,3}\\.\\d{6}")) ||
                entered_name.isEmpty()) {
            // Send a toast message saying that the input was bad.
            android.widget.Toast.makeText(this, this.getString(R.string.bad_new_exercise_entry), android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        float entered_latitude = Float.parseFloat(check_latitude);
        float entered_longitude = Float.parseFloat(check_longitude);

        // Add the exercise to the map.
        // We do this by concatenating the entered name with the entered latitude and longitude
        // and then sending it back to the exerciseMaps activity
        Intent data = new Intent();
        String new_exercise = entered_name + ":" + entered_latitude + ":" + entered_longitude;
        data.putExtra("new_exercise", new_exercise);
        setResult(RESULT_OK, data);
        finish();
    }

    public void submitexercise(View view) {
    }
}