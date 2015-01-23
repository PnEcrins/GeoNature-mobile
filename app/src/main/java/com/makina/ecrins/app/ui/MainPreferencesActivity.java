package com.makina.ecrins.app.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.makina.ecrins.app.BuildConfig;
import com.makina.ecrins.app.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Global preferences for this application.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainPreferencesActivity
        extends PreferenceActivity {

    private static final String KEY_PREFERENCE_ABOUT_APP_VERSION = "app_version";

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // backward compatibility for Android 2.3.x
        addPreferencesFromResource(R.xml.preferences);

        getPreferenceScreen().findPreference(KEY_PREFERENCE_ABOUT_APP_VERSION)
                .setSummary(
                        getString(
                                R.string.app_version,
                                BuildConfig.VERSION_NAME,
                                BuildConfig.VERSION_CODE,
                                DateFormat.getDateTimeInstance()
                                        .format(
                                                new Date(Long.valueOf(BuildConfig.BUILD_DATE))
                                        )
                        )
                );
    }
}
