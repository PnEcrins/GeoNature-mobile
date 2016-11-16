package com.makina.ecrins.flora.ui.input.taxa;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.AbstractTaxon;
import com.makina.ecrins.commons.input.InputType;
import com.makina.ecrins.commons.ui.input.taxa.AbstractTaxaInputListFragment;
import com.makina.ecrins.flora.content.MainContentProvider;
import com.makina.ecrins.flora.input.Taxon;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists all taxa from database.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class TaxaInputListFragment
        extends AbstractTaxaInputListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSavedInstanceState().putSerializable(KEY_SWITCH_LABEL, LabelSwitcher.LATIN);
        getSavedInstanceState().putBoolean(KEY_DISPLAY_TAXON_DETAILS, false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater,
                                  container,
                                  savedInstanceState);

        mSecondActionBarView.setVisibility(View.GONE);

        return view;
    }

    @NonNull
    @Override
    public AbstractTaxon createTaxon(long taxonId) {
        return new Taxon(taxonId);
    }

    @Override
    public void clearFilters() {
        getSavedInstanceState().putSerializable(KEY_SWITCH_LABEL,
                                                LabelSwitcher.LATIN);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id,
                                         Bundle args) {
        final String[] projection = {
                MainDatabaseHelper.TaxaColumns._ID,
                MainDatabaseHelper.TaxaColumns.NAME,
                MainDatabaseHelper.TaxaColumns.NAME_FR,
                MainDatabaseHelper.TaxaColumns.CLASS_ID,
                MainDatabaseHelper.TaxaColumns.NUMBER,
                MainDatabaseHelper.TaxaColumns.FILTER,
                MainDatabaseHelper.TaxaUnitiesColumns.COLOR,
                MainDatabaseHelper.TaxaColumns.PATRIMONIAL,
                MainDatabaseHelper.TaxaUnitiesColumns.NB_OBS,
                MainDatabaseHelper.TaxaUnitiesColumns.DATE,
                MainDatabaseHelper.TaxaColumns.MESSAGE
        };

        CursorLoader cursorLoader;

        String sortOrder = MainDatabaseHelper.TaxaColumns.NAME + " ASC";

        if (args.containsKey(KEY_SWITCH_LABEL)) {
            switch ((LabelSwitcher) args.getSerializable(KEY_SWITCH_LABEL)) {
                case FRENCH:
                    sortOrder = MainDatabaseHelper.TaxaColumns.NAME_FR + " COLLATE UNICODE ASC";
                    break;
                default:
                    sortOrder = MainDatabaseHelper.TaxaColumns.NAME + " ASC";
                    break;
            }
        }

        final StringBuilder selection = new StringBuilder();
        final List<String> selectionArgs = new ArrayList<>();

        // adding filter
        selection.append("((");
        selection.append(MainDatabaseHelper.TaxaColumns.FILTER);
        selection.append(" & ?) != 0)");
        selectionArgs.add(Integer.toString(InputType.FLORA.getKey()));

        if (args.containsKey(KEY_FILTER) && (args.getString(KEY_FILTER) != null)) {
            if (args.containsKey(KEY_SWITCH_LABEL)) {
                if (selection.length() > 0) {
                    selection.append(" AND ");
                }

                String filter = "%" + args.getString(KEY_FILTER) + "%";

                switch ((LabelSwitcher) args.getSerializable(KEY_SWITCH_LABEL)) {
                    case FRENCH:
                        selection.append(MainDatabaseHelper.TaxaColumns.NAME_FR);
                        break;
                    default:
                        selection.append(MainDatabaseHelper.TaxaColumns.NAME);
                        break;
                }

                selection.append(" LIKE ?");
                selectionArgs.add(filter);
            }
        }

        Log.d(TAG,
              "selection: " + selection.toString());
        Log.d(TAG,
              "selectionArgs: " + selectionArgs.toString());

        cursorLoader = new CursorLoader(getActivity(),
                                        Uri.parse(MainContentProvider.CONTENT_TAXA_UNITY_URI + "/0"),
                                        projection,
                                        selection.toString(),
                                        selectionArgs.toArray(new String[selectionArgs.size()]),
                                        sortOrder);

        return cursorLoader;
    }
}
