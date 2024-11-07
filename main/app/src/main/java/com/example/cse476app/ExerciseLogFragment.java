package com.example.cse476app;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.HashSet;

public class ExerciseLogFragment extends Fragment {
    private EditText editExerciseName;
    private Spinner spinnerExerciseType;
    private EditText editExerciseMinutes;
    private EditText editExerciseSeconds;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_log, container, false);

        editExerciseName = view.findViewById(R.id.edit_exercise_name);
        spinnerExerciseType = view.findViewById(R.id.spinner_exercise);
        editExerciseMinutes = view.findViewById(R.id.edit_exercise_minutes);
        editExerciseSeconds = view.findViewById(R.id.edit_exercise_seconds);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.exercise_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExerciseType.setAdapter(adapter);

        Button doneButton = view.findViewById(R.id.buttonAddExercise);
        doneButton.setOnClickListener(this::onClickDone);

        return view;
    }

    public void onClickDone(View view) {
        String exerciseName = editExerciseName.getText().toString();
        String exerciseType = spinnerExerciseType.getSelectedItem().toString();
        String exerciseMinutes = editExerciseMinutes.getText().toString();
        String exerciseSeconds = editExerciseSeconds.getText().toString();
        if (exerciseName.isEmpty() || exerciseMinutes.isEmpty() || exerciseSeconds.isEmpty()) {
            // Show a toast message if the username is empty.
            Toast.makeText(requireActivity(), "All Values are required.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Add exercise to SharedPreferences.
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        // Create a set for the exercise, consisting of the exercise name, type, minutes, and seconds.
        HashSet<String> exerciseSet = new HashSet<>();
        exerciseSet.add(exerciseName);
        exerciseSet.add(exerciseType);
        exerciseSet.add("M" + exerciseMinutes);
        exerciseSet.add("S" + exerciseSeconds);
        editor.putStringSet(exerciseName, exerciseSet);
        editor.apply();

        // Go back to the main activity.
        HomeFragment home = new HomeFragment();
        ((BaseActivity) requireActivity()).loadFragment(home);
    }
}