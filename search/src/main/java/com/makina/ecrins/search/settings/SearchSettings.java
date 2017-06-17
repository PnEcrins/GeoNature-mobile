package com.makina.ecrins.search.settings;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 'Search' settings.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SearchSettings
        implements Parcelable {

    int mMaxRadius;
    int mDefaultRadius;
    int mMaxFeaturesFound;

    SearchSettings() {
        mMaxRadius = 2000;
        mDefaultRadius = 1000;
        mMaxFeaturesFound = 100;
    }

    private SearchSettings(Parcel source) {
        mMaxRadius = source.readInt();
        mDefaultRadius = source.readInt();
        mMaxFeaturesFound = source.readInt();
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
