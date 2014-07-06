package com.makina.ecrins.commons.ui.input.taxa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.makina.ecrins.commons.R;

/**
 * Custom <code>ArrayAdapter</code> used for {@link TaxaStatusFilterDialogFragment}.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class TaxaStatusArrayAdapter extends ArrayAdapter<TaxonStatus> {

    private int mTextViewResourceId;
    private final LayoutInflater mInflater;

    public TaxaStatusArrayAdapter(Context context) {
        this(context, R.layout.list_item_taxa_status_multiple_choice);
    }

    public TaxaStatusArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        mTextViewResourceId = textViewResourceId;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(mTextViewResourceId, parent, false);
        }
        else {
            view = convertView;
        }

        TaxonStatus taxonStatus = getItem(position);
        view.findViewById(R.id.viewStatusColor).setBackgroundColor(getContext().getResources().getColor(taxonStatus.getResourceColorId()));
        ((TextView) view.findViewById(android.R.id.text1)).setText(getContext().getResources().getString(taxonStatus.getResourceLabelId()));
        ((CheckBox) view.findViewById(android.R.id.checkbox)).setChecked(taxonStatus.isSelected());

        return view;
    }
}