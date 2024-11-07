package com.example.cse476app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Map;
import java.util.Set;

public class MainActivity extends BaseActivity {

    private TextView welcomeText;
    private LinearLayout exerciseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        EdgeToEdge.enable(this);
        setupBottomNavigation();

        welcomeText = findViewById(R.id.title);
        exerciseList = findViewById(R.id.exercise_list);
        displayUsername();
        displayExercises();
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayUsername();
        displayExercises();
    }

    private void displayUsername() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        if (!username.isEmpty()) {
            welcomeText.setText(getString(R.string.welcome_user, username));
        } else {
            welcomeText.setText(R.string.welcome_default);
        }
    }

    private void displayExercises() {
        exerciseList.removeAllViews();
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (!(entry.getValue() instanceof Set)) {
                continue;
            }

            Set<String> exerciseSet = (Set<String>) entry.getValue();
            String exerciseName = entry.getKey();

            TextView exerciseView = new TextView(this);
            exerciseView.setPadding(16, 16, 16, 16);

            StringBuilder exerciseInfo = new StringBuilder();
            exerciseInfo.append("Exercise: ").append(exerciseName).append("\n");

            for (String detail : exerciseSet) {
                if (!detail.equals(exerciseName)) {
                    if (detail.matches("S\\d+")) {
                        exerciseInfo.append("Seconds: ").append(detail.substring(1)).append("\n");
                    } else if (detail.matches("M\\d+")) {
                        exerciseInfo.append("Minutes: ").append(detail.substring(1)).append("\n");
                    } else {
                        exerciseInfo.append("Type: ").append(detail).append("\n");
                    }
                }
            }
            exerciseView.setText(exerciseInfo.toString());
            exerciseList.addView(exerciseView);
        }
    }

    @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_home;
    }

    public void goToLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}