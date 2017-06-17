package com.makina.ecrins.search.settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Default {@code JsonReader} about reading a {@code JSON} stream and build the corresponding
 * {@link SearchSettings}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SearchSettingsReader {

    private static final String TAG = SearchSettingsReader.class.getName();

    /**
     * parse a {@code JSON} string to convert as {@link SearchSettings}.
     *
     * @param json the {@code JSON} string to parse
     *
     * @return a {@link SearchSettings} instance from the {@code JSON} string or {@code null} if something goes wrong
     *
     * @see #readSearchSettings(Reader)
     */
    @Nullable
    public SearchSettings readSearchSettings(@Nullable final String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return readSearchSettings(new StringReader(json));
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());
        }

        return null;
    }

    /**
     * parse a {@code JSON} reader to convert as {@link SearchSettings}.
     *
     * @param in the {@code Reader} to parse
     *
     * @return a {@link SearchSettings} instance from the {@code JSON} reader
     *
     * @throws IOException if something goes wrong
     * @see #readSearchSettings(JsonReader)
     */
    @NonNull
    public SearchSettings readSearchSettings(@NonNull final Reader in) throws
                                                                       IOException {
        final JsonReader jsonReader = new JsonReader(in);
        final SearchSettings searchSettings = readSearchSettings(jsonReader);
        jsonReader.close();

        return searchSettings;
    }

    /**
     * Use a {@code JsonReader} instance to convert as {@link SearchSettings}.
     *
     * @param reader the {@code JsonReader} to use
     *
     * @return a {@link SearchSettings} instance from the {@code JsonReader}
     *
     * @throws IOException if something goes wrong
     */
    @NonNull
    public SearchSettings readSearchSettings(@NonNull final JsonReader reader) throws
                                                                               IOException {
        final SearchSettings searchSettings = new SearchSettings();

        reader.beginObject();

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "max_radius":
                    searchSettings.mMaxRadius = reader.nextInt();
                    break;
                case "default_radius":
                    searchSettings.mDefaultRadius = reader.nextInt();
                    break;
                case "max_features_found":
                    searchSettings.mMaxFeaturesFound = reader.nextInt();
                    break;
            }
        }

        reader.endObject();

        return searchSettings;
    }
}
