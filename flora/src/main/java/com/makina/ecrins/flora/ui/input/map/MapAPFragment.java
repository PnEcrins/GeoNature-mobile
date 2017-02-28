package com.makina.ecrins.flora.ui.input.map;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.makina.ecrins.flora.BuildConfig;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Area;
import com.makina.ecrins.flora.input.Taxon;
import com.makina.ecrins.maps.control.DrawControl;
import com.makina.ecrins.maps.control.FeaturesControl;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Edit an area geometry on the map.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class MapAPFragment
        extends AbstractMapFragment {

    private static final String TAG = MapAPFragment.class.getName();

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_webview_ap_title;
    }

    @Override
    public boolean validate() {
        return (mInput != null) && (mInput.getCurrentSelectedTaxon() != null) &&
                (((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) && (!getSavedInstanceState().getBoolean(KEY_EDITING_FEATURE,
                                                                                                                                      false));
    }

    @Override
    public void refreshView() {
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

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "refreshView");
        }

        validateCurrentPage();

        displayAPs(true);

        // clear all editable features if no area was edited yet
        if (currentSelectedTaxon.getCurrentSelectedArea() == null) {
            clearEditableFeatures();
        }

        if (getEditableFeatures().isEmpty() && (currentSelectedTaxon.getCurrentSelectedArea() != null)) {
            final Area currentSelectedArea = currentSelectedTaxon.getCurrentSelectedArea();

            if (currentSelectedArea != null && currentSelectedArea.getFeature() != null) {
                setCurrentEditableFeature(currentSelectedArea.getFeature());

                final DrawControl drawControl = getDrawControl();

                if (drawControl != null) {
                    drawControl.setFeatures(Collections.singletonList(currentSelectedArea.getFeature()));
                }
            }
        }
    }

    @Override
    protected void onDrawControlInitialized(@NonNull DrawControl drawControl) {
        if (mInput == null) {
            Log.w(TAG,
                  "onDrawControlInitialized: null input");
            return;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        if (currentSelectedTaxon == null) {
            Log.w(TAG,
                  "onDrawControlInitialized: no selected taxon found");

            return;
        }

        // clear all editable features if no area was edited yet.
        if (currentSelectedTaxon.getCurrentSelectedArea() == null) {
            clearEditableFeatures();
        }

        if (getEditableFeatures().isEmpty() && (currentSelectedTaxon.getCurrentSelectedArea() != null)) {
            final Area currentSelectedArea = currentSelectedTaxon.getCurrentSelectedArea();

            if (currentSelectedArea != null && currentSelectedArea.getFeature() != null) {
                setCurrentEditableFeature(currentSelectedArea.getFeature());
                drawControl.setFeatures(Collections.singletonList(currentSelectedArea.getFeature()));
            }
        }
    }

    @Override
    protected void onFeatureControlInitialized(@NonNull FeaturesControl featuresControl) {
        displayAPs(true);
    }

    @Override
    protected boolean addMarkerEnabled() {
        return true;
    }

    @Override
    protected boolean addPathEnabled() {
        return true;
    }

    @Override
    protected boolean addPolygonEnabled() {
        return true;
    }

    @Override
    protected void onShowCollectionPAs(boolean show) {
        // nothing to do ...
    }

    @Override
    public void setCurrentEditableFeature(@Nullable final Feature selectedFeature) {
        super.setCurrentEditableFeature(selectedFeature);

        if (selectedFeature == null) {
            return;
        }

        if (mInput == null) {
            return;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        if (currentSelectedTaxon == null) {
            return;
        }

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
            validateCurrentPage();

            return currentSelectedTaxon.getAreas()
                                       .containsKey(selectedFeature.getId());
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

            validateCurrentPage();

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

        // clear the current selection
        if ((!TextUtils.isEmpty(currentSelectedTaxon.getCurrentSelectedAreaId()) && (currentSelectedTaxon.getCurrentSelectedAreaId()
                                                                                                         .equals(featureId)))) {
            currentSelectedTaxon.setCurrentSelectedAreaId(null);
        }

        currentSelectedTaxon.getAreas()
                            .remove(featureId);
        validateCurrentPage();

        return !currentSelectedTaxon.getAreas()
                                    .containsKey(featureId);
    }

    private void displayAPs(boolean fitBounds) {
        if (mInput == null) {
            return;
        }

        clearFeatures();

        final List<Feature> featuresAreas = new ArrayList<>();

        if ((mInput.getCurrentSelectedTaxon() != null) && (!((Taxon) mInput.getCurrentSelectedTaxon()).getAreas()
                                                                                                      .isEmpty())) {
            final Area currentArea = ((Taxon) mInput.getCurrentSelectedTaxon()).getCurrentSelectedArea();

            for (Area area : ((Taxon) mInput.getCurrentSelectedTaxon()).getAreas()
                                                                       .values()) {
                if (area.getFeature() == null) {
                    continue;
                }

                if (currentArea == null || currentArea.getFeature() == null || !currentArea.getFeature().getId().equals(area.getFeature().getId())) {
                    featuresAreas.add(area.getFeature());
                }
            }
        }

        showFeatures(featuresAreas,
                     FeatureStyle.Builder.newInstance(getContext())
                                         .setColorResourceId(R.color.feature_ap)
                                         .setFillColorResourceId(R.color.feature_ap)
                                         .build(),
                     fitBounds);
    }
}
