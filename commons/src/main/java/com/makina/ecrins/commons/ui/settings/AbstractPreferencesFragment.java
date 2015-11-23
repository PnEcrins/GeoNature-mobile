package com.makina.ecrins.commons.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.input.Observer;

/**
 * Global settings.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractPreferencesFragment
        extends PreferenceFragmentCompat {

    private static final String KEY_PREFERENCE_DEFAULT_OBSERVER = "default_observer";
    private static final String KEY_LIST_PREFERENCE_DENSITY_DISPLAY_MAP = "density_display_map";
    private static final String KEY_PREFERENCE_ABOUT_APP_VERSION = "app_version";

    private Preference mDefaultObserverPreference;

    public AbstractPreferencesFragment() {
        // default constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mDefaultObserverPreference = getPreferenceScreen().findPreference(KEY_PREFERENCE_DEFAULT_OBSERVER);

        final Preference densityDisplayMapListPreference = getPreferenceScreen().findPreference(KEY_LIST_PREFERENCE_DENSITY_DISPLAY_MAP);

        if (densityDisplayMapListPreference != null) {
            densityDisplayMapListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(
                        Preference preference,
                        Object newValue) {

                    preference.setSummary(getSummaryForMapDensity(Integer.parseInt((String) newValue)));

                    return true;
                }
            });

            densityDisplayMapListPreference.setSummary(getSummaryForMapDensity(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext())
                                                                                                                  .getString(KEY_LIST_PREFERENCE_DENSITY_DISPLAY_MAP,
                                                                                                                             "0"))));
        }

        final Preference aboutAppVersionPreference = getPreferenceScreen().findPreference(KEY_PREFERENCE_ABOUT_APP_VERSION);

        if (aboutAppVersionPreference != null) {
            aboutAppVersionPreference.setSummary(getAppVersion());
        }
    }

    @Override
    public void onResume() {

        super.onResume();

        updateDefaultObserverPreference();
    }

    @Nullable
    protected abstract Observer getDefaultObserver();

    @NonNull
    protected abstract String getSummaryForMapDensity(int density);

    @NonNull
    protected abstract String getAppVersion();

    private void updateDefaultObserverPreference() {

        if (mDefaultObserverPreference == null) {
            return;
        }

        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext())
                                                                 .edit();

        final Observer defaultObserver = getDefaultObserver();

        if (defaultObserver == null) {
            editor.remove(KEY_PREFERENCE_DEFAULT_OBSERVER);

            mDefaultObserverPreference.setSummary(R.string.preference_category_observers_no_default_set);
        }
        else {
            editor.putLong(KEY_PREFERENCE_DEFAULT_OBSERVER,
                           defaultObserver.getObserverId());

            mDefaultObserverPreference.setSummary(defaultObserver.getLastname() + " " + defaultObserver.getFirstname());
        }

        editor.apply();
    }
}