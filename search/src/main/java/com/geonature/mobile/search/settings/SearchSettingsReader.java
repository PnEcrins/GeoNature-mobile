package com.geonature.mobile.search.settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.JsonReader;

import java.io.IOException;

/**
 * Default {@code JsonReader} about reading a {@code JSON} stream and build the corresponding
 * {@link SearchSettings}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class SearchSettingsReader {

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
    SearchSettings readSearchSettings(@NonNull final JsonReader reader) throws
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
