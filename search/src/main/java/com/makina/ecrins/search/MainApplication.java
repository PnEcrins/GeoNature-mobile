package com.makina.ecrins.search;

import android.app.Application;
import android.util.Log;

import com.makina.ecrins.commons.util.MountPointUtils;
import com.makina.ecrins.search.settings.AppSettings;

/**
 * Base class to maintain global application state.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainApplication
        extends Application {

    public static final int HANDLER_SETTINGS_LOADING_START = 10;
    public static final int HANDLER_SETTINGS_LOADING = 11;
    public static final int HANDLER_SETTINGS_LOADED = 12;
    public static final int HANDLER_SETTINGS_LOADED_FAILED = 13;

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
              "external storage: " + MountPointUtils.getExternalStorage());
    }

    public AppSettings getAppSettings() {
        return mAppSettings;
    }

    public void setAppSettings(AppSettings pAppSettings) {
        this.mAppSettings = pAppSettings;
    }
}
