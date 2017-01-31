package com.makina.ecrins.flora.ui.input.map;

import android.support.annotation.NonNull;
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
import java.util.Iterator;
import java.util.List;

/**
 * Edit the prospecting area on the map.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class MapPAFragment
        extends AbstractMapFragment {

    private static final String TAG = MapPAFragment.class.getName();

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_webview_pa_title;
    }

    @Override
    public boolean validate() {
        return (mInput != null) && (mInput.getCurrentSelectedTaxon() != null) &&
                (((Taxon) mInput.getCurrentSelectedTaxon()).getProspectingArea() != null) &&
                (!getSavedInstanceState().getBoolean(KEY_EDITING_FEATURE,
                                                     false)) &&
                checkIfProspectingAreaContainsAllAreasPresences(((Taxon) mInput.getCurrentSelectedTaxon()).getProspectingArea());
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

        if (currentSelectedTaxon.getProspectingArea() == null) {
            clearEditableFeatures();
        }
        else {
            final Feature prospectingArea = currentSelectedTaxon.getProspectingArea();

            if (getEditableFeatures().isEmpty() && prospectingArea != null) {
                setCurrentEditableFeature(prospectingArea);

                final DrawControl drawControl = getDrawControl();

                if (drawControl != null) {
                    drawControl.setFeatures(Collections.singletonList(prospectingArea));
                }
            }

            // checks if this feature contains all features added to this taxon areas
            if ((prospectingArea != null) && !checkIfProspectingAreaContainsAllAreasPresences(prospectingArea)) {
                Log.d(TAG,
                      "feature '" + currentSelectedTaxon.getProspectingArea()
                                                        .getId() + "' does not contains all previously added areas");

                Toast.makeText(getContext(),
                               R.string.message_pa_not_contains_all_aps,
                               Toast.LENGTH_LONG)
                     .show();
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

        if (currentSelectedTaxon.getProspectingArea() == null) {
            clearEditableFeatures();
        }
        else {
            final Feature prospectingArea = ((Taxon) mInput.getCurrentSelectedTaxon()).getProspectingArea();

            if (prospectingArea != null) {
                setCurrentEditableFeature(prospectingArea);
                drawControl.setFeatures(Collections.singletonList(prospectingArea));
            }
        }
    }

    @Override
    protected void onFeatureControlInitialized(@NonNull FeaturesControl featuresControl) {
        displayAPs(true);
    }

    @Override
    protected boolean addMarkerEnabled() {
        return false;
    }

    @Override
    protected boolean addPathEnabled() {
        return false;
    }

    @Override
    protected boolean addPolygonEnabled() {
        return true;
    }

    @Override
    protected void onShowCollectionPAs(boolean show) {
        if (!show) {
            displayAPs(false);
        }
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
            validateCurrentPage();

            return (currentSelectedTaxon.getProspectingArea() != null);
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

        currentSelectedTaxon.setProspectingArea(null);
        validateCurrentPage();

        return true;
    }

    private void displayAPs(boolean fitBounds) {
        if (mInput == null) {
            return;
        }

        clearFeatures();

        final List<Feature> featuresAreas = new ArrayList<>();

        if ((mInput.getCurrentSelectedTaxon() != null) && (!((Taxon) mInput.getCurrentSelectedTaxon()).getAreas()
                                                                                                      .isEmpty())) {
            for (Area area : ((Taxon) mInput.getCurrentSelectedTaxon()).getAreas()
                                                                       .values()) {
                featuresAreas.add(area.getFeature());
            }
        }

        showFeatures(featuresAreas,
                     FeatureStyle.Builder.newInstance(getContext())
                                         .setColorResourceId(R.color.feature_ap)
                                         .setFillColorResourceId(R.color.feature_ap)
                                         .build(),
                     fitBounds);
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
