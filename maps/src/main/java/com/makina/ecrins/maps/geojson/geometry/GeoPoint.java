package com.makina.ecrins.maps.geojson.geometry;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;

/**
 * Describes a geographical point by its latitude and its longitude.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@Deprecated
public class GeoPoint implements IMathConstants, IGeoConstants, Serializable, Parcelable, Comparable<GeoPoint> {

    private static final long serialVersionUID = -2537159865270907402L;

    public static final int LAT_LON = 0;
    public static final int LON_LAT = 1;

    private int mLatitudeE6;
    private int mLongitudeE6;

    public GeoPoint(final int pLatitudeE6, final int pLongitudeE6) {
        this.mLatitudeE6 = pLatitudeE6;
        this.mLongitudeE6 = pLongitudeE6;
    }

    public GeoPoint(final double pLatitude, final double pLongitude) {
        this.mLatitudeE6 = (int) (pLatitude * 1E6);
        this.mLongitudeE6 = (int) (pLongitude * 1E6);
    }

    public GeoPoint(final GeoPoint pGeopoint) {
        this.mLatitudeE6 = pGeopoint.getLatitudeE6();
        this.mLongitudeE6 = pGeopoint.getLongitudeE6();
    }

    public GeoPoint(final Parcel source) {
        this.mLatitudeE6 = source.readInt();
        this.mLongitudeE6 = source.readInt();
    }

    public GeoPoint(final JSONArray json, final int representation) throws JSONException {
        if (json.length() == 2) {
            switch (representation) {
                case LAT_LON:
                    this.mLatitudeE6 = (int) (json.getDouble(0) * 1E6);
                    this.mLongitudeE6 = (int) (json.getDouble(1) * 1E6);
                    break;
                default:
                    this.mLatitudeE6 = (int) (json.getDouble(1) * 1E6);
                    this.mLongitudeE6 = (int) (json.getDouble(0) * 1E6);
                    break;
            }
        }
        else {
            throw new IllegalArgumentException("not a valid GeoPoint JSON representation !");
        }
    }

    public int getLatitudeE6() {
        return mLatitudeE6;
    }

    public void setLatitudeE6(int pLatitudeE6) {
        this.mLatitudeE6 = pLatitudeE6;
    }

    public int getLongitudeE6() {
        return mLongitudeE6;
    }

    public void setLongitudeE6(int pLongitudeE6) {
        this.mLongitudeE6 = pLongitudeE6;
    }

    public void setCoordsE6(final int pLatitudeE6, final int pLongitudeE6) {
        this.mLatitudeE6 = pLatitudeE6;
        this.mLongitudeE6 = pLongitudeE6;
    }

    public JSONArray getJSONObject(int representation) {
        JSONArray json = new JSONArray();

        try {
            switch (representation) {
                case LAT_LON:
                    json.put(mLatitudeE6 / 1E6);
                    json.put(mLongitudeE6 / 1E6);
                    break;
                default:
                    json.put(mLongitudeE6 / 1E6);
                    json.put(mLatitudeE6 / 1E6);
                    break;
            }
        }
        catch (JSONException je) {
            Log.w(GeoPoint.class.getName(), je.getMessage(), je);
        }

        return json;
    }

    /**
     * Encodes this object as a compact JSON string.
     */
    @Override
    public String toString() {
        return getJSONObject(LON_LAT).toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mLatitudeE6);
        dest.writeInt(mLongitudeE6);
    }

    @Override
    public int compareTo(@NonNull GeoPoint another) {
        if (this == another) {
            return 0;
        }

        return Integer.valueOf(mLatitudeE6).compareTo(another.getLatitudeE6()) +
                Integer.valueOf(mLongitudeE6).compareTo(another.getLongitudeE6());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof GeoPoint)) {
            return false;
        }

        GeoPoint geoPoint = (GeoPoint) o;

        return mLatitudeE6 == geoPoint.mLatitudeE6 && mLongitudeE6 == geoPoint.mLongitudeE6;
    }

    @Override
    public int hashCode() {
        int result = mLatitudeE6;
        result = 31 * result + mLongitudeE6;
        return result;
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
     * @return The approximate geodesic length in meters
     */
    public double distanceTo(final GeoPoint other) {
        final double dLat = ((other.getLatitudeE6() / 1E6) - (this.mLatitudeE6 / 1E6)) * DEG2RAD;
        final double dLng = ((other.getLongitudeE6() / 1E6) - (this.mLongitudeE6 / 1E6)) * DEG2RAD;

        final double lat1 = (this.mLatitudeE6 / 1E6) * DEG2RAD;
        final double lat2 = (other.getLongitudeE6() / 1E6) * DEG2RAD;

        final double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLng / 2) * Math.sin(dLng / 2) * Math.cos(lat1) * Math.cos(lat2);
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return (RADIUS_EARTH_METERS * c);
    }

    public static final Parcelable.Creator<GeoPoint> CREATOR = new Parcelable.Creator<GeoPoint>() {
        @Override
        public GeoPoint createFromParcel(Parcel source) {
            return new GeoPoint(source);
        }

        @Override
        public GeoPoint[] newArray(int size) {
            return new GeoPoint[size];
        }
    };
}
