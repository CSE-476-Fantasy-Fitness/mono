package com.example.fantasy_fitness_mvp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

public class LogExerciseActivity extends BaseActivity {
    private EditText nameInput;
    private EditText typeInput;
    private EditText timeInput;
    private Bundle extras;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nameInput = findViewById(R.id.inputName);
        typeInput = findViewById(R.id.inputType);
        timeInput = findViewById(R.id.inputTime);

        extras = getIntent().getExtras();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_log_exercise;
    }

    /**
     * Handles the Cancel button click. Creates an intent to switch to the home activity.
     *
     * @param view The view that was clicked (the Cancel button).
     */
    public void onClickCancel(android.view.View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    /**
     * Handles the Done button click. Runs validation on exercise info, and upon success
     * creates an intent to switch to the home activity with saved exercise.
     *
     * @param view The view that was clicked (the Done button).
     */
    public void onClickDone(android.view.View view) {
        if(validateExercise()) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtras(extras);
            // TODO: create exercise class, and pass an instance along with user in intent extras.
            startActivity(intent);
        }
    }

    /**
     * Validates the username and password EditText fields and creates
     * a Toast to notify the user of a need for correction, if any.
     *
     * @return boolean indicating successful validation.
     */
    private Boolean validateExercise() {
        String name = nameInput.getText().toString();
        String type = typeInput.getText().toString();
        String time = timeInput.getText().toString();

        if (name.isEmpty() || type.isEmpty() || time.isEmpty()) {
            Toast.makeText(this, "Please enter all fields to continue!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
