package com.makina.ecrins.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.makina.ecrins.maps.content.ITilesLayerDataSource;
import com.makina.ecrins.maps.content.TilesLayerDataSourceFactory;
import com.makina.ecrins.maps.control.IControl;
import com.makina.ecrins.maps.control.MainControl;
import com.makina.ecrins.maps.jts.geojson.Feature;
import com.makina.ecrins.maps.jts.geojson.FeatureCollection;
import com.makina.ecrins.maps.location.MockLocationProvider;
import com.makina.ecrins.maps.settings.LayerSettings;
import com.makina.ecrins.maps.settings.MapSettings;
import com.makina.ecrins.maps.util.DebugUtils;
import com.makina.ecrins.maps.util.PermissionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Basic implementation of a WebView Fragment embedding a WebView with Leaflet.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractWebViewFragment
        extends Fragment
        implements IWebViewFragment {

    private static final String TAG = AbstractWebViewFragment.class.getName();

    private static final String KEY_MAP_SETTINGS = "map_settings";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_SELECTED_LAYER = "selected_layer";
    private static final String KEY_EDITABLE_FEATURES = "editable_features";
    private static final String KEY_SELECTED_EDITABLE_FEATURE = "selected_editable_feature";

    private static final int REQUEST_LOCATION = 0;

    private Bundle mSavedState;
    private WebView mWebView = null;
    private FrameLayout mLayout;

    protected LinearLayout mLeftToolbarLayout;
    protected LinearLayout mRightToolbarLayout;

    private final Map<String, ITilesLayerDataSource> mTilesLayersDataSources = new HashMap<>();
    private final Map<String, IControl> mControls = new HashMap<>();

    private final AtomicBoolean mIsMapInitialized = new AtomicBoolean();
    private final AtomicBoolean mIsMapVisibleToUser = new AtomicBoolean(true);

    private boolean mIsTilesLayersDataSourcesInitialized = false;

    private MockLocationProvider mMockLocationProvider = null;
    private LocationManager mLocationManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mSavedState = new Bundle();

            // sets the default selected layer
            mSavedState.putParcelable(KEY_SELECTED_LAYER,
                                      getMapSettings().getLayers()
                                                      .get(0));

            mSavedState.putParcelable(KEY_EDITABLE_FEATURES,
                                      new FeatureCollection());
        }
        else {
            mSavedState = savedInstanceState;
        }

        mIsTilesLayersDataSourcesInitialized = initializeTilesLayersDataSources();
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "onCreate [center: " + getMapSettings().getCenter() + ", zoom: " + getMapSettings().getZoom() + "]");
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_webview,
                                           container,
                                           false);
        mLayout = (FrameLayout) view.findViewById(R.id.frameLayout);
        mLeftToolbarLayout = (LinearLayout) view.findViewById(R.id.leftToolbarLayout);
        mRightToolbarLayout = (LinearLayout) view.findViewById(R.id.rightToolbarLayout);

        if (mWebView != null) {
            mWebView.destroy();
        }

        mWebView = new WebView(getActivity().getApplicationContext());
        mLayout.addView(mWebView);

        mWebView.getSettings()
                .setJavaScriptEnabled(true);
        mWebView.getSettings()
                .setSupportZoom(true);
        mWebView.getSettings()
                .setBuiltInZoomControls(false);

        // see: http://code.google.com/p/android/issues/detail?id=35288
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE,
                                  null);
        }
        else {
            mWebView.setLayerType(View.LAYER_TYPE_HARDWARE,
                                  null);
        }

        // cache settings
        /*
        mWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
        mWebView.getSettings().setAppCachePath(getActivity().getCacheDir().getPath());
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        */

        mWebView.getSettings()
                .setCacheMode(WebSettings.LOAD_NO_CACHE);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (DebugUtils.isDebuggable(getActivity())) {
            mMockLocationProvider = new MockLocationProvider(getActivity());
        }

        if (mIsTilesLayersDataSourcesInitialized) {
            mWebView.setWebChromeClient(SimpleWebChromeClient.getInstance());

            // declare the default control
            MainControl mainControl = new MainControl(getActivity());
            addControl(mainControl,
                       null);
            mainControl.add(this);

            // loads all additional controls
            loadControls();
        }
    }

    @Override
    public void onPause() {
        mIsMapInitialized.set(false);

        if (DebugUtils.isDebuggable(getActivity()) && (mMockLocationProvider != null)) {
            mMockLocationProvider.shutdown();
        }

        if (mLeftToolbarLayout != null) {
            mLeftToolbarLayout.removeAllViews();
        }

        if (mRightToolbarLayout != null) {
            mRightToolbarLayout.removeAllViews();
        }

        for (String controlName : this.getControls()) {
            removeControl(this.getControl(controlName));
        }

        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (PermissionUtils.checkPermissions(grantResults)) {
                    Toast.makeText(getActivity(),
                                   R.string.message_permission_location_available,
                                   Toast.LENGTH_LONG)
                         .show();

                    for (IControl control : this.mControls.values()) {
                        if (control instanceof LocationListener) {
                            requestLocationUpdates((LocationListener) control);
                        }
                    }
                }
                else {
                    Toast.makeText(getActivity(),
                                   R.string.message_permissions_not_granted,
                                   Toast.LENGTH_LONG)
                         .show();
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode,
                                                 permissions,
                                                 grantResults);
        }
    }

    @Override
    public void onDestroyView() {
        if (mLayout != null) {
            mLayout.removeAllViews();
        }

        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }

        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        for (IControl control : this.mControls.values()) {
            if (control.hasOptionsMenu()) {
                control.onCreateOptionsMenu(menu,
                                            inflater);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        for (IControl control : this.mControls.values()) {
            if (control.hasOptionsMenu()) {
                control.onPrepareOptionsMenu(menu);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        for (IControl control : this.mControls.values()) {
            if (control.hasOptionsMenu() && control.onOptionsItemSelected(item)) {
                invalidateMenu();
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putAll(mSavedState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        this.mIsMapVisibleToUser.set(isVisibleToUser);
    }

    @Override
    public boolean isMapVisibleToUser() {
        return this.mIsMapVisibleToUser.get();
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public ActionBar getActionBar() {
        return (getActivity() == null) ? null : ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public Bundle getSavedInstanceState() {
        return this.mSavedState;
    }

    @Override
    public MapSettings getMapSettings() {
        MapSettings settings = null;

        if (mSavedState.containsKey(KEY_MAP_SETTINGS)) {
            settings = mSavedState.getParcelable(KEY_MAP_SETTINGS);
        }

        return settings;
    }

    @Override
    public void setMapSettings(MapSettings mapSettings) {
        if (mapSettings != null) {
            mSavedState.putParcelable(KEY_MAP_SETTINGS,
                                      mapSettings);
        }
    }

    @Override
    public List<String> getTilesLayersDataSources() {
        return new ArrayList<>(this.mTilesLayersDataSources.keySet());
    }

    @Override
    public ITilesLayerDataSource getTilesLayersDataSource(String name) {
        return this.mTilesLayersDataSources.get(name);
    }

    @Override
    public LayerSettings getSelectedLayer() {
        return this.mSavedState.getParcelable(KEY_SELECTED_LAYER);
    }

    @Override
    public void setSelectedLayer(LayerSettings layerSettings) {
        this.mSavedState.putParcelable(KEY_SELECTED_LAYER,
                                       layerSettings);
    }

    @Override
    public void loadUrl(String url) {
        if (this.mIsMapInitialized.get() && (this.mWebView != null) && (url != null)) {
            this.mWebView.loadUrl(url);
        }
        else {
            Log.w(TAG,
                  "unable to load url '" + url + "'");
        }
    }

    @Override
    public void reload() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "reload");
        }

        if (!this.mIsMapInitialized.getAndSet(true)) {
            mWebView.loadUrl("file:///android_asset/www/map.html");
        }
        else {
            for (IControl control : this.mControls.values()) {
                control.refresh();
            }
        }
    }

    @Override
    public Location getCurrentLocation() {
        return (Location) mSavedState.get(KEY_LOCATION);
    }

    @Override
    public void setCurrentLocation(Location location) {
        if (location == null) {
            mSavedState.remove(KEY_LOCATION);
        }
        else {
            mSavedState.putParcelable(KEY_LOCATION,
                                      location);
        }
    }

    @SuppressLint({
                          "JavascriptInterface",
                          "AddJavascriptInterface"
                  })
    @Override
    public void addControl(IControl control,
                           ViewGroup parent) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "addControl " + control.getName());
        }

        if (this.mControls.containsKey(control.getName())) {
            Log.w(TAG,
                  "addControl: '" + control.getName() + "' is already registered");
        }
        else {
            this.mWebView.addJavascriptInterface(control,
                                                 control.getName());

            if ((parent != null) && (control.getView(false) != null)) {
                if (control.getView(false)
                           .getParent() != null) {
                    parent.addView(control.getView(true));
                }
                else {
                    parent.addView(control.getView(false));
                }
            }

            if (control instanceof LocationListener) {
                requestLocationUpdates((LocationListener) control);
            }

            this.mControls.put(control.getName(),
                               control);
        }
    }

    @Override
    @SuppressLint("NewApi")
    public void removeControl(IControl control) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "removeControl " + control.getName());
        }

        if (control instanceof LocationListener) {
            // noinspection MissingPermission
            mLocationManager.removeUpdates((LocationListener) control);
        }

        control.remove(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            this.mWebView.removeJavascriptInterface(control.getName());
        }

        this.mControls.remove(control.getName());
    }

    @Override
    public List<String> getControls() {
        return new ArrayList<>(this.mControls.keySet());
    }

    @Override
    public IControl getControl(String name) {
        return this.mControls.get(name);
    }

    @Override
    public boolean hasControl(String name) {
        return this.mControls.containsKey(name);
    }

    @Override
    public void invalidateMenu() {
        final Activity activity = getActivity();

        if (activity != null) {
            ActivityCompat.invalidateOptionsMenu(activity);
        }
    }

    @Override
    public FeatureCollection getEditableFeatures() {
        return this.mSavedState.getParcelable(KEY_EDITABLE_FEATURES);
    }

    public Feature getCurrentEditableFeature() {
        return this.mSavedState.getParcelable(KEY_SELECTED_EDITABLE_FEATURE);
    }

    public void setCurrentEditableFeature(@Nullable final Feature selectedFeature) {
        if (selectedFeature == null) {
            this.mSavedState.remove(KEY_SELECTED_EDITABLE_FEATURE);
        }
        else {
            this.mSavedState.putParcelable(KEY_SELECTED_EDITABLE_FEATURE,
                                           selectedFeature);
        }
    }

    @Override
    public boolean addOrUpdateEditableFeature(@NonNull Feature selectedFeature) {
        if (selectedFeature.getGeometry()
                           .isValid()) {
            final FeatureCollection featureCollection = this.mSavedState.getParcelable(KEY_EDITABLE_FEATURES);

            if (featureCollection == null) {
                return false;
            }

            featureCollection.addFeature(selectedFeature);
            this.mSavedState.putParcelable(KEY_EDITABLE_FEATURES,
                                           featureCollection);

            return true;
        }
        else {
            Log.w(TAG,
                  "addOrUpdateEditableFeature feature '" + selectedFeature.getId() + "' invalid !");

            return false;
        }
    }

    @Override
    public boolean deleteEditableFeature(String featureId) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "deleteEditableFeature '" + featureId + "'");
        }

        FeatureCollection featureCollection = this.mSavedState.getParcelable(KEY_EDITABLE_FEATURES);

        if (featureCollection == null) {
            return false;
        }

        featureCollection.removeFeature(featureId);
        this.mSavedState.putParcelable(KEY_EDITABLE_FEATURES,
                                       featureCollection);

        return !featureCollection.hasFeature(featureId);
    }

    @Override
    public void requestLocationUpdates(LocationListener listener) {
        if (ActivityCompat.checkSelfPermission(getContext(),
                                               Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(),
                                                                                                                                                                      Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                                              new String[] {
                                                      Manifest.permission.ACCESS_COARSE_LOCATION,
                                                      Manifest.permission.ACCESS_FINE_LOCATION
                                              },
                                              REQUEST_LOCATION);
        }
        else {
            mLocationManager.removeUpdates(listener);

            if (DebugUtils.isDebuggable(getActivity())) {
                if (mLocationManager.getProvider(MockLocationProvider.MOCK_LOCATION_PROVIDER) == null) {
                    mMockLocationProvider = new MockLocationProvider(getActivity());
                }

                if (mMockLocationProvider.isProviderEnabled()) {
                    mLocationManager.requestLocationUpdates(MockLocationProvider.MOCK_LOCATION_PROVIDER,
                                                            30000,
                                                            0.5f,
                                                            listener);
                }
            }
            else {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                                        30000,
                                                        0.5f,
                                                        listener);
            }
        }
    }

    @Override
    public MockLocationProvider getMockLocationProvider() {
        return this.mMockLocationProvider;
    }

    @Override
    public String getLocalizedMessage(String messageId) {
        return getString(getResources().getIdentifier(messageId,
                                                      "string",
                                                      getActivity().getPackageName()));
    }

    /**
     * Initializes all tiles layers data sources implementing {@link ITilesLayerDataSource}.
     *
     * @return <code>true</code> if the initialization process was successful (contains at least one tiles layer).
     */
    protected boolean initializeTilesLayersDataSources() {
        try {
            TilesLayerDataSourceFactory tilesLayerDataSourceFactory = new TilesLayerDataSourceFactory(getTilesSourcePath());

            for (LayerSettings layerSettings : getMapSettings().getLayers()) {
                try {
                    this.mTilesLayersDataSources.put(layerSettings.getName(),
                                                     tilesLayerDataSourceFactory.getTilesLayerDataSource(layerSettings));
                }
                catch (UnsupportedOperationException | IOException ge) {
                    noTilesSourceFound(layerSettings,
                                       ge);
                }
            }

            LayerSettings unitiesLayerSettings = getMapSettings().getUnityLayer();

            if (unitiesLayerSettings != null) {
                try {
                    this.mTilesLayersDataSources.put(unitiesLayerSettings.getName(),
                                                     tilesLayerDataSourceFactory.getTilesLayerDataSource(unitiesLayerSettings));
                }
                catch (UnsupportedOperationException | IOException ge) {
                    noTilesSourceFound(unitiesLayerSettings,
                                       ge);
                }
            }
        }
        catch (IOException ioe) {
            Log.w(TAG,
                  ioe.getMessage());
        }

        return !getTilesLayersDataSources().isEmpty();
    }

    /**
     * Declares all {@link IControl} to load.
     *
     * @see IWebViewFragment#addControl(IControl, ViewGroup)
     */
    protected abstract void loadControls();

    /**
     * Gets the current root path for all tiles.
     *
     * @return the root path as {@link File}
     *
     * @throws IOException
     */
    protected abstract File getTilesSourcePath() throws
                                                 IOException;

    private void noTilesSourceFound(LayerSettings pLayerSettings,
                                    Throwable t) {
        Log.w(TAG,
              t.getMessage());

        Toast.makeText(getActivity(),
                       String.format(getString(R.string.message_mbtiles_not_found),
                                     pLayerSettings.getName()),
                       Toast.LENGTH_LONG)
             .show();
    }

    private static final class SimpleWebChromeClient
            extends WebChromeClient {
        private static final SimpleWebChromeClient instance = new SimpleWebChromeClient();

        private SimpleWebChromeClient() {
            super();
        }

        public static SimpleWebChromeClient getInstance() {
            return instance;
        }

        @Override
        public boolean onConsoleMessage(@NonNull ConsoleMessage consoleMessage) {
            final String message = consoleMessage.sourceId() + " (line " + consoleMessage.lineNumber() + "): " + consoleMessage.message();

            switch (consoleMessage.messageLevel()) {
                case ERROR:
                    Log.e(TAG,
                          message);
                    break;
                case WARNING:
                    Log.w(TAG,
                          message);
                    break;
                default:
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG,
                              message);
                    }
                    break;
            }

            return true;
        }
    }
}
