package com.example.cse476app;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ExerciseMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private final float ZOOM_LEVEL_INIT = 16.0f;
    private boolean permissionDenied = false;

    // ActivityResultLauncher for requesting location permission
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Enable the location layer.
                    enableMyLocation();
                } else {
                    // Permission is denied. Show an error message.
                    permissionDenied = true;
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_exercise_map, container, false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        FloatingActionButton logExerciseButton = view.findViewById(R.id.log_exercise_button);
        logExerciseButton.setOnClickListener(this::onClickAdd);

        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        enableMyLocation();

        // Add markers for exercises
        addExerciseMarkers();
    }

    private Map<String, Set<String>> getAllExercises() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();
        Map<String, Set<String>> exercises = new HashMap<>();

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getValue() instanceof Set) {
                exercises.put(entry.getKey(), (Set<String>) entry.getValue());
            }
        }
        return exercises;
    }

    private void addExerciseMarkers() {
        Map<String, Set<String>> exercises = getAllExercises();

        if (exercises.isEmpty()) {
            Toast.makeText(requireContext(), "No exercises found.", Toast.LENGTH_SHORT).show();
            return;
        }

        Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());

        for (Map.Entry<String, Set<String>> entry : exercises.entrySet()) {
            String exerciseName = entry.getKey();
            Set<String> exerciseData = entry.getValue();

            String locationString = null;
            for (String data : exerciseData) {
                if (data.startsWith("L")) {
                    locationString = data.substring(1); // Remove the "L" prefix
                    break;
                }
            }

            if (locationString != null) {
                try {
                    List<Address> addressList = geocoder.getFromLocationName(locationString, 1);
                    if (addressList != null && !addressList.isEmpty()) {
                        Address location = addressList.get(0);
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        LatLng exerciseLocation = new LatLng(latitude, longitude);

                        // Add a marker for this exercise
                        mMap.addMarker(new MarkerOptions()
                                .position(exerciseLocation)
                                .title(exerciseName)
                                .snippet(locationString));
                    }
                } catch (IOException e) {
                    Toast.makeText(requireContext(), "Error finding address for " + exerciseName, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // Check if permissions are granted
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(requireActivity(), location -> {
                        if (location != null) {
                            LatLng currPos = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currPos, ZOOM_LEVEL_INIT));
                        }
                    });
        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Snackbar.make(requireView(), "Location permission is required to show your current location.",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", view -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }).show();
        } else {
            // Request permission
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (permissionDenied) {
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog.newInstance(true).show(getChildFragmentManager(), "dialog");
    }

    public void onClickAdd(View view) {
        ExerciseLogFragment exerciseLog = new ExerciseLogFragment();
        ((BaseActivity) requireActivity()).loadFragment(exerciseLog);
    }
}
