package com.makina.ecrins.commons.sync;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Default settings for synchronizing data.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SyncSettings
        implements Parcelable {

    public static final String KEY_SERVER_URL = "url";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_STATUS_URL = "status_url";
    public static final String KEY_IMPORT_URL = "import_url";
    public static final String KEY_EXPORTS = "exports";

    private String mServerUrl;
    private String mToken;
    private String mStatusUrl;
    private String mImportUrl;
    private final List<ExportSettings> mExports = new ArrayList<>();

    public SyncSettings(Parcel source) {
        mServerUrl = source.readString();
        mToken = source.readString();
        mStatusUrl = source.readString();
        mImportUrl = source.readString();
        source.readTypedList(mExports,
                             ExportSettings.CREATOR);
    }

    @Deprecated
    public SyncSettings(JSONObject json) throws
                                         JSONException {
        mServerUrl = json.getString(KEY_SERVER_URL);
        mToken = json.getString(KEY_TOKEN);
        mStatusUrl = json.getString(KEY_STATUS_URL);
        mImportUrl = json.getString(KEY_IMPORT_URL);

        JSONArray exportsJsonArray = json.getJSONArray(KEY_EXPORTS);

        for (int i = 0; i < exportsJsonArray.length(); i++) {
            mExports.add(new ExportSettings(exportsJsonArray.getJSONObject(i)));
        }
    }

    private SyncSettings(@NonNull final String serverUrl,
                         @NonNull final String token,
                         @NonNull final String statusUrl,
                         @NonNull final String importUrl,
                         @NonNull final List<ExportSettings> exports) {
        this.mServerUrl = serverUrl;
        this.mToken = token;
        this.mStatusUrl = statusUrl;
        this.mImportUrl = importUrl;
        this.mExports.addAll(exports);
    }

    @NonNull
    public String getServerUrl() {
        return mServerUrl;
    }

    @NonNull
    public String getToken() {
        return mToken;
    }

    @NonNull
    public String getStatusUrl() {
        return mStatusUrl;
    }

    @NonNull
    public String getImportUrl() {
        return mImportUrl;
    }

    @NonNull
    public List<ExportSettings> getExports() {
        return mExports;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeString(mServerUrl);
        dest.writeString(mToken);
        dest.writeString(mStatusUrl);
        dest.writeString(mImportUrl);
        dest.writeTypedList(mExports);
    }

    /**
     * Builder implementation used to create new {@link SyncSettings}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public static final class Builder {

        private String serverUrl;
        private String token;
        private String statusUrl;
        private String importUrl;
        private final List<ExportSettings> exports = new ArrayList<>();

        private Builder() {
        }

        @NonNull
        public static Builder newInstance() {
            return new Builder();
        }

        /**
         * Set the base sync server url.
         *
         * @param serverUrl the base server url
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder setServerUrl(@NonNull final String serverUrl) {
            this.serverUrl = serverUrl;

            return this;
        }

        @NonNull
        public Builder setToken(@NonNull final String token) {
            this.token = token;

            return this;
        }

        @NonNull
        public Builder setStatusUrl(@NonNull final String statusUrl) {
            this.statusUrl = statusUrl;

            return this;
        }

        @NonNull
        public Builder setImportUrl(@NonNull final String importUrl) {
            this.importUrl = importUrl;

            return this;
        }

        /**
         * Add a file to export locally.
         *
         * @param url  the url on which to retrieve the file
         * @param file the local file to save
         *
         * @return Fluent interface
         *
         * @see #addExportSettings(ExportSettings)
         */
        @NonNull
        public Builder addExportSettings(@NonNull final String url,
                                         @NonNull final String file) {
            return addExportSettings(new ExportSettings(url,
                                                        file));
        }

        /**
         * Add a {@link ExportSettings}.
         *
         * @param exportSettings the {@link ExportSettings} to add
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder addExportSettings(@NonNull final ExportSettings exportSettings) {
            this.exports.add(exportSettings);

            return this;
        }

        /**
         * Remove all {@link ExportSettings}.
         *
         * @return Fluent interface
         */
        @NonNull
        public Builder removeAllExportSettings() {
            this.exports.clear();

            return this;
        }

        /**
         * Builds a new instance of {@link SyncSettings}.
         *
         * @return new instance of {@link SyncSettings}
         */
        @NonNull
        public SyncSettings build() {
            return new SyncSettings(serverUrl,
                                    token,
                                    statusUrl,
                                    importUrl,
                                    exports);
        }
    }

    public static final Creator<SyncSettings> CREATOR = new Creator<SyncSettings>() {
        @Override
        public SyncSettings createFromParcel(Parcel source) {
            return new SyncSettings(source);
        }

        @Override
        public SyncSettings[] newArray(int size) {
            return new SyncSettings[size];
        }
    };
}
