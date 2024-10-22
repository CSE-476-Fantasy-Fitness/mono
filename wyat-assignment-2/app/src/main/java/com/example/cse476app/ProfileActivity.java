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
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ProfileActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageView profileImage;
    private Button buttonTakePicture, buttonShowCameraPreview;
    private ImageCapture imageCapture;
    private SharedPreferences sharedPreferences;
    private boolean isPreviewVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_profile);

        // Initialize views
        previewView = findViewById(R.id.previewView);
        profileImage = findViewById(R.id.profileImage);
        buttonTakePicture = findViewById(R.id.buttonTakePicture);
        buttonShowCameraPreview = findViewById(R.id.buttonShowCameraPreview);

        // Hide the camera preview initially
        previewView.setVisibility(PreviewView.GONE);

        // Initialize SharedPreferences to save image path
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        // Load the saved image, if any, on startup
        loadSavedImage();


        // Check if the camera is available on the device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            Toast.makeText(this, "No camera found on this device", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Check for camera permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                PermissionUtils.requestPermission(this, 1,
                        Manifest.permission.CAMERA, false);
            }

        }

        if (savedInstanceState != null) {
            isPreviewVisible = savedInstanceState.getBoolean("preview_visible", false);
            String buttonText = savedInstanceState.getString("button_text", "Show Camera Preview");
            if (isPreviewVisible)
            {
                startCamera();
            }
            // Restore button text and preview visibility
            buttonShowCameraPreview.setText(buttonText);
            previewView.setVisibility(isPreviewVisible ? PreviewView.VISIBLE : PreviewView.GONE);
        }

    }

    public void onClickPreview(View view)
    {
        toggleCameraPreview();
    }

    public void onClickTakePicture(View view)
    {
        takePicture();
    }

    // Save instance state
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the preview visibility state and the button text
        outState.putBoolean("preview_visible", isPreviewVisible);
        outState.putString("button_text", buttonShowCameraPreview.getText().toString());
    }

    private void toggleCameraPreview() {
        if (isPreviewVisible) {
            previewView.setVisibility(PreviewView.GONE);
            buttonShowCameraPreview.setText("Show Camera Preview");
            isPreviewVisible = false;
        } else {
            previewView.setVisibility(PreviewView.VISIBLE);
            buttonShowCameraPreview.setText("Hide Camera Preview");
            startCamera();
            isPreviewVisible = true;
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePicture() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera not ready", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = new File(getExternalFilesDir(null), "profile_picture.jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                // Load the captured image
                Bitmap capturedImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

                // Adjust the image rotation based on EXIF metadata
                Bitmap rotatedImage = adjustImageRotation(photoFile.getAbsolutePath());

                // Apply blur to the rotated image
                Bitmap blurredImage = applyBlurMaskFilter(rotatedImage);

                // Display the blurred image in the ImageView
                profileImage.setImageBitmap(blurredImage);

                // Save the blurred image to internal storage
                String savedImagePath = saveImageToInternalStorage(blurredImage);

                // Save the image path to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("profileImagePath", savedImagePath);
                editor.apply();

                Toast.makeText(ProfileActivity.this, "Picture saved!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e("CameraX", "Image capture failed: " + exception.getMessage());
                Toast.makeText(ProfileActivity.this, "Failed to take picture.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap adjustImageRotation(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Matrix matrix = new Matrix();

            if (orientation == ExifInterface.ORIENTATION_UNDEFINED) {
                // If EXIF data is not available, apply manual rotation based on device orientation
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                switch (rotation) {
                    case Surface.ROTATION_0:
                        matrix.postRotate(90);  // Default portrait rotation
                        break;
                    case Surface.ROTATION_90:
                        matrix.postRotate(0);   // No rotation needed for landscape
                        break;
                    case Surface.ROTATION_180:
                        matrix.postRotate(270); // Upside down portrait
                        break;
                    case Surface.ROTATION_270:
                        matrix.postRotate(180); // Reverse landscape
                        break;
                    default:
                        matrix.postRotate(0);   // Default to no rotation
                        break;
                }
            } else {
                // Rotate based on EXIF orientation
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        matrix.postRotate(90);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        matrix.postRotate(180);
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        matrix.postRotate(270);
                        break;
                    default:
                        // No rotation needed
                        return bitmap;
                }
            }

            // Apply rotation and return the rotated bitmap
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        } catch (IOException e) {
            e.printStackTrace();
            // If something goes wrong with EXIF, fallback to default bitmap
            return bitmap;
        }
    }

    private Bitmap applyBlurMaskFilter(Bitmap originalBitmap) {
        Paint paint = new Paint();
        paint.setMaskFilter(new BlurMaskFilter(50, android.graphics.BlurMaskFilter.Blur.NORMAL));

        Bitmap blurredBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blurredBitmap);
        canvas.drawBitmap(originalBitmap, 0, 0, paint);

        return blurredBitmap;
    }

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

    private void loadSavedImage() {
        String savedImagePath = sharedPreferences.getString("profileImagePath", null);
        if (savedImagePath != null) {
            Bitmap savedImage = BitmapFactory.decodeFile(savedImagePath);
            profileImage.setImageBitmap(savedImage);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 101 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission granted.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}

