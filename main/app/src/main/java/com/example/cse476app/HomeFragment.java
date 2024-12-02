package com.example.cse476app;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment {
    private TextView welcomeText;
    private LinearLayout exerciseList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        user = mAuth.getCurrentUser();
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        welcomeText = view.findViewById(R.id.title);
        exerciseList = view.findViewById(R.id.exercise_list);

        displayUsername();
        displayExercises();

        Button loginButton = view.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this::goToLogin);

        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(this::logoutUser);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        displayUsername();
        displayExercises();
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

    // Fetch exercises from Firebase
    private void displayExercises() {
        exerciseList.removeAllViews();

        // Fetch exercises from Firebase
        mDatabase.child("exercises").get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e("firebase", "Error getting data", task.getException());
            } else {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists() && isAdded()) {  // Check if fragment is attached
                    for (DataSnapshot exerciseSnapshot : snapshot.getChildren()) {
                        Map<String, Object> exerciseData = (Map<String, Object>) exerciseSnapshot.getValue();
                        if (exerciseData != null) {
                            String exerciseName = (String) exerciseData.get("exerciseName");
                            String exerciseType = (String) exerciseData.get("exerciseType");
                            String exerciseLocation = (String) exerciseData.get("exerciseLocation");
                            String exerciseMinutes = (String) exerciseData.get("exerciseMinutes");
                            String exerciseSeconds =  (String) exerciseData.get("exerciseSeconds");
                            String exerciseUsername = (String) exerciseData.get("username");

                            // Create TextView for each exercise
                            TextView exerciseView = new TextView(requireActivity());
                            exerciseView.setPadding(16, 16, 16, 16);

                            StringBuilder exerciseInfo = new StringBuilder();
                            exerciseInfo.append("Exercise: ").append(exerciseName).append("\n")
                                        .append("Username: ").append(exerciseUsername).append("\n")
                                        .append("Type: ").append(exerciseType).append("\n")
                                        .append("Location: ").append(exerciseLocation).append("\n")
                                        .append("Minutes: ").append(exerciseMinutes).append("\n")
                                        .append("Seconds: ").append(exerciseSeconds).append("\n");
                            // Set the formatted text to the TextView
                            exerciseView.setText(exerciseInfo.toString());

                            // Add TextView to the layout
                            exerciseList.addView(exerciseView);
                        }
                    }
                } else if (!snapshot.exists()) {
                    Log.d("firebase", "No exercises found.");
                }
            }
        });
    }

    public void goToLogin(View view) {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        startActivity(intent);
    }

    public void logoutUser(View view) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
            Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        } else {
            Toast.makeText(requireContext(), "You are not signed in!", Toast.LENGTH_SHORT).show();
        }    }
}