package com.makina.ecrins.maps.control;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.widget.ImageButton;

import com.makina.ecrins.maps.BuildConfig;
import com.makina.ecrins.maps.IWebViewFragment;
import com.makina.ecrins.maps.R;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureStyle;
import com.makina.ecrins.maps.jts.geojson.GeoPoint;
import com.makina.ecrins.maps.jts.geojson.filter.NearestFeaturesFilter;
import com.makina.ecrins.maps.jts.geojson.io.GeoJsonWriter;
import com.makina.ecrins.maps.util.DebugUtils;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A control to add support for drawing and editing {@link Feature}s.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class DrawControl
        extends AbstractControl
        implements OnClickListener,
                   LocationListener {

    private static final String TAG = DrawControl.class.getName();

    private static final String KEY_ADD_OR_UPDATE_FEATURE = "add_or_update_feature";

    private ImageButton mImageButtonAddMarker;
    private ImageButton mImageButtonAddPath;
    private ImageButton mImageButtonAddPolygon;
    private ImageButton mImageButtonEdit;
    private ImageButton mImageButtonDelete;

    private boolean mAddingSingleFeature = false;

    private boolean mIsImageButtonAddMarkerEnabled = true;
    private boolean mIsImageButtonAddPathEnabled = true;
    private boolean mIsImageButtonAddPolygonEnabled = true;
    private boolean mIsLocationProviderEnabled = true;

    private boolean mIsImageButtonAddMarkerSelected = false;
    private boolean mIsImageButtonAddPathSelected = false;
    private boolean mIsImageButtonAddPolygonSelected = false;
    private boolean mIsImageButtonEditSelected = false;
    private boolean mIsImageButtonDeleteSelected = false;
    private boolean mIsActionMarkerFromLocationSelected = false;

    private int mZoom;

    private View mView = null;

    private OnDrawControlListener mOnDrawControlListener;

    private FeatureStyle mFeatureDefaultStyle;
    private FeatureStyle mFeatureAddStyle;
    private FeatureStyle mFeatureEditStyle;

    public DrawControl(Context pContext) {
        super(pContext);

        mFeatureDefaultStyle = FeatureStyle.Builder.newInstance(pContext)
                                                   .setOpacity(0.9)
                                                   .build();
        mFeatureAddStyle = FeatureStyle.Builder.newInstance(pContext)
                                               .build();
        mFeatureEditStyle = FeatureStyle.Builder.newInstance(pContext)
                                                .build();
        setControlListener(new OnIControlListener() {
            @Override
            public void onControlInitialized() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "onControlInitialized");
                }

                mWebViewFragment.requestLocationUpdates(DrawControl.this);

                mZoom = mWebViewFragment.getMapSettings()
                                        .getZoom();
                mIsImageButtonAddMarkerSelected = false;
                mIsImageButtonAddPathSelected = false;
                mIsImageButtonAddPolygonSelected = false;
                mIsImageButtonEditSelected = false;
                mIsImageButtonDeleteSelected = false;
                mOnDrawControlListener = null;

                if (mWebViewFragment.getCurrentEditableFeature() != null) {
                    if (mWebViewFragment.getEditableFeatures()
                                        .hasFeature(mWebViewFragment.getCurrentEditableFeature()
                                                                    .getId())) {
                        mIsImageButtonEditSelected = true;
                    }
                    else {
                        switch (mWebViewFragment.getCurrentEditableFeature()
                                                .getGeometry()
                                                .getGeometryType()) {
                            case "Point":
                                mIsImageButtonAddMarkerSelected = true;
                                break;
                            case "LineString":
                                mIsImageButtonAddPathSelected = true;
                                break;
                            case "Polygon":
                                mIsImageButtonAddPolygonSelected = true;
                                break;
                            default:
                                break;
                        }
                    }
                }

                if (mWebViewFragment.getMockLocationProvider() != null) {
                    mWebViewFragment.getMockLocationProvider()
                                    .enableProvider(true);
                    mIsLocationProviderEnabled = true;
                }

                updateButtons();
            }
        });
    }

    @Override
    public View getView(boolean forceCreate) {
        if ((this.mView == null) || forceCreate) {
            this.mView = LayoutInflater.from(getContext())
                                       .inflate(R.layout.control_draw_toolbar_layout,
                                                null);

            mImageButtonAddMarker = (ImageButton) this.mView.findViewById(R.id.imageButtonAddMarker);
            mImageButtonAddPath = (ImageButton) this.mView.findViewById(R.id.imageButtonAddPath);
            mImageButtonAddPolygon = (ImageButton) this.mView.findViewById(R.id.imageButtonAddPolygon);
            mImageButtonEdit = (ImageButton) this.mView.findViewById(R.id.imageButtonEdit);
            mImageButtonDelete = (ImageButton) this.mView.findViewById(R.id.imageButtonDelete);

            mImageButtonAddMarker.setOnClickListener(this);
            mImageButtonAddPath.setOnClickListener(this);
            mImageButtonAddPolygon.setOnClickListener(this);
            mImageButtonEdit.setOnClickListener(this);
            mImageButtonDelete.setOnClickListener(this);

            mImageButtonAddMarker.setEnabled(false);
            mImageButtonAddPath.setEnabled(false);
            mImageButtonAddPolygon.setEnabled(false);
            mImageButtonEdit.setEnabled(false);
            mImageButtonDelete.setEnabled(false);
        }

        return this.mView;
    }

    @Override
    public boolean hasOptionsMenu() {
        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        if (isControlInitialized()) {
            inflater.inflate(R.menu.menu_draw_control,
                             menu);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (isControlInitialized()) {
            final MenuItem itemMarkerFromLocation = menu.findItem(R.id.itemMarkerFromLocation);

            if (this.mIsActionMarkerFromLocationSelected) {
                itemMarkerFromLocation.setIcon(R.drawable.ic_action_marker_from_location_selected);
            }
            else {
                itemMarkerFromLocation.setIcon(R.drawable.ic_action_marker_from_location);
            }

            if (mIsLocationProviderEnabled) {
                if (this.mWebViewFragment.getMapSettings()
                                         .getMinimumZoomPointing() > this.mWebViewFragment.getMapSettings()
                                                                                          .getZoom()) {
                    itemMarkerFromLocation.setEnabled(false);
                    this.mIsActionMarkerFromLocationSelected = false;
                }
                else {
                    itemMarkerFromLocation.setEnabled(mWebViewFragment.getSavedInstanceState()
                                                                      .getBoolean(KEY_ADD_OR_UPDATE_FEATURE,
                                                                                  false));

                    if (!mWebViewFragment.getSavedInstanceState()
                                         .getBoolean(KEY_ADD_OR_UPDATE_FEATURE,
                                                     false)) {
                        this.mIsActionMarkerFromLocationSelected = false;
                        itemMarkerFromLocation.setIcon(R.drawable.ic_action_marker_from_location);
                    }
                }
            }
            else {
                itemMarkerFromLocation.setEnabled(false);
                this.mIsActionMarkerFromLocationSelected = false;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.itemMarkerFromLocation) {
            this.mIsActionMarkerFromLocationSelected = !this.mIsActionMarkerFromLocationSelected;
            mWebViewFragment.invalidateMenu();

            // for debugging purpose only
            if (this.mIsActionMarkerFromLocationSelected && DebugUtils.isDebuggable(mWebViewFragment.getContext())) {
                mWebViewFragment.getMockLocationProvider()
                                .pushRandomLocationFromCurrentLocation(50.0);
            }

            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void add(IWebViewFragment webViewFragment) {
        super.add(webViewFragment);

        initializeJSController("js/Control.NativeDraw.js",
                               "new L.Control.NativeDraw()");
    }

    @Override
    public void remove(IWebViewFragment webViewFragment) {
        super.remove(webViewFragment);

        mOnDrawControlListener = null;
    }

    @Override
    public void onClick(View v) {
        v.setSelected(!v.isSelected());
        updateButtonsSelection((ImageButton) v);

        if (v.getId() == R.id.imageButtonAddMarker) {
            if (v.isSelected()) {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".startDrawFeature(\"" + Point.class.getSimpleName() + "\")");
            }
            else {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endDrawFeature()");
            }
        }
        else if (v.getId() == R.id.imageButtonAddPath) {
            if (v.isSelected()) {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".startDrawFeature(\"" + LineString.class.getSimpleName() + "\")");
            }
            else {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endDrawFeature()");
            }
        }
        else if (v.getId() == R.id.imageButtonAddPolygon) {
            if (v.isSelected()) {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".startDrawFeature(\"" + Polygon.class.getSimpleName() + "\")");
            }
            else {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endDrawFeature()");
            }
        }
        else if (v.getId() == R.id.imageButtonEdit) {
            if (v.isSelected()) {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".startFindFeature(\"edit\")");
            }
            else {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endUpdateFeature()");
            }
        }
        else if (v.getId() == R.id.imageButtonDelete) {
            if (v.isSelected()) {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".startFindFeature(\"delete\")");
            }
            else {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endUpdateFeature()");
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (isControlInitialized()) {
            boolean checkIfLocationInsideMapBounds = (mWebViewFragment.getMapSettings()
                                                                      .getPolygonBounds() != null) && mWebViewFragment.getMapSettings()
                                                                                                                      .getPolygonBounds()
                                                                                                                      .contains(new GeoPoint(location.getLatitude(),
                                                                                                                                             location.getLongitude()).getPoint());

            // checks if this location is inside the map or not
            if (mIsActionMarkerFromLocationSelected && checkIfLocationInsideMapBounds) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "onLocationChanged [provider: " + location.getProvider() + ", lat: " + location.getLatitude() + ", lon: " + location.getLongitude() + ", acc: " + location.getAccuracy() + ", bearing: " + location.getBearing());
                }

                mIsActionMarkerFromLocationSelected = false;
                mWebViewFragment.invalidateMenu();

                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".addLatLngToFeature(" + location.getLatitude() + ", " + location.getLongitude() + ")");
            }

            if (mIsActionMarkerFromLocationSelected && !checkIfLocationInsideMapBounds) {
                mIsActionMarkerFromLocationSelected = false;
                mWebViewFragment.invalidateMenu();
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        this.mIsLocationProviderEnabled = false;

        if (mWebViewFragment != null) {
            mWebViewFragment.invalidateMenu();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        this.mIsLocationProviderEnabled = true;

        if (mWebViewFragment != null) {
            mWebViewFragment.invalidateMenu();
        }
    }

    @Override
    public void onStatusChanged(String provider,
                                int status,
                                Bundle extras) {
        // nothing to do ...
    }

    public void setAddingSingleFeature(boolean pAddingSingleFeature) {
        this.mAddingSingleFeature = pAddingSingleFeature;
    }

    public void enableAddMarker(boolean pEnabled) {
        this.mIsImageButtonAddMarkerEnabled = pEnabled;

        updateButtons();
    }

    public void enableAddPath(boolean pEnabled) {
        this.mIsImageButtonAddPathEnabled = pEnabled;

        updateButtons();
    }

    public void enableAddPolygon(boolean pEnabled) {
        this.mIsImageButtonAddPolygonEnabled = pEnabled;

        updateButtons();
    }

    public void setOnDrawControlListener(OnDrawControlListener pOnDrawControlListener) {
        this.mOnDrawControlListener = pOnDrawControlListener;
    }

    @NonNull
    public FeatureStyle getFeatureDefaultStyle() {
        return mFeatureDefaultStyle;
    }

    public void setFeatureDefaultStyle(@NonNull final FeatureStyle pFeatureDefaultStyle) {
        this.mFeatureDefaultStyle = pFeatureDefaultStyle;
    }

    @NonNull
    public FeatureStyle getFeatureAddStyle() {
        return mFeatureAddStyle;
    }

    public void setFeatureAddStyle(@NonNull final FeatureStyle pFeatureAddStyle) {
        this.mFeatureAddStyle = pFeatureAddStyle;
    }

    @NonNull
    public FeatureStyle getFeatureEditStyle() {
        return mFeatureEditStyle;
    }

    public void setFeatureEditStyle(@NonNull final FeatureStyle pFeatureEditStyle) {
        this.mFeatureEditStyle = pFeatureEditStyle;
    }

    /**
     * Adds a {@code List} of {@link Feature}s to edit.
     *
     * @param features a {@code List} of {@link Feature}s
     */
    public void setFeatures(@NonNull final List<Feature> features) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (isControlInitialized()) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "setFeatures, size: " + features.size());
                    }

                    mWebViewFragment.getEditableFeatures()
                                    .clearAllFeatures();
                    mWebViewFragment.getEditableFeatures()
                                    .addAllFeatures(features);
                    mWebViewFragment.loadUrl(getJSUrlPrefix() + ".setFeatures()");
                    updateButtons();
                }
                else {
                    Log.w(TAG,
                          "setFeatures: Control '" + getName() + "' is not initialized !");
                }
            }
        });
    }

    /**
     * Clears all {@link Feature}s.
     */
    public void clearFeatures() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (isControlInitialized()) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "clearFeatures");
                    }

                    mWebViewFragment.getEditableFeatures()
                                    .clearAllFeatures();
                    mWebViewFragment.loadUrl(getJSUrlPrefix() + ".clearFeatures()");
                    updateButtons();
                }
                else {
                    Log.w(TAG,
                          "clearFeatures: Control '" + getName() + "' is not initialized !");
                }
            }
        });
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
                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "setZoom " + zoom);
                }

                mZoom = zoom;
                mWebViewFragment.invalidateMenu();
                updateButtons();
            }
        });
    }

    @JavascriptInterface
    public int getMinimumZoomPointing() {
        return mWebViewFragment.getMapSettings()
                               .getMinimumZoomPointing();
    }

    @JavascriptInterface
    public String loadFeatures() {
        final String featuresAsJson = new GeoJsonWriter().write(mWebViewFragment.getEditableFeatures());

        if (TextUtils.isEmpty(featuresAsJson)) {
            return "{}";
        }

        return featuresAsJson;
    }

    @JavascriptInterface
    public String loadSelectedFeature() {
        final String featureAsJson = new GeoJsonWriter().write(mWebViewFragment.getCurrentEditableFeature());

        if (TextUtils.isEmpty(featureAsJson)) {
            return "{}";
        }

        return featureAsJson;
    }

    @JavascriptInterface
    public String getFeatureDefaultStyleAsString() {
        return mFeatureDefaultStyle.toString();
    }

    @JavascriptInterface
    public String getFeatureAddStyleAsString() {
        return mFeatureAddStyle.toString();
    }

    @JavascriptInterface
    public String getFeatureEditStyleAsString() {
        return mFeatureEditStyle.toString();
    }

    @JavascriptInterface
    public void findFeature(final String mode,
                            final double latitude,
                            final double longitude) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                final GeoPoint location = new GeoPoint(latitude,
                                                       longitude);

                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "findFeature on location: " + location.toString());
                }

                Feature featureFound = null;
                final Iterator<Feature> iterator = mWebViewFragment.getEditableFeatures()
                                                                   .getFeatures()
                                                                   .iterator();

                while ((featureFound == null) && iterator.hasNext()) {
                    Feature featureToCheck = iterator.next();

                    if (featureToCheck.getGeometry()
                                      .contains(location.getPoint())) {
                        featureFound = featureToCheck;
                    }
                }

                if (featureFound == null) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "findFeature: no feature found at location " + location.toString() + ", try to find the closest feature ...");
                    }

                    final List<Feature> filteredFeatures = NearestFeaturesFilter.getFilteredFeatures(location,
                                                                                                     100d,
                                                                                                     mWebViewFragment.getEditableFeatures());
                    // use the closest Feature found
                    if (!filteredFeatures.isEmpty()) {
                        featureFound = filteredFeatures.get(0);
                    }
                }

                if (featureFound == null) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "no feature found");
                    }
                }
                else {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "nearest feature found '" + featureFound.getId() + "'");
                    }

                    if (mode.equals("edit")) {
                        mWebViewFragment.loadUrl(getJSUrlPrefix() +
                                                         ".startUpdateFeature(\"edit\", \"" +
                                                         featureFound.getId() +
                                                         "\")");
                    }
                    else {
                        // refreshes all features
                        if (mWebViewFragment.deleteEditableFeature(featureFound.getId())) {
                            mWebViewFragment.setCurrentEditableFeature(null);
                            mWebViewFragment.loadUrl(getJSUrlPrefix() + ".loadFeatures()");
                        }

                        updateButtons();
                    }
                }
            }
        });
    }

    @JavascriptInterface
    public void addOrUpdateFeature(final String featureAsString) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "addOrUpdateFeature: " + featureAsString);
        }

        getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    // warning: this is not a real GeoJSON Feature...
                    final JSONObject featureAsJson = new JSONObject(featureAsString);
                    final String id = featureAsJson.getString("key");

                    if (TextUtils.isEmpty(id)) {
                        Log.w(TAG,
                              "addOrUpdateFeature, No ID defined for Feature: " + featureAsString);

                        return;
                    }

                    final String type = featureAsJson.getString("type");

                    if (TextUtils.isEmpty(type)) {
                        Log.w(TAG,
                              "addOrUpdateFeature, No type defined for Feature: " + featureAsString);

                        return;
                    }

                    Geometry geometry = null;

                    switch (type) {
                        case "Point":
                            JSONObject pointCoordinates = featureAsJson.getJSONObject("coordinates");

                            if (pointCoordinates.has("lat") && pointCoordinates.has("lng")) {
                                final GeoPoint geoPoint = new GeoPoint(pointCoordinates.getDouble("lat"),
                                                                       pointCoordinates.getDouble("lng"));
                                geometry = geoPoint.getPoint();
                            }

                            break;
                        case "LineString":
                        case "Polygon":
                            final JSONArray arrayCoordinates = featureAsJson.getJSONArray("coordinates");
                            final List<Coordinate> coordinates = new ArrayList<>();

                            for (int i = 0; i < arrayCoordinates.length(); i++) {
                                final GeoPoint geoPoint = new GeoPoint(arrayCoordinates.getJSONObject(i)
                                                                                       .getDouble("lat"),
                                                                       arrayCoordinates.getJSONObject(i)
                                                                                       .getDouble("lng"));
                                coordinates.add(new Coordinate(geoPoint.getPoint()
                                                                       .getCoordinate()));
                            }

                            if (type.equals("LineString") && (coordinates.size() > 1)) {
                                geometry = new GeometryFactory().createLineString(coordinates.toArray(new Coordinate[coordinates.size()]));
                            }

                            if (type.equals("Polygon") && (coordinates.size() > 2)) {
                                // add a last coordinate as the same first coordinate to create a valid closed LineString
                                if (!coordinates.get(0)
                                                .equals(coordinates.get(coordinates.size() - 1))) {
                                    coordinates.add(coordinates.get(0));
                                }

                                geometry = new GeometryFactory().createPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
                            }

                            break;
                    }

                    if (geometry == null) {
                        Log.w(TAG,
                              "addOrUpdateFeature, No geometry found or valid for Feature: " + featureAsString);

                        return;
                    }

                    mWebViewFragment.setCurrentEditableFeature(new Feature(id,
                                                                           geometry));
                }
                catch (JSONException je) {
                    Log.w(TAG,
                          je);
                }
            }
        });
    }

    @JavascriptInterface
    public void deleteFeature(final String featureId) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "deleteFeature: " + featureId);
                }

                // refreshes all features
                if (mWebViewFragment.deleteEditableFeature(featureId)) {
                    mWebViewFragment.loadUrl(getJSUrlPrefix() + ".loadFeatures(false)");
                }

                updateButtons();
            }
        });
    }

    @JavascriptInterface
    public void onAddingFeature(final boolean adding) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (!adding) {
                    addOrUpdateSelectedFeature();
                }

                mWebViewFragment.getSavedInstanceState()
                                .putBoolean(KEY_ADD_OR_UPDATE_FEATURE,
                                            adding);
                mWebViewFragment.invalidateMenu();

                if (mOnDrawControlListener != null) {
                    mOnDrawControlListener.onAddingFeature(adding);
                }
            }
        });
    }

    @JavascriptInterface
    public void onEditingFeature(final boolean editing) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (!editing) {
                    addOrUpdateSelectedFeature();
                }

                mWebViewFragment.getSavedInstanceState()
                                .putBoolean(KEY_ADD_OR_UPDATE_FEATURE,
                                            editing);
                mWebViewFragment.invalidateMenu();

                if (mOnDrawControlListener != null) {
                    mOnDrawControlListener.onEditingFeature(editing);
                }
            }
        });
    }

    @JavascriptInterface
    public void onDeletingFeature(final boolean deleting) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                mWebViewFragment.setCurrentEditableFeature(null);
                mWebViewFragment.getSavedInstanceState()
                                .putBoolean(KEY_ADD_OR_UPDATE_FEATURE,
                                            false);
                mWebViewFragment.invalidateMenu();

                if (mOnDrawControlListener != null) {
                    mOnDrawControlListener.onDeletingFeature(deleting);
                }
            }
        });
    }

    private void updateButtons() {
        if (isControlInitialized()) {
            boolean checkZoom = mWebViewFragment.getMapSettings()
                                                .getMinimumZoomPointing() <= mZoom;
            boolean checkAddingFeature = (mAddingSingleFeature && mWebViewFragment.getEditableFeatures()
                                                                                  .isEmpty()) ^ (!mAddingSingleFeature);

            if (mIsImageButtonAddMarkerEnabled) {
                mImageButtonAddMarker.setVisibility(View.VISIBLE);
                mImageButtonAddMarker.setEnabled(checkZoom && checkAddingFeature);
            }
            else {
                mImageButtonAddMarker.setVisibility(View.GONE);
                mIsImageButtonAddMarkerSelected = false;
                mImageButtonAddMarker.setEnabled(false);
            }

            if (mIsImageButtonAddPathEnabled) {
                mImageButtonAddPath.setVisibility(View.VISIBLE);
                mImageButtonAddPath.setEnabled(checkZoom && checkAddingFeature);
            }
            else {
                mImageButtonAddPath.setVisibility(View.GONE);
                mIsImageButtonAddPathSelected = false;
                mImageButtonAddPath.setEnabled(false);
            }

            if (mIsImageButtonAddPolygonEnabled) {
                mImageButtonAddPolygon.setVisibility(View.VISIBLE);
                mImageButtonAddPolygon.setEnabled(checkZoom && checkAddingFeature);
            }
            else {
                mImageButtonAddPolygon.setVisibility(View.GONE);
                mIsImageButtonAddPolygonSelected = false;
                mImageButtonAddPolygon.setEnabled(false);
            }

            mImageButtonEdit.setEnabled(!mWebViewFragment.getEditableFeatures()
                                                         .getFeatures()
                                                         .isEmpty() && checkZoom);
            mImageButtonDelete.setEnabled(!mWebViewFragment.getEditableFeatures()
                                                           .getFeatures()
                                                           .isEmpty() && checkZoom);

            if (!checkZoom) {
                mIsImageButtonAddMarkerSelected = false;
                mIsImageButtonAddPathSelected = false;
                mIsImageButtonAddPolygonSelected = false;
                mIsImageButtonEditSelected = false;
                mIsImageButtonDeleteSelected = false;
            }

            if (checkZoom && (!checkAddingFeature)) {
                mIsImageButtonAddMarkerSelected = false;
                mIsImageButtonAddPathSelected = false;
                mIsImageButtonAddPolygonSelected = false;
            }

            if (checkZoom && mWebViewFragment.getEditableFeatures()
                                             .getFeatures()
                                             .isEmpty()) {
                mIsImageButtonEditSelected = false;
                mIsImageButtonDeleteSelected = false;
            }

            mImageButtonAddMarker.setSelected(mIsImageButtonAddMarkerSelected);
            mImageButtonAddPath.setSelected(mIsImageButtonAddPathSelected);
            mImageButtonAddPolygon.setSelected(mIsImageButtonAddPolygonSelected);
            mImageButtonEdit.setSelected(mIsImageButtonEditSelected);
            mImageButtonDelete.setSelected(mIsImageButtonDeleteSelected);
        }
    }

    private void addOrUpdateSelectedFeature() {
        final Feature feature = mWebViewFragment.getCurrentEditableFeature();

        if (feature == null) {
            Log.w(TAG,
                  "addOrUpdateSelectedFeature: nothing to add or update");

            updateButtons();
        }
        else {
            // specific case for feature type Point
            if (!mWebViewFragment.getEditableFeatures()
                                 .hasFeature(feature.getId()) && feature.getGeometry()
                                                                        .getGeometryType()
                                                                        .equals("Point")) {
                getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mImageButtonAddMarker.setSelected(false);
                        mIsImageButtonAddMarkerSelected = false;
                    }
                });
            }

            // refreshes all features
            mWebViewFragment.addOrUpdateEditableFeature(feature);
            mWebViewFragment.loadUrl(getJSUrlPrefix() + ".loadFeatures(false)");
            mWebViewFragment.setCurrentEditableFeature(null);

            updateButtons();
        }
    }

    private void updateButtonsSelection(ImageButton imageButton) {
        if (isControlInitialized()) {
            if (imageButton.getId() == R.id.imageButtonAddMarker) {
                mIsImageButtonAddMarkerSelected = imageButton.isSelected();
            }
            else if (imageButton.getId() == R.id.imageButtonAddPath) {
                mIsImageButtonAddPathSelected = imageButton.isSelected();
            }
            else if (imageButton.getId() == R.id.imageButtonAddPolygon) {
                mIsImageButtonAddPolygonSelected = imageButton.isSelected();
            }
            else if (imageButton.getId() == R.id.imageButtonEdit) {
                mIsImageButtonEditSelected = imageButton.isSelected();
            }
            else if (imageButton.getId() == R.id.imageButtonDelete) {
                mIsImageButtonDeleteSelected = imageButton.isSelected();
            }

            if (imageButton.isSelected()) {
                boolean checkAddingFeature = (mAddingSingleFeature && mWebViewFragment.getEditableFeatures()
                                                                                      .isEmpty()) ^ (!mAddingSingleFeature);

                if (imageButton.getId() == R.id.imageButtonAddMarker) {
                    if (checkAddingFeature) {
                        mIsImageButtonAddPathSelected = false;
                        mIsImageButtonAddPolygonSelected = false;
                        mImageButtonAddPath.setSelected(false);
                        mImageButtonAddPath.setEnabled(false);
                        mImageButtonAddPolygon.setSelected(false);
                        mImageButtonAddPolygon.setEnabled(false);
                    }
                    else {
                        if (mIsImageButtonAddPathSelected || mIsImageButtonAddPolygonSelected) {
                            mIsImageButtonAddPathSelected = false;
                            mIsImageButtonAddPolygonSelected = false;
                            mImageButtonAddPath.setSelected(false);
                            mImageButtonAddPolygon.setSelected(false);

                            this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endDrawFeature()");
                        }
                    }

                    if (mIsImageButtonEditSelected || mIsImageButtonDeleteSelected) {
                        mIsImageButtonEditSelected = false;
                        mIsImageButtonDeleteSelected = false;
                        mImageButtonEdit.setSelected(false);
                        mImageButtonDelete.setSelected(false);

                        this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endUpdateFeature()");
                    }
                }
                else if (imageButton.getId() == R.id.imageButtonAddPath) {
                    if (checkAddingFeature) {
                        mIsImageButtonAddMarkerSelected = false;
                        mIsImageButtonAddPolygonSelected = false;
                        mImageButtonAddMarker.setSelected(false);
                        mImageButtonAddMarker.setEnabled(false);
                        mImageButtonAddPolygon.setSelected(false);
                        mImageButtonAddPolygon.setEnabled(false);
                    }
                    else {
                        if (mIsImageButtonAddMarkerSelected || mIsImageButtonAddPolygonSelected) {
                            mIsImageButtonAddMarkerSelected = false;
                            mIsImageButtonAddPolygonSelected = false;
                            mImageButtonAddMarker.setSelected(false);
                            mImageButtonAddPolygon.setSelected(false);

                            this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endDrawFeature()");
                        }
                    }

                    if (mIsImageButtonEditSelected || mIsImageButtonDeleteSelected) {
                        mIsImageButtonEditSelected = false;
                        mIsImageButtonDeleteSelected = false;
                        mImageButtonEdit.setSelected(false);
                        mImageButtonDelete.setSelected(false);

                        this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endUpdateFeature()");
                    }
                }
                else if (imageButton.getId() == R.id.imageButtonAddPolygon) {
                    if (checkAddingFeature) {
                        mIsImageButtonAddMarkerSelected = false;
                        mIsImageButtonAddPathSelected = false;
                        mImageButtonAddMarker.setSelected(false);
                        mImageButtonAddMarker.setEnabled(false);
                        mImageButtonAddPath.setSelected(false);
                        mImageButtonAddPath.setEnabled(false);
                    }
                    else {
                        if (mIsImageButtonAddMarkerSelected || mIsImageButtonAddPathSelected) {
                            mIsImageButtonAddMarkerSelected = false;
                            mIsImageButtonAddPathSelected = false;
                            mImageButtonAddMarker.setSelected(false);
                            mImageButtonAddPath.setSelected(false);

                            this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endDrawFeature()");
                        }
                    }

                    if (mIsImageButtonEditSelected || mIsImageButtonDeleteSelected) {
                        mIsImageButtonEditSelected = false;
                        mIsImageButtonDeleteSelected = false;
                        mImageButtonEdit.setSelected(false);
                        mImageButtonDelete.setSelected(false);

                        this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endUpdateFeature()");
                    }
                }
                else if (imageButton.getId() == R.id.imageButtonEdit) {
                    if (mIsImageButtonAddMarkerSelected || mIsImageButtonAddPathSelected || mIsImageButtonAddPolygonSelected) {
                        mIsImageButtonAddMarkerSelected = false;
                        mIsImageButtonAddPathSelected = false;
                        mIsImageButtonAddPolygonSelected = false;
                        mImageButtonAddMarker.setSelected(false);
                        mImageButtonAddPath.setSelected(false);
                        mImageButtonAddPolygon.setSelected(false);

                        this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endDrawFeature()");
                    }

                    if (mIsImageButtonDeleteSelected) {
                        mIsImageButtonDeleteSelected = false;
                        mImageButtonDelete.setSelected(false);

                        this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endUpdateFeature()");
                    }
                }
                else if (imageButton.getId() == R.id.imageButtonDelete) {
                    if (mIsImageButtonAddMarkerSelected || mIsImageButtonAddPathSelected || mIsImageButtonAddPolygonSelected) {
                        mIsImageButtonAddMarkerSelected = false;
                        mIsImageButtonAddPathSelected = false;
                        mIsImageButtonAddPolygonSelected = false;
                        mImageButtonAddMarker.setSelected(false);
                        mImageButtonAddPath.setSelected(false);
                        mImageButtonAddPolygon.setSelected(false);

                        this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endDrawFeature()");
                    }

                    if (mIsImageButtonEditSelected) {
                        mIsImageButtonEditSelected = false;
                        mImageButtonEdit.setSelected(false);

                        this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endUpdateFeature()");
                    }
                }
            }
            else {
                updateButtons();
            }
        }
    }

    public interface OnDrawControlListener {

        void onAddingFeature(boolean adding);

        void onEditingFeature(boolean editing);

        void onDeletingFeature(boolean deleting);
    }
}
