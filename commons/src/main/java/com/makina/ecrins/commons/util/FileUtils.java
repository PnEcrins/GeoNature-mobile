package com.makina.ecrins.commons.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.annotation.NonNull;

import com.makina.ecrins.commons.model.MountPoint;

import java.io.File;
import java.io.IOException;

/**
 * Helpers for {@code File} utilities.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FileUtils
        extends org.apache.commons.io.FileUtils {

    /**
     * {@link FileUtils} instances should NOT be constructed in standard programming.
     */
    private FileUtils() {

    }

    /**
     * Gets the relative path used by this context.
     *
     * @param context the current context
     *
     * @return the relative path
     *
     * @throws NameNotFoundException if the relative path cannot be found
     */
    @NonNull
    public static String getRelativeSharedPath(Context context) throws NameNotFoundException {

        return "Android" + File.separator + "data" + File.separator + context.getPackageManager()
                                                                             .getPackageInfo(context.getPackageName(),
                                                                                             PackageManager.GET_META_DATA).sharedUserId + File.separator;
    }

    /**
     * Tries to find the mount point used by external storage as {@code File}.
     * If not, returns the default {@code Environment.getExternalStorageDirectory()}.
     *
     * @return the mount point as {@code File} used by external storage if available
     */
    @NonNull
    public static File getExternalStorageDirectory() {

        final MountPoint externalMountPoint = MountPointUtils.getExternalStorage();

        if ((externalMountPoint == null) || !MountPointUtils.isMounted(externalMountPoint)) {
            return MountPointUtils.getInternalStorage()
                                  .getMountPath();
        }

        return externalMountPoint.getMountPath();
    }

    /**
     * Gets the root folder as {@code File} used by this context.
     *
     * @param context     the current context
     * @param storageType the {@link MountPoint.StorageType} to use
     *
     * @return the root folder as {@code File}
     *
     * @throws IOException
     */
    @NonNull
    public static File getRootFolder(
            Context context,
            @NonNull MountPoint.StorageType storageType) throws IOException {

        try {
            return FileUtils.getFile((storageType == MountPoint.StorageType.EXTERNAL) ? getExternalStorageDirectory() : MountPointUtils.getInternalStorage()
                                                                                                                                       .getMountPath(),
                                     getRelativeSharedPath(context));
        }
        catch (NameNotFoundException nnfe) {
            throw new IOException(nnfe);
        }
    }

    /**
     * Gets the {@code inputs/} folder as {@code File} used by this context.
     * The relative path used is {@code inputs/&lt;package_name&gt;}
     *
     * @param context the current context
     *
     * @return the {@code inputs/} folder as {@code File}
     *
     * @throws IOException
     */
    @NonNull
    public static File getInputsFolder(Context context) throws IOException {

        return FileUtils.getFile(getRootFolder(context,
                                               MountPoint.StorageType.INTERNAL),
                                 "inputs",
                                 context.getPackageName());
    }

    /**
     * Gets the {@code databases/} folder as {@code File} used by this context.
     *
     * @param context     the current context
     * @param storageType the {@link MountPoint.StorageType} to use
     *
     * @return the {@code databases/} folder
     *
     * @throws IOException
     */
    @NonNull
    public static File getDatabaseFolder(
            Context context,
            @NonNull MountPoint.StorageType storageType) throws IOException {

        return FileUtils.getFile(getRootFolder(context,
                                               storageType),
                                 "databases");
    }
}
