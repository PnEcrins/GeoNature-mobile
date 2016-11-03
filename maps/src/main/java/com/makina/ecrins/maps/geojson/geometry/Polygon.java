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
 * Basic implementation of {@link Polygon} (with or without holes as {@link LineString}) as a
 * {@link LineString} object.
 * <p/>
 * A {@link Polygon} must contains at least three {@link Point}s.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@Deprecated
public class Polygon implements IGeometry, IMathConstants, IGeoConstants {

    private LineString mPolygon;
    private final List<Polygon> mHoles = new ArrayList<>();

    public Polygon(List<Point> pPoints) {
        this.mPolygon = new LineString(pPoints);
    }

    public Polygon(Parcel source) {
        source.readParcelable(LineString.class.getClassLoader());
        source.readTypedList(mHoles, Polygon.CREATOR);
    }

    @Override
    public GeoJSONType getType() {
        return GeoJSONType.POLYGON;
    }

    @Override
    public List<IGeometry> getCoordinates() {
        List<IGeometry> coordinates = new ArrayList<>();

        coordinates.add(getPolygon());

        for (Polygon hole : getHoles()) {
            coordinates.add(hole.getPolygon());
        }

        return coordinates;
    }

    @Override
    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray coordinatesArray = new JSONArray();

        // as LineString array
        for (IGeometry lineString : getCoordinates()) {
            coordinatesArray.put(lineString.getJSONObject()
                    .get("coordinates"));
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
        dest.writeParcelable(mPolygon, 0);
        dest.writeTypedList(mHoles);
    }

    public LineString getPolygon() {
        return mPolygon;
    }

    public void setPolygon(List<Point> pPoints) {
        this.mPolygon = new LineString(pPoints);
    }

    public void addHole(List<Point> pPolygon) {
        mHoles.add(new Polygon(pPolygon));
    }

    public void addHole(Polygon pPolygon) {
        mHoles.add(pPolygon);
    }

    public List<Polygon> getHoles() {
        return mHoles;
    }

    /**
     * Calculate the approximate length of this {@link Polygon} were it projected onto the Earth.
     *
     * @return the approximate geodesic length in meters.
     */
    public double getGeodesicLength() {
        double length = mPolygon.getGeodesicLength();

        // adds the last segment of the LineString if the last Point is not the same as the first Point
        if (!mPolygon.getPoints()
                .isEmpty() &&
                (mPolygon.getPoints()
                        .size() > 2) &&
                !(mPolygon.getPoints()
                        .get(mPolygon.getPoints()
                                .size() - 1)
                        .getGeoPoint()
                        .equals(mPolygon.getPoints()
                                .get(0)
                                .getGeoPoint()))) {
            length += mPolygon.getPoints()
                    .get(mPolygon.getPoints()
                            .size() - 1)
                    .getGeoPoint()
                    .distanceTo(mPolygon.getPoints()
                            .get(0)
                            .getGeoPoint());
        }

        return length;
    }

    /**
     * Calculates the approximate area of this {@link Polygon} were it projected onto the Earth.
     *
     * @param checkHoles also check if this {@link Polygon} contains holes and subtract the areas of
     *                   all these internal holes
     * @return the approximate geodesic area in square meters.
     * @see <a href="http://trs-new.jpl.nasa.gov/dspace/handle/2014/40409">http://trs-new.jpl.nasa.gov/dspace/handle/2014/40409</a>
     */
    public double getGeodesicArea(boolean checkHoles) {
        double area = 0.0;

        if (mPolygon.getPoints()
                .size() > 2) {
            for (int i = 0; i < mPolygon.getPoints()
                    .size(); i++) {
                // Do not add the last point if it's the same as the first point
                if ((i == mPolygon.getPoints()
                        .size() - 1) &&
                        (mPolygon.getPoints()
                                .get(mPolygon.getPoints()
                                        .size() - 1)
                                .getGeoPoint()
                                .equals(mPolygon.getPoints()
                                        .get(0)
                                        .getGeoPoint()))) {
                    break;
                }

                GeoPoint p1 = mPolygon.getPoints()
                        .get(i)
                        .getGeoPoint();
                GeoPoint p2 = mPolygon.getPoints()
                        .get((i + 1) % mPolygon.getPoints()
                                .size())
                        .getGeoPoint();
                area += (((p2.getLongitudeE6() / 1E6) - (p1.getLongitudeE6() / 1E6)) * DEG2RAD) *
                        (2 + Math.sin((p1.getLatitudeE6() / 1E6) * DEG2RAD) + Math.sin((p2.getLatitudeE6() / 1E6) * DEG2RAD));
            }

            area = area * RADIUS_EARTH_METERS * RADIUS_EARTH_METERS / 2.0;

            if (checkHoles) {
                for (Polygon hole : mHoles) {
                    area -= hole.getGeodesicArea(false);
                }
            }
        }

        return Math.abs(area);
    }

    public static final Parcelable.Creator<Polygon> CREATOR = new Parcelable.Creator<Polygon>() {
        @Override
        public Polygon createFromParcel(Parcel source) {
            return new Polygon(source);
        }

        @Override
        public Polygon[] newArray(int size) {
            return new Polygon[size];
        }
    };
}
