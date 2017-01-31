package com.makina.ecrins.commons.settings;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Default inner settings for local database.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class DbSettings
        implements Parcelable {

    public static final DbSettings DEFAULT = new DbSettings("data.db",
                                                            1);

    public static final String KEY_DB_NAME = "name";
    public static final String KEY_DB_VERSION = "version";

    private String mName;
    private int mVersion;

    public DbSettings(Parcel source) {
        this.mName = source.readString();
        this.mVersion = source.readInt();
    }

    public DbSettings(@NonNull final String name,
                      int version) {
        this.mName = name;
        this.mVersion = version;
    }

    @Deprecated
    public DbSettings(JSONObject json) throws
                                       JSONException {
        this.mName = json.getString(KEY_DB_NAME);
        this.mVersion = json.getInt(KEY_DB_VERSION);
    }

    @NonNull
    public String getName() {
        return mName;
    }

    public int getVersion() {
        return mVersion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeString(mName);
        dest.writeInt(mVersion);
    }

    public static final Creator<DbSettings> CREATOR = new Creator<DbSettings>() {
        @Override
        public DbSettings createFromParcel(Parcel source) {

            return new DbSettings(source);
        }

        @Override
        public DbSettings[] newArray(int size) {

            return new DbSettings[size];
        }
    };
}
