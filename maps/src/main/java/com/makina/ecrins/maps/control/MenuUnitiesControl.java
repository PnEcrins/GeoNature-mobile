package com.makina.ecrins.maps.control;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.makina.ecrins.maps.BuildConfig;
import com.makina.ecrins.maps.IWebViewFragment;
import com.makina.ecrins.maps.R;
import com.makina.ecrins.maps.content.ITilesLayerDataSource;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;
import com.makina.ecrins.maps.jts.geojson.GeoPoint;
import com.makina.ecrins.maps.jts.geojson.io.GeoJsonWriter;
import com.makina.ecrins.maps.location.Geolocation;
import com.makina.ecrins.maps.settings.MapSettings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

/**
 * Menu items control to :
 * <ul>
 * <li>add or move a single marker on the map</li>
 * <li>add a single marker to the current location and highlight the corresponding unity if any</li>
 * <li>show or hide unities layer if any</li>
 * </ul>
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public final class MenuUnitiesControl
        extends AbstractControl {

    private static final String TAG = MenuUnitiesControl.class.getName();

    private static final String KEY_SHOW_UNITIES_LAYER = "display_unities_layer";
    private static final String KEY_SELECTED_UNITY = "selected_unity";
    private static final String KEY_POINTING_LOCATION = "pointing_location";

    private static JSONArray jsonArray = new JSONArray();
    private static JSONObject jsonObject = new JSONObject();

    private boolean mIsActionMarkerSelected = false;

    /**
     * Default constructor.
     */
    public MenuUnitiesControl(Context pContext) {
        super(pContext);

        setControlListener(new OnIControlListener() {
            @Override
            public void onControlInitialized() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "onControlInitialized");
                }

                mIsActionMarkerSelected = false;
                mWebViewFragment.invalidateMenu();
            }
        });
    }

    @Override
    public View getView(boolean forceCreate) {
        return null;
    }

    @Override
    public boolean hasOptionsMenu() {
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        if (isControlInitialized()) {
            inflater.inflate(R.menu.menu_unities_layer,
                             menu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (isControlInitialized()) {
            final MenuItem itemMarker = menu.findItem(R.id.itemMarker);
            final MenuItem itemMarkerFromLocation = menu.findItem(R.id.itemMarkerFromLocation);
            final MenuItem itemToggleUnitiesLayers = menu.findItem(R.id.itemToggleUnitiesLayers);

            if (this.mIsActionMarkerSelected) {
                itemMarker.setIcon(R.drawable.ic_action_marker_selected);
            }
            else {
                itemMarker.setIcon(R.drawable.ic_action_marker);
            }

            if (this.mWebViewFragment.getMapSettings()
                                     .getMinimumZoomPointing() > this.mWebViewFragment.getMapSettings()
                                                                                      .getZoom()) {
                itemMarker.setEnabled(false);
                itemMarkerFromLocation.setEnabled(false);
                this.mIsActionMarkerSelected = false;
                mWebViewFragment.loadUrl(getJSUrlPrefix() + ".removeOnClickEvent()");
            }
            else {
                itemMarker.setEnabled(true);
                itemMarkerFromLocation.setEnabled(true);
            }

            if (this.hasUnitiesLayerSource()) {
                itemToggleUnitiesLayers.setIcon(mWebViewFragment.getSavedInstanceState()
                                                                .getBoolean(KEY_SHOW_UNITIES_LAYER,
                                                                            true) ? R.drawable.ic_action_layer_remove : R.drawable.ic_action_layer_add);
            }
            else {
                menu.removeItem(R.id.itemToggleUnitiesLayers);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.itemMarker) {
            if (!this.mIsActionMarkerSelected) {
                this.mIsActionMarkerSelected = true;
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".addOnClickEvent()");
            }

            return true;
        }
        else if (item.getItemId() == R.id.itemMarkerFromLocation) {
            Location currentLocation = this.mWebViewFragment.getCurrentLocation();

            if (currentLocation != null) {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".addMarker(" + currentLocation.getLatitude() + ", " + currentLocation.getLongitude() + ", " + currentLocation.getAccuracy() + ")");
            }

            return true;
        }
        else if (item.getItemId() == R.id.itemToggleUnitiesLayers) {
            this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".showOrHideUnitiesLayers()");

            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void add(IWebViewFragment webViewFragment) {
        super.add(webViewFragment);

        this.mWebViewFragment.getSavedInstanceState()
                             .putBoolean(KEY_SHOW_UNITIES_LAYER,
                                         this.mWebViewFragment.getMapSettings()
                                                              .isShowUnitiesLayer());

        initializeJSController("js/Control.Unities.js",
                               "new L.Control.Unities({" +
                                       "hasUnityLayerSource: " + hasUnitiesLayerSource() + ", " +
                                       "minZoom: " + this.mWebViewFragment.getMapSettings()
                                                                          .getMinZoom() + ", " +
                                       "maxZoom: " + this.mWebViewFragment.getMapSettings()
                                                                          .getMaxZoom() +
                                       "})");
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
                mWebViewFragment.invalidateMenu();
            }
        });
    }

    /**
     * Gets the minimum zoom level for pointing on the map
     *
     * @return the minimum zoom level for pointing
     *
     * @see MapSettings#getMinimumZoomPointing()
     */
    @JavascriptInterface
    public int getMinimumZoomPointing() {
        return mWebViewFragment.getMapSettings()
                               .getMinimumZoomPointing();
    }

    /**
     * Gets the current marker position as {@link JSONArray}.
     *
     * @return the current marker position
     */
    @JavascriptInterface
    public String getMarkerPosition() {
        final GeoPoint pointing = mWebViewFragment.getSavedInstanceState()
                                                  .getParcelable(KEY_POINTING_LOCATION);

        if (pointing == null) {
            return "[]";
        }

        return pointing.toString();
    }

    /**
     * Updates the current position marker.
     *
     * @param latitude  the latitude in degrees
     * @param longitude the longitude in degrees
     * @param accuracy  the accuracy of this position
     */
    @JavascriptInterface
    public void setMarkerPosition(final double latitude,
                                  final double longitude,
                                  int accuracy) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                mIsActionMarkerSelected = false;
                mWebViewFragment.invalidateMenu();
                mWebViewFragment.getSavedInstanceState()
                                .putParcelable(KEY_POINTING_LOCATION,
                                               new GeoPoint(latitude,
                                                            longitude));
            }
        });
    }

    @JavascriptInterface
    public String getUnityLayerSource() {
        if (hasUnitiesLayerSource()) {
            return mWebViewFragment.getMapSettings()
                                   .getUnityLayer()
                                   .getName();
        }
        else {
            return "";
        }
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
     * Retrieves the tile as a <code>Base64</code> representation from a given {@link ITilesLayerDataSource} source according to given parameters.
     *
     * @param tilesSource the {@link ITilesLayerDataSource} source to use
     * @param zoomLevel   the current zoom level
     * @param column      column index of the tile
     * @param row         row index of the tile
     *
     * @return the tile as a <code>Base64</code> representation
     *
     * @see ITilesLayerDataSource#getTile(int, int, int)
     */
    @JavascriptInterface
    public String getTile(String tilesSource,
                          int zoomLevel,
                          int column,
                          int row) {
        return this.mWebViewFragment.getTilesLayersDataSource(tilesSource)
                                    .getTile(zoomLevel,
                                             column,
                                             row);
    }

    /**
     * Flag to indicate if we have to show the unities layer.
     *
     * @return <code>true</code> if the unities layer must be shown, <code>false</code> otherwise
     */
    @JavascriptInterface
    public boolean showUnitiesLayer() {
        return this.mWebViewFragment.getSavedInstanceState()
                                    .getBoolean(KEY_SHOW_UNITIES_LAYER);
    }

    /**
     * Shows or hides the unities layer.
     *
     * @param added flag to indicate if we have to show or hide the unities layer
     */
    @JavascriptInterface
    public void toggleUnitiesLayer(final boolean added) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                mWebViewFragment.getSavedInstanceState()
                                .putBoolean(KEY_SHOW_UNITIES_LAYER,
                                            added);
                mWebViewFragment.invalidateMenu();
            }
        });
    }

    /**
     * Returns the previously selected unity as {@code JSONObject}.
     *
     * @return the selected unity as {@code JSONObject} if found
     */
    @JavascriptInterface
    public String getSelectedUnity() {
        final Feature selectedFeature = mWebViewFragment.getSavedInstanceState()
                                                        .getParcelable(KEY_SELECTED_UNITY);

        if (selectedFeature == null) {
            return "{}";
        }

        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.addFeature(selectedFeature);
        final String toJson = new GeoJsonWriter().write(featureCollection);

        return TextUtils.isEmpty(toJson) ? "{}" : toJson;
    }

    /**
     * Returns the selected unity for a given location.
     *
     * @param latitude  the latitude in degrees
     * @param longitude the longitude in degrees
     *
     * @return the selected unity as {@link JSONObject} if found
     */
    @JavascriptInterface
    public String getUnityFromLocation(double latitude,
                                       double longitude,
                                       int accuracy) {
        final Geolocation geolocation = new Geolocation(longitude,
                                                        latitude,
                                                        accuracy);
        final GeoPoint geoPoint = new GeoPoint(latitude,
                                               longitude);

        final List<Feature> unities = this.mWebViewFragment.getFeatures();
        final Iterator<Feature> iterator = unities.iterator();
        Feature selectedFeature = mWebViewFragment.getSavedInstanceState()
                                                  .getParcelable(KEY_SELECTED_UNITY);

        // checks before if the previously selected unity is still valid
        if ((selectedFeature != null) && !selectedFeature.getGeometry()
                                                         .contains(geoPoint.getPoint())) {
            selectedFeature = null;
        }

        while ((selectedFeature == null) && iterator.hasNext()) {
            final Feature featureToCheck = iterator.next();

            if (featureToCheck.getGeometry()
                              .contains(geoPoint.getPoint())) {
                selectedFeature = featureToCheck;
            }
        }

        final Feature featureToUpdate = selectedFeature;

        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (featureToUpdate == null) {
                    mWebViewFragment.getSavedInstanceState()
                                    .remove(KEY_SELECTED_UNITY);
                }
                else {
                    mWebViewFragment.getSavedInstanceState()
                                    .putParcelable(KEY_SELECTED_UNITY,
                                                   featureToUpdate);
                }

                mWebViewFragment.setSelectedFeature(geolocation,
                                                    featureToUpdate);
            }
        });

        if (selectedFeature == null) {
            return "{}";
        }

        final FeatureCollection featureCollection = new FeatureCollection();
        featureCollection.addFeature(selectedFeature);
        final String toJson = new GeoJsonWriter().write(featureCollection);

        return TextUtils.isEmpty(toJson) ? "{}" : toJson;
    }

    private boolean hasUnitiesLayerSource() {
        return (this.mWebViewFragment.getMapSettings()
                                     .getUnityLayer() != null) &&
                (!this.mWebViewFragment.getMapSettings()
                                       .getUnityLayer()
                                       .getName()
                                       .isEmpty()) &&
                (!this.mWebViewFragment.getFeatures()
                                       .isEmpty());
    }
}
