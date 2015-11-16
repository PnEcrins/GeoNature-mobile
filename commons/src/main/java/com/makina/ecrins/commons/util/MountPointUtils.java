package com.makina.ecrins.commons.util;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.EnvironmentCompat;
import android.text.TextUtils;
import android.util.Log;

import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.model.MountPoint;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class helper about {@link MountPoint}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MountPointUtils {

    /**
     * Return the primary external storage as {@link MountPoint}.
     *
     * @return the primary external storage
     */
    @NonNull
    public static MountPoint getInternalStorage() {

        final String externalStorage = System.getenv("EXTERNAL_STORAGE");

        if (TextUtils.isEmpty(externalStorage)) {
            return new MountPoint(Environment.getExternalStorageDirectory()
                                             .getAbsolutePath(),
                                  MountPoint.StorageType.INTERNAL);
        }
        else {
            return new MountPoint(externalStorage,
                                  MountPoint.StorageType.INTERNAL);
        }
    }

    /**
     * Return the secondary external storage as {@link MountPoint} if found.
     *
     * @return the secondary external storage or {@code null} if not found
     */
    @Nullable
    public static MountPoint getExternalStorage() {

        // try to found the secondary external storage using System environment
        final List<MountPoint> mountPoints = getMountPointsFromSystemEnv();
        final Iterator<MountPoint> mountPointIterator = mountPoints.iterator();
        MountPoint externalMountPoint = null;

        while (mountPointIterator.hasNext() && (externalMountPoint == null)) {
            final MountPoint mountPoint = mountPointIterator.next();
            externalMountPoint = (mountPoint.getStorageType()
                                            .equals(MountPoint.StorageType.EXTERNAL) ? mountPoint : null);
        }

        // fallback: parse file 'vold.fstab' and try to find the secondary external storage
        if (externalMountPoint == null) {
            mountPoints.clear();
            mountPoints.addAll(getMountPointsFromVold());

            while (mountPointIterator.hasNext() && (externalMountPoint == null)) {
                MountPoint mountPoint = mountPointIterator.next();
                externalMountPoint = (mountPoint.getStorageType()
                                                .equals(MountPoint.StorageType.EXTERNAL) ? mountPoint : null);
            }
        }

        return externalMountPoint;
    }

    /**
     * Retrieves a {@code List} of all available {@link MountPoint}s
     *
     * @return a {@code List} of available {@link MountPoint}s
     *
     * @see #getMountPointsFromSystemEnv()
     * @see #getMountPointsFromVold()
     */
    @NonNull
    public static List<MountPoint> getMountPoints() {

        final List<MountPoint> mountPoints = getMountPointsFromSystemEnv();

        // fallback: parse file 'vold.fstab' and try to find all external storage
        if (mountPoints.size() == 1) {
            final List<MountPoint> mountPointsFromVold = getMountPointsFromVold();

            for (MountPoint mountPoint : mountPointsFromVold) {
                if (!mountPoint.getStorageType()
                               .equals(MountPoint.StorageType.INTERNAL)) {
                    mountPoints.add(mountPoint);
                }
            }
        }

        return mountPoints;
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

        final String storageState = EnvironmentCompat.getStorageState(mountPoint.getMountPath());

        return storageState.equals(Environment.MEDIA_MOUNTED) || storageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
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
    public static String formatStorageSize(
            Context context,
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
     * @param context the current context
     * @param status  the storage status
     *
     * @return a human representation of the storage status
     */
    @NonNull
    public static String formatStorageStatus(
            Context context,
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
     * Retrieves a {@code List} of {@link MountPoint}s from {@code 'vold.fstab'} system file.
     *
     * @return a {@code List} of available {@link MountPoint}s
     */
    @NonNull
    static List<MountPoint> getMountPointsFromVold() {

        final List<MountPoint> mountPoints = new ArrayList<>();
        final File voldFstabFile = new File("/system/etc/vold.fstab");
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        if (voldFstabFile.exists()) {
            try {
                fileReader = new FileReader(voldFstabFile);
                bufferedReader = new BufferedReader(fileReader);
                String line;
                MountPoint.StorageType storageType = null;

                while ((line = bufferedReader.readLine()) != null) {
                    if (TextUtils.isEmpty(line)) {
                        continue;
                    }

                    line = line.trim();

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
                            final File mountPath = new File(tokens[2]);

                            if (mountPath.isDirectory()) {
                                mountPoints.add(new MountPoint(tokens[2],
                                                               storageType));
                            }
                        }
                    }
                }
            }
            catch (IOException ioe) {
                Log.w(MountPointUtils.class.getName(),
                      ioe.getMessage(),
                      ioe);
            }
            finally {
                if (fileReader != null) {
                    try {
                        fileReader.close();
                    }
                    catch (IOException ioe) {
                        Log.w(MountPointUtils.class.getName(),
                              ioe.getMessage(),
                              ioe);
                    }
                }

                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    }
                    catch (IOException ioe) {
                        Log.w(MountPointUtils.class.getName(),
                              ioe.getMessage(),
                              ioe);
                    }
                }
            }
        }

        return mountPoints;
    }

    /**
     * Retrieves a {@code List} of {@link MountPoint}s from {@code System} environment.
     *
     * @return a {@code List} of available {@link MountPoint}s
     */
    @NonNull
    static List<MountPoint> getMountPointsFromSystemEnv() {

        final List<MountPoint> mountPoints = new ArrayList<>();

        final String externalStorage = System.getenv("EXTERNAL_STORAGE");

        if (TextUtils.isEmpty(externalStorage)) {
            mountPoints.add(new MountPoint(Environment.getExternalStorageDirectory()
                                                      .getAbsolutePath(),
                                           MountPoint.StorageType.INTERNAL));
        }
        else {
            mountPoints.add(new MountPoint(externalStorage,
                                           MountPoint.StorageType.INTERNAL));
        }

        final String secondaryStorage = System.getenv("SECONDARY_STORAGE");

        if (!TextUtils.isEmpty(secondaryStorage)) {
            final String[] paths = secondaryStorage.split(":");
            boolean firstSecondaryStorage = true;

            for (String path : paths) {
                final File file = new File(path);

                if (file.isDirectory()) {
                    mountPoints.add(new MountPoint(path,
                                                   (firstSecondaryStorage) ? MountPoint.StorageType.EXTERNAL : MountPoint.StorageType.USB));
                    firstSecondaryStorage = false;
                }
            }
        }

        return mountPoints;
    }
}
