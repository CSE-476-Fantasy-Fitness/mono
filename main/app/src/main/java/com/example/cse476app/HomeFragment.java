package com.example.cse476app;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment {
    private TextView welcomeText;
    private LinearLayout exerciseList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        welcomeText = view.findViewById(R.id.title);
        exerciseList = view.findViewById(R.id.exercise_list);

        displayUsername();
        displayExercises();

        Button loginButton = view.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this::goToLogin);

        return view;
    }

    private void displayUsername() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        if (!username.isEmpty()) {
            welcomeText.setText(getString(R.string.welcome_user, username));
        } else {
            welcomeText.setText(R.string.welcome_default);
        }
    }

    private void displayExercises() {
        exerciseList.removeAllViews();
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (!(entry.getValue() instanceof Set)) {
                continue;
            }

            Set<String> exerciseSet = (Set<String>) entry.getValue();
            String exerciseName = entry.getKey();

            TextView exerciseView = new TextView(requireActivity());
            exerciseView.setPadding(16, 16, 16, 16);

            StringBuilder exerciseInfo = new StringBuilder();
            exerciseInfo.append("Exercise: ").append(exerciseName).append("\n");

            for (String detail : exerciseSet) {
                if (!detail.equals(exerciseName)) {
                    if (detail.startsWith("S")) {
                        exerciseInfo.append("Seconds: ").append(detail.substring(1)).append("\n");
                    } else if (detail.startsWith("M")) {
                        exerciseInfo.append("Minutes: ").append(detail.substring(1)).append("\n");
                    } else if (detail.startsWith("L")) {
                        exerciseInfo.append("Location: ").append(detail.substring(1)).append("\n");
                    } else {
                        exerciseInfo.append("Type: ").append(detail).append("\n");
                    }
                }
            }
            
            exerciseView.setText(exerciseInfo.toString());
            exerciseList.addView(exerciseView);
        }
    }

    public void goToLogin(View view) {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
    }
}