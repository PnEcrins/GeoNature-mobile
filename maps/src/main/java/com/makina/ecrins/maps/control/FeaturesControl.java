package com.makina.ecrins.maps.control;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.makina.ecrins.maps.BuildConfig;
import com.makina.ecrins.maps.IWebViewFragment;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;
import com.makina.ecrins.maps.jts.geojson.FeatureStyle;
import com.makina.ecrins.maps.jts.geojson.GeoPoint;
import com.makina.ecrins.maps.jts.geojson.filter.NearestFeaturesFilter;
import com.makina.ecrins.maps.jts.geojson.io.GeoJsonWriter;
import com.makina.ecrins.maps.location.Geolocation;

import java.util.Iterator;
import java.util.List;

/**
 * Basic control to display a list of {@link Feature}s.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FeaturesControl
        extends AbstractControl {

    private static final String TAG = FeaturesControl.class.getName();

    private boolean mFeaturesClickable = false;
    private FeatureStyle mFeatureDefaultStyle;
    private FeatureStyle mFeatureSelectedStyle;

    /**
     * Default constructor.
     */
    public FeaturesControl(Context pContext) {
        super(pContext);

        mFeatureDefaultStyle = FeatureStyle.Builder.newInstance(pContext)
                                                   .build();
        mFeatureSelectedStyle = FeatureStyle.Builder.newInstance(pContext)
                                                    .setOpacity(0.9)
                                                    .build();
    }

    @Override
    public View getView(boolean forceCreate) {
        return null;
    }

    @Override
    public void add(IWebViewFragment webViewFragment) {
        super.add(webViewFragment);

        initializeJSController("js/Control.Features.js",
                               "new L.Control.Features()");
    }

    @NonNull
    public FeatureStyle getFeatureDefaultStyle() {
        return mFeatureDefaultStyle;
    }

    public void setFeatureDefaultStyle(@NonNull final FeatureStyle pFeatureDefaultStyle) {
        this.mFeatureDefaultStyle = pFeatureDefaultStyle;
    }

    @NonNull
    public FeatureStyle getFeatureSelectedStyle() {
        return mFeatureSelectedStyle;
    }

    public void setFeatureSelectedStyle(@NonNull final FeatureStyle pFeatureSelectedStyle) {
        this.mFeatureSelectedStyle = pFeatureSelectedStyle;
    }

    /**
     * Adds a <code>list</code> of {@link Feature}s to display with a default style.
     * <p>Creates a {@link FeatureCollection} instance to store these {@link Feature}s.</p>
     *
     * @param features  a <code>list</code> of {@link Feature}s
     * @param fitBounds sets the map view that contains the given geographical bounds (from all given features) with the maximum zoom level possible
     *
     * @see #addFeatures(java.util.List, FeatureStyle, boolean)
     */
    public void addFeatures(@NonNull final List<Feature> features,
                            boolean fitBounds) {
        addFeatures(features,
                    getFeatureDefaultStyle(),
                    fitBounds);
    }

    /**
     * Adds a <code>list</code> of {@link Feature}s to display.
     * <p>Creates a {@link FeatureCollection} instance to store these {@link Feature}s.</p>
     *
     * @param features  a <code>list</code> of {@link Feature}s
     * @param style     style to apply to these {@link Feature}s.
     * @param fitBounds sets the map view that contains the given geographical bounds (from all given features) with the maximum zoom level possible
     */
    public void addFeatures(@NonNull final List<Feature> features,
                            @NonNull final FeatureStyle style,
                            final boolean fitBounds) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (isControlInitialized()) {
                    mWebViewFragment.getFeatures()
                                    .addAll(features);

                    final FeatureCollection featureCollection = new FeatureCollection();
                    featureCollection.addAllFeatures(features);

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              "addFeatures size: " + featureCollection.getFeatures()
                                                                      .size());
                    }

                    mWebViewFragment.loadUrl(getJSUrlPrefix() +
                                                     ".addFeatures('" +
                                                     new GeoJsonWriter().write(featureCollection) +
                                                     "', '" +
                                                     style.toString() +
                                                     "', " +
                                                     fitBounds +
                                                     ")");
                }
                else {
                    Log.w(TAG,
                          "addFeatures: Control '" + getName() + "' is not initialized !");
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

                    mWebViewFragment.getFeatures()
                                    .clear();
                    mWebViewFragment.loadUrl(getJSUrlPrefix() + ".clearFeatures()");
                }
                else {
                    Log.w(TAG,
                          "clearFeatures: Control '" + getName() + "' is not initialized !");
                }
            }
        });
    }

    @JavascriptInterface
    public void findFeature(final double latitude,
                            final double longitude) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                final GeoPoint location = new GeoPoint(latitude,
                                                       longitude);

                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "findFeature on location " + location.toString());
                }

                Feature featureFound = null;
                final Iterator<Feature> iterator = mWebViewFragment.getFeatures()
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
                                                                                                     mWebViewFragment.getFeatures());

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

                    mWebViewFragment.setSelectedFeature(new Geolocation(longitude,
                                                                        latitude,
                                                                        0),
                                                        featureFound);
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
        return mFeatureDefaultStyle.toString();
    }

    @JavascriptInterface
    public String getFeatureSelectedStyleAsString() {
        return mFeatureSelectedStyle.toString();
    }

    @JavascriptInterface
    public boolean isFeaturesClickable() {
        return mFeaturesClickable;
    }

    public void setFeaturesClickable(boolean pFeaturesClickable) {
        this.mFeaturesClickable = pFeaturesClickable;
    }
}
