package com.makina.ecrins.flora.input;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.makina.ecrins.flora.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Frequency as a percentage.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class Frequency implements Parcelable {

    public static final String KEY_TYPE = "type";
    public static final String KEY_RECOMMENDED_STEP = "computed_recommended_step";
    public static final String KEY_TRANSECTS = "transects";
    public static final String KEY_TRANSECT_YES = "transect_yes";
    public static final String KEY_TRANSECT_NO = "transect_no";
    public static final String KEY_VALUE = "value";

    private FrequencyType mType;
    private double mRecommendedStep;
    private int mTransects;
    private int mTransectYes;
    private int mTransectNo;
    private double mValue;

    public Frequency(FrequencyType pType) {
        mType = pType;
        mRecommendedStep = 0.0d;
        mTransects = 1;
        mTransectYes = 0;
        mTransectNo = 0;
        mValue = 0.0d;
    }

    public Frequency(Parcel source) {
        mType = (FrequencyType) source.readSerializable();
        mRecommendedStep = source.readDouble();
        mTransects = source.readInt();
        mTransectYes = source.readInt();
        mTransectNo = source.readInt();
        mValue = source.readDouble();
    }

    public FrequencyType getType() {
        return mType;
    }

    public double getRecommendedStep() {
        return mRecommendedStep;
    }

    public void setRecommendedStep(double pRecommendedStep) {
        this.mRecommendedStep = pRecommendedStep;
    }

    public int getTransects() {
        return mTransects;
    }

    public void setTransects(int pTransects) {
        this.mTransects = pTransects;
    }

    public int getTransectYes() {
        return mTransectYes;
    }

    public void setTransectYes(int pTransectYes) {
        this.mTransectYes = pTransectYes;
    }

    public int getTransectNo() {
        return mTransectNo;
    }

    public void setTransectNo(int pTransectNo) {
        this.mTransectNo = pTransectNo;
    }

    public double getValue() {
        return mValue;
    }

    public void setValue(double pValue) {
        this.mValue = pValue;
    }

    @Deprecated
    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(KEY_TYPE, mType.getValue());
        json.put(KEY_RECOMMENDED_STEP, mRecommendedStep);
        json.put(KEY_TRANSECTS, mTransects);
        json.put(KEY_TRANSECT_YES, mTransectYes);
        json.put(KEY_TRANSECT_NO, mTransectNo);
        json.put(KEY_VALUE, mValue);

        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mType);
        dest.writeDouble(mRecommendedStep);
        dest.writeInt(mTransects);
        dest.writeInt(mTransectYes);
        dest.writeInt(mTransectNo);
        dest.writeDouble(mValue);
    }

    public enum FrequencyType {
        ESTIMATION("estimation", R.string.activity_frequency_estimation_title),
        TRANSECT("transect", R.string.activity_frequency_transect_title);

        private final String value;
        private final int resourceNameId;

        FrequencyType(String value, int resourceNameId) {
            this.value = value;
            this.resourceNameId = resourceNameId;
        }

        public String getValue() {
            return this.value;
        }

        public int getResourceNameId() {
            return resourceNameId;
        }

        @Nullable
        public static FrequencyType fromValue(@Nullable final String value) {
            if (TextUtils.isEmpty(value)) {
                return null;
            }

            for (FrequencyType frequencyType : values()) {
                if (frequencyType.getValue()
                             .equals(value)) {
                    return frequencyType;
                }
            }

            return null;
        }
    }

    public static final Parcelable.Creator<Frequency> CREATOR = new Parcelable.Creator<Frequency>() {
        @Override
        public Frequency createFromParcel(Parcel source) {
            return new Frequency(source);
        }

        @Override
        public Frequency[] newArray(int size) {
            return new Frequency[size];
        }
    };
}
