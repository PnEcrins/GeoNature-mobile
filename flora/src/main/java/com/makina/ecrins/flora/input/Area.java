package com.makina.ecrins.flora.input;

import android.os.Parcel;
import android.os.Parcelable;

import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.flora.input.Counting.CountingType;
import com.makina.ecrins.maps.geojson.Feature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes an geographic area edited in the map.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class Area implements Parcelable {

    public static final String KEY_ID = "id";
    public static final String KEY_AREA = "computed_area";
    public static final String KEY_FEATURE = "feature";
    public static final String KEY_FREQUENCY = "frequency";
    public static final String KEY_PHENOLOGY = "phenology";
    public static final String KEY_COUNTING = "counting";
    public static final String KEY_PHYSIOGNOMY = "physiognomy";
    public static final String KEY_DISTURBANCES = "disturbances";
    public static final String KEY_COMMENT = "comment";

    private long mAreaId;
    private double mComputedArea;
    private Feature mFeature;
    private Frequency mFrequency;
    private long mPhenologyId;
    private Counting mCounting;
    private final List<Long> mSelectedPhysiognomy;
    private final List<Long> mSelectedDisturbances;
    private String mComment;

    public Area(Feature pFeature) {
        mAreaId = AbstractInput.generateId();
        mComputedArea = 0.0;
        mFeature = pFeature;

        mPhenologyId = -1;
        mSelectedPhysiognomy = new ArrayList<>();
        mSelectedDisturbances = new ArrayList<>();

        // sets the default counting method (i.e. none)
        mCounting = new Counting(CountingType.NONE);

        mComment = "";
    }

    public Area(Parcel source) {
        mAreaId = source.readLong();
        mSelectedPhysiognomy = new ArrayList<>();
        mSelectedDisturbances = new ArrayList<>();

        mComputedArea = source.readDouble();
        mFeature = source.readParcelable(Feature.class.getClassLoader());
        mFrequency = source.readParcelable(Frequency.class.getClassLoader());
        mPhenologyId = source.readLong();
        mCounting = source.readParcelable(Counting.class.getClassLoader());
        source.readList(mSelectedPhysiognomy, Long.class.getClassLoader());
        source.readList(mSelectedDisturbances, Long.class.getClassLoader());
        mComment = source.readString();
    }

    public long getAreaId() {
        return mAreaId;
    }

    public double getComputedArea() {
        return mComputedArea;
    }

    public void setComputedArea(double pComputedArea) {
        this.mComputedArea = pComputedArea;
    }

    public Feature getFeature() {
        return mFeature;
    }

    public void setFeature(Feature pFeature) {
        this.mFeature = pFeature;
    }

    public Frequency getFrequency() {
        return mFrequency;
    }

    public void setFrequency(Frequency pFrequency) {
        this.mFrequency = pFrequency;
    }

    public long getPhenologyId() {
        return mPhenologyId;
    }

    public void setPhenologyId(long pPhenologyId) {
        this.mPhenologyId = pPhenologyId;
    }

    public Counting getCounting() {
        return mCounting;
    }

    public void setCounting(Counting pCounting) {
        this.mCounting = pCounting;
    }

    public List<Long> getSelectedPhysiognomy() {
        return mSelectedPhysiognomy;
    }

    public List<Long> getSelectedDisturbances() {
        return mSelectedDisturbances;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String pComment) {
        this.mComment = pComment;
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(KEY_ID, mAreaId);
        json.put(KEY_AREA, Double.valueOf(mComputedArea)
                .intValue());
        json.put(KEY_FEATURE, mFeature.getJSONObject());
        json.put(KEY_FREQUENCY, mFrequency.getJSONObject());
        json.put(KEY_PHENOLOGY, mPhenologyId);
        json.put(KEY_COUNTING, mCounting.getJSONObject());

        JSONArray jsonPhysiognomy = new JSONArray();

        for (Long physiognomyId : mSelectedPhysiognomy) {
            jsonPhysiognomy.put(physiognomyId);
        }

        json.put(KEY_PHYSIOGNOMY, jsonPhysiognomy);

        JSONArray jsonDisturbances = new JSONArray();

        for (Long disturbanceId : mSelectedDisturbances) {
            jsonDisturbances.put(disturbanceId);
        }

        json.put(KEY_DISTURBANCES, jsonDisturbances);
        json.put(KEY_COMMENT, mComment);

        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mAreaId);
        dest.writeDouble(mComputedArea);
        dest.writeParcelable(mFeature, 0);
        dest.writeParcelable(mFrequency, 0);
        dest.writeLong(mPhenologyId);
        dest.writeParcelable(mCounting, 0);
        dest.writeList(mSelectedPhysiognomy);
        dest.writeList(mSelectedDisturbances);
    }

    public static final Parcelable.Creator<Area> CREATOR = new Parcelable.Creator<Area>() {
        @Override
        public Area createFromParcel(Parcel source) {
            return new Area(source);
        }

        @Override
        public Area[] newArray(int size) {
            return new Area[size];
        }
    };
}
