package com.makina.ecrins.flora.ui.input.phenology;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.ui.pager.AbstractPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.content.MainContentProvider;
import com.makina.ecrins.flora.input.Taxon;

/**
 * Lists all available phenology.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class PhenologyListFragment extends ListFragment
        implements
        IValidateFragment,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = PhenologyListFragment.class.getName();

    private SimpleCursorAdapter mAdapter;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // give some text to display if there is no data
        setEmptyText(getString(R.string.phenology_no_data));

        // create an empty adapter we will use to display the loaded data
        mAdapter = new SimpleCursorAdapter(
                getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                new String[] {
                                MainDatabaseHelper.CriteriaColumns.NAME
                },
                new int[] {
                                android.R.id.text1
                },
                0
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                Cursor cursor = (Cursor) getItem(position);

                if ((((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon() != null) &&
                        (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                        (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                                .getPhenologyId() == cursor.getInt(cursor
                                .getColumnIndex(MainDatabaseHelper.PhenologyColumns.CODE)))) {
                    getListView().setSelection(position);
                    view.setBackgroundColor(getResources().getColor(R.color.holo_blue_light));
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
        Log.d(TAG, "onPause");

        getLoaderManager().destroyLoader(0);

        super.onPause();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);

        long phenologyCode = cursor.getLong(
                cursor.getColumnIndex(MainDatabaseHelper.PhenologyColumns.CODE));

        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
            ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .setPhenologyId(phenologyCode);
        }

        mAdapter.notifyDataSetChanged();

        ((AbstractPagerFragmentActivity) getActivity()).validateCurrentPage();
    }

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_phenology_title;
    }

    @Override
    public boolean getPagingEnabled() {
        return true;
    }

    @Override
    public boolean validate() {
        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() == null) ||
                ((((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon() != null) &&
                        (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                                .getCurrentSelectedTaxon()).getCurrentSelectedArea() == null))) {
            Log.w(TAG, "validate: no taxon selected !");

            return false;
        }
        else {
            return (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null) &&
                    ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                            .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                            .getPhenologyId() != -1;
        }
    }

    @Override
    public void refreshView() {
        // prepare the loader, either re-connect with an existing one, or start a new one
        getLoaderManager().restartLoader(0, null, this);

        // start out with a progress indicator
        setListShown(false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                        MainDatabaseHelper.PhenologyColumns._ID,
                        MainDatabaseHelper.PhenologyColumns.CODE,
                        MainDatabaseHelper.PhenologyColumns.NAME
        };

        return new CursorLoader(
                getActivity(),
                MainContentProvider.CONTENT_PHENOLOGY_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
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
}
