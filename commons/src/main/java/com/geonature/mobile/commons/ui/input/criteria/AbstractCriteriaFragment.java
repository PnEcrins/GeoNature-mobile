package com.geonature.mobile.commons.ui.input.criteria;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.geonature.mobile.commons.R;
import com.geonature.mobile.commons.content.MainDatabaseHelper;
import com.geonature.mobile.commons.input.AbstractInput;
import com.geonature.mobile.commons.input.AbstractTaxon;
import com.geonature.mobile.commons.ui.pager.AbstractPagerFragmentActivity;
import com.geonature.mobile.commons.ui.pager.IValidateFragment;
import com.geonature.mobile.commons.util.ThemeUtils;

/**
 * Lists all criteria.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public abstract class AbstractCriteriaFragment extends ListFragment
        implements IValidateFragment,
        LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter mAdapter;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        final FragmentActivity activity = getActivity();

        if (activity == null) {
            return;
        }

        // give some text to display if there is no data
        setEmptyText(getString(R.string.criteria_no_data));

        // create an empty adapter we will use to display the loaded data
        mAdapter = new SimpleCursorAdapter(activity, android.R.layout.simple_list_item_1, null,
                                           new String[] {
                                                   MainDatabaseHelper.CriteriaColumns.NAME
                        },
                                           new int[] {
                                android.R.id.text1
                        }, 0
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                if (getInput().getTaxa().get(getInput().getCurrentSelectedTaxonId()).getCriterionId() == getItemId(position)) {
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        final FragmentActivity activity = getActivity();

        if (activity == null || mAdapter == null) {
            return;
        }

        Cursor cursor = (Cursor) mAdapter.getItem(position);
        long criterionId = cursor.getLong(cursor.getColumnIndex(MainDatabaseHelper.CriteriaColumns._ID));
        String criterionLabel = cursor.getString(cursor.getColumnIndex(MainDatabaseHelper.CriteriaColumns.NAME));

        getInput().getTaxa().get(getInput().getLastInsertedTaxonId()).setCriterionId(criterionId);
        getInput().getTaxa().get(getInput().getLastInsertedTaxonId()).setCriterionLabel(criterionLabel);

        mAdapter.notifyDataSetChanged();

        ((AbstractPagerFragmentActivity) activity).validateCurrentPage();
    }

    @Override
    public int getResourceTitle() {
        return R.string.pager_fragment_criteria_title;
    }

    @Override
    public boolean getPagingEnabled() {
        return true;
    }

    @Override
    public boolean validate() {
        final AbstractTaxon selectedTaxon = getInput().getTaxa().get(getInput().getCurrentSelectedTaxonId());

        return selectedTaxon != null && selectedTaxon.getCriterionId() != -1;
    }

    @Override
    public void refreshView() {
        if (isAdded()) {
            // prepare the loader, either re-connect with an existing one, or start a new one
            getLoaderManager().restartLoader(0, null, this);

            // start out with a progress indicator
            setListShown(false);
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] projection = {
                MainDatabaseHelper.CriteriaColumns._ID,
                MainDatabaseHelper.CriteriaColumns.NAME,
                MainDatabaseHelper.CriteriaColumns.SORT,
                MainDatabaseHelper.CriteriaColumns.CLASS_ID
        };

        return new CursorLoader(getContext(), getLoaderUri(), projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
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
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        mAdapter.swapCursor(null);
    }

    public abstract AbstractInput getInput();

    public abstract Uri getLoaderUri();
}
