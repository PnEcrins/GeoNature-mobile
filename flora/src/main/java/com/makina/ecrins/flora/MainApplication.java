package com.makina.ecrins.flora;

import android.app.Application;
import android.util.Log;

import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.commons.util.MountPointUtils;
import com.makina.ecrins.flora.settings.AppSettings;

/**
 * Base class to maintain global application state.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainApplication
        extends Application {

    private static MainApplication sInstance;

    private boolean mCloseApplication;
    private AppSettings mAppSettings;
    private Observer mDefaultObserver;

    public static MainApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        this.mCloseApplication = false;

        /*
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.clear();
        editor.commit();
        */

        this.mAppSettings = null;
        this.mDefaultObserver = null;

        Log.i(getClass().getName(),
              "internal storage: " + MountPointUtils.getInternalStorage());
        Log.i(getClass().getName(),
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

    public Observer getDefaultObserver() {
        return mDefaultObserver;
    }

    public void setDefaultObserver(Observer pDefaultObserver) {
        this.mDefaultObserver = pDefaultObserver;
    }
}
