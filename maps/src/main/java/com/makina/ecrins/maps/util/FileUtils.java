package com.makina.ecrins.maps.util;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helpers for {@code File} utilities.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();

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
     * Base64-encode the given data and return a newly allocated String with the result.
     *
     * @param inputStream the data to encode
     */
    @NonNull
    public static String toBase64(@NonNull final InputStream inputStream) {
        byte[] bytes;
        byte[] buffer = new byte[8192];
        int bytesRead;

        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer,
                             0,
                             bytesRead);
            }
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());
        }

        bytes = output.toByteArray();

        try {
            output.close();
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());
        }

        return Base64.encodeToString(bytes,
                                     Base64.DEFAULT);
    }
}
