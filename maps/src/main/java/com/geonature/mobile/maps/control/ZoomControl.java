package com.geonature.mobile.maps.control;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.widget.ImageButton;

import com.geonature.mobile.maps.BuildConfig;
import com.geonature.mobile.maps.IWebViewFragment;
import com.geonature.mobile.maps.R;

/**
 * A basic zoom control with two buttons (zoom in and zoom out).
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public final class ZoomControl
        extends AbstractControl
        implements OnClickListener {

    private static final String TAG = ZoomControl.class.getName();

    private ImageButton mImageButtonZoomIn;
    private ImageButton mImageButtonZoomOut;

    private View mView = null;

    /**
     * Default constructor.
     */
    public ZoomControl(Context pContext) {
        super(pContext);

        setControlListener(new OnIControlListener() {
            @Override
            public void onControlInitialized() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "onControlInitialized");
                }

                if (mWebViewFragment.getMapSettings()
                                    .getMinZoom() < mWebViewFragment.getMapSettings()
                                                                    .getZoom()) {
                    mImageButtonZoomOut.setEnabled(true);
                }

                if (mWebViewFragment.getMapSettings()
                                    .getMaxZoom() > mWebViewFragment.getMapSettings()
                                                                    .getZoom()) {
                    mImageButtonZoomIn.setEnabled(true);
                }
            }
        });
    }

    @Override
    public View getView(boolean forceCreate) {
        if ((this.mView == null) || forceCreate) {
            this.mView = View.inflate(getContext(),
                                      R.layout.control_zoom_layout,
                                      null);

            mImageButtonZoomIn = this.mView.findViewById(R.id.imageButtonZoomIn);
            mImageButtonZoomOut = this.mView.findViewById(R.id.imageButtonZoomOut);

            mImageButtonZoomIn.setOnClickListener(this);
            mImageButtonZoomOut.setOnClickListener(this);

            mImageButtonZoomIn.setEnabled(false);
            mImageButtonZoomOut.setEnabled(false);
        }

        return this.mView;
    }

    @Override
    public void add(IWebViewFragment webViewFragment) {
        super.add(webViewFragment);

        initializeJSController("js/Control.NativeZoom.js",
                               "new L.Control.NativeZoom()");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageButtonZoomIn) {
            v.setEnabled(false);
            this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".zoomIn()");
        }
        else if (v.getId() == R.id.imageButtonZoomOut) {
            v.setEnabled(false);
            this.mWebViewFragment.loadUrl(getJSUrlPrefix() + ".zoomOut()");
        }
    }

    /**
     * Sets the current zoom level
     *
     * @param zoom the zoom level to set
     */
    @JavascriptInterface
    public void setZoom(final int zoom) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "setZoom " + zoom);
                }

                if (mWebViewFragment.getMapSettings()
                                    .getMinZoom() < zoom) {
                    mImageButtonZoomOut.setEnabled(true);
                }

                if (mWebViewFragment.getMapSettings()
                                    .getMaxZoom() > zoom) {
                    mImageButtonZoomIn.setEnabled(true);
                }
            }
        });
    }
}
