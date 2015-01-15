package com.makina.ecrins.search.settings;

import android.os.Parcel;
import android.os.Parcelable;

import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.maps.MapSettings;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Global internal settings for this application.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class AppSettings
        extends AbstractAppSettings {

    public static final String KEY_MAP = "map";
    public static final String KEY_SEARCH = "search";

    private MapSettings mMapSettings;
    private SearchSettings mSearchSettings;

    public AppSettings(Parcel source) {
        super(source);

        mMapSettings = source.readParcelable(MapSettings.class.getClassLoader());
        mSearchSettings = source.readParcelable(SearchSettings.class.getClassLoader());
    }

    public AppSettings(JSONObject json) throws JSONException {
        super(json);

        mMapSettings = new MapSettings(json.getJSONObject(KEY_MAP));

        if (json.has(KEY_SEARCH)) {
            mSearchSettings = new SearchSettings(json.getJSONObject(KEY_SEARCH));
        }
        else {
            mSearchSettings = new SearchSettings();
        }
    }

    public MapSettings getMapSettings() {
        return mMapSettings;
    }

    public SearchSettings getSearchSettings() {
        return mSearchSettings;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(
            Parcel dest,
            int flags) {
        dest.writeParcelable(
                mMapSettings,
                0
        );
    }

    public static final Parcelable.Creator<AppSettings> CREATOR = new Parcelable.Creator<AppSettings>() {
        
        @Override
        public AppSettings createFromParcel(Parcel source) {
            return new AppSettings(source);
        }

        @Override
        public AppSettings[] newArray(int size) {
            return new AppSettings[size];
        }
    };
}
