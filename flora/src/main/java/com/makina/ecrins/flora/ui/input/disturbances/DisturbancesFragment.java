package com.makina.ecrins.flora.ui.input.disturbances;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CheckedTextView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.TextView;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.ui.AbstractBaseActivity;
import com.makina.ecrins.commons.ui.input.IInputFragment;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.commons.ui.widget.AbstractGroupsCursorAdapter;
import com.makina.ecrins.flora.BuildConfig;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.content.MainContentProvider;
import com.makina.ecrins.flora.input.Input;
import com.makina.ecrins.flora.input.Taxon;
import com.makina.ecrins.flora.ui.input.PagerFragmentActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Disturbances as an {@code ExpendableListView}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class DisturbancesFragment
        extends Fragment
        implements IValidateFragment,
                   IInputFragment,
                   LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = DisturbancesFragment.class.getName();
    private static final String KEY_SELECTED_DISTURBANCE_IDS = "KEY_SELECTED_DISTURBANCE_IDS";

    private AbstractGroupsCursorAdapter<String> mAdapter;

    private View mProgressContainer;
    private View mListContainer;
    private ExpandableListView mExpandableListView;
    private TextView mTextViewEmpty;

    private final Handler mHandler = new Handler();

    private Input mInput;
    private final List<String> mSelectedClassifications = new ArrayList<>();

    private boolean mListShown;
    private boolean mIsVisibleToUser = false;
    private final AtomicBoolean mBackButtonEvent = new AtomicBoolean();
    private final AtomicBoolean mRefreshActionMode = new AtomicBoolean();

    private ActionMode mMode;
    private final ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onPrepareActionMode(ActionMode mode,
                                           Menu menu) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMode = null;

            if ((mInput == null) || !mIsVisibleToUser) {
                return;
            }

            if (mRefreshActionMode.getAndSet(false)) {
                return;
            }

            final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

            if (!mBackButtonEvent.get() &&
                    (currentSelectedTaxon != null) &&
                    (currentSelectedTaxon.getCurrentSelectedArea() != null) &&
                    !currentSelectedTaxon.getCurrentSelectedArea()
                                         .getSelectedDisturbances()
                                         .isEmpty()) {
                ((PagerFragmentActivity) getActivity()).goToNextPage();
            }

            if (mBackButtonEvent.getAndSet(false) &&
                    (currentSelectedTaxon != null) &&
                    (currentSelectedTaxon.getCurrentSelectedArea() != null)) {
                currentSelectedTaxon.getCurrentSelectedArea()
                                    .getSelectedDisturbances()
                                    .clear();
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode,
                                          Menu menu) {
            if (mInput == null) {
                return false;
            }

            final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

            if ((currentSelectedTaxon != null) &&
                    (currentSelectedTaxon.getCurrentSelectedArea() != null) &&
                    !currentSelectedTaxon.getCurrentSelectedArea()
                                         .getSelectedDisturbances()
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
        public boolean onActionItemClicked(final ActionMode mode,
                                           final MenuItem item) {
            switch (item.getItemId()) {
                case 0:
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mInput == null) {
                                return;
                            }

                            final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

                            if ((currentSelectedTaxon != null) && (currentSelectedTaxon.getCurrentSelectedArea() != null)) {
                                currentSelectedTaxon.getCurrentSelectedArea()
                                                    .getSelectedDisturbances()
                                                    .clear();
                            }

                            mAdapter.notifyDataSetChanged();
                            mode.finish();
                        }
                    });

                    return true;
                default:
                    return false;
            }
        }
    };

    private final AbstractBaseActivity.OnDispatchKeyEventListener mOnDispatchKeyEventListener = new AbstractBaseActivity.OnDispatchKeyEventListener() {
        @Override
        public boolean dispatchKeyEvent(KeyEvent event) {
            // catch back button event when ActionMode is still active
            if (mMode != null) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && (event.getAction() == KeyEvent.ACTION_UP || event.getAction() == KeyEvent.ACTION_DOWN)) {
                    mBackButtonEvent.set(true);

                    return true;
                }
            }

            return false;
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_expandable_list,
                                           container,
                                           false);

        mProgressContainer = view.findViewById(R.id.progressContainer);
        mListContainer = view.findViewById(R.id.listContainer);
        mExpandableListView = view.findViewById(R.id.expandableListView);
        mTextViewEmpty = view.findViewById(R.id.textViewEmpty);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              Bundle savedInstanceState) {
        super.onViewCreated(view,
                            savedInstanceState);

        setListShown(false,
                     false);

        mTextViewEmpty.setText(R.string.disturbances_no_data);

        mAdapter = new AbstractGroupsCursorAdapter<String>(getActivity(),
                                                           R.layout.simple_expandable_list_item_1,
                                                           new String[] {
                                                                   MainDatabaseHelper.DisturbancesColumns.CLASSIFICATION
                                                           },
                                                           new int[] {
                                                                   android.R.id.text1
                                                           },
                                                           R.layout.simple_list_item_multiple_choice,
                                                           new String[] {
                                                                   MainDatabaseHelper.DisturbancesColumns.DESCRIPTION
                                                           },
                                                           new int[] {
                                                                   android.R.id.text1
                                                           }) {
            @Override
            public View getGroupView(int groupPosition,
                                     boolean isExpanded,
                                     View convertView,
                                     ViewGroup parent) {
                final View view = super.getGroupView(groupPosition,
                                                     isExpanded,
                                                     convertView,
                                                     parent);
                if (!mSelectedClassifications.isEmpty()) {
                    final Cursor cursor = mAdapter.getGroup(groupPosition);
                    final String classification = cursor.getString(cursor.getColumnIndex(MainDatabaseHelper.DisturbancesColumns.CLASSIFICATION));

                    if (!TextUtils.isEmpty(classification) && mSelectedClassifications.contains(classification)) {
                        mExpandableListView.smoothScrollToPosition(mExpandableListView.getFlatListPosition(ExpandableListView.getPackedPositionForGroup(groupPosition)));
                        mExpandableListView.expandGroup(groupPosition);
                    }
                }

                return view;
            }

            @Override
            public View getChildView(int groupPosition,
                                     int childPosition,
                                     boolean isLastChild,
                                     View convertView,
                                     ViewGroup parent) {
                final View view = super.getChildView(groupPosition,
                                                     childPosition,
                                                     isLastChild,
                                                     convertView,
                                                     parent);

                if (mInput == null) {
                    return view;
                }

                final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

                if ((currentSelectedTaxon != null) && (currentSelectedTaxon.getCurrentSelectedArea() != null)) {
                    final CheckedTextView checkedTextView = view.findViewById(android.R.id.text1);
                    final Cursor cursor = mAdapter.getChild(groupPosition,
                                                            childPosition);
                    final long childId = cursor.getInt(cursor.getColumnIndex(MainDatabaseHelper.DisturbancesColumns.CODE));
                    final boolean selected = currentSelectedTaxon.getCurrentSelectedArea()
                                                                 .getSelectedDisturbances()
                                                                 .contains(childId);
                    checkedTextView.setChecked(selected);
                }

                return view;
            }

            @Override
            protected LoaderManager getLoaderManager() {
                return DisturbancesFragment.this.getLoaderManager();
            }

            @Override
            protected String getGroupId(Cursor groupCursor) {
                return groupCursor.getString(groupCursor.getColumnIndex(MainDatabaseHelper.DisturbancesColumns.CLASSIFICATION));
            }

            @Override
            protected LoaderCallbacks<Cursor> getLoaderCallbacks() {
                return DisturbancesFragment.this;
            }
        };

        mExpandableListView.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent,
                                        View v,
                                        int groupPosition,
                                        long id) {

                mAdapter.setExpendAllGroups(false);
                mSelectedClassifications.clear();

                return false;
            }
        });

        mExpandableListView.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent,
                                        View v,
                                        int groupPosition,
                                        int childPosition,
                                        long id) {
                if (mInput == null) {
                    return false;
                }

                final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

                if ((currentSelectedTaxon != null) && (currentSelectedTaxon.getCurrentSelectedArea() != null)) {
                    final CheckedTextView checkedTextView = v.findViewById(android.R.id.text1);
                    final Cursor cursor = mAdapter.getChild(groupPosition,
                                                            childPosition);
                    final long childId = cursor.getInt(cursor.getColumnIndex(MainDatabaseHelper.DisturbancesColumns.CODE));

                    if (currentSelectedTaxon.getCurrentSelectedArea()
                                            .getSelectedDisturbances()
                                            .contains(childId)) {
                        currentSelectedTaxon.getCurrentSelectedArea()
                                            .getSelectedDisturbances()
                                            .remove(childId);
                    }
                    else {
                        currentSelectedTaxon.getCurrentSelectedArea()
                                            .getSelectedDisturbances()
                                            .add(childId);
                    }

                    checkedTextView.setChecked(currentSelectedTaxon.getCurrentSelectedArea()
                                                                   .getSelectedDisturbances()
                                                                   .contains(childId));

                    updateActionMode();
                }

                return true;
            }
        });

        mExpandableListView.setEmptyView(mTextViewEmpty);
        mExpandableListView.setAdapter(mAdapter);
    }

    @Override
    public void onDestroyView() {
        mListShown = false;

        mProgressContainer = null;
        mExpandableListView = null;
        mTextViewEmpty = null;

        super.onDestroyView();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        mIsVisibleToUser = isVisibleToUser;

        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                  "setUserVisibleHint: " + mIsVisibleToUser);
        }

        updateActionMode();
    }

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_disturbances_title;
    }

    @Override
    public boolean getPagingEnabled() {
        return true;
    }

    @Override
    public boolean validate() {
        // Always true
        return true;
    }

    @Override
    public void refreshView() {
        final Bundle args = new Bundle();

        ((AbstractBaseActivity) getActivity()).setOnDispatchKeyEventListener(mOnDispatchKeyEventListener);

        if (mMode != null) {
            mRefreshActionMode.set(true);
            mMode.finish();
        }

        if (mInput != null) {
            final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

            if ((currentSelectedTaxon != null) && (currentSelectedTaxon.getCurrentSelectedArea() != null) && !currentSelectedTaxon.getCurrentSelectedArea()
                                                                                                                                  .getSelectedDisturbances()
                                                                                                                                  .isEmpty()) {
                final List<Long> selectedDisturbances = currentSelectedTaxon.getCurrentSelectedArea()
                                                                            .getSelectedDisturbances();
                long[] selectedIds = new long[selectedDisturbances.size()];

                for (int i = 0; i < selectedDisturbances.size(); i++) {
                    selectedIds[i] = selectedDisturbances.get(i);
                }

                args.putLongArray(KEY_SELECTED_DISTURBANCE_IDS,
                                  selectedIds);
            }
        }

        if (isAdded()) {
            // prepare the loader, either re-connect with an existing one, or start a new one
            getLoaderManager().restartLoader(args.containsKey(KEY_SELECTED_DISTURBANCE_IDS) ? -2 : -1,
                                             args,
                                             this);

            // start out with a progress indicator
            setListShown(false,
                         true);
        }
    }

    @Override
    public void setInput(@NonNull AbstractInput input) {
        this.mInput = (Input) input;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id,
                                         Bundle args) {
        final String[] projection = {
                MainDatabaseHelper.DisturbancesColumns._ID,
                MainDatabaseHelper.DisturbancesColumns.CODE,
                MainDatabaseHelper.DisturbancesColumns.CLASSIFICATION,
                MainDatabaseHelper.DisturbancesColumns.DESCRIPTION
        };

        if (id < 0) {
            StringBuilder selection = new StringBuilder();

            if (args != null) {
                final long[] selectedIds = args.getLongArray(KEY_SELECTED_DISTURBANCE_IDS);

                if ((selectedIds != null) && (selectedIds.length > 0)) {
                    final List<Long> selectedIdsAsArray = new ArrayList<>();

                    for (long selectedId : selectedIds) {
                        selectedIdsAsArray.add(selectedId);
                    }

                    selection.append(MainDatabaseHelper.DisturbancesColumns.CODE);
                    selection.append(" IN (");
                    selection.append(TextUtils.join(",",
                                                    selectedIdsAsArray));
                    selection.append(")");
                }
            }

            // group cursor
            return new CursorLoader(getContext(),
                                    MainContentProvider.CONTENT_DISTURBANCES_CLASSIFICATIONS_URI,
                                    projection,
                                    (selection.length() == 0) ? null : selection.toString(),
                                    null,
                                    null);
        }
        else {
            // children cursor
            return new CursorLoader(getContext(),
                                    Uri.withAppendedPath(MainContentProvider.CONTENT_DISTURBANCES_CLASSIFICATIONS_URI,
                                                         args.getString(AbstractGroupsCursorAdapter.KEY_SELECTED_GROUP_ID)),
                                    projection,
                                    null,
                                    null,
                                    null);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader,
                               Cursor data) {
        if (loader.getId() == -2) {
            mSelectedClassifications.clear();

            try {
                if (data != null) {
                    while (data.moveToNext()) {
                        mSelectedClassifications.add(data.getString(data.getColumnIndex(MainDatabaseHelper.DisturbancesColumns.CLASSIFICATION)));
                    }
                }
            }
            finally {
                if (data != null) {
                    data.close();
                }
            }

            if (isAdded()) {
                getLoaderManager().restartLoader(-1,
                                                 null,
                                                 this);
            }
        }
        else if (loader.getId() == -1) {
            mAdapter.setGroupCursor(data);

            // the list should now be shown
            if (isResumed()) {
                setListShown(true,
                             true);
            }
            else {
                setListShown(true,
                             false);
            }

            updateActionMode();
        }
        else {
            try {
                mAdapter.setChildrenCursor(loader.getId(),
                                           data);
            }
            catch (NullPointerException npe) {
                Log.w(TAG,
                      "onLoadFinished : adapter expired");
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // this is called when the last Cursor provided to onLoadFinished() is about to be closed.
        if (mAdapter != null) {
            if (loader.getId() == -1) {
                mAdapter.setGroupCursor(null);
            }
            else {
                try {
                    mAdapter.setChildrenCursor(loader.getId(),
                                               null);
                }
                catch (NullPointerException npe) {
                    Log.w(TAG,
                          "onLoaderReset: adapter expired");
                }
            }
        }
    }

    private void updateActionMode() {
        final Taxon currentSelectedTaxon = (mInput == null) ? null : (Taxon) mInput.getCurrentSelectedTaxon();

        if ((currentSelectedTaxon == null) || !mIsVisibleToUser) {
            if (mMode != null) {
                mMode.finish();
            }
        }
        else {
            if ((currentSelectedTaxon.getCurrentSelectedArea() != null) && isVisible()) {
                if (currentSelectedTaxon.getCurrentSelectedArea()
                                        .getSelectedDisturbances()
                                        .isEmpty()) {
                    if (mMode != null) {
                        mMode.finish();
                    }
                }
                else {
                    if (mMode == null) {
                        mMode = ((AppCompatActivity) getActivity()).startSupportActionMode(mActionModeCallback);
                        mMode.setTitle(getResourceTitle());
                    }

                    if (mMode != null) {
                        mMode.setSubtitle(String.format(getString(R.string.action_title_item_selected),
                                                        currentSelectedTaxon.getCurrentSelectedArea()
                                                                            .getSelectedDisturbances()
                                                                            .size()));
                    }
                }
            }
        }
    }

    /**
     * Control whether the list is being displayed.
     * You can make it not displayed if you are waiting for the initial data to show in it.
     * During this time an indeterminate progress indicator will be shown instead.
     *
     * @param shown   If <code>true</code>, the list view is shown; if <code>false</code>, the
     *                progress indicator. The initial value is true.
     * @param animate If <code>true</code>, an animation will be used to transition to the new state.
     */
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
            else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
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
            else {
                mProgressContainer.clearAnimation();
                mListContainer.clearAnimation();
            }

            mProgressContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.GONE);
        }
    }
}
