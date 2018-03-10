package com.makina.ecrins.commons.ui.observers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;

import com.makina.ecrins.commons.R;
import com.makina.ecrins.commons.content.AbstractMainContentProvider;
import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.Observer;
import com.makina.ecrins.commons.ui.widget.AlphabetSectionIndexerCursorAdapter;
import com.makina.ecrins.commons.util.ThemeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Lists all {@link com.makina.ecrins.commons.input.Observer}s.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 * @deprecated use {@link AbstractObserverListActivity} instead
 */
@SuppressWarnings("ALL")
@Deprecated
public abstract class AbstractObserversFragmentActivity
        extends AppCompatActivity {

    public static final String CHOICE_MODE = "choice_mode";

    private final Map<Long, Observer> mSelectedObservers = new TreeMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager fm = getSupportFragmentManager();

        // create the list fragment and add it as our sole content
        if (fm.findFragmentById(android.R.id.content) == null) {
            ObserversListFragment listFragment = new ObserversListFragment();
            listFragment.setArguments(getIntent().getExtras());
            fm.beginTransaction()
              .add(android.R.id.content,
                   listFragment)
              .commit();
        }
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

    /**
     * Gets a list of the current selected observers.
     *
     * @return a list as a {@link Map}
     */
    @NonNull
    public Map<Long, Observer> getSelectedObservers() {
        return mSelectedObservers;
    }

    public boolean isSingleChoice() {
        return (getIntent().getExtras() == null) || ((getIntent().getExtras()
                                                                 .containsKey(CHOICE_MODE)) && (getIntent().getExtras()
                                                                                                           .getInt(CHOICE_MODE) == ListView.CHOICE_MODE_SINGLE));
    }

    /**
     * Gets the current filter to apply to the loader.
     *
     * @return filter from {@link com.makina.ecrins.commons.input.InputType#getKey()}
     */
    public abstract int getFilter();

    /**
     * Gets the loader URI to use by the loader.
     *
     * @param id                 the ID whose loader is to be created (
     *                           {@link com.makina.ecrins.commons.content.AbstractMainContentProvider#OBSERVERS} or
     *                           {@link com.makina.ecrins.commons.content.AbstractMainContentProvider#OBSERVER_ID})
     * @param selectedObserverId the current selected {@link Observer}
     *
     * @return the URI to use
     */
    @NonNull
    public abstract Uri getLoaderUri(int id,
                                     long selectedObserverId);

    /**
     * Updates {@link #getSelectedObservers()} before loading the list view.
     */
    public abstract void initializeSelection();

    /**
     * Performs a custom action when a given {@link Observer} was selected or not.
     *
     * @return <code>true</code> if this custom action was successful and at least one {@link Observer} was selected, <code>false</code> otherwise
     */
    public abstract boolean updateSelection();

    @SuppressWarnings("deprecation")
    public static class ObserversListFragment
            extends ListFragment
            implements LoaderManager.LoaderCallbacks<Cursor> {
        private static final String KEY_FILTER = "filter";
        private static final String KEY_SELECTED_OBSERVER = "selected_observer";

        protected Bundle mSavedState;
        protected AlphabetSectionIndexerCursorAdapter mAdapter = null;
        protected final Handler mHandler = new Handler();

        private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode,
                                              Menu menu) {
                if (!((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                        .isEmpty()) {
                    final MenuItem menuItem = menu.add(Menu.NONE,
                                                       0,
                                                       Menu.NONE,
                                                       R.string.action_unselect_all)
                                                  .setIcon(R.drawable.ic_action_unselect_all);
                    MenuItemCompat.setShowAsAction(menuItem,
                                                   MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
                }

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode,
                                               Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode,
                                               final MenuItem item) {
                switch (item.getItemId()) {
                    case 0:
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                                   .clear();
                                ((AbstractObserversFragmentActivity) getActivity()).updateSelection();

                                mAdapter.notifyDataSetChanged();
                                mode.finish();

                                ActivityCompat.invalidateOptionsMenu(getActivity());
                            }
                        });
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mMode = null;

                if (((AbstractObserversFragmentActivity) getActivity()).updateSelection()) {
                    getActivity().finish();
                }
            }
        };

        private ActionMode mMode;

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
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // we have a menu item to show in action bar
            setHasOptionsMenu(true);

            // give some text to display if there is no data
            setEmptyText(getString(R.string.observers_no_data));

            setChoiceMode(getArguments());

            ((AbstractObserversFragmentActivity) getActivity()).initializeSelection();

            // prepare the loader, either re-connect with an existing one, or start a new one
            getLoaderManager().initLoader(AbstractMainContentProvider.OBSERVERS,
                                          mSavedState,
                                          this);

            // start out with a progress indicator
            setListShown(false);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu,
                                        MenuInflater inflater) {
            final MenuItem menuItemSearch = menu.add(Menu.NONE,
                                                     0,
                                                     Menu.NONE,
                                                     R.string.action_search);
            menuItemSearch.setIcon(R.drawable.ic_action_search);
            MenuItemCompat.setShowAsAction(menuItemSearch,
                                           MenuItemCompat.SHOW_AS_ACTION_ALWAYS | MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

            final SearchView searchView = new SearchView(getActivity());
            searchView.setQueryHint(getString(R.string.observers_search_hint));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN,
                                        0);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    mAdapter.getFilter()
                            .filter(!TextUtils.isEmpty(newText) ? newText : null);
                    return true;
                }
            });

            MenuItemCompat.setActionView(menuItemSearch,
                                         searchView);

            if (!((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                    .isEmpty()) {
                final MenuItem menuItem = menu.add(Menu.NONE,
                                                   1,
                                                   Menu.NONE,
                                                   R.string.action_unselect_all)
                                              .setIcon(R.drawable.ic_action_unselect_all);
                MenuItemCompat.setShowAsAction(menuItem,
                                               MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case 1:
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                               .clear();
                            ((AbstractObserversFragmentActivity) getActivity()).updateSelection();

                            mAdapter.notifyDataSetChanged();
                            ActivityCompat.invalidateOptionsMenu(getActivity());
                        }
                    });
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onListItemClick(ListView l,
                                    View v,
                                    int position,
                                    long id) {
            long selectedObserverId = mAdapter.getItemId(position);

            Log.d(AbstractObserversFragmentActivity.class.getName(),
                  "onListItemClick : " + selectedObserverId);

            CheckBox checkBox = v.findViewById(android.R.id.checkbox);

            if (checkBox != null) {
                boolean isSelected = !checkBox.isChecked();
                checkBox.setChecked(isSelected);
                v.setBackgroundColor(checkBox.isChecked() ? ThemeUtils.getAccentColor(getContext()) : Color.TRANSPARENT);

                if (isSelected) {
                    mSavedState.putLong(KEY_SELECTED_OBSERVER,
                                        selectedObserverId);
                    getLoaderManager().restartLoader(AbstractMainContentProvider.OBSERVER_ID,
                                                     mSavedState,
                                                     this);
                }
                else {
                    if (isSingleChoice()) {
                        ((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                           .clear();

                        if (mMode != null) {
                            mMode.finish();
                        }
                    }
                    else {
                        ((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                           .remove(selectedObserverId);

                        if (((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                               .isEmpty()) {
                            // update menu
                            ActivityCompat.invalidateOptionsMenu(getActivity());

                            if (mMode != null) {
                                mMode.finish();
                            }
                        }
                        else {
                            if (mMode == null) {
                                mMode = ((AbstractObserversFragmentActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                            }

                            mMode.setTitle(String.format(getString(R.string.action_title_item_selected),
                                                         ((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                                                            .size()));
                        }
                    }

                    ((AbstractObserversFragmentActivity) getActivity()).updateSelection();
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

            switch (id) {
                case AbstractMainContentProvider.OBSERVERS:
                    final StringBuilder selection = new StringBuilder();
                    final List<String> selectionArgs = new ArrayList<>();

                    // adding filter
                    selection.append("((");
                    selection.append(MainDatabaseHelper.ObserversColumns.FILTER);
                    selection.append(" & ?) != 0)");
                    selectionArgs.add(Integer.toString(((AbstractObserversFragmentActivity) getActivity()).getFilter()));

                    if (args.containsKey(KEY_FILTER) && (args.getString(KEY_FILTER) != null)) {
                        selection.append(" AND (");
                        String filter = "%" + args.getString(KEY_FILTER) + "%";

                        selection.append(MainDatabaseHelper.ObserversColumns.LASTNAME);
                        selection.append(" LIKE ?");
                        selection.append(" OR ");
                        selection.append(MainDatabaseHelper.ObserversColumns.FIRSTNAME);
                        selection.append(" LIKE ?)");
                        selectionArgs.add(filter);
                        selectionArgs.add(filter);
                    }

                    return new CursorLoader(getActivity(),
                                            ((AbstractObserversFragmentActivity) getActivity()).getLoaderUri(id,
                                                                                                             args.getLong(KEY_SELECTED_OBSERVER)),
                                            projection,
                                            selection.toString(),
                                            selectionArgs.toArray(new String[selectionArgs.size()]),
                                            null);
                case AbstractMainContentProvider.OBSERVER_ID:
                    return new CursorLoader(getActivity(),
                                            ((AbstractObserversFragmentActivity) getActivity()).getLoaderUri(id,
                                                                                                             args.getLong(KEY_SELECTED_OBSERVER)),
                                            projection,
                                            null,
                                            null,
                                            null);
                default:
                    throw new IllegalArgumentException("Unknown loader : " + id);
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader,
                                   Cursor data) {
            switch (loader.getId()) {
                case AbstractMainContentProvider.OBSERVERS:
                    if (mAdapter == null) {
                        initializeAdapter(data);
                    }
                    else {
                        mAdapter.swapCursor(data);
                    }

                    getListView().setFastScrollEnabled(true);
                    getListView().setScrollingCacheEnabled(true);

                    if (!((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                            .isEmpty()) {
                        if (mMode == null) {
                            mMode = ((AbstractObserversFragmentActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                        }

                        mMode.setTitle(String.format(getString(R.string.action_title_item_selected),
                                                     ((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                                                        .size()));
                        // update menu
                        ActivityCompat.invalidateOptionsMenu(getActivity());
                    }

                    if (isResumed()) {
                        setListShown(true);
                    }
                    else {
                        setListShownNoAnimation(true);
                    }

                    break;
                case AbstractMainContentProvider.OBSERVER_ID:
                    if (data.moveToFirst()) {
                        if (isSingleChoice()) {
                            ((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                               .clear();
                        }

                        ((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                           .put(data.getLong(data.getColumnIndex(MainDatabaseHelper.ObserversColumns._ID)),
                                                                                new Observer(data.getLong(data.getColumnIndex(MainDatabaseHelper.ObserversColumns._ID)),
                                                                                             data.getString(data.getColumnIndex(MainDatabaseHelper.ObserversColumns.LASTNAME)),
                                                                                             data.getString(data.getColumnIndex(MainDatabaseHelper.ObserversColumns.FIRSTNAME))));
                        ((AbstractObserversFragmentActivity) getActivity()).updateSelection();

                        if (mMode == null) {
                            mMode = ((AbstractObserversFragmentActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                        }
                        mMode.setTitle(String.format(getString(R.string.action_title_item_selected),
                                                     ((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                                                        .size()));
                    }
                    else {
                        Log.w(AbstractObserversFragmentActivity.class.getName(),
                              "onLoadFinished, unable to fetch the selected observer from database");
                    }

                    mAdapter.notifyDataSetChanged();
                    ActivityCompat.invalidateOptionsMenu(getActivity());

                    break;
                default:
                    break;
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            switch (loader.getId()) {
                case AbstractMainContentProvider.OBSERVERS:
                    // data is not available anymore, delete reference
                    if (mAdapter != null) {
                        mAdapter.swapCursor(null);
                    }

                    break;
                default:
                    break;
            }
        }

        private void initializeAdapter(Cursor cursor) {
            if (mAdapter == null) {
                // create the adapter we will use to display the loaded data
                mAdapter = new AlphabetSectionIndexerCursorAdapter(getActivity(),
                                                                   R.layout.list_item_2_multiple_choice,
                                                                   R.plurals.observers_count,
                                                                   cursor,
                                                                   MainDatabaseHelper.ObserversColumns.LASTNAME,
                                                                   new String[] {
                                                                           MainDatabaseHelper.ObserversColumns.LASTNAME,
                                                                           MainDatabaseHelper.ObserversColumns.FIRSTNAME
                                                                   },
                                                                   new int[] {
                                                                           android.R.id.text1,
                                                                           android.R.id.text2
                                                                   },
                                                                   0) {
                    @Override
                    public View getView(int position,
                                        View convertView,
                                        ViewGroup parent) {
                        View view = super.getView(position,
                                                  convertView,
                                                  parent);

                        if (getItemViewType(position) == TYPE_NORMAL) {
                            final CheckBox checkBox = view.findViewById(android.R.id.checkbox);
                            checkBox.setChecked(((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                                                   .containsKey(getItemId(position)));

                            view.setBackgroundColor(checkBox.isChecked() ? ThemeUtils.getAccentColor(getContext()) : Color.TRANSPARENT);
                        }

                        return view;
                    }
                };
                // sets a custom ViewBinder for this adapter
                mAdapter.setViewBinder(new ViewBinder() {
                    @Override
                    public boolean setViewValue(View view,
                                                Cursor cursor,
                                                int columnIndex) {
                        String filter = mSavedState.getString(KEY_FILTER);
                        String selectedColor = Integer.toHexString(ThemeUtils.getAccentColor(getContext()))
                                                      .substring(2);

                        switch (view.getId()) {
                            case android.R.id.text1:
                                String lastname = cursor.getString(columnIndex);
                                Spanned lastnameFilterFormat = (filter != null) ? Html.fromHtml(lastname.replaceAll(Pattern.compile("(?i)(" + filter + ")")
                                                                                                                           .pattern(),
                                                                                                                    "<font color=\"#" + selectedColor + "\">$1</font>")) : SpannedString.valueOf(lastname);
                                ((TextView) view).setText(lastnameFilterFormat);
                                return true;
                            case android.R.id.text2:
                                String firstname = cursor.getString(columnIndex);
                                Spanned firstnameFilterFormat = (filter != null) ? Html.fromHtml(firstname.replaceAll(Pattern.compile("(?i)(" + filter + ")")
                                                                                                                             .pattern(),
                                                                                                                      "<font color=\"#" + selectedColor + "\">$1</font>")) : SpannedString.valueOf(firstname);
                                ((TextView) view).setText(firstnameFilterFormat);
                                return true;
                        }

                        return false;
                    }
                });
                // sets a custom FilterQueryProvider for this adapter
                mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                    @Override
                    public Cursor runQuery(CharSequence constraint) {
                        mSavedState.putString(KEY_FILTER,
                                              (constraint != null) ? constraint.toString() : null);
                        getLoaderManager().restartLoader(AbstractMainContentProvider.OBSERVERS,
                                                         mSavedState,
                                                         ObserversListFragment.this);

                        return mAdapter.getCursor();
                    }
                });

                setListAdapter(mAdapter);

                // sets the current position to the first selected observer
                if (!((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                        .isEmpty()) {
                    ArrayList<Observer> sortedObservers = new ArrayList<>(((AbstractObserversFragmentActivity) getActivity()).getSelectedObservers()
                                                                                                                             .values());
                    Collections.sort(sortedObservers);
                    getListView().setSelection(mAdapter.getItemPosition(sortedObservers.get(0)
                                                                                       .getObserverId()));
                }
            }
        }

        private boolean isSingleChoice() {
            return getListView().getChoiceMode() == ListView.CHOICE_MODE_SINGLE;
        }

        private void setChoiceMode(final Bundle args) {
            getListView().setChoiceMode((args != null) ? args.getInt(CHOICE_MODE,
                                                                     ListView.CHOICE_MODE_SINGLE) : ListView.CHOICE_MODE_SINGLE);
        }
    }
}
