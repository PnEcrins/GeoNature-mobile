package com.makina.ecrins.commons.settings;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.makina.ecrins.commons.sync.SyncSettings;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Global internal settings.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressLint("ParcelCreator")
public abstract class AbstractAppSettings implements Parcelable {

    public static final String KEY_DB = "db";
    public static final String KEY_SYNC = "sync";

    private DbSettings mDbSettings;
    private SyncSettings mSyncSettings;

    public AbstractAppSettings(Parcel source) {
        mDbSettings = source.readParcelable(DbSettings.class.getClassLoader());
        mSyncSettings = source.readParcelable(SyncSettings.class.getClassLoader());
    }

    public AbstractAppSettings(JSONObject json) throws JSONException {
        if (json.has(KEY_DB)) {
            mDbSettings = new DbSettings(json.getJSONObject(KEY_DB));
        }
        else {
            mDbSettings = new DbSettings("data.db", 1);
        }

        mSyncSettings = new SyncSettings(json.getJSONObject(KEY_SYNC));
    }

    public DbSettings getDbSettings() {
        return mDbSettings;
    }

    public SyncSettings getSyncSettings() {
        return mSyncSettings;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mDbSettings, 0);
        dest.writeParcelable(mSyncSettings, 0);
    }
}
