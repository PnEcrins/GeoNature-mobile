package com.makina.ecrins.invertebrate.ui.input.environments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.ui.pager.AbstractPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.commons.util.ThemeUtils;
import com.makina.ecrins.invertebrate.MainApplication;
import com.makina.ecrins.invertebrate.R;
import com.makina.ecrins.invertebrate.content.MainContentProvider;
import com.makina.ecrins.invertebrate.inputs.Input;

/**
 * Lists all environments.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class EnvironmentsFragment
        extends ListFragment
        implements IValidateFragment,
                   LoaderCallbacks<Cursor> {

    protected SimpleCursorAdapter mAdapter;

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState) {
        // give some text to display if there is no data
        setEmptyText(getString(R.string.environments_no_data));

        // create an empty adapter we will use to display the loaded data
        mAdapter = new SimpleCursorAdapter(getActivity(),
                                           android.R.layout.simple_list_item_1,
                                           null,
                                           new String[] {
                                                   MainDatabaseHelper.EnvironmentsColumns.NAME
                                           },
                                           new int[] {
                                                   android.R.id.text1
                                           },
                                           0) {
            @Override
            public View getView(
                    int position,
                    View convertView,
                    ViewGroup parent) {

                View view = super.getView(position,
                                          convertView,
                                          parent);

                if (getInput().getEnvironmentId() == getItemId(position)) {
                    getListView().setSelection(position);
                    view.setBackgroundColor(ThemeUtils.getAccentColor(getContext()));
                }
                else {
                    view.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }

                return view;
            }
        };

        setListAdapter(mAdapter);
        getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onPause() {

        Log.d(getClass().getName(),
              "onPause");

        getLoaderManager().destroyLoader(0);

        super.onPause();
    }

    @Override
    public void onListItemClick(
            ListView l,
            View v,
            int position,
            long id) {

        Cursor cursor = (Cursor) mAdapter.getItem(position);
        long environmentId = cursor.getLong(cursor.getColumnIndex(MainDatabaseHelper.EnvironmentsColumns._ID));

        getInput().setEnvironmentId(environmentId);

        mAdapter.notifyDataSetChanged();

        ((AbstractPagerFragmentActivity) getActivity()).validateCurrentPage();
    }

    @Override
    public int getResourceTitle() {

        return R.string.pager_fragment_environments_title;
    }

    @Override
    public boolean getPagingEnabled() {

        return true;
    }

    @Override
    public boolean validate() {

        return getInput().getEnvironmentId() >= 0;
    }

    @Override
    public void refreshView() {

        ((AbstractPagerFragmentActivity) getActivity()).getSupportActionBar()
                                                       .setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // prepare the loader, either re-connect with an existing one, or start a new one
        getLoaderManager().restartLoader(0,
                                         null,
                                         this);

        // start out with a progress indicator
        setListShown(false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(
            int id,
            Bundle args) {

        String[] projection = {
                MainDatabaseHelper.EnvironmentsColumns._ID,
                MainDatabaseHelper.EnvironmentsColumns.NAME
        };

        return new CursorLoader(getActivity(),
                                MainContentProvider.CONTENT_ENVIRONMENTS_URI,
                                projection,
                                null,
                                null,
                                null);
    }

    @Override
    public void onLoadFinished(
            Loader<Cursor> loader,
            Cursor data) {

        mAdapter.swapCursor(data);

        // the list should now be shown
        if (isResumed()) {
            setListShown(true);
        }
        else {
            setListShownNoAnimation(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        mAdapter.swapCursor(null);
    }

    protected Input getInput() {

        return ((MainApplication) getActivity().getApplication()).getInput();
    }
}
