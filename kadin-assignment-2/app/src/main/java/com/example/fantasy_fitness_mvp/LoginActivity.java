package com.example.fantasy_fitness_mvp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

/**
 * LoginActivity class that implements BaseActivity and manages the User login functionality.
 */
public class LoginActivity extends BaseActivity {
    private EditText usernameInput;
    private EditText passwordInput;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usernameInput = findViewById(R.id.inputUsername);
        passwordInput = findViewById(R.id.inputPassword);
    }
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_login;
    }

    /**
     * Handles the Login button click. Runs validation, and upon success
     * creates an intent to switch to the home activity.
     *
     * @param view The view that was clicked (the Login button).
     */
    public void onClickLogin(android.view.View view) {
        if(validateCredentials()) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("user", new User(usernameInput.getText().toString()));
            startActivity(intent);
        }
    }

    /**
     * Validates the username and password EditText fields and creates
     * a Toast to notify the user of a need for correction, if any.
     *
     * @return boolean indicating successful validation.
     */
    private Boolean validateCredentials() {
        String username = usernameInput.getText().toString();
        String password = passwordInput.getText().toString();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your username and password to continue!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
