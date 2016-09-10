package com.makina.ecrins.invertebrate.settings;

import android.os.Parcel;
import android.os.Parcelable;

import com.makina.ecrins.commons.settings.AbstractAppSettings;
import com.makina.ecrins.maps.settings.MapSettings;

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

    private MapSettings mMapSettings;

    public AppSettings(Parcel source) {

        super(source);

        mMapSettings = source.readParcelable(MapSettings.class.getClassLoader());
    }

    public AppSettings(JSONObject json) throws JSONException {

        super(json);

        mMapSettings = new MapSettings(json.getJSONObject(KEY_MAP));
    }

    public MapSettings getMapSettings() {

        return mMapSettings;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(
            Parcel dest,
            int flags) {

        dest.writeParcelable(mMapSettings,
                             0);
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
