package com.makina.ecrins.fauna.ui.input.map;

import android.util.Log;

import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.commons.util.FileUtils;
import com.makina.ecrins.fauna.MainApplication;
import com.makina.ecrins.fauna.R;
import com.makina.ecrins.fauna.ui.input.PagerFragmentActivity;
import com.makina.ecrins.maps.AbstractWebViewFragment;
import com.makina.ecrins.maps.MapSettings;
import com.makina.ecrins.maps.control.CenterPositionControl;
import com.makina.ecrins.maps.control.MenuUnitiesControl;
import com.makina.ecrins.maps.control.SwitchLayersControl;
import com.makina.ecrins.maps.control.ZoomControl;
import com.makina.ecrins.maps.geojson.Feature;
import com.makina.ecrins.maps.location.Geolocation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of a WebView Fragment embedding a WebView with Leaflet.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class WebViewFragment
        extends AbstractWebViewFragment
        implements IValidateFragment {

    @Override
    public void onResume() {
        super.onResume();

        reload();
    }

    @Override
    public int getResourceTitle() {

        return R.string.pager_fragment_webview_title;
    }

    @Override
    public boolean getPagingEnabled() {

        return false;
    }

    @Override
    public boolean validate() {

        boolean validate = (((MainApplication) getActivity().getApplication()).getInput()
                                                                              .getGeolocation() != null);

        Log.d(getClass().getName(),
              "validate : " + validate);

        return validate;
    }

    @Override
    public void refreshView() {

        Log.d(getClass().getName(),
              "refreshView");
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
    public List<Feature> getFeatures() {
        // may be null
        if (getActivity() == null) {
            return new ArrayList<>();
        }
        else {
            return ((MainApplication) getActivity().getApplication()).getUnities();
        }
    }

    @Override
    public void setSelectedFeature(
            Geolocation geolocation,
            Feature selectedFeature) {

        ((MainApplication) WebViewFragment.this.getActivity()
                                               .getApplication()).getInput()
                                                                 .setGeolocation(geolocation);
        ((MainApplication) WebViewFragment.this.getActivity()
                                               .getApplication()).getInput()
                                                                 .setFeatureId((selectedFeature == null) ? null : selectedFeature.getId());

        ((PagerFragmentActivity) WebViewFragment.this.getActivity()).validateCurrentPage();
    }

    @Override
    protected void loadControls() {

        addControl(new ZoomControl(getActivity()),
                   mRightToolbarLayout);
        addControl(new CenterPositionControl(getActivity()),
                   mRightToolbarLayout);
        addControl(new MenuUnitiesControl(getActivity()),
                   null);
        addControl(new SwitchLayersControl(getActivity()),
                   null);
    }

    @Override
    protected File getTilesSourcePath() throws IOException {

        return FileUtils.getFileFromApplicationStorage(getActivity(),
                                                       "databases");
    }
}
