package com.makina.ecrins.search.ui.maps;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;

import com.makina.ecrins.maps.IWebViewFragment;
import com.makina.ecrins.maps.control.AbstractControl;
import com.makina.ecrins.maps.control.FeaturesControl;
import com.makina.ecrins.maps.control.IControl;
import com.makina.ecrins.maps.geojson.Feature;
import com.makina.ecrins.maps.geojson.geometry.GeoPoint;
import com.makina.ecrins.maps.geojson.geometry.GeometryUtils;
import com.makina.ecrins.maps.geojson.geometry.Point;
import com.makina.ecrins.search.BuildConfig;
import com.makina.ecrins.search.R;
import com.makina.ecrins.search.ui.dialog.SearchDialogFragment;
import com.makina.ecrins.search.ui.dialog.SearchDialogFragment.OnSearchDialogValidateListener;

import org.json.JSONArray;

/**
 * Basic control to perform search and give a list of {@link Feature}s found.
 * <p>
 * This {@link IControl} uses {@link FeaturesControl} to display all {@link Feature}s found on the map.
 * </p>
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class SearchControl
        extends AbstractControl
        implements LocationListener {

    protected static final String KEY_SEARCH_LOCATION = "search_location";

    private static final String SEARCH_DIALOG_FRAGMENT = "search_dialog";

    private static JSONArray jsonArray = new JSONArray();

    private OnSearchDialogValidateListener mOnSearchDialogValidateListener;

    private MenuItem mItemMarker;
    private MenuItem mItemMarkerFromLocation;

    private int mMinRadius;
    private int mMaxRadius;
    private int mRadius;

    protected boolean mIsActionMarkerSelected = false;
    protected boolean mIsItemMarkerFromLocationEnabled = false;

    /**
     * Default constructor.
     */
    public SearchControl(Context pContext) {
        super(pContext);

        setControlListener(
                new OnIControlListener() {
                    @Override
                    public void onControlInitialized() {
                        mIsActionMarkerSelected = false;
                        mWebViewFragment.invalidateMenu();
                    }
                }
        );
    }

    @Override
    public View getView(boolean forceCreate) {
        return null;
    }

    @Override
    public void add(IWebViewFragment webViewFragment) {
        super.add(webViewFragment);

        initializeJSController(
                "js/Control.Search.js",
                "new L.Control.Search()"
        );
    }

    @Override
    public boolean hasOptionsMenu() {
        return true;
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu,
            MenuInflater inflater) {
        if (isControlInitialized()) {
            inflater.inflate(
                    R.menu.menu_search,
                    menu
            );
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (isControlInitialized()) {
            mItemMarker = menu.findItem(R.id.itemMarker);
            mItemMarkerFromLocation = menu.findItem(R.id.itemMarkerFromLocation);

            if (this.mIsActionMarkerSelected) {
                mItemMarker.setIcon(R.drawable.ic_action_marker_selected);
            }
            else {
                mItemMarker.setIcon(R.drawable.ic_action_marker);
            }

            mItemMarker.setEnabled(true);
            mItemMarkerFromLocation.setEnabled(mIsItemMarkerFromLocationEnabled);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.itemMarker) {
            if (!this.mIsActionMarkerSelected) {
                this.mIsActionMarkerSelected = true;
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".addOnClickEvent()");
            }

            return true;
        }
        else if (item.getItemId() == R.id.itemMarkerFromLocation) {
            Location currentLocation = this.mWebViewFragment.getCurrentLocation();

            if (currentLocation != null) {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".addMarker(" + currentLocation.getLatitude() + ", " + currentLocation.getLongitude() + ", " + currentLocation.getAccuracy() + ")");
            }

            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (BuildConfig.DEBUG) {
            Log.d(
                    getClass().getName(),
                    "onLocationChanged [provider: " + location.getProvider() + ", lat: " + location.getLatitude() + ", lon: " + location.getLongitude() + ", acc: " + location.getAccuracy() + ", bearing: " + location.getBearing()
            );
        }

        // checks if this location is inside the map or not
        mIsItemMarkerFromLocationEnabled = isControlInitialized() && GeometryUtils.contains(
                new Point(
                        new GeoPoint(
                                location.getLatitude(),
                                location.getLongitude()
                        )
                ),
                this.mWebViewFragment.getMapSettings().getPolygonBounds()
        );

        if (mWebViewFragment != null) {
            mWebViewFragment.invalidateMenu();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        mIsItemMarkerFromLocationEnabled = false;

        if (mWebViewFragment != null) {
            mWebViewFragment.invalidateMenu();
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        mIsItemMarkerFromLocationEnabled = true;

        if (mWebViewFragment != null) {
            mWebViewFragment.invalidateMenu();
        }
    }

    @Override
    public void onStatusChanged(
            String provider,
            int status,
            Bundle extras) {
        // nothing to do ...
    }

    public void setOnSearchDialogValidateListener(OnSearchDialogValidateListener pOnSearchDialogValidateListener) {
        if (pOnSearchDialogValidateListener != null) {
            this.mOnSearchDialogValidateListener = pOnSearchDialogValidateListener;
        }
    }

    public int getMinRadius() {
        return mMinRadius;
    }

    public void setMinRadius(int pMinRadius) {
        this.mMinRadius = pMinRadius;
    }

    public int getMaxRadius() {
        return mMaxRadius;
    }

    public void setMaxRadius(int pMaxRadius) {
        this.mMaxRadius = pMaxRadius;
    }

    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int pRadius) {
        this.mRadius = pRadius;
    }

    /**
     * Gets the current marker position as {@link org.json.JSONArray}.
     *
     * @return the current marker position
     */
    @JavascriptInterface
    public String getMarkerPosition() {
        final GeoPoint pointing = mWebViewFragment.getSavedInstanceState()
                .getParcelable(KEY_SEARCH_LOCATION);

        if (pointing != null) {
            jsonArray = pointing.getJSONObject(GeoPoint.LAT_LON);
            return jsonArray.toString();
        }
        else {
            jsonArray = new JSONArray();
            return jsonArray.toString();
        }
    }

    /**
     * Updates the current position marker.
     *
     * @param latitude  the latitude in degrees
     * @param longitude the longitude in degrees
     * @param accuracy  the accuracy of this position
     */
    @JavascriptInterface
    public void setMarkerPosition(
            final double latitude,
            final double longitude,
            int accuracy) {
        getHandler().post(
                new Runnable() {
                    @Override
                    public void run() {
                        mIsActionMarkerSelected = false;
                        mWebViewFragment.invalidateMenu();

                        if ((mWebViewFragment.getContext() != null) && (mWebViewFragment.getContext() instanceof FragmentActivity)) {
                            SearchDialogFragment dialogFragment = SearchDialogFragment.newInstance(
                                    getMaxRadius(),
                                    getRadius(),
                                    new GeoPoint(
                                            latitude,
                                            longitude
                                    )
                            );
                            dialogFragment.setOnSearchDialogValidateListener(mOnSearchDialogValidateListener);
                            dialogFragment.show(
                                    ((FragmentActivity) mWebViewFragment.getContext()).getSupportFragmentManager(),
                                    SEARCH_DIALOG_FRAGMENT
                            );
                        }
                    }
                }
        );
    }
}
