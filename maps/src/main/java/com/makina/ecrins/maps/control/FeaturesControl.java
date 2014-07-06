package com.makina.ecrins.maps.control;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.makina.ecrins.maps.IWebViewFragment;
import com.makina.ecrins.maps.geojson.Feature;
import com.makina.ecrins.maps.geojson.FeatureCollection;
import com.makina.ecrins.maps.geojson.FeatureStyle;
import com.makina.ecrins.maps.geojson.geometry.GeoPoint;
import com.makina.ecrins.maps.geojson.geometry.GeometryUtils;
import com.makina.ecrins.maps.geojson.geometry.Point;
import com.makina.ecrins.maps.geojson.operation.DistanceFilter;
import com.makina.ecrins.maps.location.Geolocation;

import org.json.JSONException;

import java.util.Iterator;
import java.util.List;

/**
 * Basic control to display a list of {@link Feature}s.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FeaturesControl extends AbstractControl {

    private boolean mFeaturesClickable = false;
    private FeatureStyle mFeatureDefaultStyle = new FeatureStyle();
    private FeatureStyle mFeatureSelectedStyle = new FeatureStyle().setOpacity(0.9);

    /**
     * {@link FeaturesControl} as Singleton.
     */
    public FeaturesControl(Context pContext) {
        super(pContext);
    }

    @Override
    public View getView(boolean forceCreate) {
        return null;
    }

    @Override
    public void add(IWebViewFragment webViewFragment) {
        super.add(webViewFragment);

        initializeJSController("js/Control.Features.js", "new L.Control.Features()");
    }

    public FeatureStyle getFeatureDefaultStyle() {
        return mFeatureDefaultStyle;
    }

    public void setFeatureDefaultStyle(FeatureStyle pFeatureDefaultStyle) {
        this.mFeatureDefaultStyle = pFeatureDefaultStyle;
    }

    public FeatureStyle getFeatureSelectedStyle() {
        return mFeatureSelectedStyle;
    }

    public void setFeatureSelectedStyle(FeatureStyle pFeatureSelectedStyle) {
        this.mFeatureSelectedStyle = pFeatureSelectedStyle;
    }

    /**
     * Adds a <code>list</code> of {@link Feature}s to display with a default style.
     * <p>Creates a {@link FeatureCollection} instance to store these {@link Feature}s.</p>
     *
     * @param features  a <code>list</code> of {@link Feature}s
     * @param fitBounds sets the map view that contains the given geographical bounds (from all given features) with the maximum zoom level possible
     * @see #addFeatures(java.util.List, com.makina.ecrins.maps.geojson.FeatureStyle, boolean)
     */
    public void addFeatures(final List<Feature> features, boolean fitBounds) {
        addFeatures(features, getFeatureDefaultStyle(), fitBounds);
    }

    /**
     * Adds a <code>list</code> of {@link Feature}s to display.
     * <p>Creates a {@link FeatureCollection} instance to store these {@link Feature}s.</p>
     *
     * @param features  a <code>list</code> of {@link Feature}s
     * @param style     style to apply to these {@link Feature}s.
     * @param fitBounds sets the map view that contains the given geographical bounds (from all given features) with the maximum zoom level possible
     */
    public void addFeatures(final List<Feature> features, final FeatureStyle style, boolean fitBounds) {
        if (isControlInitialized()) {
            mWebViewFragment.getFeatures()
                    .addAll(features);

            final FeatureCollection featureCollection = new FeatureCollection(features);

            Log.d(getClass().getName(), "addFeatures size : " + featureCollection.getFeatures()
                    .size());

            try {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() +
                        ".addFeatures('" +
                        featureCollection.getJSONObject()
                                .toString() +
                        "', '" +
                        style.getJSONObject(getContext())
                                .toString() +
                        "', " +
                        fitBounds +
                        ")");
            }
            catch (JSONException je) {
                Log.w(getClass().getName(), je.getMessage());
            }
        }
        else {
            Log.w(getClass().getName(), "addFeatures : Control '" + getName() + "' is not initialized !");
        }
    }

    /**
     * Clears all {@link Feature}s.
     */
    public void clearFeatures() {
        if (isControlInitialized()) {
            Log.d(getClass().getName(), "clearFeatures");

            this.mWebViewFragment.getFeatures()
                    .clear();
            this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".clearFeatures()");
        }
        else {
            Log.w(getClass().getName(), "clearFeatures : control not initialized");
        }
    }

    @JavascriptInterface
    public void findFeature(final double latitude, final double longitude) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                final GeoPoint location = new GeoPoint(latitude, longitude);
                final Point locationAsPoint = new Point(location);

                Log.d(FeaturesControl.class.getName(), "findFeature on location " + location.toString());

                Feature featureFound = null;
                final Iterator<Feature> iterator = mWebViewFragment.getFeatures()
                        .iterator();

                while ((featureFound == null) && iterator.hasNext()) {
                    Feature featureToCheck = iterator.next();

                    if (GeometryUtils.contains(locationAsPoint, featureToCheck.getGeometry())) {
                        featureFound = featureToCheck;
                    }
                }

                if (featureFound == null) {
                    Log.d(FeaturesControl.class.getName(), "findFeature : no feature found at location " + location.toString() + ", try to find the closest feature ...");

                    final DistanceFilter distanceFilter = new DistanceFilter(location, 100);

                    for (Feature feature : mWebViewFragment.getFeatures()) {
                        feature.apply(distanceFilter);
                    }

                    if (!distanceFilter.getFiltereredFeatures()
                            .isEmpty()) {
                        featureFound = distanceFilter.getFiltereredFeatures()
                                .get(0);
                    }
                }

                if (featureFound == null) {
                    Log.d(FeaturesControl.class.getName(), "no feature found");
                }
                else {
                    Log.d(FeaturesControl.class.getName(), "nearest feature found '" + featureFound.getId() + "'");

                    mWebViewFragment.setSelectedFeature(new Geolocation(longitude, latitude, 0), featureFound);
                    mWebViewFragment.loadUrl(getJSUrlPrefix() +
                            ".selectFeature(\"" +
                            featureFound.getId() +
                            "\")");
                }
            }
        });
    }

    @JavascriptInterface
    public String getFeatureDefaultStyleAsString() {
        try {
            return mFeatureDefaultStyle.getJSONObject(getContext())
                    .toString();
        }
        catch (JSONException je) {
            Log.w(getClass().getName(), je.getMessage(), je);

            return "{}";
        }
    }

    @JavascriptInterface
    public String getFeatureSelectedStyleAsString() {
        try {
            return mFeatureSelectedStyle.getJSONObject(getContext())
                    .toString();
        }
        catch (JSONException je) {
            Log.w(getClass().getName(), je.getMessage(), je);

            return "{}";
        }
    }

    @JavascriptInterface
    public boolean isFeaturesClickable() {
        return mFeaturesClickable;
    }

    public void setFeaturesClickable(boolean pFeaturesClickable) {
        this.mFeaturesClickable = pFeaturesClickable;
    }
}
