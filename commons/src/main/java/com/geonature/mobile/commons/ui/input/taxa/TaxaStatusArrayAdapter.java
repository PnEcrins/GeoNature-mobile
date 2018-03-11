package com.geonature.mobile.commons.ui.input.taxa;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.geonature.mobile.commons.R;

/**
 * Custom {@code ArrayAdapter} used for {@link TaxaStatusFilterDialogFragment}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class TaxaStatusArrayAdapter extends ArrayAdapter<TaxonStatus> {

    private final int mTextViewResourceId;
    private final LayoutInflater mInflater;

    TaxaStatusArrayAdapter(Context context) {
        this(context, R.layout.list_item_taxa_status_multiple_choice);
    }

    private TaxaStatusArrayAdapter(Context context,
                                   int textViewResourceId) {
        super(context, textViewResourceId);
        mTextViewResourceId = textViewResourceId;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(mTextViewResourceId, parent, false);
        }
        else {
            view = convertView;
        }

        final TaxonStatus taxonStatus = getItem(position);

        if (taxonStatus == null) {
            return view;
        }

        view.findViewById(R.id.viewStatusColor).setBackgroundColor(getContext().getResources().getColor(taxonStatus.getResourceColorId()));
        ((TextView) view.findViewById(android.R.id.text1)).setText(getContext().getResources().getString(taxonStatus.getResourceLabelId()));
        ((CheckBox) view.findViewById(android.R.id.checkbox)).setChecked(taxonStatus.isSelected());

        return view;
    }
}