package com.makina.ecrins.maps.control;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.widget.ImageButton;

import com.makina.ecrins.maps.R;
import com.makina.ecrins.maps.IWebViewFragment;
import com.makina.ecrins.maps.geojson.geometry.GeoPoint;
import com.makina.ecrins.maps.geojson.geometry.GeometryUtils;
import com.makina.ecrins.maps.geojson.geometry.Point;

/**
 * Simple control to center the map to the current position.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public final class CenterPositionControl extends AbstractControl implements OnClickListener, LocationListener {

    protected ImageButton mImageButtonCenterPosition;
    protected Boolean mIsCenterPosition = false;

    private LayoutInflater mInflater;
    private View mView = null;

    public CenterPositionControl(Context pContext) {
        super(pContext);

        mInflater = (LayoutInflater) pContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setControlListener(new OnIControlListener() {
            @Override
            public void onControlInitialized() {
                mWebViewFragment.requestLocationUpdates(CenterPositionControl.this);

                mIsCenterPosition = false;
                mImageButtonCenterPosition.setEnabled((mWebViewFragment.getCurrentLocation() != null) && GeometryUtils.contains(new Point(new GeoPoint(mWebViewFragment.getCurrentLocation()
                        .getLatitude(), mWebViewFragment.getCurrentLocation()
                        .getLongitude())), mWebViewFragment.getMapSettings()
                        .getPolygonBounds()));
            }
        });
    }

    @Override
    public View getView(boolean forceCreate) {
        if ((this.mView == null) || forceCreate) {
            this.mView = mInflater.inflate(R.layout.control_center_position_layout, null);

            mImageButtonCenterPosition = (ImageButton) this.mView.findViewById(R.id.imageButtonCenterPosition);

            mImageButtonCenterPosition.setOnClickListener(this);
            mImageButtonCenterPosition.setEnabled(true);
        }

        return this.mView;
    }

    @Override
    public void add(IWebViewFragment webViewFragment) {
        super.add(webViewFragment);

        initializeJSController("js/Control.CenterPosition.js", "new L.Control.CenterPosition()");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageButtonCenterPosition) {
            this.mIsCenterPosition = !this.mIsCenterPosition;

            if (this.mIsCenterPosition) {
                AnimationDrawable accessLocationSearchingDrawable = (AnimationDrawable) getContext().getResources()
                        .getDrawable(R.drawable.ic_action_access_location_searching);
                mImageButtonCenterPosition.setImageDrawable(accessLocationSearchingDrawable);
                accessLocationSearchingDrawable.start();

                Location location = this.mWebViewFragment.getCurrentLocation();

                if (location != null) {
                    this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".setCenter(" + location.getLatitude() + ", " + location.getLongitude() + ")");
                }
            }
            else {
                mImageButtonCenterPosition.setImageResource(R.drawable.ic_action_access_location_none);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(CenterPositionControl.class.getName(), "onLocationChanged [provider: " + location.getProvider() + ", lat: " + location.getLatitude() + ", lon: " + location.getLongitude() + ", acc: " + location.getAccuracy() + ", bearing: " + location.getBearing());

        // checks if this location is inside the map or not
        if (isControlInitialized() && GeometryUtils.contains(new Point(new GeoPoint(location.getLatitude(), location.getLongitude())), this.mWebViewFragment.getMapSettings()
                .getPolygonBounds())) {
            mImageButtonCenterPosition.setEnabled(true);

            if (isControlInitialized() && mIsCenterPosition) {
                this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".setCenter(" + location.getLatitude() + ", " + location.getLongitude() + ")");
            }
        }
        else {
            // center map from location feature is disabled
            mImageButtonCenterPosition.setEnabled(false);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        mImageButtonCenterPosition.setImageResource(R.drawable.ic_action_access_location_off);
        mImageButtonCenterPosition.setEnabled(false);
    }

    @Override
    public void onProviderEnabled(String provider) {
        mImageButtonCenterPosition.setImageResource(R.drawable.ic_action_access_location_none);
        mImageButtonCenterPosition.setEnabled(true);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // nothing to do ...
    }

    @JavascriptInterface
    public void setCenter(double latitude, double longitude) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (mIsCenterPosition) {
                    mImageButtonCenterPosition.setImageResource(R.drawable.ic_action_access_location_found);
                }
            }
        });
    }
}
