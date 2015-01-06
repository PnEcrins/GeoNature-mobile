package com.makina.ecrins.app.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Simple {@code Adapter} using String resources.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class StringResourcesArrayAdapter
        extends ArrayAdapter<Integer> {
    private int mTextViewResourceId;
    private final LayoutInflater mInflater;

    public StringResourcesArrayAdapter(Context context) {
        this(
                context,
                android.R.layout.simple_list_item_1
        );
    }

    public StringResourcesArrayAdapter(
            Context context,
            int textViewResourceId) {
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

        ((TextView) view.findViewById(android.R.id.text1)).setText(getItem(position));

        return view;
    }
}
