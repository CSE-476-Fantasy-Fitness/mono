package com.example.cse476app;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.cse476app.databinding.ActivityMapsBinding;

import java.util.ArrayList;

public class MapsActivity extends BaseActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private SharedPreferences mSharedPref;
    private ArrayList<MarkerOptions> mMarkers = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng selectedLatLng;
    private LatLng currentLatLng; // Store the current location of the user
    private static final float ZOOM_LEVEL_INIT = 16.0f;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;

    /**
     * A pre-registered activity result launcher that will be used to add an exercise to the map.
     */
    ActivityResultLauncher<Intent> addExerciseFromResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        String exerciseName = data.getStringExtra("exercise_name");
                        String exerciseType = data.getStringExtra("exercise_type");

                        LatLng locationToUse = (selectedLatLng != null) ? selectedLatLng : currentLatLng; // Use selected or current location
                        if (locationToUse != null) {
                            MarkerOptions marker = new MarkerOptions()
                                    .position(locationToUse)
                                    .title(exerciseName)
                                    .snippet(exerciseType);
                            mMarkers.add(marker);
                            mMap.addMarker(marker);
                        } else {
                            Toast.makeText(MapsActivity.this, "Unable to get location.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Restore saved markers or load from SharedPreferences
        if (savedInstanceState != null) {
            mMarkers = savedInstanceState.getParcelableArrayList("markers");
        } else {
            mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            loadMarkersFromSharedPreferences();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected int getNavigationMenuItemId() {
        return 0;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission();
        enableMyLocation();

        // Set a listener for map clicks
        mMap.setOnMapClickListener(this);

        // Display the markers
        for (MarkerOptions marker : mMarkers) {
            mMap.addMarker(marker);
        }

        // If the map is centered on the user's location, move the camera there
        if (!mMap.isMyLocationEnabled()) {
            LatLng defaultLocation = new LatLng(-34, 151); // Default Sydney location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, ZOOM_LEVEL_INIT));
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        // Set the selected location and launch the ExerciseActivity
        selectedLatLng = latLng;
    }

    private void openExerciseActivity() {
        Intent intent = new Intent(this, ExerciseActivity.class);
        addExerciseFromResult.launch(intent);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, ZOOM_LEVEL_INIT));
                            }
                        }
                    });
        } else {
            checkLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission is required for this feature", Toast.LENGTH_LONG).show();
                permissionDenied = true;
            }
        }
    }

    private void loadMarkersFromSharedPreferences() {
        if (mSharedPref.contains("markers")) {
            String markersString = mSharedPref.getString("markers", "");
            if (!markersString.equals("")) {
                String[] markersArray = markersString.split(";");
                for (String marker : markersArray) {
                    String[] components = marker.split(":");
                    String name = components[0];
                    float latitude = Float.parseFloat(components[1]);
                    float longitude = Float.parseFloat(components[2]);
                    MarkerOptions newMarker = new MarkerOptions().position(new LatLng(latitude, longitude)).title(name);
                    mMarkers.add(newMarker);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelableArrayList("markers", mMarkers);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveMarkersToSharedPreferences();
    }

    private void saveMarkersToSharedPreferences() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        StringBuilder markerSaveString = new StringBuilder();
        for (MarkerOptions marker : mMarkers) {
            markerSaveString.append(marker.getTitle()).append(":")
                    .append(marker.getPosition().latitude).append(":")
                    .append(marker.getPosition().longitude).append(";");
        }
        editor.putString("markers", markerSaveString.toString());
        editor.apply();
    }

    // Called when the "+" button is clicked
    public void addExercise(View view) {
        if (selectedLatLng == null) {
            selectedLatLng = currentLatLng; // Use the current location if no location was clicked
        }

        if (selectedLatLng != null) {
            openExerciseActivity();
        } else {
            Toast.makeText(this, "Unable to get location. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }
}
