package com.example.cse476app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class ExerciseLogFragment extends Fragment implements OnMapReadyCallback {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText editExerciseName;
    private Spinner spinnerExerciseType;
    private FirebaseAuth mAuth;
    private DatabaseReference mDbRef;
    private EditText editExerciseMinutes;
    private EditText editExerciseSeconds;
    private EditText editWorkoutLocation;
    private MapView mapView;
    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercise_log, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDbRef = FirebaseDatabase.getInstance().getReference();

        editExerciseName = view.findViewById(R.id.edit_exercise_name);
        spinnerExerciseType = view.findViewById(R.id.spinner_exercise);
        editExerciseMinutes = view.findViewById(R.id.edit_exercise_minutes);
        editExerciseSeconds = view.findViewById(R.id.edit_exercise_seconds);
        editWorkoutLocation = view.findViewById(R.id.edit_exercise_location);
        mapView = view.findViewById(R.id.map_view);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.exercise_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerExerciseType.setAdapter(adapter);

        Button doneButton = view.findViewById(R.id.buttonAddExercise);
        doneButton.setOnClickListener(this::onClickDone);

        Button updateMapButton = view.findViewById(R.id.button_update_map);
        updateMapButton.setOnClickListener(this::updateMapWithAddressOnClick);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Request Location permission and fetch the user's location
        requestLocationPermission();

        return view;
    }

    /**
     * Lifecycle methods for the MapView
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    /**
     * Move the camera to the specified location
     * @param location location to move camera to
     */
    private void moveMapCamera(LatLng location) {
        if (googleMap == null) return;

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(location).title("Workout Location"));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
    }

    /**
     * Update the map with the specified address on click
     * @param view the view that was clicked
     */
    public void updateMapWithAddressOnClick(View view) {
        String address = editWorkoutLocation.getText().toString();
        if (address.isEmpty()) return;

        // Use Geocoder to convert the address to latitude and longitude
        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address location = addressList.get(0);
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Move the camera to the new location
                LatLng newLocation = new LatLng(latitude, longitude);
                moveMapCamera(newLocation);
            } else {
                Toast.makeText(requireContext(), "Address not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(), "Error finding address", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Request location permission and fetch the user's location
     */
    private void requestLocationPermission() {
        // Check if location permission is already granted
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission if not already granted
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE, Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            // Permission is already granted, fetch the location
            getLastKnownLocation();
        }
    }

    /**
     * Fetch the user's last known location
     */
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                // Get latitude and longitude
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Use Geocoder to get the address based on latitude and longitude
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    assert addresses != null;
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        String fullAddress = address.getAddressLine(0);  // Get the full address
                        // Autofill the workout location
                        editWorkoutLocation.setText(fullAddress);
                    }
                } catch (IOException e) {
                    editWorkoutLocation.setText(R.string.unknown_location);
                }

                // Move and zoom the camera to the user's location
                LatLng userLocation = new LatLng(latitude, longitude);
                moveMapCamera(userLocation);
            } else {
                Toast.makeText(requireContext(), "Unable to fetch location.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClickDone(View view) {
        String exerciseName = editExerciseName.getText().toString();
        String exerciseType = spinnerExerciseType.getSelectedItem().toString();
        String exerciseMinutes = editExerciseMinutes.getText().toString();
        String exerciseSeconds = editExerciseSeconds.getText().toString();
        String exerciseLocation = editWorkoutLocation.getText().toString();

        if (exerciseName.isEmpty() || exerciseMinutes.isEmpty() || exerciseSeconds.isEmpty() || exerciseLocation.isEmpty()) {
            Toast.makeText(requireActivity(), "All Values are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get reference to exercise list from our Firebase Realtime Database instance
        DatabaseReference exercises = mDbRef.child("exercises");
        String key = exercises.push().getKey();

        // Create a map for the exercise consisting of the user id, exercise name, type, location, minutes, and seconds.
        Map<String, String> exerciseMap = new HashMap<>();
        exerciseMap.put("userId", mAuth.getUid());
        exerciseMap.put("username", Objects.requireNonNull(mAuth.getCurrentUser()).getEmail());
        exerciseMap.put("exerciseName", exerciseName);
        exerciseMap.put("exerciseType", exerciseType);
        exerciseMap.put("exerciseMinutes", exerciseMinutes);
        exerciseMap.put("exerciseSeconds", exerciseSeconds);
        exerciseMap.put("exerciseLocation", exerciseLocation);

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/exercises/" + key, exerciseMap);
        childUpdates.put("/user-exercises/" + mAuth.getUid() + "/" + key, exerciseMap);

        // Push changes to db instance
        mDbRef.updateChildren(childUpdates);

        // Go back to the main activity.
        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        HomeFragment home = new HomeFragment();
        ((BaseActivity) requireActivity()).loadFragment(home);

    }
}