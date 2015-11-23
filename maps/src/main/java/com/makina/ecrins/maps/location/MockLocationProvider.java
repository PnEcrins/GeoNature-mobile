package com.makina.ecrins.maps.location;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.makina.ecrins.maps.geojson.geometry.IGeoConstants;
import com.makina.ecrins.maps.geojson.geometry.IMathConstants;
import com.makina.ecrins.maps.util.DebugUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Simple mock location provider.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MockLocationProvider
        implements IMathConstants,
                   IGeoConstants {

    private static final String TAG = MockLocationProvider.class.getName();

    public static final String MOCK_LOCATION_PROVIDER = "mock_location_provider";

    private final Context mContext;
    private final LocationManager mLocationManager;
    private Location mCurrentLocation;

    public MockLocationProvider(Context pContext) {

        mContext = pContext;
        mLocationManager = (LocationManager) pContext.getSystemService(Context.LOCATION_SERVICE);

        shutdown();

        try {
            mLocationManager.addTestProvider(MOCK_LOCATION_PROVIDER,
                                             false,
                                             false,
                                             false,
                                             false,
                                             true,
                                             true,
                                             true,
                                             Criteria.POWER_LOW,
                                             Criteria.ACCURACY_FINE);
            mLocationManager.setTestProviderEnabled(MOCK_LOCATION_PROVIDER,
                                                    true);
        }
        catch (SecurityException se) {
            Log.w(TAG,
                  se.getMessage());
        }

        mCurrentLocation = null;
    }

    public void enableProvider(boolean enabled) {

        if (DebugUtils.isDebuggable(mContext) && (mLocationManager.getProvider(MOCK_LOCATION_PROVIDER) != null)) {
            mLocationManager.setTestProviderEnabled(MOCK_LOCATION_PROVIDER,
                                                    enabled);
        }
    }

    public boolean isProviderEnabled() {

        return mLocationManager.getProvider(MOCK_LOCATION_PROVIDER) != null && mLocationManager.isProviderEnabled(MOCK_LOCATION_PROVIDER);
    }

    public void pushLocation(Geolocation geolocation) {

        Log.d(TAG,
              "pushLocation " + geolocation.toString());

        Location location = new Location(MOCK_LOCATION_PROVIDER);
        location.setTime(System.currentTimeMillis());
        location.setLatitude(geolocation.getLatitude());
        location.setLongitude(geolocation.getLongitude());
        location.setAccuracy(geolocation.getAccuracy());

        if (isProviderEnabled()) {
            makeComplete(location);

            mCurrentLocation = location;
            mLocationManager.setTestProviderLocation(MOCK_LOCATION_PROVIDER,
                                                     location);
        }
        else {
            Log.w(TAG,
                  "pushLocation: provider '" + MOCK_LOCATION_PROVIDER + "' is not enabled !");
        }
    }

    /**
     * Given the current location, a random bearing and a given distance, this will calculate a new
     * random location traveling along a (shortest distance) great circle arc.
     * <p>
     * <dl>
     * <dt>Formula used<dt>
     * <dd>φ2 = asin(sin(φ1).cos(d/R) + cos(φ1).sin(d/R).cos(θ))
     * <br/>
     * λ2 = λ1 + atan2(sin(θ).sin(d/R).cos(φ1), cos(d/R) − sin(φ1).sin(φ2))
     * </dd>
     * <dt>where</dt>
     * <dd>φ is latitude, λ is longitude, θ is the bearing (in radians, clockwise from north),
     * d is the distance traveled, R is the earth’s radius (d/R is the angular distance, in radians)
     * </dd>
     * </dl>
     * </p>
     *
     * @param distance distance in meter
     */
    public void pushRandomLocationFromCurrentLocation(double distance) {

        if (this.mCurrentLocation == null) {
            Log.w(TAG,
                  "pushNewLocationFromCurrentLocation, failed to push new location: the current location is not defined yet");
        }
        else {
            final double dR = distance / Integer.valueOf(RADIUS_EARTH_METERS)
                                                .doubleValue();
            final double bearing = Math.random() * 2 * Math.PI;
            final double lat1 = this.mCurrentLocation.getLatitude() * DEG2RAD;
            final double lon1 = this.mCurrentLocation.getLongitude() * DEG2RAD;
            final double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dR) + Math.cos(lat1) * Math.sin(dR) * Math.cos(bearing));
            final double lon2 = lon1 + Math.atan2(Math.sin(bearing) * Math.sin(dR) * Math.cos(lat1),
                                                  Math.cos(dR) - Math.sin(lat1) * Math.sin(lat2));

            Location location = new Location(MOCK_LOCATION_PROVIDER);
            location.setTime(System.currentTimeMillis());
            location.setLatitude(lat2 * RAD2DEG);
            location.setLongitude(lon2 * RAD2DEG);
            location.setAccuracy(this.mCurrentLocation.getAccuracy());

            if (isProviderEnabled()) {
                makeComplete(location);

                mCurrentLocation = location;
                mLocationManager.setTestProviderLocation(MOCK_LOCATION_PROVIDER,
                                                         location);
            }
            else {
                Log.w(TAG,
                      "pushRandomLocationFromCurrentLocation: provider '" + MOCK_LOCATION_PROVIDER + "' is not enabled !");
            }
        }
    }

    public Location getCurrentLocation() {

        return this.mCurrentLocation;
    }

    public void shutdown() {

        if (mLocationManager.getProvider(MOCK_LOCATION_PROVIDER) != null) {
            mLocationManager.removeTestProvider(MOCK_LOCATION_PROVIDER);
        }
    }

    /**
     * See http://code.google.com/p/android/issues/detail?id=52919.
     */
    private void makeComplete(Location location) {

        try {
            Method locationJellyBeanFixMethod = Location.class.getMethod("makeComplete");

            if (locationJellyBeanFixMethod != null) {
                locationJellyBeanFixMethod.invoke(location);
            }
        }
        catch (NoSuchMethodException | IllegalArgumentException | IllegalAccessException | InvocationTargetException ge) {
            // nothing to do ...
        }
    }
}
