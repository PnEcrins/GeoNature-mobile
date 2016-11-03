package com.makina.ecrins.maps.geojson.geometry;

import android.os.Parcel;
import android.os.Parcelable;

import com.makina.ecrins.maps.geojson.GeoJSONType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of {@link Point} as a {@link IGeometry} object.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@Deprecated
public class Point implements IGeometry {

    private GeoPoint mGeoPoint;

    public Point(GeoPoint pGeoPoint) {
        this.mGeoPoint = pGeoPoint;
    }

    public Point(Parcel source) {
        mGeoPoint = source.readParcelable(GeoPoint.class.getClassLoader());
    }

    public GeoPoint getGeoPoint() {
        return mGeoPoint;
    }

    public void setGeoPoint(GeoPoint pGeoPoint) {
        this.mGeoPoint = pGeoPoint;
    }

    @Override
    public GeoJSONType getType() {
        return GeoJSONType.POINT;
    }

    @Override
    public List<IGeometry> getCoordinates() {
        List<IGeometry> coordinates = new ArrayList<>();
        coordinates.add(this);

        return coordinates;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("type", getType().getValue());
        json.put("coordinates", getGeoPoint().getJSONObject(GeoPoint.LON_LAT));

        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mGeoPoint, 0);
    }

    public static final Parcelable.Creator<Point> CREATOR = new Parcelable.Creator<Point>() {
        @Override
        public Point createFromParcel(Parcel source) {
            return new Point(source);
        }

        @Override
        public Point[] newArray(int size) {
            return new Point[size];
        }
    };
}
