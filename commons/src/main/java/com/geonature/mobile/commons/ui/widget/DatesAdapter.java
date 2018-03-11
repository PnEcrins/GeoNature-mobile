package com.geonature.mobile.commons.ui.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Date;

/**
 * Custom {@code ArrayAdapter} for {@code Date} instances.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class DatesAdapter extends ArrayAdapter<Date> {

    private final int mTextViewResourceId;
    private final int mStringDateFormatResourceId;
    private final LayoutInflater mInflater;

    public DatesAdapter(Context context, int textViewResourceId, int stringDateFormatResourceId) {
        super(context, textViewResourceId);
        mTextViewResourceId = textViewResourceId;
        mStringDateFormatResourceId = stringDateFormatResourceId;
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

        Date date = getItem(position);

        ((TextView) view.findViewById(android.R.id.text1)).setText(DateFormat.format(getContext().getString(mStringDateFormatResourceId), date));

        return view;
    }
}
