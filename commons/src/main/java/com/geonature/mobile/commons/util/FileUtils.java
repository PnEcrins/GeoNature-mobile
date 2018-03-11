package com.geonature.mobile.commons.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.geonature.mobile.commons.model.MountPoint;

import java.io.File;
import java.io.IOException;

/**
 * Helpers for {@code File} utilities.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FileUtils
        extends org.apache.commons.io.FileUtils {

    private static final String TAG = FileUtils.class.getName();

    /**
     * {@link FileUtils} instances should NOT be constructed in standard programming.
     */
    private FileUtils() {

    }

    /**
     * Construct a file from the set of name elements.
     *
     * @param directory the parent directory
     * @param names     the name elements
     *
     * @return the corresponding file
     */
    @NonNull
    public static File getFile(@NonNull final File directory,
                               @NonNull final String... names) {
        File file = directory;

        for (String name : names) {
            file = new File(file,
                            name);
        }

        return file;
    }

    /**
     * Gets the relative path used by this context.
     *
     * @param context the current {@code Context}
     *
     * @return the relative path
     *
     * @throws NameNotFoundException if the relative path cannot be found
     */
    @NonNull
    public static String getRelativeSharedPath(Context context) throws
                                                                NameNotFoundException {

        return "Android" + File.separator + "data" + File.separator + context.getPackageManager()
                                                                             .getPackageInfo(context.getPackageName(),
                                                                                             PackageManager.GET_META_DATA).sharedUserId + File.separator;
    }

    /**
     * Tries to find the mount point used by external storage as {@code File}.
     * If not, returns the default {@code Environment.getExternalStorageDirectory()}.
     *
     * @param context the current {@code Context}
     *
     * @return the mount point as {@code File} used by external storage if available
     */
    @NonNull
    public static File getExternalStorageDirectory(@NonNull final Context context) {

        final MountPoint externalMountPoint = MountPointUtils.getExternalStorage(context,
                                                                                 Environment.MEDIA_MOUNTED,
                                                                                 Environment.MEDIA_MOUNTED_READ_ONLY);

        if (externalMountPoint == null) {
            Log.w(TAG,
                  "getExternalStorageDirectory: external mount point is not available. Use default: " + MountPointUtils.getInternalStorage());

            return MountPointUtils.getInternalStorage()
                                  .getMountPath();
        }

        return externalMountPoint.getMountPath();
    }

    /**
     * Gets the root folder as {@code File} used by this context.
     *
     * @param context     the current {@code Context}
     * @param storageType the {@link MountPoint.StorageType} to use
     *
     * @return the root folder as {@code File}
     *
     * @throws IOException if something goes wrong
     */
    @NonNull
    public static File getRootFolder(Context context,
                                     @NonNull MountPoint.StorageType storageType) throws
                                                                                  IOException {

        try {
            return getFile((storageType == MountPoint.StorageType.EXTERNAL) ? getExternalStorageDirectory(context) : MountPointUtils.getInternalStorage()
                                                                                                                                    .getMountPath(),
                           getRelativeSharedPath(context));
        }
        catch (NameNotFoundException nnfe) {
            throw new IOException(nnfe);
        }
    }

    /**
     * Gets the {@code inputs/} folder as {@code File} used by this context.
     * The relative path used is {@code inputs/<package_name>}
     *
     * @param context the current {@code Context}
     *
     * @return the {@code inputs/} folder as {@code File}
     *
     * @throws IOException if something goes wrong
     */
    @NonNull
    public static File getInputsFolder(Context context) throws
                                                        IOException {

        return getFile(getRootFolder(context,
                                     MountPoint.StorageType.INTERNAL),
                       "inputs",
                       context.getPackageName());
    }

    /**
     * Gets the {@code databases/} folder as {@code File} used by this context.
     *
     * @param context     the current {@code Context}
     * @param storageType the {@link MountPoint.StorageType} to use
     *
     * @return the {@code databases/} folder
     *
     * @throws IOException if something goes wrong
     */
    @NonNull
    public static File getDatabaseFolder(Context context,
                                         @NonNull MountPoint.StorageType storageType) throws
                                                                                      IOException {

        return getFile(getRootFolder(context,
                                     storageType),
                       "databases");
    }
}
