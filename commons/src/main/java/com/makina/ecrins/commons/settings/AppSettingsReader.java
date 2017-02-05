package com.makina.ecrins.commons.settings;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

import com.makina.ecrins.commons.sync.SyncSettingsReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Default {@code JsonReader} about reading a {@code JSON} stream and build the corresponding
 * {@link AbstractAppSettings}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AppSettingsReader {

    private static final String TAG = AppSettingsReader.class.getName();

    private SyncSettingsReader mSyncSettingsReader;
    private final AppSettingsReader.OnAppSettingsReaderListener mOnAppSettingsReaderListener;

    public AppSettingsReader(@NonNull final OnAppSettingsReaderListener onAppSettingsReaderListener) {
        this.mSyncSettingsReader = new SyncSettingsReader();
        this.mOnAppSettingsReaderListener = onAppSettingsReaderListener;
    }

    /**
     * parse a {@code JSON} string to convert as {@link AbstractAppSettings}.
     *
     * @param json the {@code JSON} string to parse
     *
     * @return a {@link AbstractAppSettings} instance from the {@code JSON} string or {@code null}
     * if something goes wrong
     *
     * @see #read(Reader)
     */
    @Nullable
    public AbstractAppSettings read(@Nullable final String json) {
        if (TextUtils.isEmpty(json)) {
            return null;
        }

        try {
            return read(new StringReader(json));
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());
        }

        return null;
    }

    /**
     * parse a {@code JSON} reader to convert as {@link AbstractAppSettings}.
     *
     * @param in the {@code Reader} to parse
     *
     * @return a {@link AbstractAppSettings} instance from the {@code JSON} reader
     *
     * @throws IOException if something goes wrong
     */
    @NonNull
    public AbstractAppSettings read(@NonNull final Reader in) throws
                                                              IOException {
        final JsonReader jsonReader = new JsonReader(in);
        final AbstractAppSettings appSettings = readAppSettings(jsonReader);
        jsonReader.close();

        return appSettings;
    }

    @NonNull
    private AbstractAppSettings readAppSettings(@NonNull final JsonReader reader) throws
                                                                                  IOException {
        final AbstractAppSettings appSettings = mOnAppSettingsReaderListener.createAppSettings();

        reader.beginObject();

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "db":
                    final DbSettings dbSettings = readDbSettings(reader);

                    if (dbSettings != null) {
                        appSettings.mDbSettings = dbSettings;
                    }

                    break;
                case "sync":
                    appSettings.mSyncSettings = mSyncSettingsReader.readSyncSettings(reader);
                    break;
                default:
                    mOnAppSettingsReaderListener.readAdditionalSettings(reader,
                                                                        keyName,
                                                                        appSettings);
            }
        }

        reader.endObject();

        return appSettings;
    }

    @Nullable
    private DbSettings readDbSettings(@NonNull final JsonReader reader) throws
                                                                        IOException {
        reader.beginObject();

        String name = null;
        int version = 0;

        while (reader.hasNext()) {
            final String keyName = reader.nextName();

            switch (keyName) {
                case "name":
                    name = reader.nextString();
                    break;
                case "version":
                    version = reader.nextInt();
                    break;
            }
        }

        reader.endObject();

        if (TextUtils.isEmpty(name) || version == 0) {
            return null;
        }

        return new DbSettings(name,
                              version);
    }

    /**
     * Callback used by {@link AppSettingsReader}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public interface OnAppSettingsReaderListener {

        /**
         * Returns a new instance of {@link AbstractAppSettings}.
         *
         * @return new instance of {@link AbstractAppSettings}
         */
        @NonNull
        AbstractAppSettings createAppSettings();

        /**
         * Reading some additional settings to set to the given {@link AbstractAppSettings}.
         *
         * @param reader      the current @code JsonReader} to use
         * @param keyName     the JSON key read
         * @param appSettings the current {@link AbstractAppSettings} to use
         *
         * @throws IOException
         */
        void readAdditionalSettings(@NonNull final JsonReader reader,
                                    @NonNull final String keyName,
                                    @NonNull final AbstractAppSettings appSettings) throws
                                                                                    IOException;
    }
}
