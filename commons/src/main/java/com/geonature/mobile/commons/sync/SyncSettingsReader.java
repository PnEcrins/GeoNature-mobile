package com.geonature.mobile.commons.sync;

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
 * {@link SyncSettings}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SyncSettingsReader {

    private static final String TAG = SyncSettingsReader.class.getName();

    /**
     * parse a {@code JSON} string to convert as {@link SyncSettings}.
     *
     * @param json the {@code JSON} string to parse
     *
     * @return a {@link SyncSettings} instance from the {@code JSON} string or {@code null} if
     * something goes wrong
     *
     * @see #readSyncSettings(Reader)
     */
    @Nullable
    public SyncSettings readSyncSettings(@Nullable final String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return readSyncSettings(new StringReader(json));
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());
        }

        return null;
    }

    /**
     * parse a {@code JSON} reader to convert as {@link SyncSettings}.
     *
     * @param in the {@code Reader} to parse
     *
     * @return a {@link SyncSettings} instance from the {@code JSON} reader
     *
     * @throws IOException if something goes wrong
     * @see #readSyncSettings(JsonReader)
     */
    @NonNull
    public SyncSettings readSyncSettings(@NonNull final Reader in) throws
                                                                   IOException {
        final JsonReader jsonReader = new JsonReader(in);
        final SyncSettings syncSettings = readSyncSettings(jsonReader);
        jsonReader.close();

        return syncSettings;
    }

    /**
     * Use a {@code JsonReader} instance to convert as {@link SyncSettings}.
     *
     * @param reader the {@code JsonReader} to use
     *
     * @return a {@link SyncSettings} instance from the {@code JsonReader}
     *
     * @throws IOException if something goes wrong
     */
    @NonNull
    public SyncSettings readSyncSettings(@NonNull final JsonReader reader) throws
                                                                           IOException {
        final SyncSettings.Builder builder = SyncSettings.Builder.newInstance();

        reader.beginObject();

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "url":
                    builder.setServerUrl(reader.nextString());
                    break;
                case "token":
                    builder.setToken(reader.nextString());
                    break;
                case "status_url":
                    builder.setStatusUrl(reader.nextString());
                    break;
                case "import_url":
                    builder.setImportUrl(reader.nextString());
                    break;
                case "exports":
                    reader.beginArray();

                    while (reader.hasNext()) {
                        final ExportSettings exportSettings = readExportSettings(reader);

                        if (exportSettings != null) {
                            builder.addExportSettings(exportSettings);
                        }
                    }

                    reader.endArray();

                    break;
            }
        }

        reader.endObject();

        return builder.build();
    }

    @Nullable
    private ExportSettings readExportSettings(@NonNull final JsonReader reader) throws
                                                                                IOException {
        reader.beginObject();

        String url = null;
        String file = null;

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "url":
                    url = reader.nextString();
                    break;
                case "file":
                    file = reader.nextString();
                    break;
            }
        }

        reader.endObject();

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(file)) {
            return null;
        }

        return new ExportSettings(url,
                                  file);
    }
}
