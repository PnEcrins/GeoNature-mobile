package com.makina.ecrins.flora.ui.input.phenology;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.ui.input.IInputFragment;
import com.makina.ecrins.commons.ui.pager.AbstractPagerFragmentActivity;
import com.makina.ecrins.commons.ui.pager.IValidateFragment;
import com.makina.ecrins.commons.util.ThemeUtils;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.content.MainContentProvider;
import com.makina.ecrins.flora.input.Input;
import com.makina.ecrins.flora.input.Taxon;

/**
 * Lists all available phenology.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class PhenologyListFragment
        extends ListFragment
        implements IValidateFragment,
                   IInputFragment,
                   LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter mAdapter;

    private Input mInput;

    @Override
    public void onViewCreated(View view,
                              Bundle savedInstanceState) {
        // give some text to display if there is no data
        setEmptyText(getString(R.string.phenology_no_data));

        // create an empty adapter we will use to display the loaded data
        mAdapter = new SimpleCursorAdapter(getActivity(),
                                           android.R.layout.simple_list_item_1,
                                           null,
                                           new String[] {
                                                   MainDatabaseHelper.CriteriaColumns.NAME
                                           },
                                           new int[] {
                                                   android.R.id.text1
                                           },
                                           0) {
            @Override
            public View getView(int position,
                                View convertView,
                                ViewGroup parent) {
                View view = super.getView(position,
                                          convertView,
                                          parent);

                final Cursor cursor = (Cursor) getItem(position);

                if (mInput == null) {
                    return view;
                }

                final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

                if ((currentSelectedTaxon != null) &&
                        (currentSelectedTaxon.getCurrentSelectedArea() != null) &&
                        (currentSelectedTaxon.getCurrentSelectedArea()
                                             .getPhenologyId() == cursor.getInt(cursor.getColumnIndex(MainDatabaseHelper.PhenologyColumns.CODE)))) {
                    getListView().setSelection(position);
                    view.setBackgroundColor(ThemeUtils.getAccentColor(getContext()));
                }
                else {
                    view.setBackgroundColor(Color.TRANSPARENT);
                }

                return view;
            }
        };

        setListAdapter(mAdapter);
        getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onListItemClick(ListView l,
                                View v,
                                int position,
                                long id) {
        final Cursor cursor = (Cursor) mAdapter.getItem(position);
        long phenologyCode = cursor.getLong(cursor.getColumnIndex(MainDatabaseHelper.PhenologyColumns.CODE));

        if (mInput == null) {
            return;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        if ((currentSelectedTaxon != null) && (currentSelectedTaxon.getCurrentSelectedArea() != null)) {
            currentSelectedTaxon.getCurrentSelectedArea()
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
        if (mInput == null) {
            return false;
        }

        final Taxon currentSelectedTaxon = (Taxon) mInput.getCurrentSelectedTaxon();

        return mInput != null && !(currentSelectedTaxon == null || currentSelectedTaxon.getCurrentSelectedArea() == null) &&
                currentSelectedTaxon.getCurrentSelectedArea() != null &&
                currentSelectedTaxon.getCurrentSelectedArea()
                                    .getPhenologyId() != -1;
    }

    @Override
    public void refreshView() {
        if (isAdded()) {
            // prepare the loader, either re-connect with an existing one, or start a new one
            getLoaderManager().restartLoader(0,
                                             null,
                                             this);

            // start out with a progress indicator
            setListShown(false);
        }
    }

    @Override
    public void setInput(@NonNull AbstractInput input) {
        this.mInput = (Input) input;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id,
                                         Bundle args) {
        final String[] projection = {
                MainDatabaseHelper.PhenologyColumns._ID,
                MainDatabaseHelper.PhenologyColumns.CODE,
                MainDatabaseHelper.PhenologyColumns.NAME
        };

        return new CursorLoader(getActivity(),
                                MainContentProvider.CONTENT_PHENOLOGY_URI,
                                projection,
                                null,
                                null,
                                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader,
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
}
