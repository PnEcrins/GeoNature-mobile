package com.makina.ecrins.invertebrate.ui.input.taxa;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.makina.ecrins.commons.ui.input.taxa.TaxaFilterStatusView;
import com.makina.ecrins.commons.ui.input.taxa.TaxaStatusFilterDialogFragment;
import com.makina.ecrins.commons.ui.input.taxa.TaxonStatus;
import com.makina.ecrins.invertebrate.MainApplication;
import com.makina.ecrins.invertebrate.R;
import com.makina.ecrins.invertebrate.content.MainContentProvider;
import com.makina.ecrins.invertebrate.inputs.Taxon;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Lists all taxa from database.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class TaxaFragment
        extends AbstractTaxaFragment
        implements OnClickListener {

    private static final String FILTER_TAXA_STATUS_DIALOG_FRAGMENT = "filter_taxa_status";

    private static final String KEY_FILTER_STATUS = "filter_status";
    private static final String KEY_FILTER_HERITAGE = "filter_heritage";
    private static final String KEY_FILTER_CLASSES = "filter_classes";

    private TaxaFilterStatusView mTaxaFilterStatusView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getSavedInstanceState().putSerializable(KEY_SWITCH_LABEL,
                                                LabelSwitcher.LATIN);
        getSavedInstanceState().putBoolean(KEY_DISPLAY_TAXON_STATUS,
                                           true);
        getSavedInstanceState().putBoolean(KEY_DISPLAY_TAXON_HERITAGE,
                                           true);

        if (!getSavedInstanceState().containsKey(KEY_FILTER_STATUS)) {
            getSavedInstanceState().putParcelableArray(KEY_FILTER_STATUS,
                                                       new TaxonStatus[] {
                                                               new TaxonStatus(TaxonStatus.STATUS_SEARCH,
                                                                               true,
                                                                               R.string.taxon_status_search,
                                                                               R.color.taxon_status_color_search),
                                                               new TaxonStatus(TaxonStatus.STATUS_NEW,
                                                                               true,
                                                                               R.string.taxon_status_new,
                                                                               R.color.taxon_status_color_new),
                                                               new TaxonStatus(TaxonStatus.STATUS_OPTIONAL,
                                                                               true,
                                                                               R.string.taxon_status_optional,
                                                                               R.color.taxon_status_color_optional)
                                                       });
        }

        if (!getSavedInstanceState().containsKey(KEY_FILTER_HERITAGE)) {
            getSavedInstanceState().putBoolean(KEY_FILTER_HERITAGE,
                                               false);
        }

        if (!getSavedInstanceState().containsKey(KEY_FILTER_CLASSES)) {
            getSavedInstanceState().putParcelable(KEY_FILTER_CLASSES,
                                                  new TaxaFilterClass(new TaxaFilterClass.TaxonFilterClass(5,
                                                                                                           getString(R.string.taxa_class_malacostraca_hint),
                                                                                                           R.id.taxaFilterClassMalacostraca,
                                                                                                           false),
                                                                      new TaxaFilterClass.TaxonFilterClass(9,
                                                                                                           getString(R.string.taxa_class_insecta_hint),
                                                                                                           R.id.taxaFilterClassInsecta,
                                                                                                           false),
                                                                      new TaxaFilterClass.TaxonFilterClass(15,
                                                                                                           getString(R.string.taxa_class_myriapoda_hint),
                                                                                                           R.id.taxaFilterClassMyriapoda,
                                                                                                           false),
                                                                      new TaxaFilterClass.TaxonFilterClass(16,
                                                                                                           getString(R.string.taxa_class_arachnida_hint),
                                                                                                           R.id.taxaFilterClassArachnida,
                                                                                                           false),
                                                                      new TaxaFilterClass.TaxonFilterClass(2,
                                                                                                           getString(R.string.taxa_class_annelida_hint),
                                                                                                           R.id.taxaFilterClassAnnelida,
                                                                                                           false),
                                                                      new TaxaFilterClass.TaxonFilterClass(8,
                                                                                                           getString(R.string.taxa_class_gastropoda_hint),
                                                                                                           R.id.taxaFilterClassGastropoda,
                                                                                                           false),
                                                                      new TaxaFilterClass.TaxonFilterClass(10,
                                                                                                           getString(R.string.taxa_class_lamellibranchia_hint),
                                                                                                           R.id.taxaFilterClassLamellibranchia,
                                                                                                           false)));
        }

        // restore TaxaStatusDialogFragment state after resume if needed
        TaxaStatusFilterDialogFragment dialogFragment = (TaxaStatusFilterDialogFragment) getFragmentManager().findFragmentByTag(FILTER_TAXA_STATUS_DIALOG_FRAGMENT);

        if (dialogFragment != null) {
            dialogFragment.setHandler(new TaxaHandler(this));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = super.onCreateView(inflater,
                                       container,
                                       savedInstanceState);

        mSecondActionBarView.addView(inflater.inflate(R.layout.taxa_filter,
                                                      null));

        mTaxaFilterStatusView = (TaxaFilterStatusView) view.findViewById(R.id.taxaFilterStatusView);

        // cosmetic issue
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            view.findViewById(R.id.taxaFilterClassMalacostraca)
                .setBackgroundDrawable(null);
            view.findViewById(R.id.taxaFilterClassInsecta)
                .setBackgroundDrawable(null);
            view.findViewById(R.id.taxaFilterClassMyriapoda)
                .setBackgroundDrawable(null);
            view.findViewById(R.id.taxaFilterClassArachnida)
                .setBackgroundDrawable(null);
            view.findViewById(R.id.taxaFilterClassAnnelida)
                .setBackgroundDrawable(null);
            view.findViewById(R.id.taxaFilterClassLamellibranchia)
                .setBackgroundDrawable(null);
            view.findViewById(R.id.taxaFilterClassGastropoda)
                .setBackgroundDrawable(null);
            view.findViewById(R.id.taxaFilterHeritageView)
                .setBackgroundDrawable(null);
        }

        return view;
    }

    @Override
    public void onViewCreated(
            View view,
            Bundle savedInstanceState) {

        super.onViewCreated(view,
                            savedInstanceState);

        mTaxaFilterStatusView.setOnClickListener(this);
        view.findViewById(R.id.taxaFilterHeritageView)
            .setOnClickListener(this);
        view.findViewById(R.id.taxaFilterClassMalacostraca)
            .setOnClickListener(this);
        view.findViewById(R.id.taxaFilterClassInsecta)
            .setOnClickListener(this);
        view.findViewById(R.id.taxaFilterClassMyriapoda)
            .setOnClickListener(this);
        view.findViewById(R.id.taxaFilterClassArachnida)
            .setOnClickListener(this);
        view.findViewById(R.id.taxaFilterClassAnnelida)
            .setOnClickListener(this);
        view.findViewById(R.id.taxaFilterClassGastropoda)
            .setOnClickListener(this);
        view.findViewById(R.id.taxaFilterClassLamellibranchia)
            .setOnClickListener(this);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void refreshView() {

        super.refreshView();

        mTaxaFilterStatusView.setToSearchSelected(((TaxonStatus) getSavedInstanceState().getParcelableArray(KEY_FILTER_STATUS)[0]).isSelected());
        mTaxaFilterStatusView.setNewSelected(((TaxonStatus) getSavedInstanceState().getParcelableArray(KEY_FILTER_STATUS)[1]).isSelected());
        mTaxaFilterStatusView.setOptionalSelected(((TaxonStatus) getSavedInstanceState().getParcelableArray(KEY_FILTER_STATUS)[2]).isSelected());

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

        if (getSavedInstanceState().getBoolean(KEY_FILTER_HERITAGE)) {
            ((ImageButton) mSecondActionBarView.findViewById(R.id.taxaFilterHeritageView)).setAlpha(255);
        }
        else {
            ((ImageButton) mSecondActionBarView.findViewById(R.id.taxaFilterHeritageView)).setAlpha(127);
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

        getSavedInstanceState().putSerializable(KEY_SWITCH_LABEL,
                                                LabelSwitcher.LATIN);

        getSavedInstanceState().putParcelableArray(KEY_FILTER_STATUS,
                                                   new TaxonStatus[] {
                                                           new TaxonStatus(TaxonStatus.STATUS_SEARCH,
                                                                           true,
                                                                           R.string.taxon_status_search,
                                                                           R.color.taxon_status_color_search),
                                                           new TaxonStatus(TaxonStatus.STATUS_NEW,
                                                                           true,
                                                                           R.string.taxon_status_new,
                                                                           R.color.taxon_status_color_new),
                                                           new TaxonStatus(TaxonStatus.STATUS_OPTIONAL,
                                                                           true,
                                                                           R.string.taxon_status_optional,
                                                                           R.color.taxon_status_color_optional)
                                                   });
        getSavedInstanceState().putBoolean(KEY_FILTER_HERITAGE,
                                           false);
        getSavedInstanceState().putParcelable(KEY_FILTER_CLASSES,
                                              new TaxaFilterClass(new TaxaFilterClass.TaxonFilterClass(5,
                                                                                                       getString(R.string.taxa_class_malacostraca_hint),
                                                                                                       R.id.taxaFilterClassMalacostraca,
                                                                                                       false),
                                                                  new TaxaFilterClass.TaxonFilterClass(9,
                                                                                                       getString(R.string.taxa_class_insecta_hint),
                                                                                                       R.id.taxaFilterClassInsecta,
                                                                                                       false),
                                                                  new TaxaFilterClass.TaxonFilterClass(15,
                                                                                                       getString(R.string.taxa_class_myriapoda_hint),
                                                                                                       R.id.taxaFilterClassMyriapoda,
                                                                                                       false),
                                                                  new TaxaFilterClass.TaxonFilterClass(16,
                                                                                                       getString(R.string.taxa_class_arachnida_hint),
                                                                                                       R.id.taxaFilterClassArachnida,
                                                                                                       false),
                                                                  new TaxaFilterClass.TaxonFilterClass(2,
                                                                                                       getString(R.string.taxa_class_annelida_hint),
                                                                                                       R.id.taxaFilterClassAnnelida,
                                                                                                       false),
                                                                  new TaxaFilterClass.TaxonFilterClass(8,
                                                                                                       getString(R.string.taxa_class_gastropoda_hint),
                                                                                                       R.id.taxaFilterClassGastropoda,
                                                                                                       false),
                                                                  new TaxaFilterClass.TaxonFilterClass(10,
                                                                                                       getString(R.string.taxa_class_lamellibranchia_hint),
                                                                                                       R.id.taxaFilterClassLamellibranchia,
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
        final List<String> selectionArgs = new ArrayList<String>();

        // adding filter
        selection.append("((");
        selection.append(MainDatabaseHelper.TaxaColumns.FILTER);
        selection.append(" & ?) != 0)");
        selectionArgs.add(Integer.toString(InputType.INVERTEBRATE.getKey()));

        // no unity selected as default
        String selectedUnityId = "0";

        if (args != null) {
            if (args.containsKey(KEY_SELECTED_UNITY)) {
                selectedUnityId = args.getString(KEY_SELECTED_UNITY);

                Log.d(getClass().getName(),
                      "selected unity : " + selectedUnityId);
            }

            if (args.containsKey(KEY_FILTER_STATUS) && (args.getParcelableArray(KEY_FILTER_STATUS) != null)) {
                TaxonStatus[] statusFilter = (TaxonStatus[]) args.getParcelableArray(KEY_FILTER_STATUS);
                boolean filterStatusAdded = false;

                if (statusFilter.length > 0) {
                    for (int i = 0; i < statusFilter.length; i++) {
                        if (statusFilter[i].isSelected()) {
                            if (!filterStatusAdded) {
                                selection.append(" AND (");
                            }
                            else {
                                selection.append(" OR ");
                            }

                            selection.append(MainDatabaseHelper.TaxaUnitiesColumns.COLOR);

                            if (statusFilter[i].getStatus()
                                               .equals(TaxonStatus.STATUS_NEW)) {
                                selection.append(" IS NULL");
                            }
                            else {
                                selection.append(" = ?");
                                selectionArgs.add(statusFilter[i].getStatus());
                            }

                            filterStatusAdded = true;
                        }
                    }

                    if (filterStatusAdded) {
                        selection.append(")");
                    }
                }
            }

            if (args.containsKey(KEY_FILTER_HERITAGE) && args.getBoolean(KEY_FILTER_HERITAGE)) {
                if (selection.length() > 0) {
                    selection.append(" AND ");
                }

                selection.append(MainDatabaseHelper.TaxaColumns.PATRIMONIAL);
                selection.append(" = ?");
                selectionArgs.add("True");
            }

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
                                        Uri.parse(MainContentProvider.CONTENT_TAXA_UNITY_URI + "/" + selectedUnityId),
                                        projection,
                                        selection.toString(),
                                        selectionArgs.toArray(new String[selectionArgs.size()]),
                                        sortOrder);

        return cursorLoader;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onClick(View v) {

        if (v.getId() == R.id.taxaFilterStatusView) {
            TaxaStatusFilterDialogFragment dialogFragment = TaxaStatusFilterDialogFragment.newInstance();
            dialogFragment.setHandler(new TaxaHandler(TaxaFragment.this));
            dialogFragment.getStatusFilter()
                          .clear();
            dialogFragment.getStatusFilter()
                          .addAll(Arrays.asList((TaxonStatus[]) getSavedInstanceState().getParcelableArray(KEY_FILTER_STATUS)));
            dialogFragment.show(getFragmentManager(),
                                FILTER_TAXA_STATUS_DIALOG_FRAGMENT);
        }
        else if (v.getId() == R.id.taxaFilterHeritageView) {
            if (getSavedInstanceState().getBoolean(KEY_FILTER_HERITAGE)) {
                ((ImageButton) v).setAlpha(127);
            }
            else {
                ((ImageButton) v).setAlpha(255);
            }

            getSavedInstanceState().putBoolean(KEY_FILTER_HERITAGE,
                                               !getSavedInstanceState().getBoolean(KEY_FILTER_HERITAGE));
            refreshView();
        }
        else if ((v.getId() == R.id.taxaFilterClassMalacostraca) ||
                (v.getId() == R.id.taxaFilterClassInsecta) ||
                (v.getId() == R.id.taxaFilterClassMyriapoda) ||
                (v.getId() == R.id.taxaFilterClassArachnida) ||
                (v.getId() == R.id.taxaFilterClassAnnelida) ||
                (v.getId() == R.id.taxaFilterClassGastropoda) ||
                (v.getId() == R.id.taxaFilterClassLamellibranchia)) {
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

    private static class TaxaHandler
            extends Handler {

        private final WeakReference<TaxaFragment> mTaxaFragment;

        public TaxaHandler(TaxaFragment pTaxaFragment) {

            super();
            mTaxaFragment = new WeakReference<>(pTaxaFragment);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {

            TaxaFragment taxaFragment = mTaxaFragment.get();

            switch (msg.what) {
                case HANDLER_TAXA_FILTER_STATUS:
                    List<TaxonStatus> filterStatus = (List<TaxonStatus>) msg.obj;
                    taxaFragment.getSavedInstanceState()
                                .putParcelableArray(KEY_FILTER_STATUS,
                                                    filterStatus.toArray(new TaxonStatus[filterStatus.size()]));
                    taxaFragment.refreshView();
                    break;
            }
        }
    }
}
