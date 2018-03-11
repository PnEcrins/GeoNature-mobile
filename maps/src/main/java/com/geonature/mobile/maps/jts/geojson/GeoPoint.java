package com.geonature.mobile.maps.jts.geojson;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Describes a geographical point by its latitude and its longitude.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class GeoPoint
        extends AbstractGeoJson
        implements Parcelable,
                   IMathConstants,
                   IGeoConstants,
                   Comparable<GeoPoint> {

    private final Point point;

    public GeoPoint(final int latitudeE6,
                    final int longitudeE6) {
        this(latitudeE6 / 1E6,
             longitudeE6 / 1E6);
    }

    public GeoPoint(final double latitude,
                    final double longitude) {
        this.point = new GeometryFactory().createPoint(new Coordinate(longitude,
                                                                      latitude));
    }

    public GeoPoint(@NonNull final Point point) {
        this.point = point;
    }

    protected GeoPoint(Parcel source) {
        point = (Point) source.readSerializable();
    }

    public double getLatitude() {
        return point.getY();
    }

    public double getLongitude() {
        return point.getX();
    }

    @NonNull
    public Point getPoint() {
        return point;
    }

    /**
     * Calculates the approximate length in meters between this {@link GeoPoint} and the given
     * {@link GeoPoint} projected onto the Earth.
     * <p>
     * This implementation uses the <a href= "http://en.wikipedia.org/wiki/Haversine_formula">haversine formula</a>
     * to calculate the <a href="http://en.wikipedia.org/wiki/Great-circle_distance">great-circle distance</a>
     * between two points – that is, the shortest distance over the earth’s surface – giving an
     * "as-the-crow-flies" distance between the points (ignoring any hills, of course!).
     * <dl>
     * <dt>Harversine</dt>
     * <dd>a = sin²(Δφ/2) + cos(φ1).cos(φ2).sin²(Δλ/2)</dd>
     * <dt>formula</dt>
     * <dd>c = 2.atan2(√a, √(1−a))<br/>d = R.c</dd>
     * </dl>
     * <dt>where</dt>
     * <dd>φ is latitude, λ is longitude, R is earth’s radius</dd>
     * </p>
     *
     * @param other {@link GeoPoint} to compute the distance
     *
     * @return The approximate geodesic length in meters
     */
    public double distanceTo(@NonNull final GeoPoint other) {
        final double dLat = (other.getLatitude() - getLatitude()) * DEG2RAD;
        final double dLng = (other.getLongitude() - getLongitude()) * DEG2RAD;

        final double lat1 = this.getLatitude() * DEG2RAD;
        final double lat2 = other.getLongitude() * DEG2RAD;

        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLng / 2) * Math.sin(dLng / 2) * Math.cos(lat1) * Math.cos(lat2);
        final double c = 2 * Math.atan2(Math.sqrt(a),
                                        Math.sqrt(1 - a));

        return (RADIUS_EARTH_METERS * c);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest,
                              int flags) {
        dest.writeSerializable(point);
    }

    @Override
    public int compareTo(@NonNull GeoPoint another) {
        return this.point.compareTo(another.point);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GeoPoint geoPoint = (GeoPoint) o;

        return point.equals(geoPoint.point);

    }

    @Override
    public int hashCode() {
        return point.hashCode();
    }

    /**
     * Represents this {@link GeoPoint} as a compact JSON array string.
     *
     * @return a compact JSON representation of this {@link GeoPoint}
     */
    @Override
    public String toString() {
        return "[" +
                point.getY() +
                ',' +
                point.getX() +
                ']';
    }

    public static final Creator<GeoPoint> CREATOR = new Creator<GeoPoint>() {
        @Override
        public GeoPoint createFromParcel(Parcel in) {
            return new GeoPoint(in);
        }

        @Override
        public GeoPoint[] newArray(int size) {
            return new GeoPoint[size];
        }
    };
}
