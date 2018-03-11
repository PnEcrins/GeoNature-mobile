package com.geonature.mobile.commons.sync;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Export settings.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class ExportSettings
        implements Parcelable {

    public static final String KEY_URL = "url";
    public static final String KEY_FILE = "file";

    private String mUrl;
    private String mFile;

    public ExportSettings(@NonNull final String url,
                          @NonNull final String file) {
        this.mUrl = url;
        this.mFile = file;
    }

    public ExportSettings(Parcel source) {
        mUrl = source.readString();
        mFile = source.readString();
    }

    @Deprecated
    public ExportSettings(JSONObject json) throws
                                           JSONException {
        mUrl = json.getString(KEY_URL);
        mFile = json.getString(KEY_FILE);
    }

    @NonNull
    public String getUrl() {
        return mUrl;
    }

    @NonNull
    public String getFile() {
        return mFile;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeString(mUrl);
        dest.writeString(mFile);
    }

    public static final Creator<ExportSettings> CREATOR = new Creator<ExportSettings>() {
        @Override
        public ExportSettings createFromParcel(Parcel source) {
            return new ExportSettings(source);
        }

        @Override
        public ExportSettings[] newArray(int size) {
            return new ExportSettings[size];
        }
    };
}
