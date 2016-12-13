package com.makina.ecrins.maps.control;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.makina.ecrins.maps.IWebViewFragment;
import com.makina.ecrins.maps.R;
import com.makina.ecrins.maps.content.ITilesLayerDataSource;
import com.makina.ecrins.maps.jts.geojson.GeoPoint;
import com.makina.ecrins.maps.location.Geolocation;
import com.makina.ecrins.maps.settings.CRSSettings;
import com.makina.ecrins.maps.settings.LayerSettings;
import com.makina.ecrins.maps.settings.MapSettings;
import com.makina.ecrins.maps.util.DebugUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This is the main control to be used with {@link IWebViewFragment}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainControl
        extends AbstractControl
        implements LocationListener {

    private static JSONObject jsonObject = new JSONObject();

    private boolean mDisplayWarningAboutLocationOutsideMapBoundaries = true;

    public MainControl(Context pContext) {
        super(pContext);

        setControlListener(new OnIControlListener() {
            @Override
            public void onControlInitialized() {
                Log.d(getClass().getName(),
                      "onControlInitialized");

                mWebViewFragment.requestLocationUpdates(MainControl.this);

                if (DebugUtils.isDebuggable(mWebViewFragment.getContext())) {
                    mWebViewFragment.getMockLocationProvider()
                                    .pushLocation(new Geolocation(6.027559215973642,
                                                                  44.772039260501735,
                                                                  10));
                }

                // register all additional controls
                for (String controlName : mWebViewFragment.getControls()) {
                    // do not add this instance
                    if (!controlName.equals(getName())) {
                        mWebViewFragment.getControl(controlName)
                                        .add(mWebViewFragment);
                    }
                }

                mDisplayWarningAboutLocationOutsideMapBoundaries = true;

                Location currentLocation = mWebViewFragment.getCurrentLocation();

                if (currentLocation != null) {
                    mWebViewFragment.loadUrl(getJSUrlPrefix() + ".showCurrentLocation(" + currentLocation.getLatitude() + ", " + currentLocation.getLongitude() + ", " + currentLocation.getAccuracy() + ", 0)");
                }
            }
        });
    }

    @Override
    public View getView(boolean forceCreate) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(MainControl.class.getName(),
              "onLocationChanged [provider: " + location.getProvider() + ", lat: " + location.getLatitude() + ", lon: " + location.getLongitude() + ", acc: " + location.getAccuracy() + ", bearing: " + location.getBearing());

        // checks if this location is inside the map or not
        if ((this.mWebViewFragment.getMapSettings()
                                  .getPolygonBounds() != null) && this.mWebViewFragment.getMapSettings()
                                                                                       .getPolygonBounds()
                                                                                       .contains(new GeoPoint(location.getLatitude(),
                                                                                                              location.getLongitude()).getPoint())) {
            this.mWebViewFragment.setCurrentLocation(location);

            if (isControlInitialized()) {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".showCurrentLocation(" + location.getLatitude() + ", " + location.getLongitude() + ", " + location.getAccuracy() + ", " + "0" + ")");
            }
        }
        else {
            this.mWebViewFragment.setCurrentLocation(null);

            // displays Toast to notify user that he can't use this feature
            Log.w(MainControl.class.getName(),
                  "onLocationChanged : outside map boundaries !");

            if (mDisplayWarningAboutLocationOutsideMapBoundaries) {
                Toast.makeText(this.mWebViewFragment.getContext(),
                               R.string.message_location_outside_map_boundaries,
                               Toast.LENGTH_LONG)
                     .show();
                mDisplayWarningAboutLocationOutsideMapBoundaries = false;
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(MainControl.class.getName(),
              "onProviderDisabled " + provider);

        this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".hideCurrentLocation()");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(MainControl.class.getName(),
              "onProviderEnabled " + provider);
    }

    @Override
    public void onStatusChanged(String provider,
                                int status,
                                Bundle extras) {
        Log.d(MainControl.class.getName(),
              "onStatusChanged " + provider + " : " + status);
    }

    @JavascriptInterface
    public void setMapInitialized() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(MainControl.class.getName(),
                      "setMapInitialized");

                mWebViewFragment.setMapSettings(mWebViewFragment.getMapSettings());

                initializeJSController("js/Control.Main.js",
                                       "new L.Control.Main()");
            }
        });
    }

    /**
     * Gets the current CRS configuration.
     *
     * @return {@link CRSSettings} as {@link JSONObject}
     */
    @JavascriptInterface
    public String getCRS() {
        final CRSSettings crsSettings = this.mWebViewFragment.getMapSettings()
                                                             .getCRSSettings();

        if (crsSettings == null) {
            return null;
        }

        try {
            return crsSettings.getJSONObject()
                              .toString();
        }
        catch (JSONException je) {
            Log.w(MainControl.class.getName(),
                  je.getMessage(),
                  je);

            return null;
        }
    }

    @JavascriptInterface
    public String getMaxBounds() {
        final List<GeoPoint> maxBounds = this.mWebViewFragment.getMapSettings()
                                                              .getMaxBounds();

        final StringBuilder jsonArray = new StringBuilder();
        jsonArray.append('[');

        for (int i = 0; i < maxBounds.size(); i++) {
            if (i > 0) {
                jsonArray.append(',');
            }

            jsonArray.append(maxBounds.get(i)
                                      .toString());
        }

        jsonArray.append(']');

        return jsonArray.toString();
    }

    @JavascriptInterface
    public String getCenter() {
        return this.mWebViewFragment.getMapSettings()
                                    .getCenter()
                                    .toString();
    }

    @JavascriptInterface
    public void setCenter(final double latitude,
                          final double longitude) {
        Log.d(MainControl.class.getName(),
              "setCenter [latitude : " + latitude + ", longitude : " + longitude + "]");

        getHandler().post(new Runnable() {
            @Override
            public void run() {
                final MapSettings mapSettings = mWebViewFragment.getMapSettings();
                mapSettings.setCenter(new GeoPoint(latitude,
                                                   longitude));
                mWebViewFragment.setMapSettings(mapSettings);
            }
        });
    }

    /**
     * Gets the current zoom level
     *
     * @return the current zoom level
     */
    @JavascriptInterface
    public int getZoom() {
        return this.mWebViewFragment.getMapSettings()
                                    .getZoom();
    }

    /**
     * Sets the current zoom level
     *
     * @param zoom the zoom level to set
     */
    @JavascriptInterface
    public void setZoom(final int zoom) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mWebViewFragment.getMapSettings()
                                    .getZoom() != zoom) {
                    Log.d(MainControl.class.getName(),
                          "setZoom " + zoom);

                    final MapSettings mapSettings = mWebViewFragment.getMapSettings();
                    mapSettings.setZoom(zoom);
                    mWebViewFragment.setMapSettings(mapSettings);
                }
            }
        });
    }

    @JavascriptInterface
    public String getMetadata(String mbTilesSource) {
        final ITilesLayerDataSource dataSource = this.mWebViewFragment.getTilesLayersDataSource(mbTilesSource);

        if (dataSource == null) {
            return "{}";
        }

        return dataSource.getMetadata()
                         .toString();
    }

    /**
     * Gets the minimum zoom level available from all registered {@link ITilesLayerDataSource}.
     *
     * @return the minimum zoom level
     *
     * @see ITilesLayerDataSource#getMinZoom()
     */
    @JavascriptInterface
    public int getMinZoom() {
        int minZoom = Integer.MAX_VALUE;

        for (LayerSettings layerSettings : this.mWebViewFragment.getMapSettings()
                                                                .getLayers()) {
            ITilesLayerDataSource dataSource = this.mWebViewFragment.getTilesLayersDataSource(layerSettings.getName());

            if ((dataSource != null) && (dataSource.getMinZoom() < minZoom)) {
                minZoom = dataSource.getMinZoom();
            }
        }

        final MapSettings mapSettings = this.mWebViewFragment.getMapSettings();
        mapSettings.setMinZoom(minZoom);
        this.mWebViewFragment.setMapSettings(mapSettings);

        return minZoom;
    }

    /**
     * Gets the maximum zoom level available from all registered {@link ITilesLayerDataSource}.
     *
     * @return the maximum zoom level
     *
     * @see ITilesLayerDataSource#getMaxZoom()
     */
    @JavascriptInterface
    public int getMaxZoom() {
        int maxZoom = 0;

        for (LayerSettings layerSettings : this.mWebViewFragment.getMapSettings()
                                                                .getLayers()) {
            ITilesLayerDataSource dataSource = this.mWebViewFragment.getTilesLayersDataSource(layerSettings.getName());

            if ((dataSource != null) && (dataSource.getMaxZoom() > maxZoom)) {
                maxZoom = dataSource.getMaxZoom();
            }
        }

        final MapSettings mapSettings = this.mWebViewFragment.getMapSettings();
        mapSettings.setMaxZoom(maxZoom);
        this.mWebViewFragment.setMapSettings(mapSettings);

        return maxZoom;
    }

    /**
     * Gets all available zooms level from all registered {@link ITilesLayerDataSource}.
     *
     * @return array of available zooms as <code>JSONArray</code>
     *
     * @see ITilesLayerDataSource#getZooms()
     */
    @JavascriptInterface
    public String getZooms() {
        SortedSet<Integer> zooms = new TreeSet<>();

        for (String dataSourceName : this.mWebViewFragment.getTilesLayersDataSources()) {
            ITilesLayerDataSource dataSource = this.mWebViewFragment.getTilesLayersDataSource(dataSourceName);

            if (dataSource != null) {
                zooms.addAll(dataSource.getZooms());
            }
        }

        final JSONArray jsonArray = new JSONArray(zooms);

        return jsonArray.toString();
    }

    /**
     * Gets the current selected {@link LayerSettings}.
     *
     * @return {@link LayerSettings} as {@link JSONObject}
     */
    @JavascriptInterface
    public String getSelectedLayerName() {
        return this.mWebViewFragment.getSelectedLayer()
                                    .getName();
    }

    /**
     * Retrieves the tile as a <code>Base64</code> representation from the most appropriate {@link ITilesLayerDataSource} source according to given parameters.
     *
     * @param zoomLevel the current zoom level
     * @param column    column index of the tile
     * @param row       row index of the tile
     *
     * @return the tile as a <code>Base64</code> representation
     *
     * @see ITilesLayerDataSource#getTile(int, int, int)
     */
    @JavascriptInterface
    public String getTile(int zoomLevel,
                          int column,
                          int row) {
        if (this.mWebViewFragment.getTilesLayersDataSource(this.mWebViewFragment.getSelectedLayer()
                                                                                .getName())
                                 .getZooms()
                                 .contains(zoomLevel)) {
            // try to use the current layer source if possible
            return this.mWebViewFragment.getTilesLayersDataSource(this.mWebViewFragment.getSelectedLayer()
                                                                                       .getName())
                                        .getTile(zoomLevel,
                                                 column,
                                                 row);
        }
        else {
            // try to find the most appropriate layer source from MapSettings
            for (LayerSettings layerSettings : this.mWebViewFragment.getMapSettings()
                                                                    .getLayers()) {
                if (this.mWebViewFragment.getTilesLayersDataSource(layerSettings.getName())
                                         .getZooms()
                                         .contains(zoomLevel)) {
                    Log.d(MainControl.class.getName(),
                          "getTile : switch to layer " + layerSettings.getName());

                    // switch to this layer source
                    this.mWebViewFragment.setSelectedLayer(layerSettings);

                    return this.mWebViewFragment.getTilesLayersDataSource(layerSettings.getName())
                                                .getTile(zoomLevel,
                                                         column,
                                                         row);
                }
            }
        }

        Log.w(MainControl.class.getName(),
              "getTile : no layer source found for the given zoom level !");

        return "";
    }

    @JavascriptInterface
    public boolean displayScale() {
        return this.mWebViewFragment.getMapSettings()
                                    .isDisplayScale();
    }

    @JavascriptInterface
    public String getDensityDpi() {
        return this.mWebViewFragment.getMapSettings()
                                    .getRenderQuality()
                                    .getValueAsString();
    }

    @JavascriptInterface
    public String getLocalizedMessage(String messageId) {
        return this.mWebViewFragment.getLocalizedMessage(messageId);
    }
}
