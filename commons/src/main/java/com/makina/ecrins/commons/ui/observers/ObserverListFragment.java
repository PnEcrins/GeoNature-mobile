package com.makina.ecrins.commons.ui.observers;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
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
import com.makina.ecrins.commons.ui.widget.PinnedSectionListView;
import com.makina.ecrins.commons.util.ThemeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Custom {@code Fragment} to let the user to choose an {@link Observer} from the list.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class ObserverListFragment
        extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ObserverListFragment.class.getName();

    public static final String ARG_CHOICE_MODE = "ARG_CHOICE_MODE";
    public static final String ARG_INPUT_FILTER = "ARG_INPUT_FILTER";

    private static final String KEY_SELECTED_OBSERVERS = "KEY_SELECTED_OBSERVERS";
    private static final String KEY_SELECTED_OBSERVER_ID = "KEY_SELECTED_OBSERVER_ID";
    private static final String KEY_FILTER = "KEY_FILTER";

    private AlphabetSectionIndexerCursorAdapter mAdapter;

    private OnObserverListFragmentListener mListener;

    private final Map<Long, Observer> mSelectedObservers = new TreeMap<>();
    private long mSelectedObserverId;
    private String mFilter;

    private boolean mListShown;
    private View mProgressContainer;
    private View mListContainer;

    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode,
                                          Menu menu) {
            if (!mSelectedObservers.isEmpty()) {
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
                    mSelectedObservers.clear();
                    mAdapter.notifyDataSetChanged();
                    mode.finish();

                    ActivityCompat.invalidateOptionsMenu(getActivity());
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMode = null;
            mListener.onSelectedObservers(mSelectedObservers);
        }
    };

    private ActionMode mMode;

    public ObserverListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment {@link ObserverListFragment}.
     */
    @NonNull
    public static ObserverListFragment newInstance(int choiceMode,
                                                   int inputFilter,
                                                   @NonNull final List<Observer> selectedObservers) {
        final Bundle args = new Bundle();
        args.putInt(ARG_CHOICE_MODE,
                    choiceMode);
        args.putInt(ARG_INPUT_FILTER,
                    inputFilter);
        args.putParcelableArrayList(KEY_SELECTED_OBSERVERS,
                                    new ArrayList<>(selectedObservers));

        final ObserverListFragment fragment = new ObserverListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnObserverListFragmentListener) {
            mListener = (OnObserverListFragmentListener) context;
        }
        else {
            throw new RuntimeException(context.toString() + " must implement OnObserverListFragmentListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSelectedObservers.clear();

        final List<Observer> selectedObservers;

        if (savedInstanceState == null) {
            selectedObservers = getArguments().getParcelableArrayList(KEY_SELECTED_OBSERVERS);
        }
        else {
            selectedObservers = savedInstanceState.getParcelableArrayList(KEY_SELECTED_OBSERVERS);
            mSelectedObserverId = savedInstanceState.getLong(KEY_SELECTED_OBSERVER_ID);
            mFilter = savedInstanceState.getString(KEY_FILTER);
        }

        if (selectedObservers != null) {
            for (Observer observer : selectedObservers) {
                mSelectedObservers.put(observer.getObserverId(),
                                       observer);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_observers,
                                           container,
                                           false);

        mListContainer = view.findViewById(R.id.listContainer);
        mProgressContainer = view.findViewById(R.id.progressContainer);

        mListShown = true;

        return view;
    }

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view,
                            savedInstanceState);

        // we have a menu item to show in action bar
        setHasOptionsMenu(true);

        // give some text to display if there is no data
        ((TextView) view.findViewById(android.R.id.empty)).setText(getString(R.string.observers_no_data));
        getListView().setEmptyView(view.findViewById(android.R.id.empty));

        setChoiceMode(getArguments());

        // prepare the loader, either re-connect with an existing one, or start a new one
        startLoader(AbstractMainContentProvider.OBSERVERS,
                    false);

        if (getListView() instanceof PinnedSectionListView) {
            ((PinnedSectionListView) getListView()).setShadowVisible(false);
        }

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

        if (!mSelectedObservers.isEmpty()) {
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
            case android.R.id.home:
                mListener.onSelectedObservers(mSelectedObservers);
                return true;
            case 1:
                mSelectedObservers.clear();
                mAdapter.notifyDataSetChanged();
                ActivityCompat.invalidateOptionsMenu(getActivity());
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
        final CheckBox checkBox = (CheckBox) v.findViewById(android.R.id.checkbox);

        if (checkBox != null) {
            boolean isSelected = !checkBox.isChecked();
            checkBox.setChecked(isSelected);

            if (isSelected) {
                mSelectedObserverId = selectedObserverId;
                startLoader(AbstractMainContentProvider.OBSERVER_ID,
                            true);
            }
            else {
                if (isSingleChoice()) {
                    mSelectedObservers.clear();

                    if (mMode != null) {
                        mMode.finish();
                    }
                }
                else {
                    mSelectedObservers.remove(selectedObserverId);

                    if (mSelectedObservers.isEmpty()) {
                        // update menu
                        ActivityCompat.invalidateOptionsMenu(getActivity());

                        if (mMode != null) {
                            mMode.finish();
                        }
                    }
                    else {
                        if (mMode == null) {
                            mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);

                            if (mMode != null) {
                                mMode.setTitle(R.string.activity_observers_title);
                            }
                        }

                        mMode.setSubtitle(String.format(getString(R.string.action_title_item_selected),
                                                        mSelectedObservers.size()));
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(KEY_SELECTED_OBSERVERS,
                                        new ArrayList<>(mSelectedObservers.values()));
        outState.putLong(KEY_SELECTED_OBSERVER_ID,
                         mSelectedObserverId);
        outState.putString(KEY_FILTER,
                           mFilter);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    @Override
    public void setListShown(boolean shown) {
        setListShown(shown,
                     true);
    }

    @Override
    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown,
                     false);
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
                selectionArgs.add(Integer.toString(getArguments().getInt(ARG_INPUT_FILTER)));

                if (args.containsKey(KEY_FILTER) && (!TextUtils.isEmpty(args.getString(KEY_FILTER)))) {
                    selection.append(" AND (");
                    final String filter = "%" + args.getString(KEY_FILTER) + "%";

                    selection.append(MainDatabaseHelper.ObserversColumns.LASTNAME);
                    selection.append(" LIKE ?");
                    selection.append(" OR ");
                    selection.append(MainDatabaseHelper.ObserversColumns.FIRSTNAME);
                    selection.append(" LIKE ?)");
                    selectionArgs.add(filter);
                    selectionArgs.add(filter);
                }

                return new CursorLoader(getActivity(),
                                        mListener.getLoaderUri(id,
                                                               args.getLong(KEY_SELECTED_OBSERVER_ID)),
                                        projection,
                                        selection.toString(),
                                        selectionArgs.toArray(new String[selectionArgs.size()]),
                                        null);
            case AbstractMainContentProvider.OBSERVER_ID:
                return new CursorLoader(getActivity(),
                                        mListener.getLoaderUri(id,
                                                               args.getLong(KEY_SELECTED_OBSERVER_ID)),
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

                if (!mSelectedObservers.isEmpty()) {
                    if (mMode == null) {
                        mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);

                        if (mMode != null) {
                            mMode.setTitle(R.string.activity_observers_title);
                        }
                    }

                    mMode.setSubtitle(String.format(getString(R.string.action_title_item_selected),
                                                    mSelectedObservers.size()));
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
                        mSelectedObservers.clear();
                    }

                    mSelectedObservers.put(data.getLong(data.getColumnIndex(MainDatabaseHelper.ObserversColumns._ID)),
                                           new Observer(data.getLong(data.getColumnIndex(MainDatabaseHelper.ObserversColumns._ID)),
                                                        data.getString(data.getColumnIndex(MainDatabaseHelper.ObserversColumns.LASTNAME)),
                                                        data.getString(data.getColumnIndex(MainDatabaseHelper.ObserversColumns.FIRSTNAME))));

                    if (mMode == null) {
                        mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);

                        if (mMode != null) {
                            mMode.setTitle(R.string.activity_observers_title);
                        }
                    }

                    mMode.setSubtitle(String.format(getString(R.string.action_title_item_selected),
                                                    mSelectedObservers.size()));
                }
                else {
                    Log.w(TAG,
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

    private void setListShown(boolean shown,
                              boolean animate) {
        if (mListShown == shown) {
            return;
        }

        mListShown = shown;

        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                                                                               android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                                                                           android.R.anim.fade_in));
            }

            mProgressContainer.setVisibility(View.GONE);
            mListContainer.setVisibility(View.VISIBLE);
        }
        else {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                                                                               android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                                                                           android.R.anim.fade_out));
            }

            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.INVISIBLE);
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
                        if (mSelectedObservers.containsKey(getItemId(position))) {
                            view.setBackgroundColor(ThemeUtils.getAccentColor(getContext()));
                            ((CheckBox) view.findViewById(android.R.id.checkbox)).setChecked(true);
                        }
                        else {
                            view.setBackgroundColor(Color.TRANSPARENT);
                            ((CheckBox) view.findViewById(android.R.id.checkbox)).setChecked(false);
                        }
                    }

                    return view;
                }
            };

            // sets a custom ViewBinder for this adapter
            mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
                @Override
                public boolean setViewValue(View view,
                                            Cursor cursor,
                                            int columnIndex) {
                    String selectedColor = Integer.toHexString(ThemeUtils.getAccentColor(getContext()))
                                                  .substring(2);

                    switch (view.getId()) {
                        case android.R.id.text1:
                            String lastName = cursor.getString(columnIndex);
                            Spanned lastNameFilterFormat = (TextUtils.isEmpty(mFilter)) ? SpannedString.valueOf(lastName) : Html.fromHtml(lastName.replaceAll(Pattern.compile("(?i)(" + mFilter + ")")
                                                                                                                                                                     .pattern(),
                                                                                                                                                              "<font color=\"#" + selectedColor + "\">$1</font>"));
                            ((TextView) view).setText(lastNameFilterFormat);
                            return true;
                        case android.R.id.text2:
                            String firstName = cursor.getString(columnIndex);
                            Spanned firstNameFilterFormat = (TextUtils.isEmpty(mFilter)) ? SpannedString.valueOf(firstName) : Html.fromHtml(firstName.replaceAll(Pattern.compile("(?i)(" + mFilter + ")")
                                                                                                                                                                        .pattern(),
                                                                                                                                                                 "<font color=\"#" + selectedColor + "\">$1</font>"));
                            ((TextView) view).setText(firstNameFilterFormat);
                            return true;
                    }

                    return false;
                }
            });
            // sets a custom FilterQueryProvider for this adapter
            mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
                @Override
                public Cursor runQuery(CharSequence constraint) {
                    mFilter = (constraint != null) ? constraint.toString() : null;
                    startLoader(AbstractMainContentProvider.OBSERVERS,
                                true);

                    return mAdapter.getCursor();
                }
            });

            setListAdapter(mAdapter);

            // sets the current position to the first selected observer
            if (!mSelectedObservers.isEmpty()) {
                ArrayList<Observer> sortedObservers = new ArrayList<>(mSelectedObservers.values());
                Collections.sort(sortedObservers);
                getListView().setSelection(mAdapter.getItemPosition(sortedObservers.get(0)
                                                                                   .getObserverId()));
            }
        }
    }

    private void startLoader(int loaderId,
                             boolean restart) {
        final Bundle args = new Bundle();
        args.putString(KEY_FILTER,
                       mFilter);
        args.putLong(KEY_SELECTED_OBSERVER_ID,
                     mSelectedObserverId);

        if (restart) {
            getLoaderManager().restartLoader(loaderId,
                                             args,
                                             this);
        }
        else {
            getLoaderManager().initLoader(loaderId,
                                          args,
                                          this);
        }
    }

    private boolean isSingleChoice() {
        return getListView().getChoiceMode() == ListView.CHOICE_MODE_SINGLE;
    }

    private void setChoiceMode(final Bundle args) {
        getListView().setChoiceMode((args != null) ? args.getInt(ARG_CHOICE_MODE,
                                                                 ListView.CHOICE_MODE_SINGLE) : ListView.CHOICE_MODE_SINGLE);
    }

    /**
     * Callback used by {@link ObserverListFragment}.
     *
     * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
     */
    interface OnObserverListFragmentListener {

        /**
         * Gets the loader URI to use by the loader.
         *
         * @param id                 the ID whose loader is to be created (
         *                           {@link AbstractMainContentProvider#OBSERVERS} or
         *                           {@link AbstractMainContentProvider#OBSERVER_ID})
         * @param selectedObserverId the current selected {@link Observer}
         *
         * @return the URI to use
         */
        @NonNull
        Uri getLoaderUri(int id,
                         long selectedObserverId);

        /**
         * Called when {@link Observer}s were been selected.
         *
         * @param selectedObservers the selected {@link Observer}s
         */
        void onSelectedObservers(@NonNull final Map<Long, Observer> selectedObservers);
    }
}
