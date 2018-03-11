package com.geonature.mobile.commons.util;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.geonature.mobile.commons.BuildConfig;
import com.geonature.mobile.commons.R;
import com.geonature.mobile.commons.model.MountPoint;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * Class helper about {@link MountPoint}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MountPointUtils {

    private static final String TAG = MountPointUtils.class.getName();

    /**
     * Return the primary external storage as {@link MountPoint}.
     *
     * @return the primary external storage
     */
    @NonNull
    public static MountPoint getInternalStorage() {
        final String externalStorage = System.getenv("EXTERNAL_STORAGE");

        if (TextUtils.isEmpty(externalStorage)) {
            final MountPoint mountPoint = new MountPoint(Environment.getExternalStorageDirectory()
                                                                    .getAbsolutePath(),
                                                         MountPoint.StorageType.INTERNAL);

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "internal storage from API: " + mountPoint);
            }

            return mountPoint;
        }
        else {
            final MountPoint mountPoint = new MountPoint(externalStorage,
                                                         MountPoint.StorageType.INTERNAL);

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "internal storage from system environment: " + mountPoint);
            }

            return mountPoint;
        }
    }

    /**
     * Return the secondary external storage as {@link MountPoint} if found.
     *
     * @param context       the current {@code Context}
     * @param storageStates a set of storage states as filter if {@link MountPoint#getStorageState()}
     *                      matches at least one
     *
     * @return the secondary external storage or {@code null} if not found
     *
     * @see #getMountPoints(Context)
     */
    @Nullable
    public static MountPoint getExternalStorage(@NonNull final Context context,
                                                @Nullable String... storageStates) {
        final List<MountPoint> mountPoints = getMountPoints(context);
        final Iterator<MountPoint> mountPointIterator = mountPoints.iterator();
        MountPoint externalMountPoint = null;

        while (mountPointIterator.hasNext() && (externalMountPoint == null)) {
            final MountPoint mountPoint = mountPointIterator.next();
            final boolean checkStorageState = storageStates == null || storageStates.length == 0 || Arrays.asList(storageStates)
                                                                                                          .contains(mountPoint.getStorageState());
            externalMountPoint = mountPoint.getStorageType()
                                           .equals(MountPoint.StorageType.EXTERNAL) && checkStorageState ? mountPoint : null;
        }

        if (BuildConfig.DEBUG) {
            if (externalMountPoint == null) {
                Log.d(TAG,
                      "external storage not found");
            }
            else {
                Log.d(TAG,
                      "external storage found: " + externalMountPoint);
            }
        }

        return externalMountPoint;
    }

    /**
     * Retrieves a {@code List} of all available {@link MountPoint}s
     *
     * @param context the current {@code Context}
     *
     * @return a {@code List} of available {@link MountPoint}s
     *
     * @see #getMountPointsFromSystemEnv()
     * @see #getMountPointsFromVold()
     * @see #getMountPointsFromProcMounts()
     */
    @NonNull
    public static List<MountPoint> getMountPoints(@NonNull final Context context) {
        // avoid duplicate mount points found
        final Set<MountPoint> mountPoints = new HashSet<>();

        // first: add the primary external storage
        mountPoints.add(getInternalStorage());

        // then: add all externals storage found only from Android APIs if Android version >= 21
        if (DeviceUtils.isPostLollipop()) {
            mountPoints.addAll(getMountPointsFromAPI(context));
        }
        else {
            // then: for all other Android versions try to find all MountPoints from System environment
            final List<MountPoint> mountPointsFromSystemEnv = getMountPointsFromSystemEnv();
            mountPoints.addAll(mountPointsFromSystemEnv);

            // fallback: try to find all externals storage from 'vold.fstab'
            if (mountPointsFromSystemEnv.isEmpty()) {
                final List<MountPoint> mountPointsFromVold = getMountPointsFromVold();
                final List<MountPoint> filteredMountPointsFromVold = new ArrayList<>();

                // keep only all secondary externals storage found
                for (MountPoint mountPoint : mountPointsFromVold) {
                    if (!mountPoint.getStorageType()
                                   .equals(MountPoint.StorageType.INTERNAL)) {
                        filteredMountPointsFromVold.add(mountPoint);
                    }
                }

                mountPoints.addAll(filteredMountPointsFromVold);

                // fallback: try to find all externals storage from '/proc/mounts'
                if (filteredMountPointsFromVold.isEmpty()) {
                    final List<MountPoint> mountPointsFromProcMounts = getMountPointsFromProcMounts();
                    mountPoints.addAll(mountPointsFromProcMounts);

                    // fallback: try to find all externals storage from Android APIs if Android version >= 19
                    if (mountPointsFromProcMounts.isEmpty() && DeviceUtils.isPostKitKat()) {
                        mountPoints.addAll(getMountPointsFromAPI(context));
                    }
                }
            }
        }

        // apply natural ordering using TreeSet
        return new ArrayList<>(new TreeSet<>(mountPoints));
    }

    /**
     * Check if the given {@link MountPoint} is mounted or not:
     * <ul>
     * <li>{@code Environment.MEDIA_MOUNTED}</li>
     * <li>{@code Environment.MEDIA_MOUNTED_READ_ONLY}</li>
     * </ul>
     *
     * @param mountPoint the given {@link MountPoint} to check
     *
     * @return {@code true} if the gieven {@link MountPoint} is mounted, {@code false} otherwise
     */
    public static boolean isMounted(@NonNull final MountPoint mountPoint) {
        return mountPoint.getStorageState()
                         .equals(Environment.MEDIA_MOUNTED) || mountPoint.getStorageState()
                                                                         .equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }

    /**
     * Pretty format a storage size.
     *
     * @param context     the current context
     * @param storageSize the storage size in bytes to format
     *
     * @return a human representation of the storage size
     */
    @NonNull
    public static String formatStorageSize(Context context,
                                           long storageSize) {
        String storageSuffix = "b";
        float formatedStorageSize = storageSize;

        if (formatedStorageSize >= 1024) {
            storageSuffix = "kb";
            formatedStorageSize /= 1024f;

            if (formatedStorageSize >= 1024) {
                storageSuffix = "mb";
                formatedStorageSize /= 1024f;

                if (formatedStorageSize >= 1024) {
                    storageSuffix = "gb";
                    formatedStorageSize /= 1024f;
                }
            }
        }

        int stringResource = context.getResources()
                                    .getIdentifier("storage_size_" + storageSuffix,
                                                   "string",
                                                   context.getPackageName());

        if (stringResource == 0) {
            return context.getString(R.string.storage_size_kb,
                                     storageSize / 1024f);
        }

        return context.getString(stringResource,
                                 formatedStorageSize);
    }

    /**
     * Pretty format the storage status.
     *
     * @param context the current {@code Context}
     * @param status  the storage status
     *
     * @return a human representation of the storage status
     */
    @NonNull
    public static String formatStorageStatus(Context context,
                                             @NonNull final String status) {
        int stringResource = context.getResources()
                                    .getIdentifier("storage_status_" + status,
                                                   "string",
                                                   context.getPackageName());

        if (stringResource == 0) {
            return context.getString(R.string.storage_status_unmounted);
        }

        return context.getString(stringResource);
    }

    /**
     * Retrieves a {@code List} of {@link MountPoint}s from Android APIs.
     *
     * @param context the current {@code Context}
     *
     * @return a {@code List} of available {@link MountPoint}s
     */
    @NonNull
    private static List<MountPoint> getMountPointsFromAPI(@NonNull final Context context) {
        final List<MountPoint> mountPoints = new ArrayList<>();

        if (DeviceUtils.isPostKitKat()) {
            final File[] externalFilesDirs = context.getExternalFilesDirs(null);
            boolean firstPrimaryStorage = true;

            for (File file : externalFilesDirs) {
                if (file == null) {
                    continue;
                }

                final String path = file.getAbsolutePath();

                mountPoints.add(buildMountPoint(path.substring(0,
                                                               path.indexOf("/Android")),
                                                firstPrimaryStorage ? MountPoint.StorageType.INTERNAL : MountPoint.StorageType.EXTERNAL));

                firstPrimaryStorage = false;
            }
        }

        return mountPoints;
    }

    /**
     * Retrieves a {@code List} of {@link MountPoint}s from {@code System}
     * environment.
     *
     * @return a {@code List} of available {@link MountPoint}s
     */
    @NonNull
    private static List<MountPoint> getMountPointsFromSystemEnv() {
        final List<MountPoint> mountPoints = new ArrayList<>();

        String secondaryStorage = System.getenv("SECONDARY_STORAGE");

        if (TextUtils.isEmpty(secondaryStorage)) {
            secondaryStorage = System.getenv("EXTERNAL_SDCARD_STORAGE");
        }

        if (!TextUtils.isEmpty(secondaryStorage)) {
            final String[] paths = secondaryStorage.split(":");
            boolean firstSecondaryStorage = true;

            for (String path : paths) {
                final MountPoint mountPoint = buildMountPoint(path,
                                                              (firstSecondaryStorage) ? MountPoint.StorageType.EXTERNAL : MountPoint.StorageType.USB);

                if (mountPoint != null) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "mount point found from system environment: " + mountPoint);
                    }

                    mountPoints.add(mountPoint);
                    firstSecondaryStorage = false;
                }
            }
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "mount points found from system environment: " + mountPoints.size());
        }

        return mountPoints;
    }

    /**
     * Retrieves a {@code List} of {@link MountPoint}s from
     * {@code 'vold.fstab'} system file.
     *
     * @return a {@code List} of available {@link MountPoint}s
     */
    @NonNull
    private static List<MountPoint> getMountPointsFromVold() {
        final List<MountPoint> mountPoints = new ArrayList<>();

        try {
            final Scanner scanner = new Scanner(new File("/system/etc/vold.fstab"));

            while (scanner.hasNext()) {
                String line = scanner.nextLine();

                if (TextUtils.isEmpty(line)) {
                    continue;
                }

                line = line.trim();

                MountPoint.StorageType storageType = null;

                // parse line comment
                if (line.startsWith("#")) {
                    if (line.contains("internal")) {
                        storageType = MountPoint.StorageType.INTERNAL;
                    }
                    else if (line.contains("external")) {
                        storageType = MountPoint.StorageType.EXTERNAL;
                    }
                    else if (line.contains("usb")) {
                        storageType = MountPoint.StorageType.USB;
                    }
                    else {
                        // storage type not found from line comment. Continue anyway
                        storageType = null;
                    }
                }

                // parse 'media_type' only it the storage type was not found from line comment
                if (line.startsWith("media_type") && (storageType == null)) {
                    String[] tokens = line.split("\\s");

                    if (tokens.length == 3) {
                        if (tokens[2].contains("usb")) {
                            storageType = MountPoint.StorageType.USB;
                        }
                    }
                }

                // parse 'dev_mount'
                if (line.startsWith("dev_mount") && (storageType != null)) {
                    String[] tokens = line.split("\\s");

                    if (tokens.length >= 3) {
                        final MountPoint mountPoint = buildMountPoint(tokens[2],
                                                                      storageType);

                        if (mountPoint != null) {
                            mountPoints.add(mountPoint);
                        }
                    }
                }
            }

            scanner.close();
        }
        catch (FileNotFoundException fnfe) {
            Log.w(TAG,
                  fnfe.getMessage());
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "mount points found from 'vold.fstab': " + mountPoints.size());
        }

        return mountPoints;
    }

    /**
     * Retrieves a {@code List} of {@link MountPoint}s from
     * {@code '/proc/mounts'} system file.
     *
     * @return a {@code List} of available {@link MountPoint}s
     */
    @NonNull
    private static List<MountPoint> getMountPointsFromProcMounts() {
        final List<MountPoint> mountPoints = new ArrayList<>();

        try {
            final Scanner scanner = new Scanner(new File("/proc/mounts"));

            while (scanner.hasNext()) {
                final String line = scanner.nextLine();

                if (line.startsWith("/dev/block/vold") || line.startsWith("/dev/fuse")) {
                    final String[] tokens = line.split("\\s");

                    if (tokens.length >= 2) {
                        final MountPoint mountPoint = buildMountPoint(tokens[1],
                                                                      MountPoint.StorageType.EXTERNAL);

                        if (mountPoint != null) {
                            mountPoints.add(mountPoint);
                        }
                    }
                }
            }

            scanner.close();
        }
        catch (FileNotFoundException fnfe) {
            Log.w(TAG,
                  fnfe.getMessage());
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "mount points found from '/proc/mounts': " + mountPoints.size());
        }

        return mountPoints;
    }

    @Nullable
    private static MountPoint buildMountPoint(@NonNull final File mountPath,
                                              @NonNull final MountPoint.StorageType storageType) {
        if (!mountPath.isDirectory()) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "failed to build mount point from '" + mountPath + "'");
            }

            return null;
        }

        return new MountPoint(mountPath.getAbsolutePath(),
                              storageType);
    }

    @Nullable
    private static MountPoint buildMountPoint(@NonNull final String mountPath,
                                              @NonNull final MountPoint.StorageType storageType) {
        return buildMountPoint(new File(mountPath),
                               storageType);
    }
}
