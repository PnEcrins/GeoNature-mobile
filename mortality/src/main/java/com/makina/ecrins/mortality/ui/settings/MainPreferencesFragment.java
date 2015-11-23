package com.makina.ecrins.mortality.ui.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.commons.ui.settings.AbstractPreferencesFragment;
import com.makina.ecrins.mortality.BuildConfig;
import com.makina.ecrins.mortality.MainApplication;
import com.makina.ecrins.mortality.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Global preferences for this application.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainPreferencesFragment
        extends AbstractPreferencesFragment {

    @Override
    public void onCreatePreferences(
            Bundle bundle,
            String s) {

        // load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Nullable
    @Override
    protected Observer getDefaultObserver() {

        return ((MainApplication) getActivity().getApplication()).getDefaultObserver();
    }

    @NonNull
    @Override
    protected String getSummaryForMapDensity(int density) {

        return getResources().getStringArray(R.array.viewport_target_density_labels)[density];
    }

    @NonNull
    @Override
    protected String getAppVersion() {

        return getString(R.string.app_version,
                         BuildConfig.VERSION_NAME,
                         BuildConfig.VERSION_CODE,
                         DateFormat.getDateTimeInstance()
                                   .format(new Date(Long.valueOf(BuildConfig.BUILD_DATE))));
    }
}
