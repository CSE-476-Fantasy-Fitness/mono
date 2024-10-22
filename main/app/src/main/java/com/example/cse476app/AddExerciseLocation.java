package com.example.cse476app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class AddExerciseLocation extends AppCompatActivity {

    /**
     * Create the Add restroom activity.
     * @param savedInstanceState The instance to restore, if created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load mMap from the saved instance state
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_exercise);
    }

    /**
     * Submission for the new restroom. This method will ensure that the entries are valid.
     * If they have issues, a toast message will provided to help assist the user.
     * If they are valid, then the new restroom will be added to the map.
     */
    public void submitRestroom(View view) {
        // Get the entered data from the EditText fields
        EditText latitude = (EditText)findViewById(R.id.latitude);
        String check_latitude = latitude.getText().toString();
        EditText longitude = (EditText)findViewById(R.id.longitude);
        String check_longitude = longitude.getText().toString();
        EditText name = (EditText)findViewById(R.id.new_restroom_name);
        String entered_name = name.getText().toString();
        if (check_latitude.isEmpty() ||
                check_longitude.isEmpty() ||
                !(check_latitude.matches("-?\\d{1,3}\\.\\d{6}")) ||
                !(check_longitude.matches("-?\\d{1,3}\\.\\d{6}")) ||
                entered_name.isEmpty()) {
            // Send a toast message saying that the input was bad.
            android.widget.Toast.makeText(this, this.getString(R.string.bad_new_restroom_entry), android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        float entered_latitude = Float.parseFloat(check_latitude);
        float entered_longitude = Float.parseFloat(check_longitude);

        // Add the restroom to the map.
        // We do this by concatenating the entered name with the entered latitude and longitude
        // and then sending it back to the RestroomMaps activity
        Intent data = new Intent();
        String new_restroom = entered_name + ":" + entered_latitude + ":" + entered_longitude;
        data.putExtra("new_restroom", new_restroom);
        setResult(RESULT_OK, data);
        finish();
    }
}