package com.makina.ecrins.fauna.ui.settings;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.makina.ecrins.commons.input.InputType;
import com.makina.ecrins.commons.ui.settings.AbstractPreferencesActivity;
import com.makina.ecrins.fauna.BuildConfig;
import com.makina.ecrins.fauna.R;
import com.makina.ecrins.fauna.content.MainContentProvider;
import com.makina.ecrins.fauna.input.Input;
import com.makina.ecrins.fauna.ui.observers.ObserverListActivity;

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
