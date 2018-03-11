package com.geonature.mobile.commons.settings;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.geonature.mobile.commons.sync.SyncSettings;

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

    private static final String KEY_DB = "db";
    private static final String KEY_SYNC = "sync";
    private static final String KEY_QUALIFICATION = "qualification";

    DbSettings mDbSettings;
    SyncSettings mSyncSettings;
    QualificationSettings mQualificationSettings;

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
        mQualificationSettings = source.readParcelable(QualificationSettings.class.getClassLoader());
    }

    /**
     * @deprecated use {@link AppSettingsReader} instead
     */
    @Deprecated
    public AbstractAppSettings(JSONObject json) throws
                                                JSONException {
        this();

        if (json.has(KEY_DB)) {
            mDbSettings = new DbSettings(json.getJSONObject(KEY_DB));
        }

        mSyncSettings = new SyncSettings(json.getJSONObject(KEY_SYNC));

        final JSONObject jsonQualification = json.optJSONObject(KEY_QUALIFICATION);

        if (jsonQualification != null) {
            mQualificationSettings = new QualificationSettings(jsonQualification);
        }
    }

    @NonNull
    public DbSettings getDbSettings() {
        return mDbSettings;
    }

    @NonNull
    public SyncSettings getSyncSettings() {
        return mSyncSettings;
    }

    @Nullable
    public QualificationSettings getQualificationSettings() {
        return mQualificationSettings;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeParcelable(mDbSettings,
                             0);
        dest.writeParcelable(mSyncSettings,
                             0);
        dest.writeParcelable(mQualificationSettings,
                             0);
    }
}
