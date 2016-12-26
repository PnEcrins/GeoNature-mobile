package com.makina.ecrins.commons.settings;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.makina.ecrins.commons.sync.SyncSettings;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Global internal settings.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressLint("ParcelCreator")
public abstract class AbstractAppSettings
        implements Parcelable {

    public static final String KEY_DB = "db";
    public static final String KEY_SYNC = "sync";

    protected DbSettings mDbSettings;
    protected SyncSettings mSyncSettings;

    public AbstractAppSettings() {
        mDbSettings = DbSettings.DEFAULT;
    }

    public AbstractAppSettings(Parcel source) {
        this();

        final DbSettings dbSettings = source.readParcelable(DbSettings.class.getClassLoader());

        if (dbSettings != null) {
            mDbSettings = dbSettings;
        }

        mSyncSettings = source.readParcelable(SyncSettings.class.getClassLoader());
    }

    @Deprecated
    public AbstractAppSettings(JSONObject json) throws JSONException {
        this();

        if (json.has(KEY_DB)) {
            mDbSettings = new DbSettings(json.getJSONObject(KEY_DB));
        }

        mSyncSettings = new SyncSettings(json.getJSONObject(KEY_SYNC));
    }

    @NonNull
    public DbSettings getDbSettings() {
        return mDbSettings;
    }

    @NonNull
    public SyncSettings getSyncSettings() {
        return mSyncSettings;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(
            Parcel dest,
            int flags) {
        dest.writeParcelable(mDbSettings,
                             0);
        dest.writeParcelable(mSyncSettings,
                             0);
    }
}
