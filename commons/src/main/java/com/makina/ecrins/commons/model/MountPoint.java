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

    @NonNull
    private final File mountPath;

    @NonNull
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
     * Returns a boolean indicating whether this {@link MountPoint} is available.
     *
     * @return {@code true} if this {@link MountPoint} is available, {@code false} otherwise.
     *
     * @see File#exists()
     */
    public boolean exists() {
        return mountPath.exists();
    }

    /**
     * Indicates whether the current context is allowed to read from this {@link MountPoint}.
     *
     * @return {@code true} if this {@link MountPoint} can be read, {@code false} otherwise.
     *
     * @see File#canRead()
     */
    public boolean canRead() {
        return mountPath.canRead();
    }

    /**
     * Indicates whether the current context is allowed to write to this {@link MountPoint}.
     *
     * @return {@code true} if this {@link MountPoint} can be written, {@code false} otherwise.
     *
     * @see File#canWrite()
     */
    public boolean canWrite() {
        return mountPath.canWrite();
    }

    /**
     * Returns the total size in bytes of the partition containing this path.
     *
     * @return the total size in bytes of the partition containing this path or 0 if this path does
     * not exist
     *
     * @see File#getTotalSpace()
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
     * @see File#getFreeSpace()
     */
    public long getFreeSpace() {

        return mountPath.getFreeSpace();
    }

    @Override
    public String toString() {

        return "MountPoint{" +
                "mountPath=" + mountPath +
                ", storageType=" + storageType +
                ", exists=" + mountPath.exists() +
                ", canRead=" + mountPath.canRead() +
                ", canWrite=" + mountPath.canWrite() +
                '}';
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
