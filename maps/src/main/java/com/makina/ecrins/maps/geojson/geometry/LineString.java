package com.makina.ecrins.maps.geojson.geometry;

import android.os.Parcel;
import android.os.Parcelable;

import com.makina.ecrins.maps.geojson.GeoJSONType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of {@link LineString} as a {@link IGeometry} object.
 * <p>A {@link LineString} is a set of {@link Point} (at least two {@link Point}s).</p>
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class LineString implements IGeometry {

    private List<Point> mPoints = new ArrayList<Point>();

    public LineString(List<Point> pPoints) {
        this.mPoints = pPoints;
    }

    public LineString(Parcel source) {
        source.readTypedList(mPoints, Point.CREATOR);
    }

    public List<Point> getPoints() {
        return mPoints;
    }

    public void setPoints(List<Point> pPoints) {
        this.mPoints = pPoints;
    }

    @Override
    public GeoJSONType getType() {
        return GeoJSONType.LINE_STRING;
    }

    @Override
    public List<IGeometry> getCoordinates() {
        List<IGeometry> coordinates = new ArrayList<IGeometry>();
        coordinates.addAll(mPoints);

        return coordinates;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray coordinatesArray = new JSONArray();

        // Point array
        for (IGeometry point : getCoordinates()) {
            coordinatesArray.put(((Point) point).getGeoPoint()
                    .getJSONObject(GeoPoint.LON_LAT));
        }

        json.put("type", getType().getValue());
        json.put("coordinates", coordinatesArray);

        return json;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mPoints);
    }

    /**
     * Calculate the approximate length of this {@link LineString} were it projected onto the Earth.
     *
     * @return the approximate geodesic length in meters.
     */
    public double getGeodesicLength() {
        double length = 0.0;

        if (!mPoints.isEmpty()) {
            for (int i = 1; i < mPoints.size(); i++) {
                length += mPoints.get(i)
                        .getGeoPoint()
                        .distanceTo(mPoints.get(i - 1)
                                .getGeoPoint());
            }
        }

        return length;
    }

    public static final Parcelable.Creator<LineString> CREATOR = new Parcelable.Creator<LineString>() {
        @Override
        public LineString createFromParcel(Parcel source) {
            return new LineString(source);
        }

        @Override
        public LineString[] newArray(int size) {
            return new LineString[size];
        }
    };
}
