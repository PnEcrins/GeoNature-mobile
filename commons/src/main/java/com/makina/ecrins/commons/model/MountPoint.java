package com.makina.ecrins.commons.model;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.makina.ecrins.commons.BuildConfig;
import com.makina.ecrins.commons.util.DeviceUtils;

import java.io.File;
import java.io.IOException;

/**
 * Describes a mount point storage.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MountPoint
        implements Parcelable,
                   Comparable<MountPoint> {

    private static final String TAG = MountPoint.class.getName();

    @NonNull
    private final File mountPath;

    @NonNull
    private final StorageType storageType;

    public MountPoint(@NonNull final String mountPath,
                      @NonNull final StorageType storageType) {
        String resolvedMountPath;

        try {
            resolvedMountPath = new File(mountPath).getCanonicalPath();

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "MountPoint: '" + mountPath + "', canonical path: '" + resolvedMountPath + "'");
            }
        }
        catch (IOException ioe) {
            resolvedMountPath = mountPath;

            Log.w(TAG,
                  "MountPoint: failed to get the canonical path of '" + mountPath + "'");
        }

        this.mountPath = new File(resolvedMountPath);
        this.storageType = storageType;
    }

    private MountPoint(Parcel source) {
        this.mountPath = (File) source.readSerializable();
        this.storageType = (StorageType) source.readSerializable();
    }

    @NonNull
    public File getMountPath() {
        return mountPath;
    }

    @NonNull
    public StorageType getStorageType() {
        return storageType;
    }

    @NonNull
    public String getStorageState() {
        if (DeviceUtils.isPostLollipop()) {
            return Environment.getExternalStorageState(mountPath);
        }
        else {
            String storageState = Environment.MEDIA_UNMOUNTED;

            if (mountPath.canWrite()) {
                storageState = Environment.MEDIA_MOUNTED;
            }
            else if (mountPath.canRead()) {
                storageState = Environment.MEDIA_MOUNTED_READ_ONLY;
            }

            return storageState;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeSerializable(mountPath);
        dest.writeSerializable(storageType);
    }

    @Override
    public int compareTo(@NonNull MountPoint mountPoint) {
        if (storageType.equals(mountPoint.storageType)) {
            return mountPath.compareTo(mountPoint.mountPath);
        }

        return storageType.compareTo(mountPoint.storageType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MountPoint that = (MountPoint) o;

        return mountPath.equals(that.mountPath);
    }

    @Override
    public int hashCode() {
        return mountPath.hashCode();
    }

    @Override
    public String toString() {
        return "MountPoint{" +
                "mountPath='" + mountPath + '\'' +
                ", storageType=" + storageType +
                '}';
    }

    /**
     * Describes a storage type.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public enum StorageType {

        /**
         * Internal storage.
         */
        INTERNAL,

        /**
         * External storage.
         */
        EXTERNAL,

        /**
         * USB storage.
         */
        USB
    }

    public static final Creator<MountPoint> CREATOR = new Creator<MountPoint>() {

        @Override
        public MountPoint createFromParcel(Parcel source) {
            return new MountPoint(source);
        }

        @Override
        public MountPoint[] newArray(int size) {
            return new MountPoint[size];
        }
    };
}
