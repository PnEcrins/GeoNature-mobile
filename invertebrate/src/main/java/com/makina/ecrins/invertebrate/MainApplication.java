package com.makina.ecrins.invertebrate;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.commons.util.FileUtils;
import com.makina.ecrins.invertebrate.inputs.Input;
import com.makina.ecrins.invertebrate.settings.AppSettings;
import com.makina.ecrins.maps.geojson.Feature;

import java.util.ArrayList;
import java.util.List;

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

    public static final int HANDLER_UNITIES_LOADING_START = 20;
    public static final int HANDLER_UNITIES_LOADING = 21;
    public static final int HANDLER_UNITIES_LOADED = 22;
    public static final int HANDLER_UNITIES_LOADED_FAILED = 23;

    public static final int HANDLER_CONNECTIVITY_CHANGE = 30;

    private static MainApplication sInstance;

    private boolean mCloseApplication;
    private AppSettings mAppSettings;
    private Input mInput;
    private Observer mDefaultObserver;
    private List<Feature> mUnities;

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
        this.mInput = null;
        this.mDefaultObserver = null;
        this.mUnities = new ArrayList<Feature>();

        Log.d(getClass().getName(),
              "default storage " + Environment.getExternalStorageDirectory()
                                              .getPath());
        Log.d(getClass().getName(),
              "use storage " + FileUtils.getExternalStorageDirectory()
                                        .getPath());
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

    public Input getInput() {

        return mInput;
    }

    public void setInput(Input pInput) {

        this.mInput = pInput;
    }

    public Observer getDefaultObserver() {

        return mDefaultObserver;
    }

    public void setDefaultObserver(Observer pDefaultObserver) {

        this.mDefaultObserver = pDefaultObserver;
    }

    public List<Feature> getUnities() {

        return mUnities;
    }

    public void setUnities(List<Feature> pUnities) {

        this.mUnities = pUnities;
    }
}
