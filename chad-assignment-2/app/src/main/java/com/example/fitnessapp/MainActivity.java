package com.example.fitnessapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        editTextUsername = findViewById(R.id.editTextTextEmailAddress);
        editTextPassword = findViewById(R.id.editTextTextPassword);
        loginButton = findViewById(R.id.loginButton);

        // Find the login button
        Button loginButton = findViewById(R.id.loginButton);

        // Set an OnClickListener on the login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString(); // Get username text
                String password = editTextPassword.getText().toString(); // Get password text

                if (!username.isEmpty() && !password.isEmpty()) {

                    // Start HomeScreenActivity when login button is clicked
                    Intent intent = new Intent(MainActivity.this, HomeScreenActivity.class);
                    intent.putExtra("userName", username); // Pass the username to the next activity
                    startActivity(intent); // Start the new activity
                }
                else {
                    Toast.makeText(MainActivity.this, "Please fill in all fields",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}