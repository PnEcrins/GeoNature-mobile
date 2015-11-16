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
     * {@link com.makina.ecrins.commons.util.FileUtils} instances should NOT be constructed in
     * standard programming.
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
     * @throws android.content.pm.PackageManager.NameNotFoundException if the relative path cannot
     * be found
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
            return MountPointUtils.getInternalStorage().getMountPath();
        }

        return externalMountPoint.getMountPath();
    }

    /**
     * Gets a given filename as {@code File} according to the current application storage used
     * (default or external storage).
     *
     * @param context  the current context
     * @param filename filename to load
     *
     * @return the filename to load as {@code File}
     *
     * @throws java.io.IOException if the given filename cannot be load as {@code File}
     */
    @NonNull
    public static File getFileFromApplicationStorage(
            Context context,
            String filename) throws IOException {

        final File externalStorageDirectory = getExternalStorageDirectory();

        try {
            return new File(externalStorageDirectory.getPath() + File.separator + getRelativeSharedPath(context) + filename);
        }
        catch (NameNotFoundException nnfe) {
            throw new IOException(nnfe);
        }
    }

    /**
     * Gets the inputs folder as {@code File} used by this context.
     * The relative path used is {@code inputs/&lt;package_name&gt;}
     *
     * @param context the current context
     *
     * @return the inputs folder as {@code File}
     *
     * @throws java.io.IOException
     */
    public static File getInputsFolder(Context context) throws IOException {

        return getFileFromApplicationStorage(context,
                                             "inputs/" + context.getPackageName());
    }
}
