package com.example.cse476app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import android.Manifest;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/*
    * BaseActivity is an abstract class that extends AppCompatActivity and provides a common
    * implementation for the bottom navigation bar. It is extended by MainActivity, ExerciseActivity,
    * and ProfileActivity.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;
    protected static final int REQUEST_CAMERA_PERMISSION = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }


    protected void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId != getNavigationMenuItemId()) {
                navigateToActivity(itemId);
                return true;
            }
            return false;
        });
    }

    private void navigateToActivity(int itemId) {
        Intent intent = null;
        if (itemId == R.id.nav_home) {
            intent = new Intent(this, MainActivity.class);
        } else if (itemId == R.id.nav_exercise) {
            intent = new Intent(this, ExerciseActivity.class);
        } else if (itemId == R.id.nav_profile) {
            intent = new Intent(this, ProfileActivity.class);
        }

        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBottomNavigationSelection();
    }

    protected void updateBottomNavigationSelection() {
        if (bottomNavigationView != null) {
            bottomNavigationView.setSelectedItemId(getNavigationMenuItemId());
        }
    }

    @IdRes
    protected abstract int getNavigationMenuItemId();
}