package com.makina.ecrins.maps.geojson;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.makina.ecrins.maps.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Defines styles to be applied to {@link Feature} instances.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 * @see <a href="http://leafletjs.com/reference.html#path">http://leafletjs.com/reference.html#path</a>
 */
@Deprecated
public class FeatureStyle implements Parcelable {

    public final String KEY_STROKE = "stroke";
    public final String KEY_COLOR = "color";
    public final String KEY_WEIGHT = "weight";
    public final String KEY_OPACITY = "opacity";
    public final String KEY_FILL = "fill";
    public final String KEY_FILL_COLOR = "fillColor";
    public final String KEY_FILL_OPACITY = "fillOpacity";

    private boolean mStroke;
    private int mColorResourceId;
    private int mWeight;
    private double mOpacity;
    private boolean mFill;
    private int mFillColorResourceId;
    private double mFillOpacity;

    public FeatureStyle() {
        mStroke = true;
        mColorResourceId = R.color.feature_dark_blue;
        mWeight = 5;
        mOpacity = 0.5;
        mFill = true;
        mFillColorResourceId = mColorResourceId;
        mFillOpacity = 0.2;
    }

    public FeatureStyle(Parcel source) {
        mStroke = (source.readByte() == 1);
        mColorResourceId = source.readInt();
        mWeight = source.readInt();
        mOpacity = source.readDouble();
        mFill = (source.readByte() == 1);
        mFillColorResourceId = source.readInt();
        mFillOpacity = source.readDouble();
    }

    /**
     * Whether to draw stroke along the path. Set it to false to disable borders.
     *
     * @return <code>true</code> if borders are enabled
     */
    public boolean isStroke() {
        return mStroke;
    }

    public FeatureStyle setStroke(boolean pStroke) {
        this.mStroke = pStroke;

        return this;
    }

    public int getColorResourceId() {
        return mColorResourceId;
    }

    public FeatureStyle setColorResourceId(int pColorResourceId) {
        this.mColorResourceId = pColorResourceId;

        return this;
    }

    public int getWeight() {
        return mWeight;
    }

    public FeatureStyle setWeight(int pWeight) {
        this.mWeight = pWeight;

        return this;
    }

    public double getOpacity() {
        return mOpacity;
    }

    public FeatureStyle setOpacity(double pOpacity) {
        this.mOpacity = pOpacity;

        return this;
    }

    public boolean isFill() {
        return mFill;
    }

    public FeatureStyle setFill(boolean pFill) {
        this.mFill = pFill;

        return this;
    }

    public int getFillColorResourceId() {
        return mFillColorResourceId;
    }

    public FeatureStyle setFillColorResourceId(int pFillColorResourceId) {
        this.mFillColorResourceId = pFillColorResourceId;

        return this;
    }

    public double getFillOpacity() {
        return mFillOpacity;
    }

    public FeatureStyle setFillOpacity(double pFillOpacity) {
        this.mFillOpacity = pFillOpacity;

        return this;
    }

    public JSONObject getJSONObject(Context pContext) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(KEY_STROKE, isStroke());
        json.put(KEY_WEIGHT, getWeight());
        json.put(KEY_OPACITY, getOpacity());
        json.put(KEY_FILL, isFill());
        json.put(KEY_FILL_OPACITY, getFillOpacity());

        if (pContext != null) {
            json.put(KEY_COLOR, String.format("#%06X", (0xFFFFFF & pContext.getResources()
                    .getColor(getColorResourceId()))));
            json.put(KEY_FILL_COLOR, String.format("#%06X", (0xFFFFFF & pContext.getResources()
                    .getColor(getFillColorResourceId()))));
        }

        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mStroke ? 1 : 0)); // as boolean value
        dest.writeInt(mColorResourceId);
        dest.writeInt(mWeight);
        dest.writeDouble(mOpacity);
        dest.writeByte((byte) (mFill ? 1 : 0)); // as boolean value
        dest.writeInt(mFillColorResourceId);
        dest.writeDouble(mFillOpacity);
    }

    public static final Parcelable.Creator<FeatureStyle> CREATOR = new Parcelable.Creator<FeatureStyle>() {
        @Override
        public FeatureStyle createFromParcel(Parcel source) {
            return new FeatureStyle(source);
        }

        @Override
        public FeatureStyle[] newArray(int size) {
            return new FeatureStyle[size];
        }
    };
}
