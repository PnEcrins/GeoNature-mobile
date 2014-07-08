package com.makina.ecrins.flora.ui.input.map;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.commons.util.FileUtils;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.content.MainContentProvider;
import com.makina.ecrins.flora.input.Area;
import com.makina.ecrins.flora.input.Taxon;
import com.makina.ecrins.flora.ui.input.PagerFragmentActivity;
import com.makina.ecrins.maps.AbstractWebViewFragment;
import com.makina.ecrins.maps.MapSettings;
import com.makina.ecrins.maps.control.AbstractControl;
import com.makina.ecrins.maps.control.CenterPositionControl;
import com.makina.ecrins.maps.control.ControlUtils;
import com.makina.ecrins.maps.control.DrawControl;
import com.makina.ecrins.maps.control.FeaturesControl;
import com.makina.ecrins.maps.control.SwitchLayersControl;
import com.makina.ecrins.maps.control.ZoomControl;
import com.makina.ecrins.maps.geojson.Feature;
import com.makina.ecrins.maps.geojson.FeatureStyle;
import com.makina.ecrins.maps.geojson.geometry.GeometryUtils;
import com.makina.ecrins.maps.geojson.geometry.IGeometry;
import com.makina.ecrins.maps.location.Geolocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Basic implementation of a WebView Fragment embedding a WebView with Leaflet.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class WebViewFragment extends AbstractWebViewFragment
        implements
        IValidateFragment,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = WebViewFragment.class.getName();

    public static final String KEY_AP = "ap";
    public static final String KEY_SINGLE_FEATURE = "single_feature";
    public static final String KEY_ADD_MARKER = "add_marker";
    public static final String KEY_ADD_PATH = "add_path";
    public static final String KEY_ADD_POLYGON = "add_polygon";
    public static final String KEY_SELECTED_TAXON_ID = "taxon_id";
    public static final String KEY_EDITING_FEATURE = "editing_feature";

    protected boolean mIsActionMarkerCollectionPAsSelected = false;

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_collection_pa, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        final MenuItem itemMarker = menu.findItem(R.id.itemCollectionPAs);

        final String featureControlName = ControlUtils.getControlName(FeaturesControl.class);

        if (hasControl(featureControlName) && ((FeaturesControl) getControl(featureControlName))
                .isControlInitialized()) {
            itemMarker.setEnabled(true);
            itemMarker.setIcon(
                    (this.mIsActionMarkerCollectionPAsSelected) ? R.drawable.ic_action_collection_pa_selected : R.drawable.ic_action_collection_pa);
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
                    getLoaderManager().restartLoader(0, null, this);
                }
                else {
                    final String featureControlName = ControlUtils.getControlName(FeaturesControl.class);

                    if (hasControl(featureControlName)) {
                        ((FeaturesControl) getControl(featureControlName)).clearFeatures();
                    }

                    if (!getArguments().getBoolean(KEY_AP, true)) {
                        displayAPs();
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
        if (getArguments().getBoolean(KEY_AP, true)) {
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
        Log.d(TAG, "validate KEY_EDITING_FEATURE: " +
                getSavedInstanceState().getBoolean(KEY_EDITING_FEATURE, false));

        if (getArguments().getBoolean(KEY_AP, true)) {
            return (((MainApplication) getActivity().getApplication()).getInput().getCurrentSelectedTaxon() != null) &&
                    (((Taxon) ((MainApplication) getActivity().getApplication()).getInput().getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                    (!getSavedInstanceState().getBoolean(KEY_EDITING_FEATURE, false));
        }
        else {
            return (((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon() != null) &&
                    (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getProspectingArea() != null) &&
                    (!getSavedInstanceState().getBoolean(KEY_EDITING_FEATURE, false)) &&
                    checkIfProspectingAreaContainsAllAreasPresences(((Taxon) ((MainApplication) getActivity()
                            .getApplication()).getInput().getCurrentSelectedTaxon())
                            .getProspectingArea());
        }
    }

    @Override
    public void refreshView() {
        Log.d(TAG, "refreshView, AP: " + getArguments().getBoolean(KEY_AP, true));

        if (getArguments().getBoolean(KEY_AP, true)) {
            // clear all editable features if no area was edited yet.
            if ((((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon() != null) &&
                    (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedArea() == null)) {
                getEditableFeatures().clearAllFeatures();
            }
        }
        else {
            if ((((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon() != null) &&
                    (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getProspectingArea() == null)) {
                getEditableFeatures().clearAllFeatures();
            }
            else {
                // checks if this feature contains all features added to this taxon areas
                if (!checkIfProspectingAreaContainsAllAreasPresences(((Taxon) ((MainApplication) getActivity()
                        .getApplication()).getInput().getCurrentSelectedTaxon())
                        .getProspectingArea())) {
                    Log.d(TAG, "feature '" + ((Taxon) ((MainApplication) getActivity()
                            .getApplication()).getInput().getCurrentSelectedTaxon())
                            .getProspectingArea()
                            .getId() + "' does not contains all previously added areas");

                    Toast.makeText(
                            getActivity(),
                            R.string.message_pa_not_contains_all_aps,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public MapSettings getMapSettings() {
        MapSettings mapSettings = super.getMapSettings();

        if (mapSettings == null) {
            mapSettings = ((MainApplication) getActivity().getApplication()).getAppSettings().getMapSettings();
            setMapSettings(mapSettings);
        }

        return mapSettings;
    }

    @Override
    public List<Feature> getFeatures() {
        // no features to load
        return new ArrayList<Feature>();
    }

    @Override
    public void setSelectedFeature(Geolocation geolocation, Feature selectedFeature) {
        // nothing to do ...
    }

    @Override
    public boolean addOrUpdateEditableFeature(Feature selectedFeature) {
        if (super.addOrUpdateEditableFeature(selectedFeature) && !getEditableFeatures()
                .getFeatures().isEmpty() && (((MainApplication) getActivity().getApplication())
                .getInput().getCurrentSelectedTaxon() != null)) {
            Log.d(TAG,
                    "addOrUpdateEditableFeature: " +
                            selectedFeature.getGeometry().getType().name() +
                            ", id: " +
                            selectedFeature.getId());

            getSavedInstanceState().putBoolean(KEY_EDITING_FEATURE, false);

            if (getArguments().getBoolean(KEY_AP, true)) {
                // delete a previously added area if needed
                if ((((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                        (!((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getCurrentSelectedAreaId()
                                .equals(selectedFeature.getId()))) {
                    ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getAreas()
                            .remove(((Taxon) ((MainApplication) getActivity().getApplication())
                                    .getInput().getCurrentSelectedTaxon())
                                    .getCurrentSelectedAreaId());
                }

                // add or update the current area
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getAreas()
                        .put(selectedFeature.getId(), new Area(selectedFeature));
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon())
                        .setCurrentSelectedAreaId(selectedFeature.getId());

                ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();

                return ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getAreas().containsKey(selectedFeature.getId());
            }
            else {
                if (!((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getAreas().isEmpty()) {
                    // checks if this feature contains all features added to this taxon areas
                    if (!checkIfProspectingAreaContainsAllAreasPresences(selectedFeature)) {
                        Log.d(TAG,
                                "feature '" +
                                        selectedFeature.getId() +
                                        "' does not contains all previously added areas");

                        Toast.makeText(
                                getActivity(),
                                R.string.message_pa_not_contains_all_aps,
                                Toast.LENGTH_LONG).show();
                    }
                }

                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).setProspectingArea(selectedFeature);
                ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();

                return (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getProspectingArea() != null);
            }
        }
        else {
            if ((selectedFeature.getGeometry() == null) ||
                    (!GeometryUtils.isValid(selectedFeature.getGeometry()))) {
                // add this invalid feature to be edited by the user
                getEditableFeatures().addFeature(selectedFeature);
                Toast.makeText(
                        getActivity(),
                        com.makina.ecrins.maps.R.string.message_feature_invalid,
                        Toast.LENGTH_SHORT).show();
            }

            ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();

            return false;
        }
    }

    @Override
    public boolean deleteEditableFeature(String featureId) {
        super.deleteEditableFeature(featureId);

        getSavedInstanceState().putBoolean(KEY_EDITING_FEATURE, false);

        if (getArguments().getBoolean(KEY_AP, true)) {
            // clear the current selection
            if ((((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                    (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedAreaId()
                            .equals(featureId))) {
                ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).setCurrentSelectedAreaId(null);
            }

            ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getAreas().remove(featureId);

            ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();

            return !((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getAreas().containsKey(featureId);
        }
        else {
            ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).setProspectingArea(null);

            ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();

            return true;
        }
    }

    @Override
    protected void loadControls() {
        Log.d(TAG, "loadControls AP: " + getArguments().getBoolean(KEY_AP, true));

        // clear all editable features if no area was edited yet.
        if (getArguments().getBoolean(KEY_AP, true)) {
            if ((((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon() != null) &&
                    (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedArea() == null)) {
                Log.d(TAG, "clear all editable features (AP)");

                getEditableFeatures().clearAllFeatures();
            }
        }
        else {
            if ((((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon() != null) &&
                    (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getProspectingArea() == null)) {
                Log.d(TAG, "clear all editable features (PA)");

                getEditableFeatures().clearAllFeatures();
            }
        }

        final DrawControl drawControl = new DrawControl(getActivity());

        drawControl.setAddingSingleFeature(getArguments().getBoolean(KEY_SINGLE_FEATURE, true));
        drawControl.enableAddMarker(getArguments().getBoolean(KEY_ADD_MARKER, true));
        drawControl.enableAddPath(getArguments().getBoolean(KEY_ADD_PATH, true));
        drawControl.enableAddPolygon(getArguments().getBoolean(KEY_ADD_POLYGON, true));
        drawControl.setFeatureDefaultStyle(drawControl.getFeatureDefaultStyle()
                .setColorResourceId(R.color.feature)
                .setFillColorResourceId(R.color.feature));
        drawControl.setFeatureAddStyle(drawControl.getFeatureAddStyle()
                .setColorResourceId(R.color.feature_add)
                .setFillColorResourceId(R.color.feature_add));
        drawControl.setFeatureEditStyle(drawControl.getFeatureEditStyle()
                .setColorResourceId(R.color.feature_edit)
                .setFillColorResourceId(R.color.feature_edit));

        drawControl.addControlListener(new AbstractControl.OnIControlListener() {
            @Override
            public void onControlInitialized() {
                Log.d(getClass().getName(), "onControlInitialized drawControl");

                drawControl.setOnDrawControlListener(new DrawControl.OnDrawControlListener() {
                    @Override
                    public void onAddingFeature(boolean adding) {
                        Log.d(getClass().getName(), "onAddingFeature " + adding);

                        getSavedInstanceState().putBoolean(KEY_EDITING_FEATURE, adding);

                        ((PagerFragmentActivity) WebViewFragment.this.getActivity())
                                .validateCurrentPage();
                    }

                    @Override
                    public void onEditingFeature(boolean editing) {
                        Log.d(getClass().getName(), "onEditingFeature " + editing);

                        getSavedInstanceState().putBoolean(KEY_EDITING_FEATURE, editing);

                        ((PagerFragmentActivity) WebViewFragment.this.getActivity())
                                .validateCurrentPage();
                    }

                    @Override
                    public void onDeletingFeature(boolean deleting) {
                        Log.d(getClass().getName(), "onDeletingFeature " + deleting);

                        getSavedInstanceState().putBoolean(KEY_EDITING_FEATURE, deleting);

                        ((PagerFragmentActivity) WebViewFragment.this.getActivity())
                                .validateCurrentPage();
                    }
                });
            }
        });

        final FeaturesControl featuresControl = new FeaturesControl(getActivity());
        featuresControl.addControlListener(new AbstractControl.OnIControlListener() {
            @Override
            public void onControlInitialized() {
                Log.d(getClass().getName(), "onControlInitialized displayAPs");

                invalidateMenu();
                featuresControl.clearFeatures();
                displayAPs();
            }
        });

        addControl(new ZoomControl(getActivity()), mRightToolbarLayout);
        addControl(new CenterPositionControl(getActivity()), mRightToolbarLayout);
        addControl(drawControl, mLeftToolbarLayout);
        addControl(new SwitchLayersControl(getActivity()), null);
        addControl(featuresControl, null);
    }

    @Override
    protected File getTilesSourcePath() throws IOException {
        return FileUtils.getFileFromApplicationStorage(getActivity(), "databases");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] projection = {
                        MainDatabaseHelper.ProspectingAreasColumns._ID,
                        MainDatabaseHelper.ProspectingAreasColumns.TAXON_ID,
                        MainDatabaseHelper.ProspectingAreasColumns.GEOMETRY
        };

        if (((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() == null) {
            Log.w(TAG, "onCreateLoader: no taxon selected !");

            return new CursorLoader(
                    getActivity(),
                    Uri.withAppendedPath(
                            MainContentProvider.CONTENT_PROSPECTING_AREAS_TAXON_URI,
                            Long.toString(0)),
                    projection,
                    null,
                    null,
                    null);
        }
        else {
            return new CursorLoader(
                    getActivity(),
                    Uri.withAppendedPath(
                            MainContentProvider.CONTENT_PROSPECTING_AREAS_TAXON_URI,
                            Long.toString(((MainApplication) getActivity().getApplication())
                                    .getInput()
                                    .getCurrentSelectedTaxon()
                                    .getTaxonId())),
                    projection,
                    null,
                    null,
                    null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if ((data != null) && data.moveToFirst()) {
            final List<Feature> features = new ArrayList<Feature>();

            do {
                try {
                    features.add(
                            createFeature(
                                    Long.toString(
                                            data.getLong(
                                                    data.getColumnIndex(MainDatabaseHelper.ProspectingAreasColumns._ID))),
                                    GeometryUtils.createGeometryFromJson(
                                            new JSONObject(
                                                    data.getString(
                                                            data.getColumnIndex(MainDatabaseHelper.ProspectingAreasColumns.GEOMETRY))))));
                }
                catch (JSONException je) {
                    Log.w(TAG, je.getMessage());
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

    protected void displayAPs() {
        if (!getArguments().getBoolean(KEY_AP, true)) {
            Log.d(TAG, "displayAPs");

            // this fragment may not be attached to the current activity
            if (isAdded()) {
                final String featureControlName = ControlUtils
                        .getControlName(FeaturesControl.class);
                final List<Feature> featuresAreas = new ArrayList<Feature>();

                if ((((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon() != null) &&
                        (!((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getAreas().isEmpty())) {
                    if (hasControl(featureControlName)) {
                        ((FeaturesControl) getControl(featureControlName)).clearFeatures();
                    }

                    for (Area area : ((Taxon) ((MainApplication) getActivity().getApplication())
                            .getInput().getCurrentSelectedTaxon()).getAreas().values()) {
                        featuresAreas.add(area.getFeature());
                    }
                }

                if (!featuresAreas.isEmpty()) {
                    if (hasControl(featureControlName)) {
                        ((FeaturesControl) getControl(featureControlName))
                                .addFeatures(featuresAreas, new FeatureStyle()
                                        .setColorResourceId(R.color.feature_ap)
                                        .setFillColorResourceId(R.color.feature_ap), false);
                    }
                }
            }
        }
    }

    private void displayPAs(List<Feature> features) {
        final String featureControlName = ControlUtils.getControlName(FeaturesControl.class);

        if (!features.isEmpty() && hasControl(featureControlName)) {
            Log.d(TAG, "displayPAs");

            ((FeaturesControl) getControl(featureControlName))
                    .addFeatures(features, new FeatureStyle().setColorResourceId(R.color.feature_pa)
                            .setFillColorResourceId(R.color.feature_pa), false);
        }
    }

    private Feature createFeature(String id, IGeometry geometry) {
        Feature feature = new Feature(id);
        feature.setGeometry(geometry);

        return feature;
    }

    private boolean checkIfProspectingAreaContainsAllAreasPresences(final Feature selectedFeature) {
        try {
            Log.d(TAG, "checkIfProspectingAreaContainsAllAreasPresences for feature : " +
                    selectedFeature.getJSONObject().toString());
        }
        catch (JSONException je) {
            Log.w(TAG, je.getMessage());
        }

        boolean check = true;
        Iterator<Area> iterator = ((Taxon) ((MainApplication) getActivity().getApplication())
                .getInput().getCurrentSelectedTaxon()).getAreas().values().iterator();

        while (check && iterator.hasNext()) {
            check = check && GeometryUtils.contains(
                    iterator.next().getFeature().getGeometry(),
                    selectedFeature.getGeometry());
        }

        return check;
    }
}
