package com.makina.ecrins.maps.control;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.widget.ImageButton;

import com.makina.ecrins.maps.R;
import com.makina.ecrins.maps.IWebViewFragment;
import com.makina.ecrins.maps.geojson.Feature;
import com.makina.ecrins.maps.geojson.FeatureStyle;
import com.makina.ecrins.maps.geojson.GeoJSONType;
import com.makina.ecrins.maps.geojson.geometry.GeoPoint;
import com.makina.ecrins.maps.geojson.geometry.GeometryUtils;
import com.makina.ecrins.maps.geojson.geometry.LineString;
import com.makina.ecrins.maps.geojson.geometry.Point;
import com.makina.ecrins.maps.geojson.geometry.Polygon;
import com.makina.ecrins.maps.geojson.operation.DistanceFilter;
import com.makina.ecrins.maps.util.DebugUtils;

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
public class DrawControl extends AbstractControl implements OnClickListener, LocationListener {

    protected static final String KEY_ADD_OR_UPDATE_FEATURE = "add_or_update_feature";

    protected ImageButton mImageButtonAddMarker;
    protected ImageButton mImageButtonAddPath;
    protected ImageButton mImageButtonAddPolygon;
    protected ImageButton mImageButtonEdit;
    protected ImageButton mImageButtonDelete;

    protected boolean mAddingSingleFeature = false;

    protected boolean mIsImageButtonAddMarkerEnabled = true;
    protected boolean mIsImageButtonAddPathEnabled = true;
    protected boolean mIsImageButtonAddPolygonEnabled = true;
    protected boolean mIsLocationProviderEnabled = true;

    protected boolean mIsImageButtonAddMarkerSelected = false;
    protected boolean mIsImageButtonAddPathSelected = false;
    protected boolean mIsImageButtonAddPolygonSelected = false;
    protected boolean mIsImageButtonEditSelected = false;
    protected boolean mIsImageButtonDeleteSelected = false;
    protected boolean mIsActionMarkerFromLocationSelected = false;

    protected int mZoom;

    private LayoutInflater mInflater;
    private View mView = null;

    private OnDrawControlListener mOnDrawControlListener;

    private FeatureStyle mFeatureDefaultStyle = new FeatureStyle().setOpacity(0.9);
    private FeatureStyle mFeatureAddStyle = new FeatureStyle();
    private FeatureStyle mFeatureEditStyle = new FeatureStyle();

