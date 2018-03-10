package com.makina.ecrins.commons.ui.input.taxa;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.makina.ecrins.commons.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom {@code Dialog} used to select the statuses of taxa as filter.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class TaxaStatusFilterDialogFragment
        extends DialogFragment
        implements OnItemClickListener {

    private static final String KEY_FILTER_STATUS = "filter_status";

    private TaxaStatusArrayAdapter mAdapter;
    private Handler mHandler;
    private List<TaxonStatus> mStatusFilter = new ArrayList<>();

    private static boolean sInitialize;

    public static TaxaStatusFilterDialogFragment newInstance() {
        TaxaStatusFilterDialogFragment dialogFragment = new TaxaStatusFilterDialogFragment();
        sInitialize = true;
        return dialogFragment;
    }

    public void setHandler(Handler pHandler) {
        this.mHandler = pHandler;
    }

    public List<TaxonStatus> getStatusFilter() {
        return mStatusFilter;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getContext();

        if (context == null) {
            throw new IllegalArgumentException("Null Context while creating " + TaxaStatusFilterDialogFragment.class.getName());
        }

        final View view = View.inflate(context,
                                       R.layout.dialog_list_items,
                                       null);

        ListView listView = view.findViewById(android.R.id.list);

        mAdapter = new TaxaStatusArrayAdapter(getActivity());

        if (!sInitialize) {
            mStatusFilter = savedInstanceState.getParcelableArrayList(KEY_FILTER_STATUS);

            if (mStatusFilter == null) {
                mStatusFilter = new ArrayList<>();
            }
        }

        for (TaxonStatus taxonStatus : mStatusFilter) {
            mAdapter.add(taxonStatus);
        }

        listView.setAdapter(mAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(this);

        sInitialize = false;

        return new AlertDialog.Builder(context)
                .setIcon(R.drawable.dialog_filter_status)
                .setTitle(R.string.alert_dialog_taxa_filter_status_title)
                .setView(view)
                .setPositiveButton(R.string.alert_dialog_ok, null)
                .setNegativeButton(R.string.alert_dialog_cancel, null)
                .create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(KEY_FILTER_STATUS, (ArrayList<TaxonStatus>) mStatusFilter);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClick(AdapterView<?> parent,
                            View view,
                            int position,
                            long id) {
        final TaxonStatus item = mAdapter.getItem(position);

        if (item == null) {
            return;
        }

        item.setSelected(!item.isSelected());

        List<TaxonStatus> taxonStatusFilter = new ArrayList<>();

        for (int i = 0; i < mAdapter.getCount(); i++) {
            taxonStatusFilter.add(mAdapter.getItem(i));
        }

        Message message = mHandler.obtainMessage(AbstractTaxaFragment.HANDLER_TAXA_FILTER_STATUS);
        message.obj = taxonStatusFilter;
        message.sendToTarget();

        mAdapter.notifyDataSetChanged();
    }
}