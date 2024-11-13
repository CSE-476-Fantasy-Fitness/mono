package com.example.cse476app;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * ProfileFragment class handles the camera functionality, including capturing an image,
 * applying a blur effect, saving the image, and loading the previously saved image.
 */
public class ProfileFragment extends Fragment {
    private PreviewView mPreviewView;
    private ImageView mProfileImage;
    private Button mButtonShowCameraPreview;
    private Button mButtonTakePicture;

    private ImageCapture imageCapture;
    private SharedPreferences sharedPreferences;

    // State variable to track the camera preview visibility
    private boolean isPreviewVisible = false;

    // ActivityResultLauncher for permission requests
    private ActivityResultLauncher<String> requestPermissionLauncher;

    // TextView for displaying the username
    private TextView mTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the permission launcher
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Permission is granted.
                        Toast.makeText(requireContext(), "Camera permission granted.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Permission denied.
                        Toast.makeText(requireContext(), "Camera permission is required.", Toast.LENGTH_SHORT).show();
                        mButtonShowCameraPreview.setEnabled(false);
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize UI components
        mPreviewView = rootView.findViewById(R.id.previewView);
        mProfileImage = rootView.findViewById(R.id.profileImage);
        mButtonShowCameraPreview = rootView.findViewById(R.id.buttonShowCameraPreview);
        mButtonTakePicture = rootView.findViewById(R.id.buttonTakePicture);
        mTextView = rootView.findViewById(R.id.textViewUsername);

        // Set up button listeners
        mButtonShowCameraPreview.setOnClickListener(this::onClickPreview);
        mButtonTakePicture.setOnClickListener(this::onClickTakePicture);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide the preview initially
        mPreviewView.setVisibility(View.GONE);
        mButtonTakePicture.setVisibility(View.GONE);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        // Load the saved image from storage (if available)
        loadSavedImage();

        // Retrieve username from SharedPreferences (if it exists) and set it to TextView
        String username = sharedPreferences.getString("username", "Guest");
        mTextView.setText(username);

        // Check if the device has a camera
        if (!requireActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(requireContext(), R.string.no_cameras_found, Toast.LENGTH_SHORT).show();
            mButtonShowCameraPreview.setEnabled(false);
            mButtonTakePicture.setEnabled(false);
        } else {
            // Request camera permissions if not granted
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        }

        // Restore saved state if applicable
        if (savedInstanceState != null) {
            isPreviewVisible = savedInstanceState.getBoolean("preview_visible", false);
            String buttonText = savedInstanceState.getString("button_text", "Show Camera Preview");

            // Restore preview visibility and button text
            mButtonShowCameraPreview.setText(buttonText);
            mPreviewView.setVisibility(isPreviewVisible ? View.VISIBLE : View.GONE);
            mButtonTakePicture.setVisibility(isPreviewVisible ? View.VISIBLE : View.GONE);
            // Start the camera if the preview was visible before
            if (isPreviewVisible) {
                startCamera();
            }
        }
    }

    /**
     * Called when the user clicks the "Preview" button. Toggles the visibility of the camera preview.
     *
     * @param view The view that was clicked.
     */
    public void onClickPreview(View view) {
        toggleCameraPreview();
    }

    /**
     * Called when the user clicks the "Take Picture" button. Captures an image using the camera.
     *
     * @param view The view that was clicked.
     */
    public void onClickTakePicture(View view) {
        takePicture();
    }

    /**
     * Toggles the camera preview visibility and updates the button text.
     */
    private void toggleCameraPreview() {
        if (isPreviewVisible) {
            mPreviewView.setVisibility(View.GONE);
            mButtonTakePicture.setVisibility(View.GONE);
            mButtonShowCameraPreview.setText(R.string.show_camera_preview);
            isPreviewVisible = false;
        } else {
            mPreviewView.setVisibility(View.VISIBLE);
            mButtonTakePicture.setVisibility(View.VISIBLE);
            mButtonShowCameraPreview.setText(R.string.hide_camera_preview);
            startCamera();
            isPreviewVisible = true;
        }
    }