    public DrawControl(Context pContext) {
        super(pContext);

        mInflater = (LayoutInflater) pContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setControlListener(new OnIControlListener() {
            @Override
            public void onControlInitialized() {
                Log.d(getClass().getName(), "onControlInitialized");

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
                        if (mWebViewFragment.getCurrentEditableFeature()
                                .getGeometry() != null) {
                            switch (mWebViewFragment.getCurrentEditableFeature()
                                    .getGeometry()
                                    .getType()) {
                                case POINT:
                                    mIsImageButtonAddMarkerSelected = true;
                                    break;
                                case LINE_STRING:
                                    mIsImageButtonAddPathSelected = true;
                                    break;
                                case POLYGON:
                                    mIsImageButtonAddPolygonSelected = true;
                                    break;
                                default:
                                    break;
                            }
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
            this.mView = mInflater.inflate(R.layout.control_draw_toolbar_layout, null);

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isControlInitialized()) {
            inflater.inflate(R.menu.menu_draw_control, menu);
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
                            .getBoolean(KEY_ADD_OR_UPDATE_FEATURE, false));

                    if (!mWebViewFragment.getSavedInstanceState()
                            .getBoolean(KEY_ADD_OR_UPDATE_FEATURE, false)) {
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
            if (this.mIsActionMarkerFromLocationSelected && DebugUtils.isDebuggable(mWebViewFragment.getContext()) && DebugUtils.hasAccessMockLocationPermission(mWebViewFragment.getContext())) {
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

        initializeJSController("js/Control.NativeDraw.js", "new L.Control.NativeDraw()");
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
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".startDrawFeature(\"" + GeoJSONType.POINT.getValue() + "\")");
            }
            else {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endDrawFeature()");
            }
        }
        else if (v.getId() == R.id.imageButtonAddPath) {
            if (v.isSelected()) {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".startDrawFeature(\"" + GeoJSONType.LINE_STRING.getValue() + "\")");
            }
            else {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".endDrawFeature()");
            }
        }
        else if (v.getId() == R.id.imageButtonAddPolygon) {
            if (v.isSelected()) {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".startDrawFeature(\"" + GeoJSONType.POLYGON.getValue() + "\")");
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
            boolean checkIfLocationInsideMapBounds = GeometryUtils.contains(new Point(new GeoPoint(location.getLatitude(), location.getLongitude())), this.mWebViewFragment.getMapSettings()
                    .getPolygonBounds());

            // checks if this location is inside the map or not
            if (mIsActionMarkerFromLocationSelected && checkIfLocationInsideMapBounds) {
                Log.d(DrawControl.class.getName(), "onLocationChanged [provider: " + location.getProvider() + ", lat: " + location.getLatitude() + ", lon: " + location.getLongitude() + ", acc: " + location.getAccuracy() + ", bearing: " + location.getBearing());

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
    public void onStatusChanged(String provider, int status, Bundle extras) {
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

    public FeatureStyle getFeatureDefaultStyle() {
        return mFeatureDefaultStyle;
    }

    public void setFeatureDefaultStyle(FeatureStyle pFeatureDefaultStyle) {
        this.mFeatureDefaultStyle = pFeatureDefaultStyle;
    }

    public FeatureStyle getFeatureAddStyle() {
        return mFeatureAddStyle;
    }

    public void setFeatureAddStyle(FeatureStyle pFeatureAddStyle) {
        this.mFeatureAddStyle = pFeatureAddStyle;
    }

    public FeatureStyle getFeatureEditStyle() {
        return mFeatureEditStyle;
    }

    public void setFeatureEditStyle(FeatureStyle pFeatureEditStyle) {
        this.mFeatureEditStyle = pFeatureEditStyle;
    }

    /**
     * Sets the current zoom level
     *
     * @param zoom the zoom level to set
     */
    @JavascriptInterface
    public void setZoom(final int zoom) {
        Log.d(DrawControl.class.getName(), "setZoom " + zoom);

        getHandler().post(new Runnable() {
            @Override
            public void run() {
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
        try {
            return mWebViewFragment.getEditableFeatures()
                    .getJSONObject()
                    .toString();
        }
        catch (JSONException je) {
            Log.w(DrawControl.class.getName(), je.getMessage(), je);

            return "{}";
        }
    }

    @JavascriptInterface
    public String loadSelectedFeature() {
        try {
            if (mWebViewFragment.getCurrentEditableFeature() == null) {
                return "{}";
            }
            else {
                return mWebViewFragment.getCurrentEditableFeature()
                        .getJSONObject()
                        .toString();
            }

        }
        catch (JSONException je) {
            Log.w(DrawControl.class.getName(), je.getMessage(), je);

            return "{}";
        }
    }

    @JavascriptInterface
    public String getFeatureDefaultStyleAsString() {
        try {
            return mFeatureDefaultStyle.getJSONObject(getContext())
                    .toString();
        }
        catch (JSONException je) {
            Log.w(DrawControl.class.getName(), je.getMessage(), je);

            return "{}";
        }
    }

    @JavascriptInterface
    public String getFeatureAddStyleAsString() {
        try {
            return mFeatureAddStyle.getJSONObject(getContext())
                    .toString();
        }
        catch (JSONException je) {
            Log.w(DrawControl.class.getName(), je.getMessage(), je);

            return "{}";
        }
    }

    @JavascriptInterface
    public String getFeatureEditStyleAsString() {
        try {
            return mFeatureEditStyle.getJSONObject(getContext())
                    .toString();
        }
        catch (JSONException je) {
            Log.w(DrawControl.class.getName(), je.getMessage(), je);

            return "{}";
        }
    }

    @JavascriptInterface
    public void findFeature(final String mode, final double latitude, final double longitude) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                final GeoPoint location = new GeoPoint(latitude, longitude);
                final Point locationAsPoint = new Point(location);

                Log.d(DrawControl.class.getName(), "findFeature on location " + location.toString());

                Feature featureFound = null;
                final Iterator<Feature> iterator = mWebViewFragment.getEditableFeatures()
                        .getFeatures()
                        .iterator();

                while ((featureFound == null) && iterator.hasNext()) {
                    Feature featureToCheck = iterator.next();

                    if (GeometryUtils.contains(locationAsPoint, featureToCheck.getGeometry())) {
                        featureFound = featureToCheck;
                    }
                }

                if (featureFound == null) {
                    Log.d(DrawControl.class.getName(), "findFeature : no feature found at location " + location.toString() + ", try to find the closest feature ...");

                    final DistanceFilter distanceFilter = new DistanceFilter(location, 100);
                    mWebViewFragment.getEditableFeatures()
                            .apply(distanceFilter);

                    if (!distanceFilter.getFiltereredFeatures()
                            .isEmpty()) {
                        featureFound = distanceFilter.getFiltereredFeatures()
                                .get(0);
                    }
                }

                if (featureFound == null) {
                    Log.d(DrawControl.class.getName(), "no feature found");
                }
                else {
                    Log.d(DrawControl.class.getName(), "nearest feature found '" + featureFound.getId() + "'");

                    if (mode.equals("edit")) {
                        mWebViewFragment.loadUrl(getJSUrlPrefix() +
                                ".startUpdateFeature(\"edit\", \"" +
                                featureFound.getId() +
                                "\")");
                    }
                    else {
                        // refreshes all features
                        if (mWebViewFragment.deleteEditableFeature(featureFound.getId())) {
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
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    final JSONObject featureAsJson = new JSONObject(featureAsString);
                    final Feature feature = new Feature(featureAsJson.getString("key"));

                    if (featureAsJson.getString("type")
                            .equals(GeoJSONType.POINT.getValue())) {
                        JSONObject coordinates = featureAsJson.getJSONObject("coordinates");

                        if (coordinates.has("lat") && coordinates.has("lng")) {
                            feature.setGeometry(new Point(new GeoPoint(coordinates.getDouble("lat"), coordinates.getDouble("lng"))));
                        }
                    }
                    else if (featureAsJson.getString("type")
                            .equals(GeoJSONType.LINE_STRING.getValue()) ||
                            featureAsJson.getString("type")
                                    .equals(GeoJSONType.POLYGON.getValue())) {
                        JSONArray coordinates = featureAsJson.getJSONArray("coordinates");
                        List<Point> points = new ArrayList<Point>();

                        for (int i = 0; i < coordinates.length(); i++) {
                            points.add(new Point(new GeoPoint(coordinates.getJSONObject(i)
                                    .getDouble("lat"), coordinates.getJSONObject(i)
                                    .getDouble("lng"))));
                        }

                        if (featureAsJson.getString("type")
                                .equals(GeoJSONType.LINE_STRING.getValue())) {
                            feature.setGeometry(new LineString(points));
                        }
                        else if (featureAsJson.getString("type")
                                .equals(GeoJSONType.POLYGON.getValue())) {
                            feature.setGeometry(new Polygon(points));
                        }
                    }

                    mWebViewFragment.setCurrentEditableFeature(feature);
                }
                catch (JSONException je) {
                    Log.w(DrawControl.class.getName(), je);
                }
            }
        });
    }

    @JavascriptInterface
    public void deleteFeature(final String featureId) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d(DrawControl.class.getName(), "deleteFeature : " + featureId);

                // refreshes all features
                if (mWebViewFragment.deleteEditableFeature(featureId)) {
                    mWebViewFragment.loadUrl(getJSUrlPrefix() + ".loadFeatures()");
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
                        .putBoolean(KEY_ADD_OR_UPDATE_FEATURE, adding);
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
                        .putBoolean(KEY_ADD_OR_UPDATE_FEATURE, editing);
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
                        .putBoolean(KEY_ADD_OR_UPDATE_FEATURE, false);
                mWebViewFragment.invalidateMenu();

                if (mOnDrawControlListener != null) {
                    mOnDrawControlListener.onDeletingFeature(deleting);
                }
            }
        });
    }

    protected void updateButtons() {
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
            Log.w(DrawControl.class.getName(), "addOrUpdateSelectedFeature: nothing to add or update");

            updateButtons();
        }
        else {
            if (feature.getGeometry() != null) {
                // specific case for feature type Point
                if (!mWebViewFragment.getEditableFeatures()
                        .hasFeature(feature.getId()) && feature.getGeometry()
                        .getType()
                        .equals(GeoJSONType.POINT)) {
                    getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            mImageButtonAddMarker.setSelected(false);
                            mIsImageButtonAddMarkerSelected = false;
                        }
                    });
                }
            }

            // refreshes all features
            mWebViewFragment.addOrUpdateEditableFeature(feature);
            mWebViewFragment.loadUrl(getJSUrlPrefix() + ".loadFeatures()");
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

        public void onAddingFeature(boolean adding);

        public void onEditingFeature(boolean editing);

        public void onDeletingFeature(boolean deleting);
    }
}
