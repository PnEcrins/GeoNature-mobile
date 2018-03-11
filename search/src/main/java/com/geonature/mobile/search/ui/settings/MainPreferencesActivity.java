package com.geonature.mobile.search.ui.settings;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.geonature.mobile.commons.input.InputType;
import com.geonature.mobile.commons.ui.settings.AbstractPreferencesActivity;
import com.geonature.mobile.search.BuildConfig;
import com.geonature.mobile.search.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Global preferences for this application.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainPreferencesActivity
        extends AbstractPreferencesActivity {

    @Override
    public int getPreferencesResourceId() {
        return R.xml.preferences;
    }

    @Nullable
    @Override
    public String getObserversIntentAction() {
        return null;
    }

    @Nullable
    @Override
    public Uri getObserverLoaderUri(long ObserverId) {
        return null;
    }

    @Nullable
    @Override
    public InputType getInputTypeFilter() {
        return null;
    }

    @NonNull
    @Override
    public String getAppVersion() {
        return getString(R.string.app_version,
                         BuildConfig.VERSION_NAME,
                         BuildConfig.VERSION_CODE,
                         DateFormat.getDateTimeInstance()
                                   .format(new Date(Long.valueOf(BuildConfig.BUILD_DATE))));
    }
}
