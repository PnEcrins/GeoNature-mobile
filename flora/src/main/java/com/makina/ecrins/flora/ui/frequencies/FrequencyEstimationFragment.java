package com.makina.ecrins.flora.ui.frequencies;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.makina.ecrins.flora.MainApplication;
import com.makina.ecrins.flora.R;
import com.makina.ecrins.flora.input.Taxon;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Frequency: Estimation method.
 *
 * @author <a href="mailto:sebastien.grimault@makina-corpus.com">S. Grimault</a>
 */
public class FrequencyEstimationFragment extends Fragment {

    private SeekBar mSeekBarFrequency;
    private EditText mEditTextFrequency;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_frequency_estimation, container, false);

        final List<Abacus> abacus = new ArrayList<Abacus>();
        abacus.add(new Abacus(R.drawable.ic_abacus_01, 0.01));
        abacus.add(new Abacus(R.drawable.ic_abacus_02, 0.02));
        abacus.add(new Abacus(R.drawable.ic_abacus_05, 0.05));
        abacus.add(new Abacus(R.drawable.ic_abacus_08, 0.08));
        abacus.add(new Abacus(R.drawable.ic_abacus_15, 0.15));
        abacus.add(new Abacus(R.drawable.ic_abacus_25, 0.25));
        abacus.add(new Abacus(R.drawable.ic_abacus_50, 0.5));
        abacus.add(new Abacus(R.drawable.ic_abacus_70, 0.7));
        abacus.add(new Abacus(R.drawable.ic_abacus_90, 0.9));

        GridView gridViewAbacus = (GridView) view.findViewById(R.id.gridViewAbacus);
        gridViewAbacus.setAdapter(new AbacusAdapter(getActivity(), abacus));

        mSeekBarFrequency = (SeekBar) view.findViewById(R.id.seekBarFrequency);
        mSeekBarFrequency.setMax(100);
        mSeekBarFrequency.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // nothing to do ...
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // nothing to do ...
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    updateFrequency(progress, true);
                }
            }
        });

        mEditTextFrequency = (EditText) view.findViewById(R.id.editTextFrequency);
        mEditTextFrequency.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // nothing to do ...
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing to do ...
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString()
                        .isEmpty()) {
                    try {
                        updateFrequency(Integer.valueOf(s.toString()), false);
                    }
                    catch (NumberFormatException nfe) {
                        Log.w(FrequencyEstimationFragment.class.getName(), nfe.getMessage());
                    }
                }
            }
        });

        ((TextView) view.findViewById(R.id.textViewSeekBarMaxValue)).setText(Integer.toString(mSeekBarFrequency.getMax()));

        if ((((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon() != null) &&
                (((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                        .getCurrentSelectedTaxon()).getCurrentSelectedArea() != null)) {
            updateFrequency(((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                    .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                    .getFrequency()
                    .getValue(), true);
        }

        return view;
    }

    private void updateFrequency(double progress, boolean updateEditText) {
        double progressValue = progress;

        if (progressValue > 100.0) {
            progressValue = 100.0;
        }

        if (progressValue < 0.0) {
            progressValue = 0.0;
        }

        mSeekBarFrequency.setProgress(Double.valueOf(progressValue)
                .intValue());

        if ((updateEditText) || (progressValue != progress)) {
            mEditTextFrequency.setText(Integer.toString(Double.valueOf(progressValue)
                    .intValue()));
        }

        ((Taxon) ((MainApplication) getActivity().getApplication()).getInput()
                .getCurrentSelectedTaxon()).getCurrentSelectedArea()
                .getFrequency()
                .setValue(progressValue);
    }

    private class AbacusAdapter extends ArrayAdapter<Abacus> {

        private final LayoutInflater mInflater;
        private NumberFormat mNumberFormat;

        public AbacusAdapter(Context context, List<Abacus> abacus) {
            super(context, android.R.layout.simple_list_item_1, android.R.id.text1, abacus);

            mNumberFormat = NumberFormat.getPercentInstance();
            mNumberFormat.setMaximumFractionDigits(0);

            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                view = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            else {
                view = convertView;
            }

            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(mNumberFormat.format(getItem(position).getValue()));
            textView.setCompoundDrawablesWithIntrinsicBounds(0, getItem(position).getResourceId(), 0, 0);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);

            return view;
        }
    }

    private class Abacus {

        private int mResourceId;
        private double mValue;

        public Abacus(int pResourceId, double pValue) {
            mResourceId = pResourceId;
            mValue = pValue;
        }

        public int getResourceId() {
            return mResourceId;
        }

        public double getValue() {
            return mValue;
        }
    }
}
