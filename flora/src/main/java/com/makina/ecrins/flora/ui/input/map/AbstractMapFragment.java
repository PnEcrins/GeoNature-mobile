package com.makina.ecrins.flora.ui.input.map;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.model.MountPoint;
import com.makina.ecrins.commons.ui.input.IInputFragment;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.commons.util.FileUtils;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.content.MainContentProvider;
import com.makina.ecrins.flora.input.Input;
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
import java.util.List;

/**
 * Basic implementation of a WebView Fragment embedding a WebView with Leaflet.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public abstract class AbstractMapFragment
        extends AbstractWebViewFragment
        implements IValidateFragment,
                   IInputFragment,
                   LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = AbstractMapFragment.class.getName();

    protected static final String KEY_EDITING_FEATURE = "KEY_EDITING_FEATURE";

    protected Input mInput;
    private boolean mIsActionMarkerCollectionPAsSelected = false;

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
        final FeaturesControl featuresControl = getFeaturesControl();

        if (featuresControl == null || !featuresControl.isControlInitialized()) {
            itemMarker.setEnabled(false);
        }
        else {
            itemMarker.setEnabled(true);
            itemMarker.setIcon((this.mIsActionMarkerCollectionPAsSelected) ? R.drawable.ic_action_collection_pa_selected : R.drawable.ic_action_collection_pa);
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
                    final FeaturesControl featuresControl = getFeaturesControl();

                    if (featuresControl != null) {
                        featuresControl.clearFeatures();
                    }
                }

                onShowCollectionPAs(this.mIsActionMarkerCollectionPAsSelected);

                invalidateMenu();
                return true;
            default:
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

    @Override
    protected void loadControls() {
        final Context context = getContext();

        if (context == null) {
            Log.w(TAG,
                  "loadControls: context null");

            return;
        }

        final DrawControl drawControl = new DrawControl(context);
        drawControl.setAddingSingleFeature(true);
        drawControl.enableAddMarker(addMarkerEnabled());
        drawControl.enableAddPath(addPathEnabled());
        drawControl.enableAddPolygon(addPolygonEnabled());
        drawControl.setFeatureDefaultStyle(FeatureStyle.Builder.newInstance(context)
                                                               .from(drawControl.getFeatureDefaultStyle())
                                                               .setColorResourceId(R.color.feature)
                                                               .setFillColorResourceId(R.color.feature)
                                                               .build());
        drawControl.setFeatureAddStyle(FeatureStyle.Builder.newInstance(context)
                                                           .from(drawControl.getFeatureAddStyle())
                                                           .setColorResourceId(R.color.feature_add)
                                                           .setFillColorResourceId(R.color.feature_add)
                                                           .build());
        drawControl.setFeatureEditStyle(FeatureStyle.Builder.newInstance(context)
                                                            .from(drawControl.getFeatureEditStyle())
                                                            .setColorResourceId(R.color.feature_edit)
                                                            .setFillColorResourceId(R.color.feature_edit)
                                                            .build());
        drawControl.setOnDrawControlListener(new DrawControl.OnDrawControlListener() {
            @Override
            public void onAddingFeature(boolean adding) {
                getSavedInstanceState().putBoolean(KEY_EDITING_FEATURE,
                                                   adding);

                validateCurrentPage();
            }

            @Override
            public void onEditingFeature(boolean editing) {
                getSavedInstanceState().putBoolean(KEY_EDITING_FEATURE,
                                                   editing);

                validateCurrentPage();
            }

            @Override
            public void onDeletingFeature(boolean deleting) {
                getSavedInstanceState().putBoolean(KEY_EDITING_FEATURE,
                                                   deleting);

                validateCurrentPage();
            }
        });
        drawControl.addControlListener(new AbstractControl.OnIControlListener() {
            @Override
            public void onControlInitialized() {
                onDrawControlInitialized(drawControl);
            }
        });

        final FeaturesControl featuresControl = new FeaturesControl(context);
        featuresControl.addControlListener(new AbstractControl.OnIControlListener() {
            @Override
            public void onControlInitialized() {
                invalidateMenu();
                featuresControl.clearFeatures();
                onFeatureControlInitialized(featuresControl);
            }
        });

        addControl(new ZoomControl(context),
                   mRightToolbarLayout);
        addControl(new CenterPositionControl(context),
                   mRightToolbarLayout);
        addControl(drawControl,
                   mLeftToolbarLayout);
        addControl(new SwitchLayersControl(context),
                   null);
        addControl(featuresControl,
                   null);
    }

    @Override
    protected File getTilesSourcePath() throws
                                        IOException {
        return FileUtils.getDatabaseFolder(getContext(),
                                           MountPoint.StorageType.EXTERNAL);
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
    public boolean getPagingEnabled() {
        return false;
    }

    @Override
    public void setInput(@NonNull AbstractInput input) {
        this.mInput = (Input) input;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id,
                                         Bundle args) {
        final Context context = getContext();

        if (context == null) {
            Log.w(TAG,
                  "onCreateLoader: context null");

            return null;
        }

        final String[] projection = {
                MainDatabaseHelper.ProspectingAreasColumns._ID,
                MainDatabaseHelper.ProspectingAreasColumns.TAXON_ID,
                MainDatabaseHelper.ProspectingAreasColumns.GEOMETRY
        };

        if (mInput.getCurrentSelectedTaxon() == null) {
            Log.w(TAG,
                  "onCreateLoader: no taxon selected !");

            return new CursorLoader(context,
                                    Uri.withAppendedPath(MainContentProvider.CONTENT_PROSPECTING_AREAS_TAXON_URI,
                                                         Long.toString(0)),
                                    projection,
                                    null,
                                    null,
                                    null);
        }
        else {
            return new CursorLoader(context,
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

            clearFeatures();
            showFeatures(features,
                         FeatureStyle.Builder.newInstance(getContext())
                                             .setColorResourceId(R.color.feature_pa)
                                             .setFillColorResourceId(R.color.feature_pa)
                                             .build(),
                         false);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to do ...
    }

    @Nullable
    protected FeaturesControl getFeaturesControl() {
        return (FeaturesControl) getControl(ControlUtils.getControlName(FeaturesControl.class));
    }

    protected void clearFeatures() {
        final FeaturesControl featuresControl = getFeaturesControl();

        if (featuresControl == null) {
            Log.w(TAG,
                  "clearFeatures: FeaturesControl not found");

            return;
        }

        featuresControl.clearFeatures();
    }

    protected void showFeatures(@NonNull final List<Feature> features,
                                @NonNull final FeatureStyle featureStyle,
                                boolean fitBounds) {
        if (features.isEmpty()) {
            Log.d(TAG,
                  "showFeatures: no features to display");

            return;
        }

        final FeaturesControl featuresControl = getFeaturesControl();

        if (featuresControl == null) {
            Log.w(TAG,
                  "showFeatures: FeaturesControl not found");

            return;
        }

        featuresControl.addFeatures(features,
                                    featureStyle,
                                    fitBounds);
    }

    @Nullable
    protected DrawControl getDrawControl() {
        return (DrawControl) getControl(ControlUtils.getControlName(DrawControl.class));
    }

    protected void clearEditableFeatures() {
        final DrawControl drawControl = (DrawControl) getControl(ControlUtils.getControlName(DrawControl.class));

        if (drawControl == null) {
            Log.w(TAG,
                  "clearEditableFeatures: DrawControl not found");

            return;
        }

        drawControl.clearFeatures();
    }

    protected void validateCurrentPage() {
        final Activity activity = getActivity();

        if (activity != null) {
            ((PagerFragmentActivity) activity).validateCurrentPage();
        }
    }

    protected abstract void onDrawControlInitialized(@NonNull final DrawControl drawControl);

    protected abstract void onFeatureControlInitialized(@NonNull final FeaturesControl featuresControl);

    protected abstract boolean addMarkerEnabled();

    protected abstract boolean addPathEnabled();

    protected abstract boolean addPolygonEnabled();

    protected abstract void onShowCollectionPAs(boolean show);
}
