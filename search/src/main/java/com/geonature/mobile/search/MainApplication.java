package com.geonature.mobile.search;

import android.app.Application;
import android.util.Log;

import com.geonature.mobile.commons.util.MountPointUtils;
import com.geonature.mobile.search.settings.AppSettings;

/**
 * Base class to maintain global application state.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainApplication
        extends Application {

    private static MainApplication sInstance;
    private AppSettings mAppSettings;

    public static MainApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        /*
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.clear();
        editor.commit();
        */

        this.mAppSettings = null;

        Log.i(getClass().getName(),
              "internal storage: " + MountPointUtils.getInternalStorage());
        Log.i(getClass().getName(),
              "external storage: " + MountPointUtils.getExternalStorage(this));
    }

    public AppSettings getAppSettings() {
        return mAppSettings;
    }

    public void setAppSettings(AppSettings pAppSettings) {
        this.mAppSettings = pAppSettings;
    }
}
