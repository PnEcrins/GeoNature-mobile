package com.makina.ecrins.maps.settings;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Default settings about the definition of the Coordinate Reference System (CRS) to use.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class CRSSettings
        implements Parcelable {

    public static final String KEY_CODE = "code";
    public static final String KEY_DEF = "def";
    public static final String KEY_BBOX = "bbox";

    private final String mCode;
    private final String mDef;
    private final List<Integer> mBbox = new ArrayList<>();

    public CRSSettings(@NonNull final String code,
                       @NonNull final String def,
                       @NonNull final List<Integer> bbox) {
        this.mCode = code;
        this.mDef = def;
        this.mBbox.addAll(bbox);
    }

    public CRSSettings(Parcel source) {
        this.mCode = source.readString();
        this.mDef = source.readString();
        source.readList(mBbox,
                        Integer.class.getClassLoader());
    }

    @Deprecated
    public CRSSettings(JSONObject json) throws
                                        JSONException {
        this.mCode = json.getString(KEY_CODE);
        this.mDef = json.getString(KEY_DEF);

        JSONArray bboxJsonArray = json.getJSONArray(KEY_BBOX);

        for (int i = 0; i < bboxJsonArray.length(); i++) {
            mBbox.add(bboxJsonArray.getInt(i));
        }
    }

    @NonNull
    public String getCode() {
        return mCode;
    }

    @NonNull
    public String getDef() {
        return mDef;
    }

    @NonNull
    public List<Integer> getBbox() {
        return mBbox;
    }

    @Deprecated
    public JSONObject getJSONObject() throws
                                      JSONException {
        JSONObject json = new JSONObject();
        json.put(KEY_CODE,
                 mCode);
        json.put(KEY_DEF,
                 mDef);
        json.put(KEY_BBOX,
                 new JSONArray(mBbox));

        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeString(mCode);
        dest.writeString(mDef);
        dest.writeList(mBbox);
    }

    public static final Parcelable.Creator<CRSSettings> CREATOR = new Parcelable.Creator<CRSSettings>() {
        @Override
        public CRSSettings createFromParcel(Parcel source) {
            return new CRSSettings(source);
        }

        @Override
        public CRSSettings[] newArray(int size) {
            return new CRSSettings[size];
        }
    };
}
