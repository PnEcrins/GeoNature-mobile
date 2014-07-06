package com.makina.ecrins.flora.ui.input.physiognomy;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
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
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.commons.ui.widget.AbstractGroupsCursorAdapter;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.content.MainContentProvider;
import com.makina.ecrins.flora.input.Taxon;

/**
 * Physiognomy as an <code>ExpendableListView</code>.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class PhysiognomyFragment extends Fragment
        implements
        IValidateFragment,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = PhysiognomyFragment.class.getName();

    private AbstractGroupsCursorAdapter<String> mAdapter;

    private View mProgressContainer;
    private View mListContainer;
    private ExpandableListView mExpandableListView;
    private TextView mTextViewEmpty;

    protected Handler mHandler = new Handler();

    private boolean mListShown;
    private boolean mIsVisibleToUser = false;

    private ActionMode mMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mMode = null;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if ((((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon() != null) &&
                    (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                    !((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                            .getSelectedPhysiognomy().isEmpty()) {
                final MenuItem menuItem = menu.add(
                        Menu.NONE,
                        0,
                        Menu.NONE,
                        R.string.action_unselect_all)
                        .setIcon(R.drawable.ic_action_unselect_all);
                MenuItemCompat.setShowAsAction(menuItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
            }

            return true;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
            switch (item.getItemId()) {
                case 0:
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if ((((MainApplication) getActivity().getApplication()).getInput()
                                    .getCurrentSelectedTaxon() != null) &&
                                    (((Taxon) ((MainApplication) getActivity().getApplication())
                                            .getInput().getCurrentSelectedTaxon())
                                            .getCurrentSelectedArea() != null)) {
                                ((Taxon) ((MainApplication) getActivity().getApplication())
                                        .getInput().getCurrentSelectedTaxon())
                                        .getCurrentSelectedArea().getSelectedPhysiognomy().clear();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expandable_list, container, false);

        mProgressContainer = view.findViewById(R.id.progressContainer);
        mListContainer = view.findViewById(R.id.listContainer);
        mExpandableListView = (ExpandableListView) view.findViewById(R.id.expandableListView);
        mTextViewEmpty = (TextView) view.findViewById(R.id.textViewEmpty);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setListShown(false, false);

        mTextViewEmpty.setText(R.string.physionomy_no_data);

        mAdapter = new AbstractGroupsCursorAdapter<String>(getActivity(),
                R.layout.simple_expandable_list_item_1,
                new String[] {
                                MainDatabaseHelper.PhysiognomyColumns.GROUP_NAME
                },
                new int[] {
                                android.R.id.text1
                },
                R.layout.simple_list_item_multiple_choice,
                new String[] {
                                MainDatabaseHelper.PhysiognomyColumns.NAME
                },
                new int[] {
                                android.R.id.text1
                }
        ) {
            @Override
            public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
                View view = super
                        .getChildView(groupPosition, childPosition, isLastChild, convertView, parent);

                if ((((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon() != null) &&
                        (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
                    final CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
                    final long childId = mAdapter.getChildId(groupPosition, childPosition);

                    checkedTextView
                            .setChecked(((Taxon) ((MainApplication) getActivity().getApplication())
                                    .getInput().getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                    .getSelectedPhysiognomy().contains(childId));
                }

                return view;
            }

            @Override
            protected LoaderManager getLoaderManager() {
                return PhysiognomyFragment.this.getLoaderManager();
            }

            @Override
            protected String getGroupId(Cursor groupCursor) {
                return groupCursor.getString(groupCursor
                        .getColumnIndex(MainDatabaseHelper.PhysiognomyColumns.GROUP_NAME));
            }

            @Override
            protected LoaderCallbacks<Cursor> getLoaderCallbacks() {
                return PhysiognomyFragment.this;
            }
        };
        mAdapter.setExpendAllGroups(false);

        mExpandableListView.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                mAdapter.setExpendAllGroups(false);

                return false;
            }
        });

        mExpandableListView.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if ((((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon() != null) &&
                        (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
                    final CheckedTextView checkedTextView = (CheckedTextView) v
                            .findViewById(android.R.id.text1);

                    if (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                            .getSelectedPhysiognomy().contains(id)) {
                        ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                .getSelectedPhysiognomy().remove(id);
                    }
                    else {
                        ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                .getSelectedPhysiognomy().add(id);
                    }

                    checkedTextView
                            .setChecked(((Taxon) ((MainApplication) getActivity().getApplication())
                                    .getInput().getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                    .getSelectedPhysiognomy().contains(id));

                    updateActionMode();
                }

                return true;
            }
        });

        mExpandableListView.setEmptyView(mTextViewEmpty);
        mExpandableListView.setAdapter(mAdapter);
        mExpandableListView.setFastScrollEnabled(true);
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

        Log.d(TAG,
                "setUserVisibleHint: isVisible = " +
                        this.isVisible() +
                        ", isVisibleToUser = " +
                        isVisibleToUser);

        mIsVisibleToUser = isVisibleToUser;

        if ((!this.isVisible() || !isVisibleToUser) && (mMode != null)) {
            Log.d(TAG, "setUserVisibleHint finish action mode");

            mMode.finish();
        }
    }

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_physiognomy_title;
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
        // prepare the loader, either re-connect with an existing one, or start a new one
        getLoaderManager().restartLoader(-1, null, this);

        // start out with a progress indicator
        setListShown(false, true);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] projection = {
                        MainDatabaseHelper.PhysiognomyColumns._ID,
                        MainDatabaseHelper.PhysiognomyColumns.GROUP_NAME,
                        MainDatabaseHelper.PhysiognomyColumns.NAME
        };

        if (id == -1) {
            // group cursor
            return new CursorLoader(
                    getActivity(),
                    MainContentProvider.CONTENT_PHYSIOGNOMY_GROUPS_URI,
                    projection,
                    null,
                    null,
                    null);
        }
        else {
            // children cursor
            return new CursorLoader(
                    getActivity(),
                    Uri.withAppendedPath(
                            MainContentProvider.CONTENT_PHYSIOGNOMY_GROUPS_URI,
                            args.getString(AbstractGroupsCursorAdapter.KEY_SELECTED_GROUP_ID)),
                    projection,
                    null,
                    null,
                    null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished " + loader.getId());

        if (loader.getId() == -1) {
            mAdapter.setGroupCursor(data);

            // the list should now be shown
            if (isResumed()) {
                setListShown(true, true);
            }
            else {
                setListShown(true, false);
            }

            updateActionMode();
        }
        else {
            try {
                mAdapter.setChildrenCursor(loader.getId(), data);
            }
            catch (NullPointerException npe) {
                Log.d(TAG, "onLoadFinished : adapter expired");
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // this is called when the last Cursor provided to onLoadFinished() is about to be closed.
        if (mAdapter != null) {
            if (loader.getId() == -1) {
                mAdapter.setGroupCursor(null);
            }
            else {
                try {
                    mAdapter.setChildrenCursor(loader.getId(), null);
                }
                catch (NullPointerException npe) {
                    Log.d(TAG, "onLoaderReset: adapter expired");
                }
            }
        }
    }

    private void updateActionMode() {
        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() == null) || !mIsVisibleToUser) {
            if (mMode != null) {
                mMode.finish();
            }
        }
        else {
            if ((((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) && isVisible()) {
                if (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                        .getSelectedPhysiognomy().isEmpty()) {
                    if (mMode != null) {
                        mMode.finish();
                    }
                }
                else {
                    Log.d(TAG, "updateActionMode");

                    if (mMode == null) {
                        mMode = ((ActionBarActivity) getActivity())
                                .startSupportActionMode(mActionModeCallback);
                    }

                    if (mMode != null) {
                        mMode.setTitle(
                                String.format(
                                        getString(R.string.action_title_item_selected),
                                        ((Taxon) ((MainApplication) getActivity()
                                        .getApplication()).getInput().getCurrentSelectedTaxon())
                                        .getCurrentSelectedArea().getSelectedPhysiognomy().size()));
                    }
                }
            }
        }
    }

    /**
     * Control whether the list is being displayed.
     * You can make it not displayed if you are waiting for the initial data to show in it.
     * During this time an indeterminant progress indicator will be shown instead.
     *
     * @param shown   If <code>true</code>, the list view is shown; if <code>false</code>, the
     *                progress indicator. The initial value is true.
     * @param animate If <code>true</code>, an animation will be used to transition to the new state.
     */
    private void setListShown(boolean shown, boolean animate) {
        if (mListShown == shown) {
            return;
        }

        mListShown = shown;

        if (shown) {
            if (animate) {
                mProgressContainer.startAnimation(AnimationUtils
                        .loadAnimation(getActivity(), android.R.anim.fade_out));
                mListContainer.startAnimation(AnimationUtils
                        .loadAnimation(getActivity(), android.R.anim.fade_in));
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
                mProgressContainer.startAnimation(AnimationUtils
                        .loadAnimation(getActivity(), android.R.anim.fade_in));
                mListContainer.startAnimation(AnimationUtils
                        .loadAnimation(getActivity(), android.R.anim.fade_out));
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
