package com.makina.ecrins.maps.location;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Describes the selected geolocation.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class Geolocation implements Parcelable {

    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_ACCURACY = "accuracy";

    private double mLongitude;
    private double mLatitude;
    private int mAccuracy;

    public Geolocation(double pLongitude, double pLatitude, int pAccuracy) {
        this.mLongitude = pLongitude;
        this.mLatitude = pLatitude;
        this.mAccuracy = pAccuracy;
    }

    public Geolocation(Parcel source) {
        setLongitude(source.readDouble());
        setLatitude(source.readDouble());
        setAccuracy(source.readInt());
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public int getAccuracy() {
        return mAccuracy;
    }

    public void setAccuracy(int accuracy) {
        this.mAccuracy = accuracy;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public JSONObject getJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(KEY_LATITUDE, mLatitude);
        json.put(KEY_LONGITUDE, mLongitude);
        json.put(KEY_ACCURACY, mAccuracy);

        return json;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(mLongitude);
        dest.writeDouble(mLatitude);
        dest.writeInt(mAccuracy);
    }

    @Override
    public String toString() {
        return "Geolocation [mLongitude=" + mLongitude + ", mLatitude=" + mLatitude + ", mAccuracy=" + mAccuracy + "]";
    }

    public static final Parcelable.Creator<Geolocation> CREATOR = new Parcelable.Creator<Geolocation>() {
        @Override
        public Geolocation createFromParcel(Parcel source) {
            return new Geolocation(source);
        }

        @Override
        public Geolocation[] newArray(int size) {
            return new Geolocation[size];
        }
    };
}
