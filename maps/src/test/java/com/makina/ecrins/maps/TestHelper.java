package com.makina.ecrins.maps;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class helper about Unit tests.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class TestHelper {

    /**
     * Reads the contents of a file as {@code InputStream}.
     *
     * @param name the file to read (e.g. XML, JSON or any text file), must not be {@code null}
     *
     * @return the file contents as {@code InputStream}.
     */
    @Nullable
    public static InputStream getFixtureAsStream(@NonNull final String name) {
        return TestHelper.class.getClassLoader()
                               .getResourceAsStream("fixtures/" + name);
    }

    /**
     * Reads the contents of a file into a {@code String}.
     * <p>
     * The file is always closed.
     *
     * @param name the file to read (e.g. XML, JSON or any text file), must not be {@code null}
     *
     * @return the file contents, never {@code null}
     */
    @NonNull
    public static String getFixture(@NonNull final String name) {
        final StringBuilder stringBuilder = new StringBuilder();

        final InputStream inputStream = getFixtureAsStream(name);

        if (inputStream == null) {
            return stringBuilder.toString();
        }

        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String line = bufferedReader.readLine();

            while (line != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
                line = bufferedReader.readLine();
            }
        }
        catch (IOException ignored) {
        }
        finally {
            try {
                bufferedReader.close();
            }
            catch (IOException ignored) {
            }
        }

        return stringBuilder.toString()
                            .trim();
    }
}
