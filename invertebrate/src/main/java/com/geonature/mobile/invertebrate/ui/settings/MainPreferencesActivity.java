package com.geonature.mobile.invertebrate.ui.settings;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.geonature.mobile.commons.input.InputType;
import com.geonature.mobile.commons.ui.settings.AbstractPreferencesActivity;
import com.geonature.mobile.invertebrate.BuildConfig;
import com.geonature.mobile.invertebrate.R;
import com.geonature.mobile.invertebrate.content.MainContentProvider;
import com.geonature.mobile.invertebrate.inputs.Input;
import com.geonature.mobile.invertebrate.ui.observers.ObserverListActivity;

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

    @NonNull
    @Override
    public String getObserversIntentAction() {
        return ObserverListActivity.class.getName();
    }

    @NonNull
    @Override
    public Uri getObserverLoaderUri(long ObserverId) {
        return Uri.withAppendedPath(MainContentProvider.CONTENT_OBSERVERS_URI,
                                    Long.toString(ObserverId));
    }

    @NonNull
    @Override
    public InputType getInputTypeFilter() {
        return new Input().getType();
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
