package com.makina.ecrins.commons.ui.input.taxa;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.makina.ecrins.commons.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom <code>Dialog</code> used to select the statuses of taxa as filter.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class TaxaStatusFilterDialogFragment extends DialogFragment implements OnItemClickListener {

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
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View view = inflater.inflate(R.layout.dialog_list_items, null);

        ListView listView = (ListView) view.findViewById(android.R.id.list);

        mAdapter = new TaxaStatusArrayAdapter(getActivity());

        if (!sInitialize) {
            mStatusFilter = savedInstanceState.getParcelableArrayList(KEY_FILTER_STATUS);
        }

        for (TaxonStatus taxonStatus : mStatusFilter) {
            mAdapter.add(taxonStatus);
        }

        listView.setAdapter(mAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(this);

        sInitialize = false;

        return new AlertDialog.Builder(getActivity())
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAdapter.getItem(position).setSelected(!mAdapter.getItem(position).isSelected());

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