    /**
     * Starts the camera and binds the preview and image capture use cases to the lifecycle.
     */
    private void startCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(requireContext(), "Camera permission is not granted.", Toast.LENGTH_SHORT).show();
            return;
        }

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity().getApplicationContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Set up preview
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());

                // Set up image capture
                imageCapture = new ImageCapture.Builder()
                        .build();

                // Select front-facing camera
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                        .build();

                // Bind preview and image capture to lifecycle
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e("ProfileFragment", "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    /**
     * Captures a picture, rotates and blurs the image, and saves it to internal storage.
     */
    private void takePicture() {
        if (imageCapture == null) {
            Toast.makeText(requireContext(), R.string.camera_not_started, Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable the button to prevent multiple clicks
        mButtonTakePicture.setEnabled(false);

        // Create a file to save the image
        File photoFile = new File(requireActivity().getFilesDir(), "profile_picture.jpg");
        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Take the picture and process the result
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Rotate the captured image
                        Bitmap rotatedImage = adjustImageRotation(photoFile.getAbsolutePath());

                        // Apply blur effect
                        Bitmap blurredImage = applyBlurMaskFilter(rotatedImage);

                        // Display the blurred image in ImageView
                        mProfileImage.setImageBitmap(blurredImage);

                        // Save blurred image to internal storage
                        String savedImagePath = saveImageToInternalStorage(blurredImage);

                        // Save the image path to SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("profileImagePath", savedImagePath);
                        editor.apply();

                        Toast.makeText(requireContext(), R.string.picture_saved, Toast.LENGTH_SHORT).show();
                        // Enable button once the picture is saved
                        mButtonTakePicture.setEnabled(true);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(requireContext(), "Failed to take picture.", Toast.LENGTH_SHORT).show();
                        mButtonTakePicture.setEnabled(true);
                    }
                });
    }

    /**
     * Rotates the image based on EXIF orientation and device rotation.
     *
     * @param imagePath The path to the image file.
     * @return The rotated Bitmap.
     */
    private Bitmap adjustImageRotation(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();

            int rotation = requireActivity().getWindowManager().getDefaultDisplay().getRotation();

            if (orientation == ExifInterface.ORIENTATION_UNDEFINED) {
                // If orientation is undefined, use device rotation
                if (rotation == Surface.ROTATION_0) {
                    matrix.setRotate(270);
                } else {
                    matrix.setRotate(180);
                }
            } else {
                // Rotate the image based on EXIF orientation
                if (ExifInterface.ORIENTATION_ROTATE_270 == orientation) {
                    matrix.preScale(-1, 1);
                    matrix.postRotate(90);
                } else if (ExifInterface.ORIENTATION_ROTATE_180 == orientation) {
                    matrix.preScale(-1, 1);
                    matrix.postRotate(180);
                } else if (ExifInterface.ORIENTATION_ROTATE_90 == orientation) {
                    matrix.postRotate(0);
                }
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true);

        } catch (IOException e) {
            Log.e("ProfileFragment", "Error adjusting image rotation", e);
            return bitmap;
        }
    }

    /**
     * Applies a blur effect to the provided Bitmap.
     *
     * @param originalBitmap The Bitmap to blur.
     * @return The blurred Bitmap.
     */
    private Bitmap applyBlurMaskFilter(Bitmap originalBitmap) {
        Paint paint = new Paint();
        paint.setMaskFilter(new BlurMaskFilter(200, BlurMaskFilter.Blur.NORMAL));

        Bitmap blurredBitmap = Bitmap.createBitmap(originalBitmap.getWidth(),
                originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blurredBitmap);
        canvas.drawBitmap(originalBitmap, 0, 0, paint);

        return blurredBitmap;
    }

    /**
     * Saves the provided Bitmap to internal storage.
     *
     * @param bitmap The Bitmap to save.
     * @return The path to the saved image file.
     */
    private String saveImageToInternalStorage(Bitmap bitmap) {
        File directory = requireActivity().getDir("profile_images", Context.MODE_PRIVATE);
        File imageFile = new File(directory, "profile_picture_blurred.jpg");

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (IOException e) {
            Log.e("ProfileFragment", "Error saving image", e);
        }

        return imageFile.getAbsolutePath();
    }

    /**
     * Loads the saved image from internal storage and displays it in the ImageView.
     */
    private void loadSavedImage() {
        String savedImagePath = sharedPreferences.getString("profileImagePath", null);
        if (savedImagePath != null) {
            Bitmap savedImage = BitmapFactory.decodeFile(savedImagePath);
            mProfileImage.setImageBitmap(savedImage);
        }
    }

    /**
     * Saves the instance state, including whether the camera preview is visible and the button text.
     *
     * @param outState Bundle in which to place the saved instance state.
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("preview_visible", isPreviewVisible);
        outState.putString("button_text", mButtonShowCameraPreview.getText().toString());
    }
}