package com.geonature.mobile.commons.ui.input.observers;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.geonature.mobile.commons.input.Observer;

/**
 * Custom {@code ArrayAdapter} for {@link Observer}.
 *
 * @author <a href="mailto:sebastien.grimault@gmail.com">S. Grimault</a>
 */
public class ObserverArrayAdapter
        extends ArrayAdapter<Observer> {

    private final int mTextViewResourceId;
    private final LayoutInflater mInflater;

    ObserverArrayAdapter(Context context,
                         int textViewResourceId) {
        super(context,
              textViewResourceId);

        mTextViewResourceId = textViewResourceId;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position,
                        View convertView,
                        @NonNull ViewGroup parent) {
        final View view;

        if (convertView == null) {
            view = mInflater.inflate(mTextViewResourceId,
                                     parent,
                                     false);
        }
        else {
            view = convertView;
        }

        final Observer observer = getItem(position);

        if (observer != null) {
            ((TextView) view.findViewById(android.R.id.text1)).setText(observer.getLastname());
            ((TextView) view.findViewById(android.R.id.text2)).setText(observer.getFirstname());
        }

        return view;
    }
}
