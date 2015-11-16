package com.makina.ecrins.commons.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Describes a mount point storage.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MountPoint
        implements Parcelable {

    private final File mountPath;
    private final StorageType storageType;

    public MountPoint(
            @NonNull final String mountPath,
            @NonNull final StorageType storageType) {

        this.mountPath = new File(mountPath);
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

    /**
     * Returns the total size in bytes of the partition containing this path.
     *
     * @return the total size in bytes of the partition containing this path or 0 if this path does
     * not exist
     *
     * @see java.io.File#getTotalSpace()
     */
    public long getTotalSpace() {

        return mountPath.getTotalSpace();
    }

    /**
     * Returns the number of free bytes on the partition containing this path.
     *
     * @return the number of free bytes on the partition containing this path or 0 if this path does
     * not exist
     *
     * @see java.io.File#getFreeSpace()
     */
    public long getFreeSpace() {

        return mountPath.getFreeSpace();
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(
            Parcel dest,
            int flags) {

        dest.writeSerializable(mountPath);
        dest.writeSerializable(storageType);
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
