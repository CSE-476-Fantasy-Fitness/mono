package com.example.cse476app;
import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.exifinterface.media.ExifInterface;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * ProfileActivity class handles the camera functionality, including capturing an image,
 * applying a blur effect, saving the image, and loading the previously saved image.
 */
public class ProfileActivity extends AppCompatActivity {

    private PreviewView mPreviewView;
    private ImageView mProfileImage;
    private Button mButtonShowCameraPreview;
    private Button mButtonTakePicture;

    private ImageCapture imageCapture;
    private SharedPreferences sharedPreferences;

    // State variable to track the camera preview visibility
    private boolean isPreviewVisible = false;

    // Constant for camera request code
    private final int CAMERA_REQUEST_CODE = 1;

    // TextView for displaying the username
    private TextView mTextView;

    /**
     * Called when the activity is first created. Sets up the layout, initializes components,
     * checks for saved instance state, and requests necessary permissions.
     *
     * @param savedInstanceState Bundle object that contains the activity's previously saved state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_profile);

        // Initialize UI components
        mPreviewView = findViewById(R.id.previewView);
        mProfileImage = findViewById(R.id.profileImage);
        mButtonShowCameraPreview = findViewById(R.id.buttonShowCameraPreview);
        mButtonTakePicture = findViewById(R.id.buttonTakePicture);

        mTextView = findViewById(R.id.textViewUsername);

        // Hide the preview initially
        mPreviewView.setVisibility(PreviewView.GONE);
        mButtonTakePicture.setVisibility(PreviewView.GONE);
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Load the saved image from storage (if available)
        loadSavedImage();

        // Retrieve username from SharedPreferences (if it exists) and set it to TextView
        String username = sharedPreferences.getString("username", "Guest");
        mTextView.setText(username);

        // Check if the device has a camera
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, R.string.no_cameras_found, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Request camera permissions if not granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                PermissionUtils.requestPermission(this, CAMERA_REQUEST_CODE, Manifest.permission.CAMERA, false);
            }
        }

        // Restore saved state if applicable
        if (savedInstanceState != null) {
            isPreviewVisible = savedInstanceState.getBoolean("preview_visible", false);
            String buttonText = savedInstanceState.getString("button_text", "Show Camera Preview");

            // Restore preview visibility and button text
            mButtonShowCameraPreview.setText(buttonText);
            mPreviewView.setVisibility(isPreviewVisible ? PreviewView.VISIBLE : PreviewView.GONE);
            mButtonTakePicture.setVisibility(isPreviewVisible ? PreviewView.VISIBLE : PreviewView.GONE);
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
     * Saves the instance state, including whether the camera preview is visible and the button text.
     *
     * @param outState Bundle in which to place the saved instance state.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("preview_visible", isPreviewVisible);
        outState.putString("button_text", mButtonShowCameraPreview.getText().toString());
    }

    /**
     * Toggles the camera preview visibility and updates the button text.
     */
    private void toggleCameraPreview() {
        if (isPreviewVisible) {
            mPreviewView.setVisibility(PreviewView.GONE);
            mButtonTakePicture.setVisibility(PreviewView.GONE);
            mButtonShowCameraPreview.setText(R.string.show_camera_preview);
            isPreviewVisible = false;
        } else {
            mPreviewView.setVisibility(PreviewView.VISIBLE);
            mButtonTakePicture.setVisibility(PreviewView.VISIBLE);
            mButtonShowCameraPreview.setText(R.string.hide_camera_preview);
            startCamera();
            isPreviewVisible = true;
        }
    }

    /**
     * Starts the camera and binds the preview and image capture use cases to the lifecycle.
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

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
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * Captures a picture, rotates and blurs the image, and saves it to internal storage.
     */
    private void takePicture() {
        if (imageCapture == null) {
            Toast.makeText(this, R.string.camera_not_started, Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a file to save the image
        File photoFile = new File(getFilesDir(), "profile_picture.jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Take the picture and process the result
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
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

                Toast.makeText(ProfileActivity.this, R.string.picture_saved, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(ProfileActivity.this, "Failed to take picture.", Toast.LENGTH_SHORT).show();
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
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();

            int rotation = getWindowManager().getDefaultDisplay().getRotation();

            // Rotate based on EXIF data or device rotation
            if (orientation == ExifInterface.ORIENTATION_UNDEFINED) {
                if (rotation == Surface.ROTATION_0) {
                    matrix.setRotate(90);
                } else {
                    matrix.setRotate(180);
                }
            } else {
                matrix.postRotate(90);
            }

            // Flip because front cameras are mirrored
            matrix.preScale(-1, 1);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        } catch (IOException e) {
            e.printStackTrace();
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
        paint.setMaskFilter(new BlurMaskFilter(100, android.graphics.BlurMaskFilter.Blur.NORMAL));

        Bitmap blurredBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
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
        File directory = getDir("profile_images", MODE_PRIVATE);
        File imageFile = new File(directory, "profile_picture_blurred.jpg");

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imageFile.getAbsolutePath();
    }

    /**
     * Loads the saved image from internal storage and displays it in the ImageView with blur applied.
     */
    private void loadSavedImage() {
        String savedImagePath = sharedPreferences.getString("profileImagePath", null);
        if (savedImagePath != null) {
            Bitmap savedImage = BitmapFactory.decodeFile(savedImagePath);
            savedImage = applyBlurMaskFilter(savedImage);
            mProfileImage.setImageBitmap(savedImage);
        }
    }

    /**
     * Handles the result of a permission request.
     *
     * @param requestCode  The request code passed in requestPermissions().
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "Camera permission granted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}


