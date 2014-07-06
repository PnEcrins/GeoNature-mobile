package com.makina.ecrins.maps.control;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.ArrayAdapter;

import com.makina.ecrins.maps.R;
import com.makina.ecrins.maps.IWebViewFragment;
import com.makina.ecrins.maps.LayerSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * Switch layers control using navigation mode as list.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SwitchLayersControl extends AbstractControl implements ActionBar.OnNavigationListener {

    private ArrayAdapter<LayerSettings> mlayersAdapter = null;

    /**
     * {@link SwitchLayersControl} as Singleton.
     */
    public SwitchLayersControl(Context pContext) {
        super(pContext);

        setControlListener(new OnIControlListener() {
            @Override
            public void onControlInitialized() {
                updateNavigationList();
            }
        });
    }

    @Override
    public View getView(boolean forceCreate) {
        return null;
    }

    @Override
    public void refresh() {
        super.refresh();

        updateNavigationList();
    }

    @Override
    public void add(IWebViewFragment webViewFragment) {
        super.add(webViewFragment);

        initializeJSController("js/Control.SwitchLayers.js", "new L.Control.SwitchLayers()");
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        LayerSettings selectedLayer = mlayersAdapter.getItem(itemPosition);
        this.mWebViewFragment.setSelectedLayer(selectedLayer);
        this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".refreshMap()");

        return true;
    }

    /**
     * Updates the navigation list according to the level zoom.
     *
     * @param zoom the zoom level to set
     */
    @JavascriptInterface
    public void setZoom(final int zoom) {
        Log.d(getClass().getName(), "setZoom " + zoom);

        getHandler().post(new Runnable() {
            @Override
            public void run() {
                updateNavigationList();
            }
        });
    }

    protected void updateNavigationList() {
        Log.d(getClass().getName(), "updateNavigationList");

        if (isControlInitialized()) {
            List<LayerSettings> selectedLayers = new ArrayList<LayerSettings>();

            for (LayerSettings layerSettings : this.mWebViewFragment.getMapSettings()
                    .getLayers()) {
                if (this.mWebViewFragment.getTilesLayersDataSource(layerSettings.getName())
                        .getZooms()
                        .contains(this.mWebViewFragment.getMapSettings()
                                .getZoom())) {
                    selectedLayers.add(layerSettings);
                }
            }

            if ((this.mWebViewFragment.getActionBar() != null) && (this.mWebViewFragment.isMapVisibleToUser())) {
                mlayersAdapter = new ArrayAdapter<LayerSettings>(this.mWebViewFragment.getActionBar()
                        .getThemedContext(), R.layout.support_simple_spinner_dropdown_item, selectedLayers);
                mlayersAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

                this.mWebViewFragment.getActionBar()
                        .setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
                this.mWebViewFragment.getActionBar()
                        .setListNavigationCallbacks(mlayersAdapter, this);
                this.mWebViewFragment.getActionBar()
                        .setSelectedNavigationItem(selectedLayers.indexOf(mWebViewFragment.getSelectedLayer()));
            }
            else {
                Log.d(getClass().getName(), "updateNavigationList, not ready");
            }
        }
    }
}
