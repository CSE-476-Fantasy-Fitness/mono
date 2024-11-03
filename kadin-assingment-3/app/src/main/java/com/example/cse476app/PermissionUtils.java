package com.example.cse476app;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

public abstract class PermissionUtils {

    public static void requestPermission(Fragment fragment, int requestId, String permission, boolean finishActivity) {
        if (fragment.shouldShowRequestPermissionRationale(permission)) {
            // Show rationale
            RationaleDialog.newInstance(requestId, finishActivity)
                    .show(fragment.getParentFragmentManager(), "dialog");
        } else {
            fragment.requestPermissions(new String[]{permission}, requestId);
        }
    }

    public static boolean isPermissionGranted(String[] grantPermissions, int[] grantResults, String permission) {
        for (int i = 0; i < grantPermissions.length; i++) {
            if (permission.equals(grantPermissions[i])) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED;
            }
        }
        return false;
    }

    public static class PermissionDeniedDialog extends DialogFragment {

        public static PermissionDeniedDialog newInstance(boolean finishActivity) {
            Bundle arguments = new Bundle();
            arguments.putBoolean("finish", finishActivity);
            PermissionDeniedDialog dialog = new PermissionDeniedDialog();
            dialog.setArguments(arguments);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(requireActivity())
                    .setMessage("Permission required")
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);
            boolean finishActivity = false;
            if (finishActivity) {
                Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            }
        }
    }

    public static class RationaleDialog extends DialogFragment {
        private boolean finishActivity = false;

        public static RationaleDialog newInstance(int requestCode, boolean finishActivity) {
            Bundle arguments = new Bundle();
            arguments.putInt("requestCode", requestCode);
            arguments.putBoolean("finishActivity", finishActivity);
            RationaleDialog dialog = new RationaleDialog();
            dialog.setArguments(arguments);
            return dialog;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int requestCode = getArguments().getInt("requestCode");
            finishActivity = getArguments().getBoolean("finishActivity");

            return new AlertDialog.Builder(requireActivity())
                    .setMessage("Location permission is required")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
                        finishActivity = false;
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create();
        }

        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);
            if (finishActivity) {
                Toast.makeText(requireContext(), "Location permission required", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            }
        }
    }
}
