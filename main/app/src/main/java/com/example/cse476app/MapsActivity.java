package com.example.cse476app;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.FragmentActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.cse476app.databinding.ActivityMapsBinding;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;
import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import java.util.ArrayList;

public class MapsActivity  extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private SharedPreferences mSharedPref;
    private ArrayList<MarkerOptions> mMarkers = new ArrayList<>();
    private FusedLocationProviderClient fusedLocationClient;
    private static final float ZOOM_LEVEL_INIT = 16.0f;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean permissionDenied = false;

     /**
     * A pre-registered activity result launcher that will be used to add a exercise to the map.
     */
    ActivityResultLauncher<Intent> addExerciseFromResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        assert data != null;
                        String new_exercise = data.getStringExtra("new_exercise");
                        assert new_exercise != null;
                        String[] new_exercise_components = new_exercise.split(":");
                        String name = new_exercise_components[0];
                        float latitude = Float.parseFloat(new_exercise_components[1]);
                        float longitude = Float.parseFloat(new_exercise_components[2]);
                        MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title(name);
                        mMarkers.add(marker);
                        mMap.addMarker(marker);
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        checkLocationPermission(); // Ensure permissions are requested here
        enableMyLocation();

        // Display the markers
        for (MarkerOptions marker : mMarkers) {
            mMap.addMarker(marker);
        }

        // If the map is centered on the user's location, move the camera there, otherwise move to a default location
        if (!mMap.isMyLocationEnabled()) {
            LatLng defaultLocation = new LatLng(-34, 151); // Default Sydney location
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, ZOOM_LEVEL_INIT));
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permissions if not granted
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
                                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, ZOOM_LEVEL_INIT));
                            }
                        }
                    });
        } else {
            // Permission is not yet granted, so request it
            checkLocationPermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation(); // Permission granted, proceed to enable location
            } else {
                // Permission denied, show an error message to the user
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
        @Override
    protected int getNavigationMenuItemId() {
        return R.id.nav_exercise;
    }

       /**
     * Add a exercise to the map. This will place a marker on the map at the user's current location
     * or a passed in location.
     * @param view The view that was clicked, in this case, our add exercise button.
     */
    public void addExercise(android.view.View view) {
        Intent intent = new Intent(this, AddExerciseLocation.class);
        addExerciseFromResult.launch(intent);
    }
}
