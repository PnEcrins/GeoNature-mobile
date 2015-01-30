package com.makina.ecrins.search.settings;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 'Search' settings.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SearchSettings
        implements Parcelable {

    private static final String KEY_MAX_RADIUS = "max_radius";
    private static final String KEY_DEFAULT_RADIUS = "default_radius";
    private static final String KEY_MAX_FEATURES_FOUND = "max_features_found";

    private int mMaxRadius;
    private int mDefaultRadius;
    private int mMaxFeaturesFound;

    public SearchSettings() {
        mMaxRadius = 2000;
        mDefaultRadius = 1000;
        mMaxFeaturesFound = 100;
    }

    public SearchSettings(Parcel source) {
        mMaxRadius = source.readInt();
        mDefaultRadius = source.readInt();
        mMaxFeaturesFound = source.readInt();
    }

    public SearchSettings(JSONObject json) throws JSONException {
        if (json.has(KEY_MAX_RADIUS)) {
            this.mMaxRadius = json.getInt(KEY_MAX_RADIUS);
        }

        if (json.has(KEY_DEFAULT_RADIUS)) {
            this.mDefaultRadius = json.getInt(KEY_DEFAULT_RADIUS);
        }

        if (json.has(KEY_MAX_FEATURES_FOUND)) {
            this.mMaxFeaturesFound = json.getInt(KEY_MAX_FEATURES_FOUND);
        }
    }

    public int getMaxRadius() {
        return mMaxRadius;
    }

    public int getDefaultRadius() {
        return mDefaultRadius;
    }

    public int getMaxFeaturesFound() {
        return mMaxFeaturesFound;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(
            Parcel dest,
            int flags) {
        dest.writeInt(mMaxRadius);
        dest.writeInt(mDefaultRadius);
        dest.writeInt(mMaxFeaturesFound);
    }

    public static final Creator<SearchSettings> CREATOR = new Creator<SearchSettings>() {

        @Override
        public SearchSettings createFromParcel(Parcel source) {
            return new SearchSettings(source);
        }

        @Override
        public SearchSettings[] newArray(int size) {
            return new SearchSettings[size];
        }
    };
}
