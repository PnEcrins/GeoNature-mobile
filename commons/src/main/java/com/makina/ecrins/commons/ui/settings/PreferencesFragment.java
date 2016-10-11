package com.makina.ecrins.commons.ui.settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.XmlRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.widget.ListView;

import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.InputType;
import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.commons.ui.observers.AbstractObserverListActivity;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Global settings.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class PreferencesFragment
        extends PreferenceFragmentCompat
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String KEY_PREFERENCE_DEFAULT_OBSERVER = "default_observer";
    private static final String KEY_LIST_PREFERENCE_DENSITY_DISPLAY_MAP = "density_display_map";
    private static final String KEY_PREFERENCE_ABOUT_APP_VERSION = "app_version";
    private static final String KEY_SELECTED_OBSERVER = "selected_observer";

    private OnPreferencesFragmentListener mListener;

    private Preference mDefaultObserverPreference;

    private Observer mDefaultObserver;

    public PreferencesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment.
     *
     * @return A new instance of fragment {@link PreferencesFragment}.
     */
    @NonNull
    public static PreferencesFragment newInstance() {
        final Bundle args = new Bundle();
        final PreferencesFragment fragment = new PreferencesFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mDefaultObserverPreference = getPreferenceScreen().findPreference(KEY_PREFERENCE_DEFAULT_OBSERVER);

        if ((mDefaultObserverPreference != null) && !TextUtils.isEmpty(mListener.getObserversIntentAction()) && (mListener.getInputTypeFilter() != null)) {
            loadDefaultObserver();

            mDefaultObserverPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    final Intent intent = new Intent(mListener.getObserversIntentAction());
                    intent.putExtra(AbstractObserverListActivity.EXTRA_CHOICE_MODE,
                                    ListView.CHOICE_MODE_SINGLE);
                    intent.putExtra(AbstractObserverListActivity.EXTRA_INPUT_FILTER,
                                    mListener.getInputTypeFilter()
                                             .getKey());

                    if (mDefaultObserver != null) {
                        intent.putParcelableArrayListExtra(AbstractObserverListActivity.EXTRA_SELECTED_OBSERVERS,
                                                           new ArrayList<>(Collections.singletonList(mDefaultObserver)));
                    }

                    startActivityForResult(intent,
                                           0);

                    return true;
                }
            });
        }

        final Preference densityDisplayMapListPreference = getPreferenceScreen().findPreference(KEY_LIST_PREFERENCE_DENSITY_DISPLAY_MAP);

        if (densityDisplayMapListPreference != null) {
            densityDisplayMapListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference,
                                                  Object newValue) {

                    preference.setSummary(mListener.getSummaryForMapDensity(Integer.parseInt((String) newValue)));

                    return true;
                }
            });

            densityDisplayMapListPreference.setSummary(mListener.getSummaryForMapDensity(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getContext())
                                                                                                                           .getString(KEY_LIST_PREFERENCE_DENSITY_DISPLAY_MAP,
                                                                                                                                      "0"))));
        }

        final Preference aboutAppVersionPreference = getPreferenceScreen().findPreference(KEY_PREFERENCE_ABOUT_APP_VERSION);

        if (aboutAppVersionPreference != null) {
            aboutAppVersionPreference.setSummary(mListener.getAppVersion());
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle,
                                    String s) {
        // load the preferences from an XML resource
        addPreferencesFromResource(mListener.getPreferencesResourceId());
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnPreferencesFragmentListener) {
            mListener = (OnPreferencesFragmentListener) context;
        }
        else {
            throw new RuntimeException(context.toString() + " must implement OnPreferencesFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {
        if ((resultCode == Activity.RESULT_OK) && (data != null)) {
            final ArrayList<Observer> selectedObservers = data.getParcelableArrayListExtra(AbstractObserverListActivity.EXTRA_SELECTED_OBSERVERS);

            if (selectedObservers.size() > 0) {
                updateDefaultObserverPreference(selectedObservers.get(0));
            }
            else {
                updateDefaultObserverPreference(null);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id,
                                         Bundle args) {
        final String[] projection = {
                MainDatabaseHelper.ObserversColumns._ID,
                MainDatabaseHelper.ObserversColumns.LASTNAME,
                MainDatabaseHelper.ObserversColumns.FIRSTNAME
        };

        return new CursorLoader(getContext(),
                                mListener.getObserverLoaderUri(args.getLong(KEY_SELECTED_OBSERVER)),
                                projection,
                                null,
                                null,
                                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,
                               Cursor data) {
        if ((data != null) && data.moveToFirst()) {
            final Observer defaultObserver = new Observer(data.getLong(data.getColumnIndex(MainDatabaseHelper.ObserversColumns._ID)),
                                                          data.getString(data.getColumnIndex(MainDatabaseHelper.ObserversColumns.LASTNAME)),
                                                          data.getString(data.getColumnIndex(MainDatabaseHelper.ObserversColumns.FIRSTNAME)));
            updateDefaultObserverPreference(defaultObserver);
        }
        else {
            updateDefaultObserverPreference(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to do ...
    }

    private void updateDefaultObserverPreference(@Nullable final Observer defaultObserver) {
        mDefaultObserver = defaultObserver;

        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext())
                                                                 .edit();

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

    private void loadDefaultObserver() {
        long defaultObserverId = PreferenceManager.getDefaultSharedPreferences(getContext())
                                                  .getLong(KEY_PREFERENCE_DEFAULT_OBSERVER,
                                                           0);

        if (mListener.getObserverLoaderUri(defaultObserverId) == null) {
            return;
        }

        final Bundle args = new Bundle();
        args.putLong(KEY_SELECTED_OBSERVER,
                     defaultObserverId);

        getLoaderManager().restartLoader(0,
                                         args,
                                         this);
    }

    /**
     * Callback used by {@link PreferencesFragment}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    public interface OnPreferencesFragmentListener {

        @XmlRes
        int getPreferencesResourceId();

        @Nullable
        String getObserversIntentAction();

        /**
         * Gets the loader URI to use by the loader.
         *
         * @param ObserverId the default selected {@link Observer}
         *
         * @return the URI to use
         */
        @Nullable
        Uri getObserverLoaderUri(long ObserverId);

        @Nullable
        InputType getInputTypeFilter();

        @NonNull
        String getSummaryForMapDensity(int density);

        @NonNull
        String getAppVersion();
    }
}