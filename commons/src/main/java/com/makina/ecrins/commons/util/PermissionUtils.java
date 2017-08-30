package com.makina.ecrins.commons.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.View;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Helper class about Android permissions.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class PermissionUtils {

    /**
     * Checks that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public static boolean checkPermissions(@NonNull final int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines whether the user have been granted a set of permissions.
     *
     * @param context                       the current {@code Context}.
     * @param onCheckSelfPermissionListener the callback to use to notify if these permissions was
     *                                      granted or not
     * @param permissions                   a set of permissions being checked
     */
    public static void checkSelfPermissions(@NonNull final Context context,
                                            @NonNull final OnCheckSelfPermissionListener onCheckSelfPermissionListener,
                                            @NonNull final String... permissions) {
        boolean granted = true;
        final Iterator<String> iterator = Arrays.asList(permissions)
                                                .iterator();

        while (iterator.hasNext() && granted) {
            granted = ActivityCompat.checkSelfPermission(context,
                                                         iterator.next()) == PackageManager.PERMISSION_GRANTED;
        }

        if (granted) {
            onCheckSelfPermissionListener.onPermissionsGranted();
        }
        else {
            onCheckSelfPermissionListener.onRequestPermissions(permissions);
        }
    }

    /**
     * Requests a set of permissions from a {@code Fragment}.
     * <p>
     * If a permission has been denied previously, a {@code Snackbar} will prompt the user to grant
     * the permission, otherwise it is requested directly.
     * </p>
     *
     * @param fragment                  the current {@code Fragment}
     * @param snackbarParentView        the parent view on which to display the {@code Snackbar}
     * @param snackbarMessageResourceId the message resource ID to display
     * @param requestCode               application specific request code to match with a result
     *                                  reported to {@code ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])}.
     * @param permissions               a set of permissions to request
     */
    public static void requestPermissions(@NonNull final Fragment fragment,
                                          @NonNull final View snackbarParentView,
                                          final int snackbarMessageResourceId,
                                          final int requestCode,
                                          @NonNull final String... permissions) {
        boolean shouldShowRequestPermissions = false;
        final Iterator<String> iterator = Arrays.asList(permissions)
                                                .iterator();

        while (iterator.hasNext() && !shouldShowRequestPermissions) {
            shouldShowRequestPermissions = fragment.shouldShowRequestPermissionRationale(iterator.next());
        }

        if (shouldShowRequestPermissions) {
            Snackbar.make(snackbarParentView,
                          snackbarMessageResourceId,
                          Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok,
                               new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       fragment.requestPermissions(permissions,
                                                                   requestCode);
                                   }
                               })
                    .show();
        }
        else {
            fragment.requestPermissions(permissions,
                                        requestCode);
        }
    }

    /**
     * Requests a set of permissions from a {@code Fragment}.
     * <p>
     * If a permission has been denied previously, a {@code Snackbar} will prompt the user to grant
     * the permission, otherwise it is requested directly.
     * </p>
     *
     * @param activity                  the current {@code Activity}
     * @param snackbarParentView        the parent view on which to display the {@code Snackbar}
     * @param snackbarMessageResourceId the message resource ID to display
     * @param requestCode               application specific request code to match with a result
     *                                  reported to {@code ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])}.
     * @param permissions               a set of permissions to request
     */
    public static void requestPermissions(@NonNull final Activity activity,
                                          @NonNull final View snackbarParentView,
                                          final int snackbarMessageResourceId,
                                          final int requestCode,
                                          @NonNull final String... permissions) {
        boolean shouldShowRequestPermissions = false;
        final Iterator<String> iterator = Arrays.asList(permissions)
                                                .iterator();

        while (iterator.hasNext() && !shouldShowRequestPermissions) {
            shouldShowRequestPermissions = ActivityCompat.shouldShowRequestPermissionRationale(activity,
                                                                                               iterator.next());
        }

        if (shouldShowRequestPermissions) {
            Snackbar.make(snackbarParentView,
                          snackbarMessageResourceId,
                          Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok,
                               new View.OnClickListener() {
                                   @Override
                                   public void onClick(View view) {
                                       ActivityCompat.requestPermissions(activity,
                                                                         permissions,
                                                                         requestCode);
                                   }
                               })
                    .show();
        }
        else {
            ActivityCompat.requestPermissions(activity,
                                              permissions,
                                              requestCode);
        }
    }

    /**
     * Callback about {@link PermissionUtils#checkSelfPermissions(Context, OnCheckSelfPermissionListener, String...)}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public interface OnCheckSelfPermissionListener {
        void onPermissionsGranted();

        void onRequestPermissions(@NonNull final String... permissions);
    }
}
