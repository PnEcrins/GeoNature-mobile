package com.makina.ecrins.maps.settings;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Default settings for a given map layer.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class LayerSettings implements Parcelable, Comparable<LayerSettings> {

    public static final String KEY_NAME = "name";
    public static final String KEY_LABEL = "label";
    public static final String KEY_SOURCE = "source";

    public static final String SOURCE_MBTILES = "mbtiles";
    public static final String SOURCE_MBTILES_SPLIT = "mbtiles_split";
    public static final String SOURCE_DIR = "dir";
    public static final String SOURCE_HTTP = "http";

    private String mName;
    private String mLabel;
    private String mSource;

    public LayerSettings(String pName, String pLabel, String pSource) {
        this.mName = pName;
        this.mLabel = pLabel;
        this.mSource = pSource;
    }

    public LayerSettings(Parcel source) {
        mName = source.readString();
        mLabel = source.readString();
        mSource = source.readString();
    }

    @Deprecated
    public LayerSettings(JSONObject json) throws JSONException {
        mName = json.getString(KEY_NAME);
        mLabel = json.getString(KEY_LABEL);
        mSource = json.getString(KEY_SOURCE);
    }

    /**
     * Returns the name of this layer used to load as resource.
     *
     * @return the name of this layer
     */
    public String getName() {
        return mName;
    }

    /**
     * User friendly label of this layer.
     *
     * @return the label of this layer
     */
    public String getLabel() {
        return mLabel;
    }

    /**
     * Returns the source type of this layer :
     * <ul>
     * <li>{@link LayerSettings#SOURCE_MBTILES}</li>
     * <li>{@link LayerSettings#SOURCE_MBTILES_SPLIT}</li>
     * <li>{@link LayerSettings#SOURCE_DIR}</li>
     * <li>{@link LayerSettings#SOURCE_HTTP}</li>
     * </ul>
     *
     * @return the source type
     */
    public String getSource() {
        return mSource;
    }

    @Deprecated
    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(KEY_NAME, mName);
        json.put(KEY_LABEL, mLabel);
        json.put(KEY_SOURCE, mSource);

        return json;
    }

    @Override
    public String toString() {
        return mLabel;
    }

    @Override
    public int compareTo(@NonNull LayerSettings another) {
        return mName.compareTo(another.getName());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mLabel);
        dest.writeSerializable(mSource);
    }

    public static final Parcelable.Creator<LayerSettings> CREATOR = new Parcelable.Creator<LayerSettings>() {
        @Override
        public LayerSettings createFromParcel(Parcel source) {
            return new LayerSettings(source);
        }

        @Override
        public LayerSettings[] newArray(int size) {
            return new LayerSettings[size];
        }
    };
}
