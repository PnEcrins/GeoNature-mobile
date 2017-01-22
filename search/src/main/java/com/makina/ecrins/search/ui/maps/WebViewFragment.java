package com.makina.ecrins.search.ui.maps;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.model.MountPoint;
import com.makina.ecrins.commons.util.FileUtils;
import com.makina.ecrins.maps.AbstractWebViewFragment;
import com.makina.ecrins.maps.control.AbstractControl.OnIControlListener;
import com.makina.ecrins.maps.control.CenterPositionControl;
import com.makina.ecrins.maps.control.ControlUtils;
import com.makina.ecrins.maps.control.FeaturesControl;
import com.makina.ecrins.maps.control.SwitchLayersControl;
import com.makina.ecrins.maps.control.ZoomControl;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureStyle;
import com.makina.ecrins.maps.jts.geojson.GeoPoint;
import com.makina.ecrins.maps.location.Geolocation;
import com.makina.ecrins.maps.settings.MapSettings;
import com.makina.ecrins.search.BuildConfig;
import com.makina.ecrins.search.MainApplication;
import com.makina.ecrins.search.R;
import com.makina.ecrins.search.content.LoadFilteredFeaturesLoader;
import com.makina.ecrins.search.content.MainContentProvider;
import com.makina.ecrins.search.ui.dialog.SearchDialogFragment.OnSearchDialogValidateListener;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of a WebView Fragment embedding a WebView with Leaflet.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class WebViewFragment
        extends AbstractWebViewFragment
        implements LoaderManager.LoaderCallbacks<List<Feature>> {

    protected static final String KEY_FEATURES = "features";
    protected static final String KEY_FEATURES_FOUND = "features_found";
    protected static final String KEY_SELECTED_FEATURE = "selected_feature";
    protected static final String KEY_RADIUS = "radius";
    protected static final String KEY_SEARCH_LOCATION = "search_location";
    protected static final String KEY_TAXON = "taxon";

    private static final int LOADER_TAXA_GROUPBY = 0;
    private static final int LOADER_TAXA = 1;

    private OnFeaturesFoundListener mOnFeaturesFoundListener;

    private OnIControlListener mFeaturesControlListener = new OnIControlListener() {
        @Override
        public void onControlInitialized() {

            if (BuildConfig.DEBUG) {
                Log.d(getClass().getName(),
                      "onControlInitialized");
            }

            if (mOnFeaturesFoundListener.getSelectedFeature() != null) {
                final Feature previousFeature = getSavedInstanceState().getParcelable(KEY_SELECTED_FEATURE);

                if ((previousFeature != null) &&
                        previousFeature.getId()
                                       .equals(mOnFeaturesFoundListener.getSelectedFeature()
                                                                       .getId()) &&
                        !getSavedInstanceState().getParcelableArrayList(KEY_FEATURES)
                                                .isEmpty()) {

                    final ArrayList<Feature> features = getSavedInstanceState().getParcelableArrayList(KEY_FEATURES);
                    addFeaturesToFeaturesControl(features);

                    getActivity().setTitle(previousFeature.getProperties()
                                                          .getString(MainDatabaseHelper.SearchColumns.TAXON));
                }
                else {
                    getSavedInstanceState().putParcelable(KEY_SELECTED_FEATURE,
                                                          mOnFeaturesFoundListener.getSelectedFeature());
                    getSavedInstanceState().putString(KEY_TAXON,
                                                      mOnFeaturesFoundListener.getSelectedFeature()
                                                                              .getProperties()
                                                                              .getString(MainDatabaseHelper.SearchColumns.TAXON));
                    getLoaderManager().restartLoader(LOADER_TAXA,
                                                     getSavedInstanceState(),
                                                     WebViewFragment.this);

                    getActivity().setTitle(R.string.activity_main_title);
                }
            }
        }
    };

    private OnSearchDialogValidateListener mOnSearchDialogValidateListener = new OnSearchDialogValidateListener() {
        @Override
        public void onSearchCriteria(DialogInterface dialog,
                                     int radius,
                                     GeoPoint location) {

            if (BuildConfig.DEBUG) {
                Log.d(getClass().getName(),
                      "onSearchCriteria, radius: " + radius + "m, location : " + location.toString());
            }

            clearFeaturesToFeaturesControl();

            if (getSavedInstanceState().containsKey(KEY_SEARCH_LOCATION) &&
                    getSavedInstanceState().containsKey(KEY_RADIUS) &&
                    getSavedInstanceState().containsKey(KEY_FEATURES_FOUND)) {
                if (BuildConfig.DEBUG) {
                    Log.d(getClass().getName(),
                          "onSearchCriteria, previous location " + getSavedInstanceState().getParcelable(KEY_SEARCH_LOCATION)
                                                                                          .toString());
                    Log.d(getClass().getName(),
                          "onSearchCriteria, location " + getSavedInstanceState().getParcelable(KEY_SEARCH_LOCATION)
                                                                                 .equals(location));
                    Log.d(getClass().getName(),
                          "onSearchCriteria, radius " + (Double.valueOf(getSavedInstanceState().getDouble(KEY_RADIUS))
                                                               .intValue() == radius));
                    Log.d(getClass().getName(),
                          "onSearchCriteria, features founds " + getSavedInstanceState().getParcelableArrayList(KEY_FEATURES_FOUND)
                                                                                        .size());
                }
            }

            if (getSavedInstanceState().containsKey(KEY_SEARCH_LOCATION) &&
                    getSavedInstanceState().containsKey(KEY_RADIUS) &&
                    getSavedInstanceState().containsKey(KEY_FEATURES_FOUND) &&
                    getSavedInstanceState().getParcelable(KEY_SEARCH_LOCATION)
                                           .equals(location) &&
                    (Double.valueOf(getSavedInstanceState().getDouble(KEY_RADIUS))
                           .intValue() == radius)) {
                final ArrayList<Feature> features = getSavedInstanceState().getParcelableArrayList(KEY_FEATURES_FOUND);
                mOnFeaturesFoundListener.onFeaturesFound(features);
            }
            else {
                ((AppCompatActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(true);

                getSavedInstanceState().putParcelable(KEY_SEARCH_LOCATION,
                                                      location);
                getSavedInstanceState().putDouble(KEY_RADIUS,
                                                  radius);

                getLoaderManager().restartLoader(LOADER_TAXA_GROUPBY,
                                                 getSavedInstanceState(),
                                                 WebViewFragment.this);
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        if (BuildConfig.DEBUG) {
            Log.d(WebViewFragment.class.getName(),
                  "onAttach");
        }

        try {
            mOnFeaturesFoundListener = (OnFeaturesFoundListener) activity;
        }
        catch (ClassCastException cce) {
            throw new ClassCastException(activity.toString() + " must implement OnFeatureSelectedListener");
        }
    }

    @Override
    public void onResume() {

        super.onResume();

        updateMapSettings();
        reload();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {

        super.onCreateOptionsMenu(menu,
                                  inflater);

        inflater.inflate(R.menu.menu_list_features,
                         menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        if (BuildConfig.DEBUG) {
            Log.d(WebViewFragment.class.getName(),
                  "onPrepareOptionsMenu");
        }

        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.itemListFeatures)
            .setVisible(getSavedInstanceState().containsKey(KEY_FEATURES_FOUND) && (!getSavedInstanceState().getParcelableArrayList(KEY_FEATURES_FOUND)
                                                                                                            .isEmpty()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.itemListFeatures) {
            if (getSavedInstanceState().containsKey(KEY_FEATURES_FOUND) && (!getSavedInstanceState().getParcelableArrayList(KEY_FEATURES_FOUND)
                                                                                                    .isEmpty())) {
                final ArrayList<Feature> features = getSavedInstanceState().getParcelableArrayList(KEY_FEATURES_FOUND);
                mOnFeaturesFoundListener.onFeaturesFound(features);
            }

            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public MapSettings getMapSettings() {

        MapSettings mapSettings = super.getMapSettings();

        if (mapSettings == null) {
            mapSettings = ((MainApplication) getActivity().getApplication()).getAppSettings()
                                                                            .getMapSettings();
            setMapSettings(mapSettings);
        }

        return mapSettings;
    }

    @NonNull
    @Override
    public List<Feature> getFeatures() {

        if (!getSavedInstanceState().containsKey(KEY_FEATURES)) {
            getSavedInstanceState().putParcelableArrayList(KEY_FEATURES,
                                                           new ArrayList<Feature>());
        }

        return getSavedInstanceState().getParcelableArrayList(KEY_FEATURES);
    }

    @Override
    public void setSelectedFeature(Geolocation geolocation,
                                   Feature selectedFeature) {

        mOnFeaturesFoundListener.onFeatureSelected((GeoPoint) getSavedInstanceState().getParcelable(KEY_SEARCH_LOCATION),
                                                   selectedFeature);
    }

    @Override
    protected void loadControls() {

        final SearchControl searchControl = new SearchControl(getActivity());
        searchControl.setMinRadius(0);
        searchControl.setMaxRadius(((MainApplication) getActivity().getApplication()).getAppSettings()
                                                                                     .getSearchSettings()
                                                                                     .getMaxRadius());
        searchControl.setRadius(Double.valueOf(getSavedInstanceState().getDouble(KEY_RADIUS,
                                                                                 ((MainApplication) getActivity().getApplication()).getAppSettings()
                                                                                                                                   .getSearchSettings()
                                                                                                                                   .getDefaultRadius()))
                                      .intValue());
        searchControl.setOnSearchDialogValidateListener(mOnSearchDialogValidateListener);

        final FeaturesControl featuresControl = new FeaturesControl(getActivity());
        featuresControl.setFeaturesClickable(true);
        featuresControl.setFeatureSelectedStyle(FeatureStyle.Builder.newInstance(getContext())
                                                                    .from(featuresControl.getFeatureSelectedStyle())
                                                                    .setColorResourceId(R.color.feature_selected)
                                                                    .setFillColorResourceId(R.color.feature_selected)
                                                                    .build());
        featuresControl.addControlListener(mFeaturesControlListener);

        addControl(new ZoomControl(getActivity()),
                   mRightToolbarLayout);
        addControl(new CenterPositionControl(getActivity()),
                   mRightToolbarLayout);
        addControl(new SwitchLayersControl(getActivity()),
                   null);
        addControl(featuresControl,
                   null);
        addControl(searchControl,
                   null);
    }

    @Override
    protected File getTilesSourcePath() throws
                                        IOException {

        return FileUtils.getDatabaseFolder(getActivity(),
                                           MountPoint.StorageType.EXTERNAL);
    }

    @Override
    public Loader<List<Feature>> onCreateLoader(int id,
                                                Bundle args) {

        switch (id) {
            case LOADER_TAXA_GROUPBY:
                if (BuildConfig.DEBUG) {
                    Log.d(WebViewFragment.class.getName(),
                          "onCreateLoader LOADER_TAXA_GROUPBY");
                }

                return new LoadFilteredFeaturesLoader(getActivity(),
                                                      MainContentProvider.CONTENT_SEARCH_URI,
                                                      (GeoPoint) args.getParcelable(KEY_SEARCH_LOCATION),
                                                      args.getDouble(KEY_RADIUS),
                                                      MainDatabaseHelper.SearchColumns.TAXON);
            case LOADER_TAXA:
                if (BuildConfig.DEBUG) {
                    Log.d(WebViewFragment.class.getName(),
                          "onCreateLoader LOADER_TAXA, taxon: " + args.getString(KEY_TAXON));
                }

                return new LoadFilteredFeaturesLoader(getActivity(),
                                                      Uri.withAppendedPath(MainContentProvider.CONTENT_SEARCH_URI,
                                                                           args.getString(KEY_TAXON)),
                                                      (GeoPoint) args.getParcelable(KEY_SEARCH_LOCATION),
                                                      args.getDouble(KEY_RADIUS),
                                                      null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Feature>> loader,
                               List<Feature> data) {

        switch (loader.getId()) {
            case LOADER_TAXA_GROUPBY:
                getSavedInstanceState().putParcelableArrayList(KEY_FEATURES_FOUND,
                                                               new ArrayList<>(data));
                ((AppCompatActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(false);
                invalidateMenu();

                clearFeaturesToFeaturesControl();

                mOnFeaturesFoundListener.onFeaturesFound(data);
                break;
            case LOADER_TAXA:
                if (!data.isEmpty()) {
                    clearFeaturesToFeaturesControl();

                    if (((MainApplication) getActivity().getApplication()).getAppSettings()
                                                                          .getSearchSettings()
                                                                          .getMaxFeaturesFound() < data.size()) {
                        Toast.makeText(getActivity(),
                                       getString(R.string.message_max_features_found,
                                                 data.size()),
                                       Toast.LENGTH_LONG)
                             .show();

                        getActivity().setTitle(R.string.activity_main_title);
                    }
                    else {
                        Toast.makeText(getActivity(),
                                       MessageFormat.format(getString(R.string.message_features_found),
                                                            data.size()),
                                       Toast.LENGTH_LONG)
                             .show();

                        addFeaturesToFeaturesControl(data);

                        if (getSavedInstanceState().containsKey(KEY_SELECTED_FEATURE) && getSavedInstanceState().getParcelable(KEY_SELECTED_FEATURE) != null) {
                            getActivity().setTitle(((Feature) getSavedInstanceState().getParcelable(KEY_SELECTED_FEATURE)).getProperties()
                                                                                                                          .getString(MainDatabaseHelper.SearchColumns.TAXON));
                        }
                    }
                }

                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Feature>> loader) {

        ((AppCompatActivity) getActivity()).setSupportProgressBarIndeterminateVisibility(false);

        clearFeaturesToFeaturesControl();
    }

    private boolean updateMapSettings() {

        boolean update = getMapSettings().isDisplayScale() != PreferenceManager.getDefaultSharedPreferences(getActivity())
                                                                               .getBoolean("pointing_display_scale",
                                                                                           true);

        if (update) {
            MapSettings mapSettings = getMapSettings();
            mapSettings.setDisplayScale(PreferenceManager.getDefaultSharedPreferences(getActivity())
                                                         .getBoolean("pointing_display_scale",
                                                                     true));
            mapSettings.setShowUnitiesLayer(PreferenceManager.getDefaultSharedPreferences(getActivity())
                                                             .getBoolean("pointing_display_geographic_units",
                                                                         true));

            setMapSettings(mapSettings);
        }

        return update;
    }

    private void clearFeaturesToFeaturesControl() {

        final String featureControlName = ControlUtils.getControlName(FeaturesControl.class);

        if (hasControl(featureControlName)) {
            ((FeaturesControl) getControl(featureControlName)).clearFeatures();
        }
    }

    private void addFeaturesToFeaturesControl(final List<Feature> features) {

        final String featureControlName = ControlUtils.getControlName(FeaturesControl.class);

        if (hasControl(featureControlName)) {
            ((FeaturesControl) getControl(featureControlName)).addFeatures(features,
                                                                           true);
        }
    }

    public interface OnFeaturesFoundListener {

        void onFeaturesFound(List<Feature> features);

        Feature getSelectedFeature();

        void onFeatureSelected(GeoPoint geoPoint,
                               Feature feature);
    }
}
