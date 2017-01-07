package com.makina.ecrins.flora.ui.input.map;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.model.MountPoint;
import com.makina.ecrins.commons.ui.input.IInputFragment;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.commons.util.FileUtils;
import com.makina.ecrins.flora.BuildConfig;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.content.MainContentProvider;
import com.makina.ecrins.flora.input.Area;
import com.makina.ecrins.flora.input.Input;
import com.makina.ecrins.flora.input.Taxon;
import com.makina.ecrins.flora.ui.input.PagerFragmentActivity;
import com.makina.ecrins.maps.AbstractWebViewFragment;
import com.makina.ecrins.maps.control.AbstractControl;
import com.makina.ecrins.maps.control.CenterPositionControl;
import com.makina.ecrins.maps.control.ControlUtils;
import com.makina.ecrins.maps.control.DrawControl;
import com.makina.ecrins.maps.control.FeaturesControl;
import com.makina.ecrins.maps.control.SwitchLayersControl;
import com.makina.ecrins.maps.control.ZoomControl;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureStyle;
import com.makina.ecrins.maps.jts.geojson.io.GeoJsonReader;
import com.makina.ecrins.maps.location.Geolocation;
import com.makina.ecrins.maps.settings.MapSettings;
import com.vividsolutions.jts.geom.Geometry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Basic implementation of a WebView Fragment embedding a WebView with Leaflet.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class WebViewFragment
        extends AbstractWebViewFragment
        implements IValidateFragment,
                   IInputFragment,
                   LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = WebViewFragment.class.getName();

    public static final String KEY_AP = "ap";
    public static final String KEY_SINGLE_FEATURE = "single_feature";
    public static final String KEY_ADD_MARKER = "add_marker";
    public static final String KEY_ADD_PATH = "add_path";
    public static final String KEY_ADD_POLYGON = "add_polygon";
    public static final String KEY_EDITING_FEATURE = "editing_feature";

    protected boolean mIsActionMarkerCollectionPAsSelected = false;

    private Input mInput;

    public WebViewFragment() {
        super();

        setArguments(new Bundle());
    }

    @Override
    public void onResume() {
        super.onResume();

        reload();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,
                                  inflater);

        inflater.inflate(R.menu.menu_collection_pa,
                         menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final MenuItem itemMarker = menu.findItem(R.id.itemCollectionPAs);

        final String featureControlName = ControlUtils.getControlName(FeaturesControl.class);

        if (hasControl(featureControlName) && ((FeaturesControl) getControl(featureControlName)).isControlInitialized()) {
            itemMarker.setEnabled(true);
            itemMarker.setIcon((this.mIsActionMarkerCollectionPAsSelected) ? R.drawable.ic_action_collection_pa_selected : R.drawable.ic_action_collection_pa);
        }
        else {
            itemMarker.setEnabled(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemCollectionPAs:
                this.mIsActionMarkerCollectionPAsSelected = !this.mIsActionMarkerCollectionPAsSelected;

                if (this.mIsActionMarkerCollectionPAsSelected) {
                    if (isAdded()) {
                        getLoaderManager().restartLoader(0,
                                                         null,
                                                         this);
                    }
                }
                else {
                    if (!getArguments().getBoolean(KEY_AP,
                                                   true)) {
                        displayAPs(false);
                    }
                }

                invalidateMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public int getResourceTitle() {
        if (getArguments().getBoolean(KEY_AP,
                                      true)) {
            return R.string.pager_fragment_webview_ap_title;
        }
        else {
            return R.string.pager_fragment_webview_pa_title;
        }
    }

    @Override
    public boolean getPagingEnabled() {
        return false;
    }

    @Override
    public boolean validate() {
        Log.d(TAG,
              "validate KEY_EDITING_FEATURE: " + getSavedInstanceState().getBoolean(KEY_EDITING_FEATURE,
                                                                                    false));

        if (getArguments().getBoolean(KEY_AP,
                                      true)) {
            return (mInput != null) && (mInput.getCurrentSelectedTaxon() != null) &&
                    (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) && (!getSavedInstanceState().getBoolean(KEY_EDITING_FEATURE,
                                                                                                                                          false));
        }
        else {
            return (mInput != null) && (mInput.getCurrentSelectedTaxon() != null) &&
                    (((Taxon) mInput.getCurrentSelectedTaxon()).getProspectingArea() != null) &&
                    (!getSavedInstanceState().getBoolean(KEY_EDITING_FEATURE,
                                                         false)) &&
                    checkIfProspectingAreaContainsAllAreasPresences(((Taxon) mInput.getCurrentSelectedTaxon()).getProspectingArea());
        }
    }

    @Override
    public void refreshView() {
        Log.d(TAG,
              "refreshView, AP: " + getArguments().getBoolean(KEY_AP,
                                                              true));

        if (mInput == null) {
            Log.w(TAG,
                  "refreshView: null input");
            return;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        if (currentSelectedTaxon == null) {
            Log.w(TAG,
                  "refreshView: no selected taxon found");

            return;
        }

        if (getArguments().getBoolean(KEY_AP,
                                      true)) {
            // clear all editable features if no area was edited yet.
            if (currentSelectedTaxon.getCurrentSelectedArea() == null) {
                clearEditableFeatures();
            }

            if (getEditableFeatures().isEmpty() && (currentSelectedTaxon.getCurrentSelectedArea() != null)) {
                final Area currentSelectedArea = currentSelectedTaxon.getCurrentSelectedArea();

                if (currentSelectedArea != null && currentSelectedArea.getFeature() != null) {
                    setCurrentEditableFeature(currentSelectedArea.getFeature());

                    final String drawControlName = ControlUtils.getControlName(DrawControl.class);

                    if (hasControl(drawControlName)) {
                        ((DrawControl) getControl(drawControlName)).setFeatures(Collections.singletonList(currentSelectedArea.getFeature()));
                    }
                }
            }
        }
        else {
            displayAPs(true);

            if (currentSelectedTaxon.getProspectingArea() == null) {
                clearEditableFeatures();
            }
            else {
                final Feature prospectingArea = currentSelectedTaxon.getProspectingArea();

                if (getEditableFeatures().isEmpty() && prospectingArea != null) {
                    setCurrentEditableFeature(prospectingArea);

                    final String drawControlName = ControlUtils.getControlName(DrawControl.class);

                    if (hasControl(drawControlName)) {
                        ((DrawControl) getControl(drawControlName)).setFeatures(Collections.singletonList(prospectingArea));
                    }
                }

                // checks if this feature contains all features added to this taxon areas
                if ((prospectingArea != null) && !checkIfProspectingAreaContainsAllAreasPresences(prospectingArea)) {
                    Log.d(TAG,
                          "feature '" + currentSelectedTaxon.getProspectingArea()
                                                            .getId() + "' does not contains all previously added areas");

                    Toast.makeText(getActivity(),
                                   R.string.message_pa_not_contains_all_aps,
                                   Toast.LENGTH_LONG)
                         .show();
                }
            }
        }
    }

    @Override
    public void setInput(@NonNull AbstractInput input) {
        this.mInput = (Input) input;
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
        // no features to load
        return new ArrayList<>();
    }

    @Override
    public void setSelectedFeature(Geolocation geolocation,
                                   Feature selectedFeature) {
        // nothing to do ...
    }

    @Override
    public boolean addOrUpdateEditableFeature(@NonNull Feature selectedFeature) {
        if (super.addOrUpdateEditableFeature(selectedFeature) && !getEditableFeatures().getFeatures()
                                                                                       .isEmpty() && (mInput.getCurrentSelectedTaxon() != null)) {
            Log.d(TAG,
                  "addOrUpdateEditableFeature: " +
                          selectedFeature.getGeometry()
                                         .getGeometryType() +
                          ", id: " +
                          selectedFeature.getId());

            getSavedInstanceState().putBoolean(KEY_EDITING_FEATURE,
                                               false);

            if (mInput == null) {
                return false;
            }

            final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

            if (currentSelectedTaxon == null) {
                return false;
            }

            if (getArguments().getBoolean(KEY_AP,
                                          true)) {
                // delete a previously added area if needed
                if ((!TextUtils.isEmpty(currentSelectedTaxon.getCurrentSelectedAreaId()) && (!currentSelectedTaxon.getCurrentSelectedAreaId()
                                                                                                                  .equals(selectedFeature.getId())))) {
                    currentSelectedTaxon.getAreas()
                                        .remove(currentSelectedTaxon.getCurrentSelectedAreaId());
                }

                // add or update the current area
                currentSelectedTaxon.getAreas()
                                    .put(selectedFeature.getId(),
                                         new Area(selectedFeature));
                currentSelectedTaxon.setCurrentSelectedAreaId(selectedFeature.getId());

                ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();

                return currentSelectedTaxon.getAreas()
                                           .containsKey(selectedFeature.getId());
            }
            else {
                if (!currentSelectedTaxon.getAreas()
                                         .isEmpty()) {
                    // checks if this feature contains all features added to this taxon areas
                    if (!checkIfProspectingAreaContainsAllAreasPresences(selectedFeature)) {
                        Log.d(TAG,
                              "feature '" +
                                      selectedFeature.getId() +
                                      "' does not contains all previously added areas");

                        Toast.makeText(getActivity(),
                                       R.string.message_pa_not_contains_all_aps,
                                       Toast.LENGTH_LONG)
                             .show();
                    }
                }

                currentSelectedTaxon.setProspectingArea(selectedFeature);
                ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();

                return (currentSelectedTaxon.getProspectingArea() != null);
            }
        }
        else {
            if (!selectedFeature.getGeometry()
                                .isValid()) {
                // add this invalid feature to be edited by the user
                getEditableFeatures().addFeature(selectedFeature);
                Toast.makeText(getActivity(),
                               com.makina.ecrins.maps.R.string.message_feature_invalid,
                               Toast.LENGTH_SHORT)
                     .show();
            }

            ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();

            return false;
        }
    }

    @Override
    public boolean deleteEditableFeature(String featureId) {
        super.deleteEditableFeature(featureId);

        getSavedInstanceState().putBoolean(KEY_EDITING_FEATURE,
                                           false);

        if (mInput == null) {
            return false;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        if (currentSelectedTaxon == null) {
            return false;
        }

        if (getArguments().getBoolean(KEY_AP,
                                      true)) {
            // clear the current selection
            if ((!TextUtils.isEmpty(currentSelectedTaxon.getCurrentSelectedAreaId()) && (currentSelectedTaxon.getCurrentSelectedAreaId()
                                                                                                             .equals(featureId)))) {
                currentSelectedTaxon.setCurrentSelectedAreaId(null);
            }

            currentSelectedTaxon.getAreas()
                                .remove(featureId);
            ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();

            return !currentSelectedTaxon.getAreas()
                                        .containsKey(featureId);
        }
        else {
            currentSelectedTaxon.setProspectingArea(null);
            ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();

            return true;
        }
    }

    @Override
    protected void loadControls() {
        Log.d(TAG,
              "loadControls AP: " + getArguments().getBoolean(KEY_AP,
                                                              true));

        final DrawControl drawControl = new DrawControl(getActivity());
        drawControl.setAddingSingleFeature(getArguments().getBoolean(KEY_SINGLE_FEATURE,
                                                                     true));
        drawControl.enableAddMarker(getArguments().getBoolean(KEY_ADD_MARKER,
                                                              true));
        drawControl.enableAddPath(getArguments().getBoolean(KEY_ADD_PATH,
                                                            true));
        drawControl.enableAddPolygon(getArguments().getBoolean(KEY_ADD_POLYGON,
                                                               true));
        drawControl.setFeatureDefaultStyle(FeatureStyle.Builder.newInstance(getContext())
                                                               .from(drawControl.getFeatureDefaultStyle())
                                                               .setColorResourceId(R.color.feature)
                                                               .setFillColorResourceId(R.color.feature)
                                                               .build());
        drawControl.setFeatureAddStyle(FeatureStyle.Builder.newInstance(getContext())
                                                           .from(drawControl.getFeatureAddStyle())
                                                           .setColorResourceId(R.color.feature_add)
                                                           .setFillColorResourceId(R.color.feature_add)
                                                           .build());
        drawControl.setFeatureEditStyle(FeatureStyle.Builder.newInstance(getContext())
                                                            .from(drawControl.getFeatureEditStyle())
                                                            .setColorResourceId(R.color.feature_edit)
                                                            .setFillColorResourceId(R.color.feature_edit)
                                                            .build());
        drawControl.setOnDrawControlListener(new DrawControl.OnDrawControlListener() {
            @Override
            public void onAddingFeature(boolean adding) {

                Log.d(getClass().getName(),
                      "onAddingFeature " + adding);

                getSavedInstanceState().putBoolean(KEY_EDITING_FEATURE,
                                                   adding);

                ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();
            }

            @Override
            public void onEditingFeature(boolean editing) {

                Log.d(getClass().getName(),
                      "onEditingFeature " + editing);

                getSavedInstanceState().putBoolean(KEY_EDITING_FEATURE,
                                                   editing);

                ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();
            }

            @Override
            public void onDeletingFeature(boolean deleting) {

                Log.d(getClass().getName(),
                      "onDeletingFeature " + deleting);

                getSavedInstanceState().putBoolean(KEY_EDITING_FEATURE,
                                                   deleting);

                ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();
            }
        });
        drawControl.addControlListener(new AbstractControl.OnIControlListener() {
            @Override
            public void onControlInitialized() {
                Log.d(getClass().getName(),
                      "onControlInitialized drawControl");

                if (getArguments().getBoolean(KEY_AP,
                                              true)) {
                    // clear all editable features if no area was edited yet.
                    if ((mInput != null) && (mInput.getCurrentSelectedTaxon() != null) && (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() == null)) {
                        clearEditableFeatures();
                    }

                    if (getEditableFeatures().isEmpty() && (mInput != null) && (mInput.getCurrentSelectedTaxon() != null) && ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) {
                        final Area currentSelectedArea = ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea();

                        if (currentSelectedArea != null && currentSelectedArea.getFeature() != null) {
                            setCurrentEditableFeature(currentSelectedArea.getFeature());
                            drawControl.setFeatures(Collections.singletonList(currentSelectedArea.getFeature()));
                        }
                    }
                }
                else {
                    if ((mInput != null) && (mInput.getCurrentSelectedTaxon() != null) && (((Taxon) mInput.getCurrentSelectedTaxon()).getProspectingArea() == null)) {
                        clearEditableFeatures();
                    }

                    if (getEditableFeatures().isEmpty() && (mInput != null) && (mInput.getCurrentSelectedTaxon() != null) && ((Taxon) mInput.getCurrentSelectedTaxon()).getProspectingArea() != null) {
                        final Feature prospectingArea = ((Taxon) mInput.getCurrentSelectedTaxon()).getProspectingArea();

                        if (prospectingArea != null) {
                            setCurrentEditableFeature(prospectingArea);
                            drawControl.setFeatures(Collections.singletonList(prospectingArea));
                        }
                    }
                }
            }
        });

        final FeaturesControl featuresControl = new FeaturesControl(getActivity());
        featuresControl.addControlListener(new AbstractControl.OnIControlListener() {
            @Override
            public void onControlInitialized() {
                Log.d(getClass().getName(),
                      "onControlInitialized displayAPs");

                invalidateMenu();
                featuresControl.clearFeatures();
                displayAPs(true);
            }
        });

        addControl(new ZoomControl(getActivity()),
                   mRightToolbarLayout);
        addControl(new CenterPositionControl(getActivity()),
                   mRightToolbarLayout);
        addControl(drawControl,
                   mLeftToolbarLayout);
        addControl(new SwitchLayersControl(getActivity()),
                   null);
        addControl(featuresControl,
                   null);
    }

    @Override
    protected File getTilesSourcePath() throws
                                        IOException {
        return FileUtils.getDatabaseFolder(getActivity(),
                                           MountPoint.StorageType.EXTERNAL);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id,
                                         Bundle args) {
        final String[] projection = {
                MainDatabaseHelper.ProspectingAreasColumns._ID,
                MainDatabaseHelper.ProspectingAreasColumns.TAXON_ID,
                MainDatabaseHelper.ProspectingAreasColumns.GEOMETRY
        };

        if (mInput.getCurrentSelectedTaxon() == null) {
            Log.w(TAG,
                  "onCreateLoader: no taxon selected !");

            return new CursorLoader(getActivity(),
                                    Uri.withAppendedPath(MainContentProvider.CONTENT_PROSPECTING_AREAS_TAXON_URI,
                                                         Long.toString(0)),
                                    projection,
                                    null,
                                    null,
                                    null);
        }
        else {
            return new CursorLoader(getActivity(),
                                    Uri.withAppendedPath(MainContentProvider.CONTENT_PROSPECTING_AREAS_TAXON_URI,
                                                         Long.toString(mInput.getCurrentSelectedTaxon()
                                                                             .getTaxonId())),
                                    projection,
                                    null,
                                    null,
                                    null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,
                               Cursor data) {
        if ((data != null) && data.moveToFirst()) {
            final List<Feature> features = new ArrayList<>();

            do {
                final String id = Long.toString(data.getLong(data.getColumnIndex(MainDatabaseHelper.ProspectingAreasColumns._ID)));
                final Geometry geometry = new GeoJsonReader().readGeometry(data.getString(data.getColumnIndex(MainDatabaseHelper.ProspectingAreasColumns.GEOMETRY)));

                if (geometry == null) {
                    Log.w(TAG,
                          "onLoadFinished, invalid Geometry for Feature " + id);
                }

                if (geometry != null) {
                    features.add(new Feature(id,
                                             geometry));
                }
            }
            while (data.moveToNext());

            displayPAs(features);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to do ...
    }

    protected void displayAPs(boolean fitBounds) {
        if (mInput == null) {
            return;
        }

        if (!getArguments().getBoolean(KEY_AP,
                                       true)) {
            Log.d(TAG,
                  "displayAPs");

            // this fragment may not be attached to the current activity
            if (isAdded()) {
                final String featureControlName = ControlUtils.getControlName(FeaturesControl.class);

                if (!hasControl(featureControlName)) {
                    Log.w(TAG,
                          "displayAPs: FeaturesControl not found");

                    return;
                }

                final List<Feature> featuresAreas = new ArrayList<>();
                final FeaturesControl featuresControl = (FeaturesControl) getControl(featureControlName);

                if (hasControl(featureControlName)) {
                    featuresControl.clearFeatures();
                }

                if ((mInput.getCurrentSelectedTaxon() != null) && (!((Taxon) mInput.getCurrentSelectedTaxon()).getAreas()
                                                                                                              .isEmpty())) {
                    for (Area area : ((Taxon) mInput.getCurrentSelectedTaxon()).getAreas()
                                                                               .values()) {
                        featuresAreas.add(area.getFeature());
                    }
                }

                if (!featuresAreas.isEmpty()) {
                    if (hasControl(featureControlName)) {
                        featuresControl.addFeatures(featuresAreas,
                                                    FeatureStyle.Builder.newInstance(getContext())
                                                                        .setColorResourceId(R.color.feature_ap)
                                                                        .setFillColorResourceId(R.color.feature_ap)
                                                                        .build(),
                                                    fitBounds);
                    }
                }
            }
        }
    }

    private void displayPAs(List<Feature> features) {
        final String featureControlName = ControlUtils.getControlName(FeaturesControl.class);

        if (!features.isEmpty() && hasControl(featureControlName)) {
            Log.d(TAG,
                  "displayPAs");

            ((FeaturesControl) getControl(featureControlName)).addFeatures(features,
                                                                           FeatureStyle.Builder.newInstance(getContext())
                                                                                               .setColorResourceId(R.color.feature_pa)
                                                                                               .setFillColorResourceId(R.color.feature_pa)
                                                                                               .build(),
                                                                           false);
        }
    }

    private void clearEditableFeatures() {
        final String drawControlName = ControlUtils.getControlName(DrawControl.class);

        if (hasControl(drawControlName)) {
            ((DrawControl) getControl(drawControlName)).clearFeatures();
        }
    }

    private boolean checkIfProspectingAreaContainsAllAreasPresences(final Feature selectedFeature) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "checkIfProspectingAreaContainsAllAreasPresences for feature: " + selectedFeature.getId());
        }

        if (mInput == null) {
            return false;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        if (currentSelectedTaxon == null) {
            return false;
        }

        boolean check = true;
        final Iterator<Area> iterator = currentSelectedTaxon.getAreas()
                                                            .values()
                                                            .iterator();

        while (check && iterator.hasNext()) {
            final Area area = iterator.next();
            check = (area.getFeature() != null) && selectedFeature.getGeometry()
                                                                  .contains(area.getFeature()
                                                                                .getGeometry());
        }

        return check;
    }
}
