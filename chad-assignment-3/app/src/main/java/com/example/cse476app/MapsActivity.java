package com.example.cse476app;

import android.Manifest;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.cse476app.databinding.ActivityMapsBinding;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    // List of MarkerOptions that will store the markers on the map
    public java.util.ArrayList<MarkerOptions> mMarkers = new java.util.ArrayList<>();

    // Shared preferences object that will be used to store the markers
    private SharedPreferences mSharedPref;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean permissionDenied = false;

    private FusedLocationProviderClient fusedLocationClient;

    /**
     * A pre-registered activity result launcher that will be used to add an activity to the map.
     */
    ActivityResultLauncher<Intent> addExerciseFromResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == Activity.RESULT_OK) {
                        Intent data = o.getData();
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
            }
    );

    /**
     * Create the MapsActivity activity
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (savedInstanceState != null) {
            mMarkers = savedInstanceState.getParcelableArrayList("markers");
        } else {
            mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            if (mSharedPref.contains("markers")) {
                String markers = mSharedPref.getString("markers", "");
                if(!markers.equals("")) {
                    String[] markers_components = markers.split(";");
                    for (String marker: markers_components) {
                        String[] marker_components = marker.split(":");
                        String name = marker_components[0];
                        float latitude = Float.parseFloat(marker_components[1]);
                        float longitude = Float.parseFloat(marker_components[2]);
                        MarkerOptions new_marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title(name);
                        mMarkers.add(new_marker);
                    }
                }
            }

        }

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Destroy method to save the markers to the shared preferences.
     */
    @Override
    @SuppressWarnings("deprecation")
    protected void onDestroy() {
        super.onDestroy();
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = mSharedPref.edit();
        StringBuilder markerSaveString = new StringBuilder();
        for (MarkerOptions marker : mMarkers) {
            markerSaveString.append(marker.getTitle()).append(":").append(marker.getPosition().latitude).append(":").append(marker.getPosition().longitude).append(";");
        }
        editor.putString("markers", markerSaveString.toString());
        editor.apply();
        binding = null;
    }

    /**
     * Saves the instance in case the activity is destroyed or paused.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList("markers", mMarkers);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Restores the instance in case the activity is destroyed or paused.
     * @param savedInstanceState The saved instance state.
     */
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mMarkers = savedInstanceState.getParcelableArrayList("markers");
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        if (!mMap.isMyLocationEnabled()) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }
        for (MarkerOptions marker: mMarkers) {
            mMap.addMarker(marker);
        }
    }

    /**
     * Enalbes the My location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng currPos = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currPos, 15.0f));
                    }
                }
            });
            return;
        }

        PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION, true);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    /**
     * Add an exercise to the map. This will place a marker on the map at the user's current location
     * or a passed in location.
     * @param view The view that was clicked, in this case, our add restroom button.
     */
    public void addExercise(android.view.View view) {
        Intent intent = new Intent(this, ExerciseActivity.class);
        addExerciseFromResult.launch(intent);
    }
}

