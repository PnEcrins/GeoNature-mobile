package com.geonature.mobile.flora.input;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.geonature.mobile.flora.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Counting fertile and sterile.
 *
 * @author a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class Counting implements Parcelable {

    public static final String KEY_TYPE = "type";
    public static final String KEY_PLOT_SURFACE = "plot_surface";
    public static final String KEY_PLOTS = "plots";
    public static final String KEY_COUTING_FERTILE = "fertile";
    public static final String KEY_TOTAL_FERTILE = "total_fertile";
    public static final String KEY_COUTING_STERILE = "sterile";
    public static final String KEY_TOTAL_STERILE = "total_sterile";

    private final CountingType mType;
    private double mPlotSurface;
    private int mPlots;
    private int mCountFertile;
    private int mTotalFertile;
    private int mCountSterile;
    private int mTotalSterile;

    public Counting(CountingType pType) {
        mType = pType;
        mPlotSurface = 0.0;
        mPlots = 0;
        mCountFertile = 0;
        mTotalFertile = 0;
        mCountSterile = 0;
        mTotalSterile = 0;
    }

    public Counting(Parcel source) {
        mType = (CountingType) source.readSerializable();
        mPlotSurface = source.readDouble();
        mPlots = source.readInt();
        mCountFertile = source.readInt();
        mTotalFertile = source.readInt();
        mCountSterile = source.readInt();
        mTotalSterile = source.readInt();
    }

    public CountingType getType() {
        return mType;
    }

    public double getPlotSurface() {
        return mPlotSurface;
    }

    public void setPlotSurface(double pPlotSurface) {
        this.mPlotSurface = pPlotSurface;
    }

    public int getPlots() {
        return mPlots;
    }

    public void setPlots(int pPlots) {
        this.mPlots = pPlots;
    }

    public int getCountFertile() {
        return mCountFertile;
    }

    public void setCountFertile(int pCountFertile) {
        this.mCountFertile = pCountFertile;
    }

    public int getTotalFertile() {
        return mTotalFertile;
    }

    public void setTotalFertile(int pTotalFertile) {
        this.mTotalFertile = pTotalFertile;
    }

    public int getCountSterile() {
        return mCountSterile;
    }

    public void setCountSterile(int pCountSterile) {
        this.mCountSterile = pCountSterile;
    }

    public int getTotalSterile() {
        return mTotalSterile;
    }

    public void setTotalSterile(int pTotalSterile) {
        this.mTotalSterile = pTotalSterile;
    }

    /**
     * Whether this {@link Counting} is valid or not.
     *
     * @return <code>true</code> if this {@link Counting} is valid <code>false</code> otherwise.
     */
    public boolean isValid() {
        switch (mType) {
            case NONE:
            case EXHAUSTIVE:
                return true;
            case SAMPLING:
                return (mPlotSurface * mPlots) > 0;
            default:
                return true;
        }
    }

    @Deprecated
    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(KEY_TYPE, mType.getValue());
        json.put(KEY_PLOT_SURFACE, mPlotSurface);
        json.put(KEY_PLOTS, mPlots);
        json.put(KEY_COUTING_FERTILE, mCountFertile);
        json.put(KEY_TOTAL_FERTILE, mTotalFertile);
        json.put(KEY_COUTING_STERILE, mCountSterile);
        json.put(KEY_TOTAL_STERILE, mTotalSterile);

        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mType);
        dest.writeDouble(mPlotSurface);
        dest.writeInt(mPlots);
        dest.writeInt(mCountFertile);
        dest.writeInt(mTotalFertile);
        dest.writeInt(mCountSterile);
        dest.writeInt(mTotalSterile);
    }

    public enum CountingType {
        NONE("none", R.string.counting_no_method),
        EXHAUSTIVE("exhaustive", R.string.activity_counting_exhaustive_title),
        SAMPLING("sampling", R.string.activity_counting_sampling_title);

        private final String value;
        private final int resourceNameId;

        CountingType(String value, int resourceNameId) {
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
        public static CountingType fromValue(@Nullable final String value) {
            if (TextUtils.isEmpty(value)) {
                return null;
            }

            for (CountingType countingType : values()) {
                if (countingType.getValue()
                                 .equals(value)) {
                    return countingType;
                }
            }

            return null;
        }
    }

    public static final Parcelable.Creator<Counting> CREATOR = new Parcelable.Creator<Counting>() {
        @Override
        public Counting createFromParcel(Parcel source) {
            return new Counting(source);
        }

        @Override
        public Counting[] newArray(int size) {
            return new Counting[size];
        }
    };
}
