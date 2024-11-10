package com.example.cse476app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

/*
    * BaseActivity is an abstract class that extends AppCompatActivity and provides a common
    * implementation for the bottom navigation bar. It is extended by MainActivity, ExerciseActivity,
    * and ProfileActivity.
 */
public class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_base);

        FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("1:267773612973:android:ca81d5d09a43b168064724")
                .setApiKey("AIzaSyBfy1QBurX2hpq5abTak_Dg59SjgEro_CQ") // Web API Key from your screenshot
                .setProjectId("cse476-4ad07")                      // Project ID from your screenshot
                .setDatabaseUrl("https://cse476-4ad07.firebaseio.com") // Optional if using Realtime Database
                .build();

        // Check if FirebaseApp is already initialized to avoid duplicate initialization
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this, options);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.base), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set the initial fragment to HomeFragment
        loadFragment(new HomeFragment());

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener( item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_exercise) {
                selectedFragment = new ExerciseMapFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    public boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}