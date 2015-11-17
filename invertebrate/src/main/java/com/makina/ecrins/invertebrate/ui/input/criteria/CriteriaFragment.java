package com.makina.ecrins.invertebrate.ui.input.criteria;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.ui.input.criteria.AbstractCriteriaFragment;
import com.makina.ecrins.invertebrate.MainApplication;
import com.makina.ecrins.invertebrate.content.MainContentProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists all criteria.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class CriteriaFragment
        extends AbstractCriteriaFragment {

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
        final StringBuilder selection = new StringBuilder();
        final List<String> selectionArgs = new ArrayList<>();

        selection.append(MainDatabaseHelper.CriteriaColumns.CLASS_ID);
        selection.append(" is null");

        return new CursorLoader(getActivity(),
                                getLoaderUri(),
                                projection,
                                selection.toString(),
                                selectionArgs.toArray(new String[selectionArgs.size()]),
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
