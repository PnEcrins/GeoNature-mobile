package com.makina.ecrins.commons.settings;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Default inner settings for local database.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class DbSettings
        implements Parcelable {

    public static final String KEY_DB_NAME = "name";
    public static final String KEY_DB_VERSION = "version";

    private String mDbName;
    private int mDbVersion;

    public DbSettings(Parcel source) {

        this.mDbName = source.readString();
        this.mDbVersion = source.readInt();
    }

    public DbSettings(
            String pDbName,
            int pDbVersion) {

        this.mDbName = pDbName;
        this.mDbVersion = pDbVersion;
    }

    public DbSettings(JSONObject json) throws JSONException {

        this.mDbName = json.getString(KEY_DB_NAME);
        this.mDbVersion = json.getInt(KEY_DB_VERSION);
    }

    public String getDbName() {

        return mDbName;
    }

    public int getDbVersion() {

        return mDbVersion;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(
            Parcel dest,
            int flags) {

        dest.writeString(mDbName);
        dest.writeInt(mDbVersion);
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
