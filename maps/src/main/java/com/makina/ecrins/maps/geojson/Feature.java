package com.makina.ecrins.maps.geojson;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.makina.ecrins.maps.geojson.geometry.IGeometry;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Describes a {@link Feature} object.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class Feature implements Parcelable {

    private String mId;
    private IGeometry mGeometry;
    private FeatureStyle mFeatureStyle;
    private Bundle mProperties = new Bundle();

    public Feature(String pId) {
        this.mId = pId;
        this.mGeometry = null;
        this.mFeatureStyle = new FeatureStyle();
    }

    public Feature(Parcel source) {
        mId = source.readString();
        mGeometry = source.readParcelable(IGeometry.class.getClassLoader());
        mProperties = source.readBundle();
    }

    public GeoJSONType getType() {
        return GeoJSONType.FEATURE;
    }

    public String getId() {
        return mId;
    }

    public void setId(String pId) {
        this.mId = pId;
    }

    public IGeometry getGeometry() {
        return mGeometry;
    }

    public void setGeometry(IGeometry pGeometry) {
        this.mGeometry = pGeometry;

        if (this.mGeometry != null) {
            // sets a default style for the given IGeometry
            switch (pGeometry.getType()) {
                case LINE_STRING:
                    getFeatureStyle().setFill(false);
                    break;
                case POLYGON:
                    getFeatureStyle().setFill(true);
                    break;
                default:
                    break;
            }
        }
    }

    public FeatureStyle getFeatureStyle() {
        return mFeatureStyle;
    }

    public void setFeatureStyle(FeatureStyle pFeatureStyle) {
        this.mFeatureStyle = pFeatureStyle;
    }

    public Bundle getProperties() {
        return mProperties;
    }

    /**
     * Performs an operation on a given {@link Feature}.
     *
     * @param filter the filter to apply to this {@link Feature}
     */
    public void apply(IFeatureFilterVisitor filter) {
        filter.filter(this);
    }

    public JSONObject getJSONObject() throws JSONException {
        final JSONObject json = new JSONObject();
        json.put("id", getId());
        json.put("type", getType().getValue());
        json.put("geometry", (getGeometry() == null) ? new JSONObject() : getGeometry().getJSONObject());

        JSONObject jsonProperties = new JSONObject();

        for (String key : mProperties.keySet()) {
            jsonProperties.put(key, mProperties.get(key));
        }

        json.put("properties", jsonProperties);

        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeParcelable(mGeometry, 0);
        dest.writeBundle(mProperties);
    }

    public static final Parcelable.Creator<Feature> CREATOR = new Parcelable.Creator<Feature>() {
        @Override
        public Feature createFromParcel(Parcel source) {
            return new Feature(source);
        }

        @Override
        public Feature[] newArray(int size) {
            return new Feature[size];
        }
    };
}
