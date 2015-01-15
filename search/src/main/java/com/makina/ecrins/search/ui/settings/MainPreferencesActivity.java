package com.makina.ecrins.search.ui.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.makina.ecrins.search.BuildConfig;
import com.makina.ecrins.search.R;

import java.text.DateFormat;
import java.util.Date;

/**
 * Global preferences for this application.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class MainPreferencesActivity
        extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {

    private static final String KEY_LIST_PREFERENCE_DENSITY_DISPLAY_MAP = "density_display_map";
    private static final String KEY_PREFERENCE_ABOUT_APP_VERSION = "app_version";

    private ListPreference mDensityDisplayMapListPreference;
    private Preference mAboutAppVersionPreference;

    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // backward compatibility for Android 2.3.x
        addPreferencesFromResource(R.xml.preferences);

        mDensityDisplayMapListPreference = (ListPreference) getPreferenceScreen().findPreference(KEY_LIST_PREFERENCE_DENSITY_DISPLAY_MAP);
        mAboutAppVersionPreference = getPreferenceScreen().findPreference(KEY_PREFERENCE_ABOUT_APP_VERSION);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onResume() {
        super.onResume();

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        updateDispayMapDensitySummary(
                Integer.parseInt(
                        PreferenceManager.getDefaultSharedPreferences(this)
                                .getString(
                                        KEY_LIST_PREFERENCE_DENSITY_DISPLAY_MAP,
                                        "0"
                                )
                )
        );

        getAppVersion();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onSharedPreferenceChanged(
            SharedPreferences sharedPreferences,
            String key) {
        if (key.equals(KEY_LIST_PREFERENCE_DENSITY_DISPLAY_MAP)) {
            updateDispayMapDensitySummary(
                    Integer.parseInt(
                            sharedPreferences.getString(
                                    KEY_LIST_PREFERENCE_DENSITY_DISPLAY_MAP,
                                    "0"
                            )
                    )
            );
        }
    }

    private void updateDispayMapDensitySummary(int density) {
        mDensityDisplayMapListPreference.setSummary(getResources().getStringArray(R.array.viewport_target_density_labels)[density]);
    }

    private void getAppVersion() {
        mAboutAppVersionPreference.setSummary(
                getString(
                        R.string.app_version,
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE,
                        DateFormat.getDateTimeInstance().format(
                                new Date(Long.valueOf(BuildConfig.BUILD_DATE)))));
    }
}