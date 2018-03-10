package com.makina.ecrins.commons.ui.input.observers;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment;
import com.makina.ecrins.commons.ui.dialog.OnCalendarSetListener;
import com.makina.ecrins.commons.ui.input.IInputFragment;
import com.makina.ecrins.commons.ui.observers.AbstractObserverListActivity;
import com.makina.ecrins.commons.ui.pager.AbstractPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.commons.ui.settings.PreferencesFragment;
import com.makina.ecrins.commons.ui.widget.DatesAdapter;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Selected observer and current date as first {@code Fragment} used by
 * {@link AbstractPagerFragmentActivity}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public abstract class AbstractObserversAndDateInputFragment
        extends Fragment
        implements IValidateFragment,
                   IInputFragment,
                   LoaderManager.LoaderCallbacks<Cursor>,
                   OnCalendarSetListener {

    private static final String TAG = AbstractObserversAndDateInputFragment.class.getName();

    private static final String KEY_SELECTED_OBSERVER = "selected_observer";
    private static final String ALERT_DIALOG_CALENDAR_FRAGMENT = "ALERT_DIALOG_CALENDAR_FRAGMENT";

    private ObserverArrayAdapter mObserversAdapter;
    private DatesAdapter mDatesAdapter;

    private AbstractInput mInput;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final FragmentActivity activity = getActivity();

        if (activity == null) {
            return;
        }

        final DateTimePickerDialogFragment dialogFragment = (DateTimePickerDialogFragment) activity.getSupportFragmentManager()
                                                                                                   .findFragmentByTag(ALERT_DIALOG_CALENDAR_FRAGMENT);

        if (dialogFragment != null) {
            dialogFragment.setOnCalendarSetListener(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_observers_and_date,
                                     container,
                                     false);

        final Observer dummyObserver = new Observer(-1,
                                                    getString(R.string.observers_and_date_add_observer),
                                                    "");

        mObserversAdapter = new ObserverArrayAdapter(getContext(),
                                                     android.R.layout.simple_list_item_2);
        mObserversAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();

                // adds a dummy observer used as first entry in the listView to give user the ability to click and access to the observers list
                if (mObserversAdapter.isEmpty()) {
                    mObserversAdapter.setNotifyOnChange(false);
                    mObserversAdapter.add(dummyObserver);
                    mObserversAdapter.setNotifyOnChange(true);
                }
                else {
                    mObserversAdapter.setNotifyOnChange(false);
                    mObserversAdapter.remove(dummyObserver);
                    mObserversAdapter.setNotifyOnChange(true);
                }

                final FragmentActivity activity = getActivity();

                if (activity != null) {
                    ((AbstractPagerFragmentActivity) activity).validateCurrentPage();
                }
            }
        });

        mDatesAdapter = new DatesAdapter(getContext(),
                                         android.R.layout.simple_list_item_2,
                                         getDateFormatResourceId());

        ListView listSelectedObserversView = view.findViewById(R.id.listSelectedObservers);
        listSelectedObserversView.setAdapter(mObserversAdapter);
        listSelectedObserversView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                final Intent intent = new Intent(getObserversIntentAction());
                intent.putExtra(AbstractObserverListActivity.EXTRA_CHOICE_MODE,
                                ListView.CHOICE_MODE_MULTIPLE);
                intent.putExtra(AbstractObserverListActivity.EXTRA_INPUT_FILTER,
                                mInput.getType()
                                      .getKey());
                intent.putParcelableArrayListExtra(AbstractObserverListActivity.EXTRA_SELECTED_OBSERVERS,
                                                   new ArrayList<>(mInput.getObservers()
                                                                         .values()));
                startActivityForResult(intent,
                                       0);
            }
        });

        ListView listCurrentDateView = view.findViewById(R.id.listSelectedDate);
        listCurrentDateView.setAdapter(mDatesAdapter);
        listCurrentDateView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                final DateTimePickerDialogFragment.Builder builder = DateTimePickerDialogFragment.Builder.newInstance()
                                                                                                         .showTime(false)
                                                                                                         .maxDate(Calendar.getInstance()
                                                                                                                          .getTime());

                if (mInput != null) {
                    builder.currentDate(mInput.getDate());
                }

                final FragmentActivity activity = AbstractObserversAndDateInputFragment.this.getActivity();

                if (activity != null) {
                    final DateTimePickerDialogFragment datePickerDialogFragment = builder.create();
                    datePickerDialogFragment.setOnCalendarSetListener(AbstractObserversAndDateInputFragment.this);
                    datePickerDialogFragment.show(activity.getSupportFragmentManager(),
                                                  ALERT_DIALOG_CALENDAR_FRAGMENT);
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {
        if ((resultCode == Activity.RESULT_OK) && (data != null)) {
            final ArrayList<Observer> selectedObservers = data.getParcelableArrayListExtra(AbstractObserverListActivity.EXTRA_SELECTED_OBSERVERS);

            mObserversAdapter.clear();
            mInput.getObservers()
                  .clear();

            if (selectedObservers.size() > 0) {
                for (Observer observer : selectedObservers) {
                    mInput.getObservers()
                          .put(observer.getObserverId(),
                               observer);

                    mObserversAdapter.add(observer);
                }
            }
        }
    }

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_observers_and_date_title;
    }

    @Override
    public boolean getPagingEnabled() {
        return true;
    }

    @Override
    public boolean validate() {
        return (mInput != null) && !mInput.getObservers()
                                          .isEmpty();
    }

    @Override
    public void refreshView() {
        final AppCompatActivity activity = (AppCompatActivity) getActivity();

        if (activity == null) {
            return;
        }

        final ActionBar actionBar = activity.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }

        if (mInput == null) {
            Log.w(TAG,
                  "refreshView: null input");

            return;
        }

        mDatesAdapter.clear();
        mDatesAdapter.add(mInput.getDate());

        loadObservers();
    }

    @Override
    public void setInput(@NonNull AbstractInput input) {
        this.mInput = input;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id,
                                         Bundle args) {
        final String[] projection = {
                MainDatabaseHelper.ObserversColumns._ID,
                MainDatabaseHelper.ObserversColumns.LASTNAME,
                MainDatabaseHelper.ObserversColumns.FIRSTNAME
        };

        return new CursorLoader(getContext(),
                                getObserverLoaderUri(args.getLong(KEY_SELECTED_OBSERVER)),
                                projection,
                                null,
                                null,
                                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader,
                               Cursor data) {
        if ((data != null) && data.moveToFirst()) {
            final Observer defaultObserver = new Observer(data.getLong(data.getColumnIndex(MainDatabaseHelper.ObserversColumns._ID)),
                                                          data.getString(data.getColumnIndex(MainDatabaseHelper.ObserversColumns.LASTNAME)),
                                                          data.getString(data.getColumnIndex(MainDatabaseHelper.ObserversColumns.FIRSTNAME)));

            // adds the default observer if needed
            if (mInput.getObservers()
                      .isEmpty()) {
                mInput.getObservers()
                      .put(defaultObserver.getObserverId(),
                           defaultObserver);
            }
        }

        mObserversAdapter.clear();

        for (Observer observer : mInput.getObservers()
                                       .values()) {
            mObserversAdapter.add(observer);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // nothing to do ...
    }

    @Override
    public void onCalendarSet(Calendar calendar) {
        mInput.setDate(calendar.getTime());

        mDatesAdapter.clear();
        mDatesAdapter.add(calendar.getTime());
    }

    private void loadObservers() {
        if (isAdded()) {
            long defaultObserverId = PreferenceManager.getDefaultSharedPreferences(getContext())
                                                      .getLong(PreferencesFragment.KEY_PREFERENCE_DEFAULT_OBSERVER,
                                                               0);

            final Bundle args = new Bundle();
            args.putLong(KEY_SELECTED_OBSERVER,
                         defaultObserverId);

            getLoaderManager().restartLoader(0,
                                             args,
                                             this);
        }
    }

    @NonNull
    public abstract String getObserversIntentAction();

    /**
     * Gets the loader URI to use by the loader.
     *
     * @param ObserverId the default selected {@link Observer}
     *
     * @return the URI to use
     */
    @NonNull
    public abstract Uri getObserverLoaderUri(long ObserverId);

    public abstract int getDateFormatResourceId();
}
