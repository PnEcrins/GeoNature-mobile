package com.makina.ecrins.fauna.ui.input.criteria;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.ui.input.criteria.AbstractCriteriaFragment;
import com.makina.ecrins.fauna.MainApplication;
import com.makina.ecrins.fauna.content.MainContentProvider;

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

        final StringBuilder selection = new StringBuilder();
        final List<String> selectionArgs = new ArrayList<>();

        selection.append(MainDatabaseHelper.CriteriaColumns.CLASS_ID);
        selection.append(" = ?");
        selectionArgs.add(Long.valueOf(((MainApplication) getActivity().getApplication()).getInput()
                                                                                         .getTaxa()
                                                                                         .get(((MainApplication) getActivity().getApplication()).getInput()
                                                                                                                                                .getCurrentSelectedTaxonId())
                                                                                         .getClassId())
                              .toString());

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
