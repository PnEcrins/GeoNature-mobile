package com.makina.ecrins.invertebrate.ui.input.criteria;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.ui.input.criteria.AbstractCriteriaFragment;
import com.makina.ecrins.invertebrate.MainApplication;
import com.makina.ecrins.invertebrate.content.MainContentProvider;

/**
 * Lists all criteria.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CriteriaFragment
        extends AbstractCriteriaFragment {

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(
            int id,
            Bundle args) {
        final String[] projection = {
                MainDatabaseHelper.CriteriaColumns._ID,
                MainDatabaseHelper.CriteriaColumns.NAME,
                MainDatabaseHelper.CriteriaColumns.SORT,
                MainDatabaseHelper.CriteriaColumns.CLASS_ID
        };

        // select only criteria with no class
        final String selection = MainDatabaseHelper.CriteriaColumns.CLASS_ID + " is null";

        return new CursorLoader(getContext(),
                                getLoaderUri(),
                                projection,
                                selection,
                                new String[0],
                                null);
    }

    @Override
    public AbstractInput getInput() {
        return ((MainApplication) getActivity().getApplication()).getInput();
    }

    @Override
    public Uri getLoaderUri() {
        return MainContentProvider.CONTENT_CRITERIA_URI;
    }
}
