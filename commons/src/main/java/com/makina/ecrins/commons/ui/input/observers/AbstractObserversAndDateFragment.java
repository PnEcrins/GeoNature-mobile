package com.makina.ecrins.commons.ui.input.observers;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.commons.ui.dialog.DateTimePickerDialogFragment;
import com.makina.ecrins.commons.ui.dialog.OnCalendarSetListener;
import com.makina.ecrins.commons.ui.observers.AbstractObserversFragmentActivity;
import com.makina.ecrins.commons.ui.pager.AbstractPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.commons.ui.widget.DatesAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Selected observer and current date as first {@code Fragment} used by
 * {@link com.makina.ecrins.commons.ui.pager.AbstractPagerFragmentActivity}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 * @deprecated use {@link AbstractObserversAndDateInputFragment} instead
 */
@Deprecated
public abstract class AbstractObserversAndDateFragment
        extends Fragment
        implements IValidateFragment,
                   LoaderManager.LoaderCallbacks<Cursor>,
                   OnCalendarSetListener {
    private static final String ALERT_DIALOG_CALENDAR_FRAGMENT = "alert_dialog_calendar_fragment";
    private static final String KEY_SELECTED_OBSERVERS = "selected_observers";

    protected SimpleCursorAdapter mObserversAdapter;
    protected static DatesAdapter mDatesAdapter;

    private Bundle mSavedState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mSavedState = new Bundle();
        }
        else {
            mSavedState = savedInstanceState;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(AbstractObserversAndDateFragment.class.getName(),
              "onCreateView");

        View view = inflater.inflate(R.layout.fragment_observers_and_date,
                                     container,
                                     false);

        mObserversAdapter = new SimpleCursorAdapter(getActivity(),
                                                    android.R.layout.simple_list_item_2,
                                                    null,
                                                    new String[] {
                                                            MainDatabaseHelper.ObserversColumns.LASTNAME,
                                                            MainDatabaseHelper.ObserversColumns.FIRSTNAME
                                                    },
                                                    new int[] {
                                                            android.R.id.text1,
                                                            android.R.id.text2
                                                    },
                                                    0);
        mDatesAdapter = new DatesAdapter(getActivity(),
                                         android.R.layout.simple_list_item_2,
                                         getDateFormatResourceId());

        ListView listSelectedObserversView = (ListView) view.findViewById(R.id.listSelectedObservers);
        listSelectedObserversView.setAdapter(mObserversAdapter);
        listSelectedObserversView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                Intent intent = new Intent(getObserversIntentAction());
                intent.putExtra(AbstractObserversFragmentActivity.CHOICE_MODE,
                                ListView.CHOICE_MODE_MULTIPLE);
                startActivity(intent);
            }
        });

        ListView listCurrenDateView = (ListView) view.findViewById(R.id.listSelectedDate);
        listCurrenDateView.setAdapter(mDatesAdapter);
        listCurrenDateView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                final DateTimePickerDialogFragment datePickerDialogFragment = DateTimePickerDialogFragment.Builder.newInstance()
                                                                                                                  .showTime(false)
                                                                                                                  .maxDate(Calendar.getInstance()
                                                                                                                                   .getTime())
                                                                                                                  .create();
                datePickerDialogFragment.setOnCalendarSetListener(AbstractObserversAndDateFragment.this);
                datePickerDialogFragment.show(AbstractObserversAndDateFragment.this.getActivity()
                                                                                   .getSupportFragmentManager(),
                                              ALERT_DIALOG_CALENDAR_FRAGMENT);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(AbstractObserversAndDateFragment.class.getName(),
              "onResume");

        // adds the default observer if needed
        if (getSelectedObservers().isEmpty()) {
            if (getDefaultObserver() != null) {
                getSelectedObservers().put(getDefaultObserver().getObserverId(),
                                           getDefaultObserver());
            }
        }

        // adds a dummy observer used as first entry in the listView to give user the ability to click and access to the observers list
        if (getSelectedObservers().isEmpty()) {
            MatrixCursor matrixCursor = new MatrixCursor(new String[] {
                    MainDatabaseHelper.ObserversColumns._ID,
                    MainDatabaseHelper.ObserversColumns.LASTNAME,
                    MainDatabaseHelper.ObserversColumns.FIRSTNAME
            });
            matrixCursor.addRow(new Object[] {
                    -1,
                    getString(R.string.observers_and_date_add_observer),
                    ""
            });
            mObserversAdapter.swapCursor(matrixCursor);
        }
        else {
            mSavedState.putParcelableArrayList(KEY_SELECTED_OBSERVERS,
                                               new ArrayList<>(getSelectedObservers().values()));
            getLoaderManager().restartLoader(0,
                                             mSavedState,
                                             this);
        }

        final DateTimePickerDialogFragment dialogFragment = (DateTimePickerDialogFragment) getActivity().getSupportFragmentManager()
                                                                                                        .findFragmentByTag(ALERT_DIALOG_CALENDAR_FRAGMENT);

        if (dialogFragment != null) {
            dialogFragment.setOnCalendarSetListener(this);
        }

        mDatesAdapter.clear();
        mDatesAdapter.add(getSelectedDate());

        ((AbstractPagerFragmentActivity) getActivity()).validateCurrentPage();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putAll(mSavedState);

        super.onSaveInstanceState(outState);
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
        return !getSelectedObservers().isEmpty();
    }

    @Override
    public void refreshView() {
        Log.d(AbstractObserversAndDateFragment.class.getName(),
              "refreshView");

        ((AbstractPagerFragmentActivity) getActivity()).getSupportActionBar()
                                                       .setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id,
                                         Bundle args) {
        String[] projection = {
                MainDatabaseHelper.ObserversColumns._ID,
                MainDatabaseHelper.ObserversColumns.LASTNAME,
                MainDatabaseHelper.ObserversColumns.FIRSTNAME
        };

        final List<Observer> selectedObservers = args.getParcelableArrayList(KEY_SELECTED_OBSERVERS);

        final StringBuilder selection = new StringBuilder();
        final List<String> selectionArgs = new ArrayList<>();
        selection.append(MainDatabaseHelper.ObserversColumns._ID);
        selection.append(" IN (");

        if ((selectedObservers == null) || selectedObservers.isEmpty()) {
            selection.append("?)");
            selectionArgs.add(Long.valueOf(-1)
                                  .toString());
        }
        else {
            for (Observer observer : selectedObservers) {
                selection.append("?,");
                selectionArgs.add(Long.valueOf(observer.getObserverId())
                                      .toString());
            }

            selection.replace(selection.length() - 1,
                              selection.length(),
                              ")");
        }

        Log.d(AbstractObserversAndDateFragment.class.getName(),
              "selection " + selection);
        Log.d(AbstractObserversAndDateFragment.class.getName(),
              "selectionArgs " + selectionArgs.toString());

        return new CursorLoader(getActivity(),
                                getLoaderUri(),
                                projection,
                                selection.toString(),
                                selectionArgs.toArray(new String[selectionArgs.size()]),
                                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,
                               Cursor data) {
        Log.d(AbstractObserversAndDateFragment.class.getName(),
              "onLoadFinished, observers selected : " + data.getCount());

        mObserversAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        mObserversAdapter.swapCursor(null);
    }

    @Override
    public void onCalendarSet(Calendar calendar) {
        updateSelectedDate(calendar.getTime());

        mDatesAdapter.clear();
        mDatesAdapter.add(calendar.getTime());
    }

    public abstract String getObserversIntentAction();

    public abstract int getDateFormatResourceId();

    public abstract Observer getDefaultObserver();

    public abstract Map<Long, Observer> getSelectedObservers();

    public abstract Date getSelectedDate();

    public abstract void updateSelectedDate(Date pDate);

    public abstract Uri getLoaderUri();
}
