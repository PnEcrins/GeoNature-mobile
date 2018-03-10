package com.makina.ecrins.mortality.ui.input.taxa;


import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.makina.ecrins.commons.content.MainDatabaseHelper;
import com.makina.ecrins.commons.input.AbstractInput;
import com.makina.ecrins.commons.input.AbstractTaxon;
import com.makina.ecrins.commons.input.InputType;
import com.makina.ecrins.commons.ui.input.taxa.AbstractTaxaFragment;
import com.makina.ecrins.commons.ui.input.taxa.TaxaFilterClass;
import com.makina.ecrins.mortality.MainApplication;
import com.makina.ecrins.mortality.R;
import com.makina.ecrins.mortality.content.MainContentProvider;
import com.makina.ecrins.mortality.input.Taxon;

import java.util.ArrayList;
import java.util.List;

/**
 * Lists all taxa from database.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
@SuppressWarnings("ALL")
public class TaxaFragment
        extends AbstractTaxaFragment
        implements OnClickListener {

    private static final String KEY_FILTER_CLASSES = "filter_classes";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getSavedInstanceState().putBoolean(KEY_DISPLAY_TAXON_DETAILS,
                                           false);

        if (!getSavedInstanceState().containsKey(KEY_FILTER_CLASSES)) {
            getSavedInstanceState().putParcelable(KEY_FILTER_CLASSES,
                                                  new TaxaFilterClass(new TaxaFilterClass.TaxonFilterClass(1,
                                                                                                           getString(R.string.taxa_class_amphibia_hint),
                                                                                                           R.id.taxaFilterClassAmphibia,
                                                                                                           false),
                                                                      new TaxaFilterClass.TaxonFilterClass(11,
                                                                                                           getString(R.string.taxa_class_mammalia_hint),
                                                                                                           R.id.taxaFilterClassMammalia,
                                                                                                           false),
                                                                      new TaxaFilterClass.TaxonFilterClass(12,
                                                                                                           getString(R.string.taxa_class_aves_hint),
                                                                                                           R.id.taxaFilterClassAves,
                                                                                                           false),
                                                                      new TaxaFilterClass.TaxonFilterClass(13,
                                                                                                           getString(R.string.taxa_class_osteichthyes_hint),
                                                                                                           R.id.taxaFilterClassOsteichthyes,
                                                                                                           false),
                                                                      new TaxaFilterClass.TaxonFilterClass(14,
                                                                                                           getString(R.string.taxa_class_reptilia_hint),
                                                                                                           R.id.taxaFilterClassReptilia,
                                                                                                           false)));
        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = super.onCreateView(inflater,
                                       container,
                                       savedInstanceState);

        mSecondActionBarView.addView(inflater.inflate(R.layout.taxa_filter,
                                                      null));

        // cosmetic issue
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            view.findViewById(R.id.taxaFilterClassAmphibia)
                .setBackgroundDrawable(null);
            view.findViewById(R.id.taxaFilterClassMammalia)
                .setBackgroundDrawable(null);
            view.findViewById(R.id.taxaFilterClassAves)
                .setBackgroundDrawable(null);
            view.findViewById(R.id.taxaFilterClassOsteichthyes)
                .setBackgroundDrawable(null);
            view.findViewById(R.id.taxaFilterClassReptilia)
                .setBackgroundDrawable(null);
        }

        return view;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            Bundle savedInstanceState) {

        super.onViewCreated(view,
                            savedInstanceState);

        view.findViewById(R.id.taxaFilterClassAmphibia)
            .setOnClickListener(this);
        view.findViewById(R.id.taxaFilterClassMammalia)
            .setOnClickListener(this);
        view.findViewById(R.id.taxaFilterClassAves)
            .setOnClickListener(this);
        view.findViewById(R.id.taxaFilterClassOsteichthyes)
            .setOnClickListener(this);
        view.findViewById(R.id.taxaFilterClassReptilia)
            .setOnClickListener(this);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void refreshView() {

        super.refreshView();

        TaxaFilterClass filterClasses = getSavedInstanceState().getParcelable(KEY_FILTER_CLASSES);

        for (int i = 0; i < filterClasses.getFilterClasses()
                                         .size(); i++) {
            if (filterClasses.getFilterClasses()
                             .valueAt(i)
                             .isSelected()) {
                ((ImageButton) mSecondActionBarView.findViewById(filterClasses.getFilterClasses()
                                                                              .valueAt(i)
                                                                              .getResourceId())).setAlpha(255);
            }
            else {
                ((ImageButton) mSecondActionBarView.findViewById(filterClasses.getFilterClasses()
                                                                              .valueAt(i)
                                                                              .getResourceId())).setAlpha(100);
            }
        }
    }

    @Override
    public AbstractTaxon createTaxon(long taxonId) {

        return new Taxon(taxonId);
    }

    @Override
    public AbstractInput getInput() {

        return ((MainApplication) getActivity().getApplication()).getInput();
    }

    @Override
    public void clearFilters() {

        getSavedInstanceState().putParcelable(KEY_FILTER_CLASSES,
                                              new TaxaFilterClass(new TaxaFilterClass.TaxonFilterClass(1,
                                                                                                       getString(R.string.taxa_class_amphibia_hint),
                                                                                                       R.id.taxaFilterClassAmphibia,
                                                                                                       false),
                                                                  new TaxaFilterClass.TaxonFilterClass(11,
                                                                                                       getString(R.string.taxa_class_mammalia_hint),
                                                                                                       R.id.taxaFilterClassMammalia,
                                                                                                       false),
                                                                  new TaxaFilterClass.TaxonFilterClass(12,
                                                                                                       getString(R.string.taxa_class_aves_hint),
                                                                                                       R.id.taxaFilterClassAves,
                                                                                                       false),
                                                                  new TaxaFilterClass.TaxonFilterClass(13,
                                                                                                       getString(R.string.taxa_class_osteichthyes_hint),
                                                                                                       R.id.taxaFilterClassOsteichthyes,
                                                                                                       false),
                                                                  new TaxaFilterClass.TaxonFilterClass(14,
                                                                                                       getString(R.string.taxa_class_reptilia_hint),
                                                                                                       R.id.taxaFilterClassReptilia,
                                                                                                       false)));
    }

    @Override
    public Loader<Cursor> onCreateLoader(
            int id,
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
        selectionArgs.add(Integer.toString(InputType.MORTALITY.getKey()));

        if (args != null) {
            if (args.containsKey(KEY_FILTER_CLASSES) && args.getParcelable(KEY_FILTER_CLASSES) != null) {
                TaxaFilterClass filterClasses = args.getParcelable(KEY_FILTER_CLASSES);

                if (filterClasses.getFilterClasses()
                                 .size() > 0) {
                    boolean asAtLeastOneFilterClass = false;

                    for (int i = 0; i < filterClasses.getFilterClasses()
                                                     .size(); i++) {
                        if (filterClasses.getFilterClasses()
                                         .valueAt(i)
                                         .isSelected()) {
                            if (!asAtLeastOneFilterClass) {
                                if (selection.length() > 0) {
                                    selection.append(" AND ");
                                }

                                selection.append(MainDatabaseHelper.TaxaColumns.CLASS_ID);
                                selection.append(" IN (");
                                asAtLeastOneFilterClass = true;
                            }

                            selection.append("?,");
                            selectionArgs.add(Long.valueOf(filterClasses.getFilterClasses()
                                                                        .valueAt(i)
                                                                        .getId())
                                                  .toString());
                        }
                    }

                    if (asAtLeastOneFilterClass) {
                        selection.replace(selection.length() - 1,
                                          selection.length(),
                                          ")");
                    }
                }
            }

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
        }

        Log.d(getClass().getName(),
              "selection : " + selection.toString());
        Log.d(getClass().getName(),
              "selectionArgs : " + selectionArgs.toString());

        cursorLoader = new CursorLoader(getActivity(),
                                        Uri.parse(MainContentProvider.CONTENT_TAXA_UNITY_URI + "/0"),
                                        projection,
                                        selection.toString(),
                                        selectionArgs.toArray(new String[selectionArgs.size()]),
                                        sortOrder);

        return cursorLoader;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onClick(View v) {

        if ((v.getId() == R.id.taxaFilterClassAmphibia) ||
                (v.getId() == R.id.taxaFilterClassMammalia) ||
                (v.getId() == R.id.taxaFilterClassAves) ||
                (v.getId() == R.id.taxaFilterClassOsteichthyes) ||
                (v.getId() == R.id.taxaFilterClassReptilia)) {
            TaxaFilterClass.TaxonFilterClass filterClass = ((TaxaFilterClass) getSavedInstanceState().getParcelable(KEY_FILTER_CLASSES)).getFilterClasses()
                                                                                                                        .get(v.getId());

            if (filterClass.isSelected()) {
                ((ImageButton) v).setAlpha(127);
            }
            else {
                ((ImageButton) v).setAlpha(255);
            }

            ((TaxaFilterClass) getSavedInstanceState().getParcelable(KEY_FILTER_CLASSES)).getFilterClasses()
                                                                                         .get(v.getId())
                                                                                         .setSelected(!filterClass.isSelected());
            refreshView();
        }
    }
}
