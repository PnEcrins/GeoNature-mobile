package com.geonature.mobile.maps.control;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.JavascriptInterface;

import com.geonature.mobile.maps.BuildConfig;
import com.geonature.mobile.maps.IWebViewFragment;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Base implementation of {@link IControl} interface.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractControl
        implements IControl {

    private static final String TAG = AbstractControl.class.getName();

    private static final Handler sHandler = new Handler();
    private static final String MAP_JS_VARIABLE = "lMap";

    private boolean mControlInitialized = false;
    protected IWebViewFragment mWebViewFragment;

    private final Context mContext;
    private OnIControlListener mControlListener = null;
    private final BlockingDeque<OnIControlListener> mControlListeners = new LinkedBlockingDeque<>();

    public AbstractControl(Context pContext) {
        super();

        this.mContext = pContext;
    }

    @NonNull
    @Override
    public String getName() {
        return ControlUtils.getControlName(this);
    }

    @Override
    public void refresh() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "refresh");
        }
    }

    @Override
    public boolean hasOptionsMenu() {
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu,
                                    MenuInflater inflater) {
        // nothing to do ...
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // nothing to do ...
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    public void add(IWebViewFragment webViewFragment) {
        this.mWebViewFragment = webViewFragment;
    }

    @Override
    public void remove(IWebViewFragment webViewFragment) {
        // this.mWebViewFragment.loadUrl("javascript:" + MAP_JS_VARIABLE + ".removeControl(\"" + getName() + "\")");

        this.mControlInitialized = false;
    }

    public Context getContext() {
        return mContext;
    }

    public void setControlListener(OnIControlListener pControlListener) {
        mControlListener = pControlListener;
    }

    public void addControlListener(OnIControlListener pControlListener) {
        if (pControlListener != null) {
            mControlListeners.offerLast(pControlListener);
        }
    }

    /**
     * @return <code>true</code> if this {@link IControl} was successfully loaded, <code>false</code> otherwise
     */
    public boolean isControlInitialized() {
        return mControlInitialized;
    }

    /**
     * Called when the JS part of this {@link IControl} was successfully loaded.
     */
    @JavascriptInterface
    public void setControlInitialized() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG,
                          "setControlInitialized");
                }

                mControlInitialized = true;

                if (mControlListener != null) {
                    mControlListener.onControlInitialized();
                }

                while (mControlListeners.size() > 0) {
                    try {
                        OnIControlListener listener = mControlListeners.takeFirst();

                        if (mControlInitialized) {
                            listener.onControlInitialized();
                        }
                        else {
                            mControlListeners.offerLast(listener);
                        }
                    }
                    catch (InterruptedException ie) {
                        Log.w(TAG,
                              ie.getMessage());
                    }
                }
            }
        });
    }

    @Nullable
    @JavascriptInterface
    public String getDensityDpi() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return "device-dpi";
        }

        return null;
    }

    /**
     * Gets the global {@link Handler} for all {@link IControl} instances.
     *
     * @return the global {@link Handler}
     */
    protected static Handler getHandler() {
        return sHandler;
    }

    /**
     * Dynamically load and instantiate the JS part of this {@link IControl} instance.
     *
     * @param jsFile   relative path to the JS file to load
     * @param jsObject JS expression to instantiate the JS part.
     *
     * @see IWebViewFragment#loadUrl(String)
     */
    protected void initializeJSController(String jsFile,
                                          String jsObject) {
        this.mWebViewFragment.loadUrl("javascript:head.load(\"" + jsFile + "\", function(){" + MAP_JS_VARIABLE + ".addControl(\"" + getName() + "\", " + jsObject + ")})");
    }

    /**
     * Gets the JS URL part of this {@link IControl} (i.e. <code>javascript:</code>).
     *
     * @return the JS URL part
     */
    protected String getJSUrlPrefix() {
        return "javascript:" + MAP_JS_VARIABLE + ".getControl(\"" + getName() + "\")";
    }

    /**
     * The callback used to indicate that this {@link IControl} is initialized.
     *
     * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
     */
    public interface OnIControlListener {
        /**
         * Called when {@link IControl} is initialized.
         */
        void onControlInitialized();
    }
}
