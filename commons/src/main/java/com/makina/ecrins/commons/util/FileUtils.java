package com.makina.ecrins.commons.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.util.Log;

/**
 * Helpers for File utilities.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FileUtils extends org.apache.commons.io.FileUtils {

    /**
     * {@link com.makina.ecrins.commons.util.FileUtils} instances should NOT be constructed in standard programming.
     */
    private FileUtils() {

    }

    /**
     * Checks the current state of the primary "external" storage device.
     *
     * @return <code>true</code> if the primary "external" storage device is mounted
     */
    public static boolean checkExternalStorageState() {
        String state = Environment.getExternalStorageState();

        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Gets the relative path used by this context.
     *
     * @param context the current context
     * @return the relative path or <code>null</code>
     * @throws android.content.pm.PackageManager.NameNotFoundException if the relative path cannot be found
     */
    public static String getRelativeSharedPath(Context context) throws NameNotFoundException {
        return "Android" + File.separator + "data" + File.separator + context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA).sharedUserId + File.separator;
    }

    /**
     * Tries to find the mount point used by external storage as <code>File</code>.
     * If not, returns the default <code>Environment.getExternalStorageDirectory()</code>.
     *
     * @param context the current context
     * @return the mount point as <code>File</code> used by external storage if available or <code>null</code>
     */
    public static File getExternalStorageDirectory(Context context) {
        if (checkExternalStorageState()) {
            try {
                File externalStorage = null;
                Scanner scannerMounts = new Scanner(new File("/proc/mounts"));

                while (scannerMounts.hasNext() && (externalStorage == null)) {
                    String line = scannerMounts.nextLine();

                    if (line.startsWith("/dev/block/vold/")) {
                        // device mount_path fs_type options
                        String[] lineElements = line.split(" ");
                        // gets the mount path
                        String element = lineElements[1];

                        // ignore default mount path and others
                        if (!element.equals(Environment.getExternalStorageDirectory().getPath()) && !element.equals("/mnt/secure/asec")) {
                            externalStorage = new File(element);
                        }
                    }
                }

                scannerMounts.close();

                if (externalStorage == null) {
                    externalStorage = Environment.getExternalStorageDirectory();
                }

                return externalStorage;
            }
            catch (FileNotFoundException fnfe) {
                Log.w(FileUtils.class.getName(), fnfe.getMessage(), fnfe);
                return Environment.getExternalStorageDirectory();
            }
        }

        return null;
    }

    /**
     * Gets a given filename as {@link java.io.File} according to the current application storage used (default or external storage).
     *
     * @param context  the current context
     * @param filename filename to load
     * @return the filename to load as {@link java.io.File}
     * @throws java.io.IOException if the given filename cannot be load as <code>File</code>
     */
    public static File getFileFromApplicationStorage(Context context, String filename) throws IOException {
        File externalStorageDirectory = getExternalStorageDirectory(context);

        if (externalStorageDirectory == null) {
            throw new IOException("unable to load '" + filename + "'");
        }

        try {
            return new File(externalStorageDirectory.getPath() + File.separator + getRelativeSharedPath(context) + filename);
        }
        catch (NameNotFoundException nnfe) {
            throw new IOException(nnfe);
        }
    }

    /**
     * Gets the inputs folder as {@link java.io.File} used by this context. The relative path used is <code>inputs/&lt;package_name&gt;</code>
     *
     * @param context the current context
     * @return the inputs folder as {@link java.io.File}
     * @throws java.io.IOException
     */
    public static File getInputsFolder(Context context) throws IOException {
        return getFileFromApplicationStorage(context, "inputs/" + context.getPackageName());
    }
}
