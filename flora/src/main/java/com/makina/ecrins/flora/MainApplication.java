package com.makina.ecrins.flora;

import android.app.Application;
import android.util.Log;

import com.makina.ecrins.commons.util.MountPointUtils;
import com.makina.ecrins.flora.settings.AppSettings;

/**
 * Base class to maintain global application state.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainApplication
        extends Application {

    private static final String TAG = MainApplication.class.getName();

    private static MainApplication sInstance;

    private boolean mCloseApplication;
    private AppSettings mAppSettings;

    public static MainApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
        this.mCloseApplication = false;
        this.mAppSettings = null;

        Log.i(TAG,
              "internal storage: " + MountPointUtils.getInternalStorage());
        Log.i(TAG,
              "external storage: " + MountPointUtils.getExternalStorage(this));
    }

    public boolean isCloseApplication() {
        return mCloseApplication;
    }

    public void setCloseApplication(boolean pCloseApplication) {
        this.mCloseApplication = pCloseApplication;
    }

    public AppSettings getAppSettings() {
        return mAppSettings;
    }

    public void setAppSettings(AppSettings pAppSettings) {
        this.mAppSettings = pAppSettings;
    }
}
